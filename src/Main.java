import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.swing.SwingUtilities;

/**
 * Точка входа консольного приложения "Искусственная жизнь".
 */
public class Main {
    public static void main(String[] args) {
        // Явно фиксируем UTF-8 для вывода, чтобы эмодзи и кириллица
        // отображались корректно независимо от системной кодировки консоли.
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));

        if (args.length == 0 || !args[0].equalsIgnoreCase("--console")) {
            SwingUtilities.invokeLater(() -> new SimulationFrame().setVisible(true));
            return;
        }

        Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("Искусственная жизнь: консольная версия");
        System.out.println("Легенда: 🌿 — растение, 🐇 — травоядное, 🐺 — хищник, · — пусто");
        System.out.println();
        System.out.println("Выберите режим работы:");
        System.out.println("  1 — пошаговый (нажимать Enter после каждого хода)");
        System.out.println("  2 — автоматический (ходы идут сами с небольшой паузой)");
        System.out.println("  3 — мгновенный (без вывода промежуточных ходов, сразу итог)");
        System.out.print("> ");
        String choice = scanner.nextLine().trim();
        boolean stepByStep = choice.equals("1");
        boolean instant = choice.equals("3");

        System.out.print("Максимальное число ходов (Enter — 500 по умолчанию): ");
        String stepsInput = scanner.nextLine().trim();
        int maxSteps = 500;
        if (!stepsInput.isEmpty()) {
            try {
                maxSteps = Integer.parseInt(stepsInput);
            } catch (NumberFormatException e) {
                System.out.println("Некорректное число, используется значение по умолчанию: 500");
            }
        }

        Simulation sim = new Simulation();
        if (instant) {
            sim.runInstant(maxSteps);
        } else {
            sim.run(maxSteps, stepByStep);
        }
    }
}
