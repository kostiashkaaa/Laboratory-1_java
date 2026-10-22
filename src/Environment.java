import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Пространственная модель: двумерная дискретная сетка с чёткими границами.
 * Каждая ячейка либо пуста, либо содержит ссылку на агента.
 */
public class Environment {

    private final int width, height;
    private final Agent[][] grid;
    private static final Random rnd = new Random();

    public Environment(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new Agent[width][height];
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public boolean isInside(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    public int distance(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public Agent getAgent(int x, int y) {
        return isInside(x, y) ? grid[x][y] : null;
    }

    public void setAgent(int x, int y, Agent agent) {
        if (isInside(x, y)) {
            grid[x][y] = agent;
        }
    }

    public boolean isEmpty(int x, int y) {
        return isInside(x, y) && grid[x][y] == null;
    }

    public Agent getAgentAt(int x, int y) {
        return getAgent(x, y);
    }

    /**
     * Возвращает список координат соседних клеток в заданном радиусе.
     * При radius = 2 получаем область зрения 5x5 (агент в центре).
     */
    public List<int[]> getNeighbors(int x, int y, int radius) {
        List<int[]> result = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                if (dx == 0 && dy == 0) continue;
                int neighborX = x + dx;
                int neighborY = y + dy;
                if (isInside(neighborX, neighborY)) {
                    result.add(new int[]{neighborX, neighborY});
                }
            }
        }
        return result;
    }

    /** Соседние клетки по прямой (сверху, снизу, слева, справа) в пределах поля. */
    public List<int[]> getCardinalNeighbors(int x, int y) {
        List<int[]> result = new ArrayList<>();
        int[][] dirs = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        for (int[] d : dirs) {
            if (isInside(x + d[0], y + d[1])) {
                result.add(new int[]{x + d[0], y + d[1]});
            }
        }
        return result;
    }

    /** Возвращает случайную свободную клетку среди непосредственных соседей (для размножения/движения). */
    public int[] getFreeNeighbor(int x, int y) {
        List<int[]> neighbors = getNeighbors(x, y, 1);
        Collections.shuffle(neighbors, rnd);
        for (int[] c : neighbors) {
            if (isEmpty(c[0], c[1])) return c;
        }
        return null;
    }

    public int countAgents(Class<? extends Agent> type) {
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (grid[i][j] != null && type.isInstance(grid[i][j])) count++;
            }
        }
        return count;
    }

    /** Снимок всех живых агентов на текущий момент (для последовательного обхода за ход). */
    public List<Agent> getAllAgents() {
        List<Agent> agents = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (grid[i][j] != null) agents.add(grid[i][j]);
            }
        }
        return agents;
    }

    /**
     * Визуализация сетки эмодзи: 🌿 — растение, 🐇 — травоядное, 🐺 — хищник,
     * · — пустая клетка (как указано в задании: "ASCII-символы 🌿 🐇 🐺").
     * Рамку из +/- намеренно не рисуем: разные эмодзи занимают в терминале
     * разную ширину, из-за чего рамка почти всегда расходится с самой
     * сеткой по правому краю.
     *
     * Важно про выравнивание: сами эмодзи в большинстве терминалов/шрифтов
     * рисуются "широкими" — занимают по ширине как два обычных символа.
     * Поэтому после эмодзи пробел НЕ добавляется, а вот точку для пустой
     * клетки, наоборот, дополняем одним пробелом — чтобы обе клетки (и
     * пустая, и занятая) визуально занимали одинаковую ширину и столбцы
     * сетки не расходились ("не ехали") от строки к строке.
     */
    private static final String EMPTY_CELL = "· ";

    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Agent a = grid[x][y];
                sb.append(a == null ? EMPTY_CELL : a.getSymbol());
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
