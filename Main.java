
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Main {
    private static final double INFECTION_RADIUS_METERS = 1.0;
    private static final int TIME_STEP_MINUTES = 1;

    private static final int POPULATION_SIZE = 100;
    private static final int PANEL_WIDTH = 900;
    private static final int PANEL_HEIGHT = 720;
    private static final int TIMER_DELAY_MS = 120;

    private static final double X_MIN = 20;
    private static final double X_MAX = 30;
    private static final double Y_MIN = 20;
    private static final double Y_MAX = 28;

    private final Random random = new Random();
    private final List<Person> population = new ArrayList<>();
    private final List<Integer> susceptibleHistory = new ArrayList<>();
    private final List<Integer> infectiousHistory = new ArrayList<>();
    private final List<Integer> recoveredHistory = new ArrayList<>();

    private double maxStepMeters = 0.3;
    private double transmissionProbability = 0.15;
    private int infectiousDurationMinutes = 12;
    private int immunityDurationMinutes = 25;

    private int simulatedMinutes = 0;
    private Timer timer;

    private final JLabel statusLabel = new JLabel();
    private final SimulationPanel simulationPanel = new SimulationPanel();
    private final ChartPanel chartPanel = new ChartPanel();
    private final JLabel speedValueLabel = new JLabel();
    private final JLabel transmissionValueLabel = new JLabel();
    private final JLabel infectiousValueLabel = new JLabel();
    private final JLabel immunityValueLabel = new JLabel();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().createAndShowUI());
    }

    private void createAndShowUI() {
        initializePopulation();

        JFrame frame = new JFrame("SIR Simulation - Bewegte Personen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(8, 8));

        simulationPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        simulationPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        chartPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        centerPanel.add(simulationPanel);
        centerPanel.add(chartPanel);

        JPanel controlsPanel = new JPanel();
        JButton startButton = new JButton("Start");
        JButton pauseButton = new JButton("Pause");
        JButton stopButton = new JButton("Stop");
        JSlider speedSlider = new JSlider(5, 100, 30);
        JSlider transmissionSlider = new JSlider(0, 100, 15);
        JSlider infectiousSlider = new JSlider(1, 60, 12);
        JSlider immunitySlider = new JSlider(1, 120, 25);

        controlsPanel.add(startButton);
        controlsPanel.add(pauseButton);
        controlsPanel.add(stopButton);

        JPanel slidersPanel = new JPanel(new GridLayout(2, 4, 8, 4));
        slidersPanel.add(buildSliderControl("Geschwindigkeit", speedSlider, speedValueLabel));
        slidersPanel.add(buildSliderControl("Ansteckung %", transmissionSlider, transmissionValueLabel));
        slidersPanel.add(buildSliderControl("Infektioes (min)", infectiousSlider, infectiousValueLabel));
        slidersPanel.add(buildSliderControl("Immun (min)", immunitySlider, immunityValueLabel));

        JPanel southPanel = new JPanel(new BorderLayout(0, 4));
        southPanel.add(controlsPanel, BorderLayout.NORTH);
        southPanel.add(slidersPanel, BorderLayout.SOUTH);

        frame.add(centerPanel, BorderLayout.CENTER);
        frame.add(statusLabel, BorderLayout.NORTH);
        frame.add(southPanel, BorderLayout.SOUTH);

        timer = new Timer(TIMER_DELAY_MS, e -> advanceOneStep());

        startButton.addActionListener(e -> {
            timer.start();
        });

        pauseButton.addActionListener(e -> timer.stop());

        stopButton.addActionListener(e -> {
            timer.stop();
            resetSimulation();
        });

        speedSlider.addChangeListener(e -> {
            maxStepMeters = speedSlider.getValue() / 100.0;
            speedValueLabel.setText(String.format("%.2f m", maxStepMeters));
        });

        transmissionSlider.addChangeListener(e -> {
            transmissionProbability = transmissionSlider.getValue() / 100.0;
            transmissionValueLabel.setText(transmissionSlider.getValue() + "%");
            updateStatusText(0);
        });

        infectiousSlider.addChangeListener(e -> {
            infectiousDurationMinutes = infectiousSlider.getValue();
            infectiousValueLabel.setText(infectiousDurationMinutes + " min");
            updateStatusText(0);
        });

        immunitySlider.addChangeListener(e -> {
            immunityDurationMinutes = immunitySlider.getValue();
            immunityValueLabel.setText(immunityDurationMinutes + " min");
            updateStatusText(0);
        });

        speedValueLabel.setText(String.format("%.2f m", maxStepMeters));
        transmissionValueLabel.setText((int) Math.round(transmissionProbability * 100) + "%");
        infectiousValueLabel.setText(infectiousDurationMinutes + " min");
        immunityValueLabel.setText(immunityDurationMinutes + " min");

        resetHistoryData();
        updateStatusText(0);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void initializePopulation() {
        population.clear();

        for (int i = 0; i < POPULATION_SIZE - 2; i++) {
            population.add(new Person(
                Person.State.SUSCEPTIBLE,
                X_MIN + random.nextDouble() * (X_MAX - X_MIN),
                Y_MIN + random.nextDouble() * (Y_MAX - Y_MIN)
            ));
        }

        population.add(new Person(
            Person.State.INFECTIOUS,
            X_MIN + random.nextDouble() * (X_MAX - X_MIN),
            Y_MIN + random.nextDouble() * (Y_MAX - Y_MIN)
        ));

        population.add(new Person(
            Person.State.RECOVERED,
            X_MIN + random.nextDouble() * (X_MAX - X_MIN),
            Y_MIN + random.nextDouble() * (Y_MAX - Y_MIN)
        ));
    }

    private void advanceOneStep() {
        for (Person person : population) {
            person.moveRandomly(random, maxStepMeters, X_MIN, X_MAX, Y_MIN, Y_MAX);
        }

        List<Person> newlyInfected = new ArrayList<>();

        for (Person source : population) {
            if (source.getState() != Person.State.INFECTIOUS) {
                continue;
            }

            for (Person target : population) {
                if (target.getState() != Person.State.SUSCEPTIBLE || newlyInfected.contains(target)) {
                    continue;
                }

                double dx = source.getX() - target.getX();
                double dy = source.getY() - target.getY();
                double distance = Math.sqrt(dx * dx + dy * dy);

                if (distance <= INFECTION_RADIUS_METERS && random.nextDouble() < transmissionProbability) {
                    newlyInfected.add(target);
                }
            }
        }

        for (Person p : newlyInfected) {
            p.setState(Person.State.INFECTIOUS);
        }

        for (Person person : population) {
            person.advanceDisease(TIME_STEP_MINUTES, infectiousDurationMinutes, immunityDurationMinutes);
        }

        simulatedMinutes += TIME_STEP_MINUTES;
        appendCurrentCountsToHistory();
        updateStatusText(newlyInfected.size());
        simulationPanel.repaint();
        chartPanel.repaint();
    }

    private void resetSimulation() {
        simulatedMinutes = 0;
        initializePopulation();
        resetHistoryData();
        updateStatusText(0);
        simulationPanel.repaint();
        chartPanel.repaint();
    }

    private void updateStatusText(int newlyInfected) {
        int susceptible = countByState(Person.State.SUSCEPTIBLE);
        int infectious = countByState(Person.State.INFECTIOUS);
        int recovered = countByState(Person.State.RECOVERED);

        statusLabel.setText(
            "Zeit: " + simulatedMinutes + " min"
                + " | Neu infiziert: " + newlyInfected
                + " | Ansteckung: " + (int) Math.round(transmissionProbability * 100) + "%"
                + " | Infektioes: " + infectiousDurationMinutes + " min"
                + " | Immun: " + immunityDurationMinutes + " min"
                + " | S=" + susceptible + " I=" + infectious + " R=" + recovered
        );
    }

    private JPanel buildSliderControl(String title, JSlider slider, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(title), BorderLayout.NORTH);
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.SOUTH);
        return panel;
    }

    private int countByState(Person.State state) {
        return (int) population.stream().filter(p -> p.getState() == state).count();
    }

    private void resetHistoryData() {
        susceptibleHistory.clear();
        infectiousHistory.clear();
        recoveredHistory.clear();
        appendCurrentCountsToHistory();
    }

    private void appendCurrentCountsToHistory() {
        susceptibleHistory.add(countByState(Person.State.SUSCEPTIBLE));
        infectiousHistory.add(countByState(Person.State.INFECTIOUS));
        recoveredHistory.add(countByState(Person.State.RECOVERED));
    }

    private class SimulationPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(new Color(240, 240, 240));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            g.drawRect(10, 10, getWidth() - 20, getHeight() - 20);

            for (Person p : population) {
                int px = mapXToPixel(p.getX());
                int py = mapYToPixel(p.getY());

                if (p.getState() == Person.State.SUSCEPTIBLE) {
                    g.setColor(new Color(0, 120, 255));
                } else if (p.getState() == Person.State.INFECTIOUS) {
                    g.setColor(new Color(220, 30, 30));
                } else {
                    g.setColor(new Color(40, 170, 70));
                }

                g.fillOval(px - 4, py - 4, 8, 8);
            }
        }

        private int mapXToPixel(double x) {
            double normalized = (x - X_MIN) / (X_MAX - X_MIN);
            return 10 + (int) Math.round(normalized * (getWidth() - 20));
        }

        private int mapYToPixel(double y) {
            double normalized = (y - Y_MIN) / (Y_MAX - Y_MIN);
            return 10 + (int) Math.round(normalized * (getHeight() - 20));
        }
    }

    private class ChartPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int left = 50;
            int right = 20;
            int top = 20;
            int bottom = 40;

            int chartWidth = getWidth() - left - right;
            int chartHeight = getHeight() - top - bottom;

            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(Color.BLACK);
            g.drawRect(left, top, chartWidth, chartHeight);
            g.drawString("Personen", 8, top + 5);
            g.drawString("Zeit (min)", left + chartWidth - 55, getHeight() - 10);

            drawSeries(g, susceptibleHistory, new Color(0, 120, 255), left, top, chartWidth, chartHeight);
            drawSeries(g, infectiousHistory, new Color(220, 30, 30), left, top, chartWidth, chartHeight);
            drawSeries(g, recoveredHistory, new Color(40, 170, 70), left, top, chartWidth, chartHeight);

            g.setColor(new Color(0, 120, 255));
            g.drawString("S", left + 8, top + 16);
            g.setColor(new Color(220, 30, 30));
            g.drawString("I", left + 28, top + 16);
            g.setColor(new Color(40, 170, 70));
            g.drawString("R", left + 48, top + 16);
        }

        private void drawSeries(Graphics g, List<Integer> data, Color color,
                                int left, int top, int chartWidth, int chartHeight) {
            if (data.isEmpty()) {
                return;
            }

            int maxPoints = Math.max(data.size(), 2);
            g.setColor(color);

            for (int i = 1; i < data.size(); i++) {
                int x1 = left + (int) Math.round(((double) (i - 1) / (maxPoints - 1)) * chartWidth);
                int y1 = top + chartHeight - (int) Math.round(((double) data.get(i - 1) / POPULATION_SIZE) * chartHeight);

                int x2 = left + (int) Math.round(((double) i / (maxPoints - 1)) * chartWidth);
                int y2 = top + chartHeight - (int) Math.round(((double) data.get(i) / POPULATION_SIZE) * chartHeight);

                g.drawLine(x1, y1, x2, y2);
            }
        }
    }
}
