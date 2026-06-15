# SIR Epidemic Simulation

An interactive, agent-based **SIR (Susceptible–Infectious–Recovered)** epidemic model built in Java with Swing. Individuals move randomly through a 2D space and can infect each other based on proximity — all parameters adjustable in real time.

![Java](https://img.shields.io/badge/Java-17+-orange?logo=java) ![Swing](https://img.shields.io/badge/GUI-Swing-blue) ![Model](https://img.shields.io/badge/Model-SIR-red)

---

## What It Does

- **100 agents** move randomly across a bounded 80 square meters room
- **Proximity-based transmission**: an infectious agent infects susceptible neighbours within 1 m with a configurable probability
- **Live SIR chart** shows the epidemic curve updating in real time alongside the spatial simulation
- **All key parameters** are tweakable via sliders without restarting:

| Parameter | Range | Default |
|---|---|---|
| Movement speed (m/step) | 0.05 – 1.00 | 0.30 |
| Transmission probability | 0 – 100 % | 15 % |
| Infectious duration | 1 – 60 min | 12 min |
| Immunity duration | 1 – 120 min | 25 min |

---

## Model Design

The simulation runs on a **discrete-time SIR model** with spatial heterogeneity:

```
S → I   if within 1 m of an infectious agent AND random draw < transmission probability
I → R   after infectiousDuration minutes
R → S   after immunityDuration minutes  (SIRS variant — immunity wanes)
```

Each tick represents **1 simulated minute**; the timer fires every 120 ms real time.

---

## Getting Started

### Prerequisites
- Java 17 or higher (uses standard library only — no external dependencies)

### Run
```bash
javac Main.java
java Main
```

Or open in any Java IDE (IntelliJ, Eclipse, VS Code + Extension Pack for Java) and run `Main.main()`.

---

## Structure

```
Main.java
├── Main                  # Entry point, simulation logic, UI setup
├── Person                # Agent state machine (S / I / R) + random movement
├── SimulationPanel       # Custom JPanel — spatial dot rendering
└── ChartPanel            # Custom JPanel — live SIR time series chart
```

---

## Background & Motivation

This project was built as part of my exploration of **spatial modelling and simulation** — a topic that intersects directly with my work in geoinformatics and spatial data science. Classic SIR models assume a well-mixed population; adding **spatial movement and proximity-based contact** makes the dynamics significantly more realistic and interesting.

Key concepts demonstrated:
- Agent-based modelling (ABM)
- Discrete-time epidemic compartmental models
- Real-time data visualisation with Java2D
- Event-driven GUI programming (Swing, Timer, Listeners)

## Screenshots 
<img width="1437" height="799" alt="Bildschirmfoto 2026-06-15 um 13 04 32" src="https://github.com/user-attachments/assets/1c0d5c14-b0d4-4432-be6b-f812b8ad05c9" />

<img width="1437" height="799" alt="Bildschirmfoto 2026-06-15 um 13 05 31" src="https://github.com/user-attachments/assets/a1b92029-d5e9-47a0-ae01-da85bd59ad7b" />

