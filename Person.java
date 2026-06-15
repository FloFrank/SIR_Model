public class Person {

    // The three possible states of a person
    public enum State {
        SUSCEPTIBLE,   // can get infected
        INFECTIOUS,    // currently infected and can spread
        RECOVERED      // no longer infectious
    }

    private State state;
    private double x;
    private double y;
    private int minutesInCurrentState;

    public Person(State state, double x, double y) {
        this.state = state;
        this.x = x;
        this.y = y;
        this.minutesInCurrentState = 0;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        this.minutesInCurrentState = 0;
    }

    public double getX() { return x; }
    public double getY() { return y; }

    public void moveRandomly(java.util.Random random, double maxStepMeters,
                             double xMin, double xMax, double yMin, double yMax) {
        double stepX = (random.nextDouble() * 2 - 1) * maxStepMeters;
        double stepY = (random.nextDouble() * 2 - 1) * maxStepMeters;

        x = clamp(x + stepX, xMin, xMax);
        y = clamp(y + stepY, yMin, yMax);
    }

    public void advanceDisease(int stepMinutes, int infectiousDurationMinutes, int immunityDurationMinutes) {
        if (state == State.SUSCEPTIBLE) {
            return;
        }

        minutesInCurrentState += stepMinutes;

        if (state == State.INFECTIOUS && minutesInCurrentState >= infectiousDurationMinutes) {
            setState(State.RECOVERED);
            return;
        }

        if (state == State.RECOVERED && minutesInCurrentState >= immunityDurationMinutes) {
            setState(State.SUSCEPTIBLE);
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public String toString() {
        return "Person[" + state + ", x=" + String.format("%.1f", x) + ", y=" + String.format("%.1f", y) + "]";
    }
}
