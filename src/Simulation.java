import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * Игровой цикл консольного приложения.
 * Управляет средой, начальным заселением и пошаговым/автоматическим
 * выполнением ходов с выводом состояния в консоль.
 *
 * Баланс жизни определяется параметрами энергии, питания и размножения
 * агентов.
 */
public class Simulation {

    private final Environment env;
    private int step = 0;
    private final double plantReproductionThreshold;
    private final double herbivoreReproductionThreshold;
    private final double predatorReproductionThreshold;
    private final double plantChildEnergy;
    private final double herbivoreChildEnergy;
    private final double predatorChildEnergy;

    // Условные итоговые параметры поля и начальной популяции
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;
    private static final int INITIAL_PLANTS = 998;
    private static final int INITIAL_HERBIVORES = 79;
    private static final int INITIAL_PREDATORS = 13;

    // Журнал критических событий с номером шага.
    private final List<String> eventLog = new ArrayList<>();
    private boolean plantsExtinct = false;
    private boolean herbivoresExtinct = false;
    private boolean predatorsExtinct = false;

    public Simulation() {
        this(WIDTH, HEIGHT, INITIAL_PLANTS, INITIAL_HERBIVORES, INITIAL_PREDATORS, 10, 43, 418,
                39, 410, 850, 28, 93, 244);
    }

    public Simulation(int initialPlants, int initialHerbivores, int initialPredators,
                      double plantEnergy, double herbivoreEnergy, double predatorEnergy) {
        this(WIDTH, HEIGHT, initialPlants, initialHerbivores, initialPredators,
                plantEnergy, herbivoreEnergy, predatorEnergy,
                39, 410, 850, 28, 93, 244);
    }

    public Simulation(int width, int height,
                      int initialPlants, int initialHerbivores, int initialPredators,
                      double plantEnergy, double herbivoreEnergy, double predatorEnergy,
                      double plantReproductionThreshold,
                      double herbivoreReproductionThreshold,
                      double predatorReproductionThreshold,
                      double plantChildEnergy,
                      double herbivoreChildEnergy,
                      double predatorChildEnergy) {
        validateParameters(width, height, initialPlants, initialHerbivores,
                initialPredators, plantEnergy, herbivoreEnergy, predatorEnergy,
                plantReproductionThreshold, herbivoreReproductionThreshold,
                predatorReproductionThreshold, plantChildEnergy,
                herbivoreChildEnergy, predatorChildEnergy);
        env = new Environment(width, height);
        this.plantReproductionThreshold = plantReproductionThreshold;
        this.herbivoreReproductionThreshold = herbivoreReproductionThreshold;
        this.predatorReproductionThreshold = predatorReproductionThreshold;
        this.plantChildEnergy = plantChildEnergy;
        this.herbivoreChildEnergy = herbivoreChildEnergy;
        this.predatorChildEnergy = predatorChildEnergy;
        populate(initialPlants, initialHerbivores, initialPredators,
                plantEnergy, herbivoreEnergy, predatorEnergy);
    }

    private static void validateParameters(int width, int height,
                                           int initialPlants, int initialHerbivores,
                                           int initialPredators,
                                           double plantEnergy, double herbivoreEnergy,
                                           double predatorEnergy,
                                           double plantThreshold, double herbivoreThreshold,
                                           double predatorThreshold,
                                           double plantChildEnergy,
                                           double herbivoreChildEnergy,
                                           double predatorChildEnergy) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Размер поля должен быть положительным.");
        }
        if (initialPlants < 0 || initialHerbivores < 0 || initialPredators < 0) {
            throw new IllegalArgumentException("Начальная численность не может быть отрицательной.");
        }
        if ((long) initialPlants + initialHerbivores + initialPredators
                > (long) width * height) {
            throw new IllegalArgumentException(
                    "Начальная численность агентов превышает количество клеток поля.");
        }
        double[] values = {
                plantEnergy, herbivoreEnergy, predatorEnergy,
                plantThreshold, herbivoreThreshold, predatorThreshold,
                plantChildEnergy, herbivoreChildEnergy, predatorChildEnergy
        };
        for (double value : values) {
            if (!Double.isFinite(value) || value < 0) {
                throw new IllegalArgumentException("Параметры энергии должны быть конечными и неотрицательными.");
            }
        }
    }

    private void populate(int initialPlants, int initialHerbivores, int initialPredators,
                          double plantEnergy, double herbivoreEnergy, double predatorEnergy) {
        Random rnd = new Random();
        placeRandom(rnd, initialPlants, (x, y) -> new Plant(x, y, plantEnergy, env,
                plantReproductionThreshold, plantChildEnergy));
        placeRandom(rnd, initialHerbivores, (x, y) -> new Herbivore(x, y, herbivoreEnergy, env,
                herbivoreReproductionThreshold, herbivoreChildEnergy));
        placeRandom(rnd, initialPredators, (x, y) -> new Predator(x, y, predatorEnergy, env,
                predatorReproductionThreshold, predatorChildEnergy));
    }

    private interface AgentFactory {
        Agent create(int x, int y);
    }

    private void placeRandom(Random rnd, int count, AgentFactory factory) {
        int placed = 0;
        int attempts = 0;
        while (placed < count && attempts < count * 50) {
            int x = rnd.nextInt(env.getWidth());
            int y = rnd.nextInt(env.getHeight());
            attempts++;
            if (env.isEmpty(x, y)) {
                env.setAgent(x, y, factory.create(x, y));
                placed++;
            }
        }
    }

    /**
     * Запуск симуляции.
     * @param maxSteps    максимальное число ходов
     * @param stepByStep  true — пошаговый режим (Enter для продолжения),
     *                    false — автоматический режим с паузой между ходами
     * @param scanner     источник ввода (тот же, что использовался для выбора
     *                    режима, — второй Scanner на System.in теряет ввод)
     */
    public void run(int maxSteps, boolean stepByStep, Scanner scanner) {
        boolean collapsed = false;
        while (step < maxSteps) {
            collapsed = advanceOneStep();
            print();

            if (collapsed) {
                printCollapseBanner();
                break;
            }

            if (stepByStep) {
                System.out.println("Enter — следующий шаг, q + Enter — выход.");
                if (!scanner.hasNextLine()) break;
                String line = scanner.nextLine();
                if (line.trim().equalsIgnoreCase("q")) break;
            } else {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ignored) {
                }
            }
        }
        printEventLog();
        System.out.println("Симуляция завершена. Всего ходов: " + step);
    }

    /**
     * "Мгновенный" режим: сразу просчитывает всё до последнего хода без
     * покадрового вывода и задержек, и показывает только итоговое состояние
     * поля. Удобно, когда не нужно наблюдать за миллионом промежуточных
     * итераций, а интересен только конечный результат.
     */
    public void runInstant(int maxSteps) {
        boolean collapsed = false;
        while (step < maxSteps) {
            collapsed = advanceOneStep();
            if (collapsed) break;
        }
        print();
        if (collapsed) {
            printCollapseBanner();
        }
        printEventLog();
        System.out.println("Симуляция завершена. Всего ходов: " + step);
    }

    /**
     * Один ход симуляции: все агенты действуют. Возвращает true, если
     * ЛЮБОЙ из трёх видов (растения, травоядные, хищники) полностью
     * исчез — жизнь считается "продолжающейся" только пока живы все три.
     */
    public void step() {
        advanceOneStep();
    }

    /**
     * Просчитывает симуляцию без промежуточной визуализации до лимита шагов
     * или до исчезновения одного из видов.
     */
    public void runToStepLimit(int maxSteps) {
        while (step < maxSteps && !isCollapsed()) {
            advanceOneStep();
        }
    }

    public int getPlantCount() {
        return env.countAgents(Plant.class);
    }

    public int getHerbivoreCount() {
        return env.countAgents(Herbivore.class);
    }

    public int getPredatorCount() {
        return env.countAgents(Predator.class);
    }

    public int getStep() {
        return step;
    }

    public int getWidth() {
        return env.getWidth();
    }

    public int getHeight() {
        return env.getHeight();
    }

    public Agent getAgent(int x, int y) {
        return env.getAgentAt(x, y);
    }

    public boolean isCollapsed() {
        return getPlantCount() == 0 || getHerbivoreCount() == 0 || getPredatorCount() == 0;
    }

    public String getCollapseDescription() {
        if (getPlantCount() == 0) {
            return "растения вымерли";
        }
        if (getHerbivoreCount() == 0) {
            return "травоядные вымерли";
        }
        if (getPredatorCount() == 0) {
            return "хищники вымерли";
        }
        return "";
    }

    private boolean advanceOneStep() {
        step++;
        // снимок агентов на начало хода, чтобы вновь рождённые не действовали в этом же ходу
        List<Agent> agents = env.getAllAgents();
        for (Agent a : agents) {
            a.act();
        }

        int plants = env.countAgents(Plant.class);
        int herbs = env.countAgents(Herbivore.class);
        int preds = env.countAgents(Predator.class);
        trackExtinctionEvents(plants, herbs, preds);

        return plants == 0 || herbs == 0 || preds == 0;
    }

    /** Отслеживает момент полного исчезновения вида. */
    private void trackExtinctionEvents(int plants, int herbs, int preds) {
        if (plants == 0 && !plantsExtinct) {
            logEvent("🌿 Растения полностью исчезли с поля.");
            plantsExtinct = true;
        }
        if (herbs == 0 && !herbivoresExtinct) {
            logEvent("🐇 Травоядные полностью вымерли.");
            herbivoresExtinct = true;
        }
        if (preds == 0 && !predatorsExtinct) {
            logEvent("🐺 Хищники полностью вымерли.");
            predatorsExtinct = true;
        }
    }

    private void logEvent(String message) {
        eventLog.add("Шаг " + step + ": " + message);
    }

    /**
     * Заметный баннер на весь экран, когда симуляция останавливается
     * из-за исчезновения одного из видов. Жизнь считается продолжительной,
     * только пока живы все три вида одновременно.
     */
    private void printCollapseBanner() {
        int plants = env.countAgents(Plant.class);
        int herbs = env.countAgents(Herbivore.class);
        int preds = env.countAgents(Predator.class);
        System.out.println();
        System.out.println("‼️‼️‼️  ЭКОСИСТЕМА НЕ ВЫЖИЛА  ‼️‼️‼️");
        System.out.println("Шаг гибели: " + step);
        if (plants == 0) System.out.println("🌿 Растения полностью исчезли.");
        if (herbs == 0) System.out.println("🐇 Травоядные полностью вымерли.");
        if (preds == 0) System.out.println("🐺 Хищники полностью вымерли.");
        System.out.println("Итог: растений " + plants + ", травоядных " + herbs + ", хищников " + preds + ".");
        System.out.println();
    }

    /** Печатает журнал всех вымираний видов за прогон. */
    private void printEventLog() {
        System.out.println();
        System.out.println("=== Журнал ключевых событий ===");
        if (eventLog.isEmpty()) {
            System.out.println("Значимых событий не было — популяции держались стабильно.");
        } else {
            for (String entry : eventLog) {
                System.out.println(entry);
            }
        }
        System.out.println("================================");
    }

    private void print() {
        // очистка консоли (ANSI-escape, работает в большинстве терминалов)
        System.out.print("\033[H\033[2J");
        System.out.flush();

        System.out.println(env.render());

        int plants = env.countAgents(Plant.class);
        int herbs = env.countAgents(Herbivore.class);
        int preds = env.countAgents(Predator.class);
        System.out.printf(
                "Шаг: %d  |  Растения (P): %d  |  Травоядные (H): %d  |  Хищники (X): %d%n",
                step, plants, herbs, preds);
    }
}