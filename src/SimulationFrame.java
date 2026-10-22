import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JScrollPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Component;

/**
 * Альтернативный интерфейс: тёмная панель управления слева и поле среды
 * отдельно от настроек.
 */
@SuppressWarnings({"serial", "this-escape"})
public class SimulationFrame extends JFrame {

    private static final Color BACKGROUND = new Color(238, 242, 247);
    private static final Color SIDEBAR = new Color(25, 34, 50);

    private Simulation simulation;
    private SimulationPanel simulationPanel;
    private Timer timer;
    private JLabel stateLabel;
    private JLabel stepLabel;
    private JLabel plantsLabel;
    private JLabel herbivoresLabel;
    private JLabel predatorsLabel;
    private JComboBox<String> modeComboBox;

    private JSpinner plantsSpinner;
    private JSpinner herbivoresSpinner;
    private JSpinner predatorsSpinner;
    private JSpinner widthSpinner;
    private JSpinner heightSpinner;
    private JSpinner plantEnergySpinner;
    private JSpinner herbivoreEnergySpinner;
    private JSpinner predatorEnergySpinner;
    private JSpinner plantThresholdSpinner;
    private JSpinner herbivoreThresholdSpinner;
    private JSpinner predatorThresholdSpinner;
    private JSpinner plantChildEnergySpinner;
    private JSpinner herbivoreChildEnergySpinner;
    private JSpinner predatorChildEnergySpinner;
    private JSpinner maxStepsSpinner;

    public SimulationFrame() {
        super("Artificial Life · Ecology Lab");
        createSimulationFromSettings();
        buildWindow();
    }

    private void buildWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(BACKGROUND);
        setLayout(new BorderLayout());

        add(createSidebar(), BorderLayout.WEST);
        add(createWorkspace(), BorderLayout.CENTER);
        updateStatistics();
        setMinimumSize(new Dimension(1050, 760));
        setSize(1220, 850);
        setLocationRelativeTo(null);
    }

    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout(0, 12));
        workspace.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        workspace.setOpaque(false);

        JPanel title = new JPanel(new BorderLayout());
        title.setOpaque(false);
        JLabel heading = new JLabel("Экосистема");
        heading.setFont(heading.getFont().deriveFont(Font.BOLD, 26f));
        JLabel hint = new JLabel("Наблюдение за поведением логических агентов");
        hint.setForeground(new Color(100, 113, 130));
        title.add(heading, BorderLayout.NORTH);
        title.add(hint, BorderLayout.SOUTH);

        JPanel fieldCard = new JPanel(new BorderLayout());
        fieldCard.setBackground(Color.WHITE);
        fieldCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        fieldCard.add(simulationPanel, BorderLayout.CENTER);

        workspace.add(title, BorderLayout.NORTH);
        workspace.add(fieldCard, BorderLayout.CENTER);
        return workspace;
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout(0, 14));
        sidebar.setPreferredSize(new Dimension(370, 0));
        sidebar.setBackground(SIDEBAR);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 18, 18, 18));

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 5));
        top.setOpaque(false);
        JLabel brand = new JLabel("LIFE / 01");
        brand.setForeground(new Color(120, 210, 177));
        brand.setFont(brand.getFont().deriveFont(Font.BOLD, 16f));
        JLabel title = new JLabel("Управление");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        stateLabel = new JLabel("● ПАУЗА");
        stateLabel.setForeground(new Color(175, 190, 205));
        top.add(brand);
        top.add(title);
        top.add(stateLabel);

        JPanel content = new JPanel();
        content.setLayout(new javax.swing.BoxLayout(content, javax.swing.BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        content.add(createActionPanel());
        content.add(createStatsPanel());
        content.add(createSettingsPanel());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(scrollPane, BorderLayout.CENTER);
        return sidebar;
    }

    private JPanel createActionPanel() {
        JPanel panel = darkSection("УПРАВЛЕНИЕ");
        JButton start = button("▶  Запустить", new Color(55, 173, 125));
        JButton pause = button("Ⅱ  Пауза", new Color(67, 82, 101));
        JButton step = button("＋  Один шаг", new Color(67, 82, 101));
        JButton reset = button("↻  Сбросить поле", new Color(67, 82, 101));
        start.addActionListener(event -> start());
        pause.addActionListener(event -> pause());
        step.addActionListener(event -> {
            if (!timer.isRunning()) advance();
        });
        reset.addActionListener(event -> resetSimulation());
        JPanel actionButtons = new JPanel(new GridLayout(4, 1, 0, 7));
        actionButtons.setOpaque(false);
        actionButtons.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionButtons.setMaximumSize(new Dimension(245, 165));
        actionButtons.add(start);
        actionButtons.add(pause);
        actionButtons.add(step);
        actionButtons.add(reset);
        panel.add(actionButtons);

        modeComboBox = new JComboBox<>(new String[]{
                "Пошаговый режим",
                "Автоматический режим",
                "Мгновенный итог"
        });
        modeComboBox.setSelectedIndex(1);
        modeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel modeLabel = label("РЕЖИМ");
        modeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(modeLabel);
        panel.add(modeComboBox);

        JLabel speedValue = label("18 шагов/с");
        speedValue.setAlignmentX(Component.CENTER_ALIGNMENT);
        JSlider speed = new JSlider(1, 35, 18);
        speed.setOpaque(false);
        speed.setPaintTicks(true);
        speed.setMajorTickSpacing(5);
        speed.setMinorTickSpacing(1);
        speed.addChangeListener(event -> {
            int stepsPerSecond = speed.getValue();
            timer.setDelay(1000 / stepsPerSecond);
            speedValue.setText(stepsPerSecond + " шагов/с");
        });
        JLabel speedLabel = label("СКОРОСТЬ");
        speedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(speedLabel);
        panel.add(speed);
        panel.add(speedValue);
        return panel;
    }

    private JPanel createStatsPanel() {
        JPanel panel = darkSection("ТЕКУЩЕЕ СОСТОЯНИЕ");
        stepLabel = darkValue();
        plantsLabel = darkValue();
        herbivoresLabel = darkValue();
        predatorsLabel = darkValue();
        panel.add(stepLabel);
        panel.add(plantsLabel);
        panel.add(herbivoresLabel);
        panel.add(predatorsLabel);
        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = darkSection("НОВАЯ СИМУЛЯЦИЯ");
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 620));
        widthSpinner = spinner(50, 5, 200, 5);
        heightSpinner = spinner(50, 5, 200, 5);
        plantsSpinner = spinner(998, 0, 2500, 50);
        herbivoresSpinner = spinner(79, 0, 1000, 1);
        predatorsSpinner = spinner(13, 0, 500, 1);
        plantEnergySpinner = spinner(10, 0, 10000, 10);
        herbivoreEnergySpinner = spinner(43, 0, 10000, 10);
        predatorEnergySpinner = spinner(418, 0, 10000, 50);
        plantThresholdSpinner = spinner(39, 0, 100, 10);
        herbivoreThresholdSpinner = spinner(410, 0, 10000, 10);
        predatorThresholdSpinner = spinner(850, 0, 10000, 10);
        plantChildEnergySpinner = spinner(28, 0, 10000, 10);
        herbivoreChildEnergySpinner = spinner(93, 0, 10000, 10);
        predatorChildEnergySpinner = spinner(244, 0, 10000, 10);
        maxStepsSpinner = spinner(5000, 1, 1_000_000, 500);
        addSetting(panel, "Ширина поля", widthSpinner);
        addSetting(panel, "Высота поля", heightSpinner);
        addSetting(panel, "Растения", plantsSpinner);
        addSetting(panel, "Травоядные", herbivoresSpinner);
        addSetting(panel, "Хищники", predatorsSpinner);
        addSetting(panel, "E растений", plantEnergySpinner);
        addSetting(panel, "E травоядных", herbivoreEnergySpinner);
        addSetting(panel, "E хищников", predatorEnergySpinner);
        addSetting(panel, "Порог размн. растений", plantThresholdSpinner);
        addSetting(panel, "Порог размн. травоядных", herbivoreThresholdSpinner);
        addSetting(panel, "Порог размн. хищников", predatorThresholdSpinner);
        addSetting(panel, "Обмен E растений", plantChildEnergySpinner);
        addSetting(panel, "Обмен E травоядных", herbivoreChildEnergySpinner);
        addSetting(panel, "Обмен E хищников", predatorChildEnergySpinner);
        addSetting(panel, "Макс. шагов", maxStepsSpinner);
        return panel;
    }

    private JPanel darkSection(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new javax.swing.BoxLayout(panel, javax.swing.BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 86, 104)),
                BorderFactory.createEmptyBorder(8, 10, 12, 10)));
        JLabel header = label(title);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        return panel;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(new Color(170, 184, 199));
        label.setFont(label.getFont().deriveFont(Font.BOLD, 10f));
        label.setBorder(BorderFactory.createEmptyBorder(7, 2, 3, 2));
        return label;
    }

    private JLabel darkValue() {
        JLabel value = new JLabel();
        value.setForeground(Color.WHITE);
        value.setFont(value.getFont().deriveFont(Font.BOLD, 14f));
        value.setBorder(BorderFactory.createEmptyBorder(3, 2, 3, 2));
        return value;
    }

    private JButton button(String text, Color color) {
        JButton button = new JButton(text);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        return button;
    }

    private void addSetting(JPanel panel, String name, JSpinner spinner) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(name);
        label.setForeground(new Color(215, 222, 230));
        label.setPreferredSize(new Dimension(145, 24));
        row.add(label, BorderLayout.WEST);
        row.add(spinner, BorderLayout.EAST);
        panel.add(row);
    }

    private JSpinner spinner(int value, int min, int max, int step) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
        spinner.setPreferredSize(new Dimension(105, 26));
        spinner.setMaximumSize(new Dimension(105, 26));
        return spinner;
    }

    private void createSimulationFromSettings() {
        if (plantsSpinner == null) {
            simulation = new Simulation();
        } else {
            simulation = new Simulation(
                    (Integer) widthSpinner.getValue(),
                    (Integer) heightSpinner.getValue(),
                    (Integer) plantsSpinner.getValue(),
                    (Integer) herbivoresSpinner.getValue(),
                    (Integer) predatorsSpinner.getValue(),
                    ((Number) plantEnergySpinner.getValue()).doubleValue(),
                    ((Number) herbivoreEnergySpinner.getValue()).doubleValue(),
                    ((Number) predatorEnergySpinner.getValue()).doubleValue(),
                    ((Number) plantThresholdSpinner.getValue()).doubleValue(),
                    ((Number) herbivoreThresholdSpinner.getValue()).doubleValue(),
                    ((Number) predatorThresholdSpinner.getValue()).doubleValue(),
                    ((Number) plantChildEnergySpinner.getValue()).doubleValue(),
                    ((Number) herbivoreChildEnergySpinner.getValue()).doubleValue(),
                    ((Number) predatorChildEnergySpinner.getValue()).doubleValue());
        }
        simulationPanel = new SimulationPanel(simulation);
        timer = new Timer(100, event -> advance());
    }

    private void resetSimulation() {
        pause();
        try {
            createSimulationFromSettings();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this, exception.getMessage(),
                    "Некорректные параметры", JOptionPane.WARNING_MESSAGE);
            return;
        }
        getContentPane().remove(1);
        getContentPane().add(createWorkspace(), BorderLayout.CENTER);
        revalidate();
        updateStatistics();
        stateLabel.setText("● ПАУЗА");
        stateLabel.setToolTipText(null);
        stateLabel.setForeground(new Color(175, 190, 205));
        repaint();
    }

    private void start() {
        if (simulation.isCollapsed()) {
            showExtinctionStatus();
            return;
        }
        int mode = modeComboBox.getSelectedIndex();
        if (mode == 0) {
            advance();
        } else if (mode == 1) {
            timer.start();
            stateLabel.setText("● СИМУЛЯЦИЯ ИДЁТ");
            stateLabel.setForeground(new Color(120, 230, 170));
        } else {
            pause();
            simulation.runToStepLimit(getMaxSteps());
            updateStatistics();
            simulationPanel.repaint();
            if (simulation.isCollapsed()) {
                showExtinctionStatus();
            } else {
                stateLabel.setText("● ПОКАЗАН ИТОГ");
                stateLabel.setForeground(new Color(120, 210, 177));
            }
        }
    }

    private void pause() {
        timer.stop();
        stateLabel.setText("● ПАУЗА");
        stateLabel.setForeground(new Color(175, 190, 205));
    }

    private void advance() {
        if (simulation.isCollapsed()) {
            showExtinctionStatus();
            return;
        }
        if (simulation.getStep() >= getMaxSteps()) {
            pause();
            stateLabel.setText("● ДОСТИГНУТ ЛИМИТ ШАГОВ");
            stateLabel.setForeground(new Color(245, 190, 90));
            return;
        }
        simulation.step();
        updateStatistics();
        simulationPanel.repaint();
        if (simulation.isCollapsed()) {
            showExtinctionStatus();
        } else if (simulation.getStep() >= getMaxSteps()) {
            pause();
            stateLabel.setText("● ДОСТИГНУТ ЛИМИТ ШАГОВ");
            stateLabel.setForeground(new Color(245, 190, 90));
        }
    }

    private int getMaxSteps() {
        return ((Number) maxStepsSpinner.getValue()).intValue();
    }

    private void showExtinctionStatus() {
        pause();
        String description = simulation.getCollapseDescription();
        stateLabel.setText("<html>● ВИД ИСЧЕЗ<br><small>Шаг "
                + simulation.getStep() + ": " + description + "</small></html>");
        stateLabel.setToolTipText("Шаг " + simulation.getStep() + ": " + description);
        stateLabel.setForeground(new Color(255, 120, 120));
    }

    private void updateStatistics() {
        stepLabel.setText("Шаг симуляции: " + simulation.getStep());
        plantsLabel.setText("●  Растения: " + simulation.getPlantCount());
        herbivoresLabel.setText("■  Травоядные: " + simulation.getHerbivoreCount());
        predatorsLabel.setText("◆  Хищники: " + simulation.getPredatorCount());
    }
}
