/**
 * Растение: не двигается, генерирует энергию "из солнца" каждый ход
 * (метаболизм = 0, только рост), размножается вегетативно в соседние клетки.
 */
public class Plant extends Agent {

    private static final double GROWTH = 1;              // +n энергии за шаг
    private static final double REPRO_THRESHOLD = 39;    // Pthreshold
    private static final double CHILD_ENERGY = 28;       // энергия ростка
    private static final double MAX_ENERGY = 100;        // ограничение накопления
    private final double reproductionThreshold;
    private final double childEnergy;

    public Plant(int x, int y, double energy, Environment env) {
        this(x, y, energy, env, REPRO_THRESHOLD, CHILD_ENERGY);
    }

    public Plant(int x, int y, double energy, Environment env,
                 double reproductionThreshold, double childEnergy) {
        super(x, y, energy, "🌿", env);
        this.reproductionThreshold = reproductionThreshold;
        this.childEnergy = childEnergy;
    }

    @Override
    public void act() {
        if (!alive) return;
        energy = Math.min(MAX_ENERGY, energy + GROWTH);
        tryReproduce();
    }

    @Override
    protected double getMetabolism() { return 0; }

    @Override
    protected double getReproductionThreshold() { return reproductionThreshold; }

    @Override
    protected double getChildEnergy() { return childEnergy; }

    @Override
    protected Agent createChild(int cx, int cy, double e) {
        return new Plant(cx, cy, e, env, reproductionThreshold, childEnergy);
    }

}
