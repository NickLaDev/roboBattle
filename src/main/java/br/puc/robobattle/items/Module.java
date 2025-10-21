package br.puc.robobattle.items;

import br.puc.robobattle.model.Equipment;
import br.puc.robobattle.model.RobotStatsBuilder;

public class Module implements Equipment {
    public enum Type { MOBILIDADE, CPU, BATERIA }

    private final Type type;
    private final int level;

    public Module(Type type, int level) { this.type = type; this.level = level; }

    @Override public String name() { return type.name(); }
    @Override public int getLevel() { return level; }
    @Override public int getCost() { return 20 * level * level; }

    @Override
    public void apply(RobotStatsBuilder b) {
        switch (type) {
            case MOBILIDADE -> b.addEvade(0.03 * level);
            case CPU       -> b.addCrit(0.04 * level);
            case BATERIA   -> { /* flag tratada na classe Robot */ }
        }
    }

    public Type type() { return type; }
}
