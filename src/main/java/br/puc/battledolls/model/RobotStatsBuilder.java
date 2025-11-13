package br.puc.battledolls.model;

public class RobotStatsBuilder {
    private int hp = 100, atk = 10, def = 5;
    private double crit = 0.05, evade = 0.05;

    public RobotStatsBuilder addHp(int v) { hp += v; return this; }
    public RobotStatsBuilder addAtk(int v) { atk += v; return this; }
    public RobotStatsBuilder addDef(int v) { def += v; return this; }
    public RobotStatsBuilder addCrit(double v) { crit += v; return this; }
    public RobotStatsBuilder addEvade(double v) { evade += v; return this; }

    public RobotStats build() { return new RobotStats(hp, atk, def, crit, evade); }
}
