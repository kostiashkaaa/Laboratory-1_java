import java.util.Random;

/**
 * Абстрактный класс логического агента.
 * Инкапсулирует общие данные (координаты, энергия, символ отображения)
 * и общие методы действий: движение, размножение (митоз), гибель.
 * Конкретное поведение (act) реализуется в наследниках через полиморфизм.
 */
public abstract class Agent {

    protected int x, y;
    protected double energy;
    protected final String symbol;
    protected final Environment env;
    protected boolean alive = true;

    protected static final Random rnd = new Random();

    public Agent(int x, int y, double energy, String symbol, Environment env) {
        this.x = x;
        this.y = y;
        this.energy = energy;
        this.symbol = symbol;
        this.env = env;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public double getEnergy() { return energy; }
    public String getSymbol() { return symbol; }
    public boolean isAlive() { return alive; }

    /** Основное действие агента за один ход симуляции. */
    public abstract void act();

    /** Расход энергии за один ход (метаболизм). Для растений = 0. */
    protected abstract double getMetabolism();

    /** Порог энергии, при превышении которого агент размножается. */
    protected abstract double getReproductionThreshold();

    /** Количество энергии, передаваемое потомку при размножении. */
    protected abstract double getChildEnergy();

    /** Фабричный метод создания потомка того же вида (полиморфизм). */
    protected abstract Agent createChild(int cx, int cy, double energy);

    /** Трата энергии за ход и проверка на гибель. */
    protected void metabolize() {
        energy -= getMetabolism();
        if (energy <= 0) {
            die();
        }
    }

    /** Гибель агента: убирается из среды, помечается неживым. */
    protected void die() {
        alive = false;
        env.setAgent(x, y, null);
    }

    /**
     * Размножение (митоз): если энергия превышает порог,
     * родитель теряет часть энергии Echild, и в соседней свободной
     * клетке появляется новый агент с этой энергией.
     */
    protected void tryReproduce() {
        if (!alive) return;
        if (energy <= getReproductionThreshold()) return;

        int[] freeCell = env.getFreeNeighbor(x, y);
        if (freeCell != null) {
            double childEnergy = getChildEnergy();
            if (childEnergy <= 0 || energy <= childEnergy) return;
            energy -= childEnergy;
            Agent child = createChild(freeCell[0], freeCell[1], childEnergy);
            env.setAgent(freeCell[0], freeCell[1], child);
        }
    }

    /** Перемещение в клетку (nx, ny), если она свободна. */
    protected void moveTo(int nx, int ny) {
        if (env.isInside(nx, ny) && env.isEmpty(nx, ny)) {
            env.setAgent(x, y, null);
            x = nx;
            y = ny;
            env.setAgent(x, y, this);
        }
    }

    /**
     * Случайное блуждание на одну клетку. Движение только по прямой —
     * вверх, вниз, влево или вправо (без диагоналей). Перебирает все
     * 4 направления в случайном порядке и уходит в первое свободное:
     * если бы агент пробовал только одно случайное направление и сразу
     * сдавался, он мог бы годами стоять на месте, даже если 3 из 4
     * соседних клеток свободны — просто из-за невезения при выборе.
     * Если ВСЕ 4 стороны заняты (агент реально окружён), он остаётся
     * на месте — тут действительно больше ничего не сделать.
     */
    protected void wander() {
        int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
        // перемешиваем порядок попыток, чтобы не было систематического
        // предпочтения одного направления
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int[] tmp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = tmp;
        }
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (env.isEmpty(nx, ny)) {
                moveTo(nx, ny);
                return;
            }
        }
        // все 4 соседних клетки заняты — агент остаётся на месте
    }

    /**
     * Направление одного шага по прямой (без диагоналей) к клетке (tx, ty),
     * либо противоположное направление, если away = true (например, чтобы
     * убегать). Двигаться агент может только по одной оси за ход — выбирается
     * та ось, по которой расстояние до цели больше. Границы поля
     * непроходимы, поэтому агент останавливается у края.
     */
    protected int[] cardinalDirection(int tx, int ty, boolean away) {
        int dx = tx - x;
        int dy = ty - y;
        if (away) {
            dx = -dx;
            dy = -dy;
        }
        if (dx == 0 && dy == 0) {
            return new int[]{0, 0};
        }
        if (Math.abs(dx) >= Math.abs(dy)) {
            return new int[]{Integer.signum(dx), 0};
        }
        return new int[]{0, Integer.signum(dy)};
    }

}
