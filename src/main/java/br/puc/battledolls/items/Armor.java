package br.puc.battledolls.items;

import br.puc.battledolls.model.Equipment;
import br.puc.battledolls.model.RobotStatsBuilder;

public class Armor implements Equipment {
    private final String name;
    private final int level;

    public Armor(String name, int level) { this.name = name; this.level = level; }

    @Override public String name() { return name; }
    @Override public int getLevel() { return level; }
    @Override public int getCost() { return 25 * level * level; }

    @Override
    public void apply(RobotStatsBuilder b) {
        b.addDef(4 * level).addHp(8 * level);
    }
}
