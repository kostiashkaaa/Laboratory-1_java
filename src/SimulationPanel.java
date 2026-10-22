import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.RenderingHints;

/**
 * Графическое представление двумерной среды.
 */
@SuppressWarnings({"serial", "this-escape"})
public class SimulationPanel extends JPanel {

    private static final Color GRID_COLOR = new Color(205, 214, 224);
    private static final Color EMPTY_COLOR = new Color(245, 248, 252);
    private static final Color PLANT_MARKER = new Color(46, 160, 67, 45);
    private static final Color HERBIVORE_MARKER = new Color(245, 158, 11, 55);
    private static final Color PREDATOR_MARKER = new Color(220, 38, 38, 55);
    private static final Color PLANT_SOLID = new Color(46, 160, 67);
    private static final Color HERBIVORE_SOLID = new Color(245, 158, 11);
    private static final Color PREDATOR_SOLID = new Color(220, 38, 38);
    private static final int MIN_GRID_CELL = 4;
    private static final int MIN_EMOJI_CELL = 10;
    private final Simulation simulation;

    public SimulationPanel(Simulation simulation) {
        this.simulation = simulation;
        setBackground(EMPTY_COLOR);
        setPreferredSize(new Dimension(760, 760));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // Клетка подстраивается под панель, чтобы поле любого размера
            // помещалось целиком (без обрезки краёв).
            int cellSize = Math.max(1, Math.min(
                    getWidth() / simulation.getWidth(),
                    getHeight() / simulation.getHeight()));
            int gridWidth = cellSize * simulation.getWidth();
            int gridHeight = cellSize * simulation.getHeight();
            int offsetX = (getWidth() - gridWidth) / 2;
            int offsetY = (getHeight() - gridHeight) / 2;

            g.setColor(EMPTY_COLOR);
            g.fillRect(offsetX, offsetY, gridWidth, gridHeight);
            // На очень мелких клетках линии сетки закрыли бы всё поле.
            if (cellSize >= MIN_GRID_CELL) {
                g.setColor(GRID_COLOR);
                for (int x = 0; x <= simulation.getWidth(); x++) {
                    int pixelX = offsetX + x * cellSize;
                    g.drawLine(pixelX, offsetY, pixelX, offsetY + gridHeight);
                }
                for (int y = 0; y <= simulation.getHeight(); y++) {
                    int pixelY = offsetY + y * cellSize;
                    g.drawLine(offsetX, pixelY, offsetX + gridWidth, pixelY);
                }
            }

            for (int y = 0; y < simulation.getHeight(); y++) {
                for (int x = 0; x < simulation.getWidth(); x++) {
                    Agent agent = simulation.getAgent(x, y);
                    if (agent == null) {
                        continue;
                    }

                    // Эмодзи в мелкой клетке не читается и вылезает за её
                    // границы — рисуем сплошной цветной квадрат.
                    if (cellSize < MIN_EMOJI_CELL) {
                        g.setColor(solidColor(agent));
                        g.fillRect(offsetX + x * cellSize, offsetY + y * cellSize,
                                cellSize, cellSize);
                        continue;
                    }

                    int markerSize = Math.max(4, cellSize - 4);
                    int markerX = offsetX + x * cellSize + (cellSize - markerSize) / 2;
                    int markerY = offsetY + y * cellSize + (cellSize - markerSize) / 2;
                    g.setColor(markerColor(agent));
                    g.fillRoundRect(markerX, markerY, markerSize, markerSize,
                            Math.max(4, markerSize / 3), Math.max(4, markerSize / 3));

                    int fontSize = Math.max(11, Math.min(24, cellSize + 4));
                    g.setFont(new Font("Apple Color Emoji", Font.PLAIN, fontSize));
                    FontMetrics metrics = g.getFontMetrics();
                    String symbol = agent.getSymbol();
                    int textWidth = metrics.stringWidth(symbol);
                    int textX = offsetX + x * cellSize
                            + (cellSize - textWidth) / 2;
                    int textY = offsetY + y * cellSize
                            + (cellSize - metrics.getHeight()) / 2
                            + metrics.getAscent();
                    g.drawString(symbol, textX, textY);
                }
            }
        } finally {
            g.dispose();
        }
    }

    private Color solidColor(Agent agent) {
        if (agent instanceof Plant) {
            return PLANT_SOLID;
        }
        if (agent instanceof Herbivore) {
            return HERBIVORE_SOLID;
        }
        return PREDATOR_SOLID;
    }

    private Color markerColor(Agent agent) {
        if (agent instanceof Plant) {
            return PLANT_MARKER;
        }
        if (agent instanceof Herbivore) {
            return HERBIVORE_MARKER;
        }
        return PREDATOR_MARKER;
    }

}
