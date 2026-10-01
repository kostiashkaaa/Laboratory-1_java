import java.util.Collections;
import java.util.List;

/**
 * Хищник: двигается, обладает зрением 5x5.
 * Логика (когнитивная модель):
 *   ЕСЛИ видит травоядное -> идти к нему и атаковать
 *   ИНАЧЕ                  -> блуждать случайно
 *
 * При достаточном запасе энергии хищник размножается так же, как остальные
 * агенты: часть энергии передаётся потомку в свободную соседнюю клетку.
 */
public class Predator extends Agent {

    private static final double METABOLISM = 0.4;      // F2
    private static final double MEAT_ENERGY = 40;       // E2 от травоядного
    private static final double REPRO_THRESHOLD = 500;  // Xthreshold
    private static final double CHILD_ENERGY = 150;
    private static final int VISION_RADIUS = 2;         // область видимости 5x5
    private static final int HUNT_SPEED = 1;            // один шаг за ход
    private static final double SPRINT_CHANCE = 0.25;   // не каждый ход хищник успевает на "рывок"
    private static final double CAPTURE_CHANCE = 0.05;  // атака рядом не всегда успешна
    private final double reproductionThreshold;
    private final double childEnergy;

    public Predator(int x, int y, double energy, Environment env) {
        this(x, y, energy, env, REPRO_THRESHOLD, CHILD_ENERGY);
    }

    public Predator(int x, int y, double energy, Environment env,
                    double reproductionThreshold, double childEnergy) {
        super(x, y, energy, "🐺", env);
        this.reproductionThreshold = reproductionThreshold;
        this.childEnergy = childEnergy;
    }

    @Override
    public void act() {
        if (!alive) return;
        metabolize();
        if (!alive) return;

        // Хищник при преследовании иногда успевает на дополнительный "рывок" —
        // это даёт ему шанс догнать травоядное, которое убегает с обычной
        // скоростью (при равной скорости и 100%-й защите побег был бы всегда успешен).
        boolean attacked = false;
        for (int i = 0; i < HUNT_SPEED && alive && !attacked; i++) {
            if (i > 0 && rnd.nextDouble() > SPRINT_CHANCE) break;
            List<int[]> vision = env.getNeighbors(x, y, VISION_RADIUS);
            int[] preyCell = findNearest(vision, Herbivore.class);
            if (preyCell != null) {
                attacked = moveTowardOrAttack(preyCell);
            } else {
                wander();
                break;
            }
        }
        tryReproduce();
    }

    private int[] findNearest(List<int[]> cells, Class<? extends Agent> type) {
        int[] best = null;
        int bestDist = Integer.MAX_VALUE;
        for (int[] c : cells) {
            Agent a = env.getAgent(c[0], c[1]);
            if (a != null && a.isAlive() && type.isInstance(a)) {
                int dist = env.distance(x, y, c[0], c[1]);
                if (dist < bestDist) {
                    bestDist = dist;
                    best = c;
                }
            }
        }
        return best;
    }

    /** Движение к травоядному по прямой; если оно оказалось в соседней клетке — атаковать. Возвращает true при успешной атаке. */
    private boolean moveTowardOrAttack(int[] target) {
        // Сначала проверяем все соседние клетки: цель может быть не в
        // выбранном направлении, особенно если несколько агентов окружили
        // хищника.
        List<int[]> adjacent = env.getNeighbors(x, y, 1);
        for (int[] cell : adjacent) {
            Agent occupant = env.getAgent(cell[0], cell[1]);
            if (occupant instanceof Herbivore
                    && rnd.nextDouble() < CAPTURE_CHANCE) {
                attack((Herbivore) occupant, cell[0], cell[1]);
                return true;
            }
        }

        int[] direction = cardinalDirection(target[0], target[1], false);
        int preferredX = x + direction[0];
        int preferredY = y + direction[1];
        if (env.isEmpty(preferredX, preferredY)) {
            moveTo(preferredX, preferredY);
            return false;
        }
        if (env.getAgent(preferredX, preferredY) instanceof Plant) {
            moveThroughPlant(preferredX, preferredY);
            return false;
        }

        // Прямой путь может быть занят растением или другим хищником.
        // Выбираем свободную соседнюю клетку, которая всё равно приближает
        // хищника к найденной жертве, вместо случайного блуждания.
        int currentDistance = env.distance(x, y, target[0], target[1]);
        List<int[]> candidates = env.getNeighbors(x, y, 1);
        Collections.shuffle(candidates, rnd);
        for (int[] cell : candidates) {
            if (env.distance(cell[0], cell[1], target[0], target[1])
                    < currentDistance
                    && (env.isEmpty(cell[0], cell[1])
                    || env.getAgent(cell[0], cell[1]) instanceof Plant)) {
                if (env.getAgent(cell[0], cell[1]) instanceof Plant) {
                    moveThroughPlant(cell[0], cell[1]);
                } else {
                    moveTo(cell[0], cell[1]);
                }
                break;
            }
        }
        return false;
    }

    private void moveThroughPlant(int nx, int ny) {
        Agent occupant = env.getAgent(nx, ny);
        if (!(occupant instanceof Plant) || !env.isInside(nx, ny)) {
            return;
        }
        occupant.die();
        env.setAgent(x, y, null);
        x = nx;
        y = ny;
        env.setAgent(x, y, this);
    }

    private void attack(Herbivore prey, int px, int py) {
        energy += MEAT_ENERGY;
        prey.die();
        env.setAgent(x, y, null);
        x = px;
        y = py;
        env.setAgent(x, y, this);
    }

    @Override
    protected double getMetabolism() { return METABOLISM; }

    @Override
    protected double getReproductionThreshold() { return reproductionThreshold; }

    @Override
    protected double getChildEnergy() { return childEnergy; }

    @Override
    protected Agent createChild(int cx, int cy, double e) {
        return new Predator(cx, cy, e, env, reproductionThreshold, childEnergy);
    }

}