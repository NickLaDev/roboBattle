package br.puc.battledolls.items;

import br.puc.battledolls.model.Equipment;
import br.puc.battledolls.model.RobotStatsBuilder;

public class Weapon implements Equipment {
    private final String name;
    private final int level;

    public Weapon(String name, int level) { this.name = name; this.level = level; }

    @Override public String name() { return name; }
    @Override public int getLevel() { return level; }
    @Override public int getCost() { return 30 * level * level; }

    @Override
    public void apply(RobotStatsBuilder b) {
        b.addAtk(6 * level).addCrit(0.02 * level);
    }
}
