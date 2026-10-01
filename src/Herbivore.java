import java.util.List;

/**
 * Травоядное: двигается, обладает зрением 5x5.
 * Логика (когнитивная модель):
 *   ЕСЛИ видит хищника      -> убегать в противоположную сторону
 *   ИНАЧЕ ЕСЛИ видит растение -> идти к нему (и съесть, если оно рядом)
 *   ИНАЧЕ                    -> блуждать случайно
 */
public class Herbivore extends Agent {

    private static final double METABOLISM = 1.0;      // F
    private static final double FOOD_ENERGY = 5;        // E1 от растения
    private static final double REPRO_THRESHOLD = 250; // Hthreshold
    private static final double CHILD_ENERGY = 100;
    private static final int VISION_RADIUS = 2;         // область видимости 5x5
    private final double reproductionThreshold;
    private final double childEnergy;

    public Herbivore(int x, int y, double energy, Environment env) {
        this(x, y, energy, env, REPRO_THRESHOLD, CHILD_ENERGY);
    }

    public Herbivore(int x, int y, double energy, Environment env,
                     double reproductionThreshold, double childEnergy) {
        super(x, y, energy, "🐇", env);
        this.reproductionThreshold = reproductionThreshold;
        this.childEnergy = childEnergy;
    }

    @Override
    public void act() {
        if (!alive) return;
        metabolize();
        if (!alive) return;

        List<int[]> vision = env.getNeighbors(x, y, VISION_RADIUS);
        int[] predatorCell = findNearest(vision, Predator.class);

        if (predatorCell != null) {
            fleeFrom(predatorCell);
        } else {
            if (eatAdjacentPlant()) {
                tryReproduce();
                return;
            }
            int[] plantCell = findNearest(vision, Plant.class);
            if (plantCell != null) {
                moveTowardOrEat(plantCell);
            } else {
                wander();
            }
        }
        tryReproduce();
    }

    private boolean eatAdjacentPlant() {
        List<int[]> adjacent = env.getNeighbors(x, y, 1);
        for (int[] cell : adjacent) {
            Agent occupant = env.getAgent(cell[0], cell[1]);
            if (occupant instanceof Plant) {
                eat((Plant) occupant, cell[0], cell[1]);
                return true;
            }
        }
        return false;
    }

    /** Поиск ближайшего агента заданного типа в области зрения (манхэттенское расстояние). */
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

    /** Убегание в сторону, противоположную угрозе (движение только по прямой). */
    private void fleeFrom(int[] threat) {
        int[] d = cardinalDirection(threat[0], threat[1], true);
        if (d[0] == 0 && d[1] == 0) { wander(); return; }
        int nx = x + d[0];
        int ny = y + d[1];
        if (env.isEmpty(nx, ny)) {
            moveTo(nx, ny);
        } else {
            // прямой путь побега занят — пробуем убежать в любую другую
            // свободную сторону, а не просто застыть перед хищником
            wander();
        }
    }

    /** Движение к растению по прямой; если оно оказалось в соседней клетке — съесть. */
    private void moveTowardOrEat(int[] target) {
        int[] d = cardinalDirection(target[0], target[1], false);
        int nx = x + d[0];
        int ny = y + d[1];
        Agent occupant = env.getAgent(nx, ny);
        if (occupant instanceof Plant) {
            eat((Plant) occupant, nx, ny);
        } else if (env.isEmpty(nx, ny)) {
            moveTo(nx, ny);
        } else {
            wander();
        }
    }

    private void eat(Plant plant, int px, int py) {
        energy += FOOD_ENERGY;
        plant.die();
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
        return new Herbivore(cx, cy, e, env, reproductionThreshold, childEnergy);
    }

}
