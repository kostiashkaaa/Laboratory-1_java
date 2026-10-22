import java.util.Collections;
import java.util.List;

/**
 * Хищник: двигается, обладает зрением 5x5.
 * Логика (когнитивная модель):
 *   ЕСЛИ видит травоядное -> идти к нему и атаковать
 *   ИНАЧЕ                  -> блуждать случайно
 *
 * Атака происходит, когда травоядное находится в соседней клетке по прямой
 * (сверху, снизу, слева или справа): хищник съедает его и занимает клетку.
 * При достаточном запасе энергии хищник размножается так же, как остальные
 * агенты: часть энергии передаётся потомку в свободную соседнюю клетку.
 */
public class Predator extends Agent {

    private static final double METABOLISM = 0.4;      // F2
    private static final double MEAT_ENERGY = 80;       // E2 от травоядного
    private static final double REPRO_THRESHOLD = 850;  // Xthreshold
    private static final double CHILD_ENERGY = 244;
    private static final int VISION_RADIUS = 2;         // область видимости 5x5
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

        if (!attackAdjacentHerbivore()) {
            List<int[]> vision = env.getNeighbors(x, y, VISION_RADIUS);
            int[] preyCell = findNearest(vision, Herbivore.class);
            if (preyCell != null) {
                moveToward(preyCell);
            } else {
                wander();
            }
        }
        tryReproduce();
    }

    /** Атака травоядного в соседней клетке по прямой. Возвращает true, если атака состоялась. */
    private boolean attackAdjacentHerbivore() {
        List<int[]> adjacent = env.getCardinalNeighbors(x, y);
        Collections.shuffle(adjacent, rnd);
        for (int[] cell : adjacent) {
            Agent occupant = env.getAgent(cell[0], cell[1]);
            if (occupant instanceof Herbivore) {
                attack((Herbivore) occupant, cell[0], cell[1]);
                return true;
            }
        }
        return false;
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

    /** Шаг к травоядному по прямой (без диагоналей). */
    private void moveToward(int[] target) {
        int[] direction = cardinalDirection(target[0], target[1], false);
        int preferredX = x + direction[0];
        int preferredY = y + direction[1];
        if (env.isEmpty(preferredX, preferredY)) {
            moveTo(preferredX, preferredY);
            return;
        }

        // Прямой путь занят (растением или другим хищником) — выбираем
        // другую свободную соседнюю клетку по прямой, которая всё равно
        // приближает хищника к жертве. Если такой нет — остаёмся на месте.
        int currentDistance = env.distance(x, y, target[0], target[1]);
        List<int[]> candidates = env.getCardinalNeighbors(x, y);
        Collections.shuffle(candidates, rnd);
        for (int[] cell : candidates) {
            if (env.isEmpty(cell[0], cell[1])
                    && env.distance(cell[0], cell[1], target[0], target[1]) < currentDistance) {
                moveTo(cell[0], cell[1]);
                return;
            }
        }
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
