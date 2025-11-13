package br.puc.battledolls.model;

public final class RobotStats {
    public final int maxHp;
    public final int atk;
    public final int def;
    public final double crit;   // 0.05 = 5%
    public final double evade;  // 0.05 = 5%

    RobotStats(int maxHp, int atk, int def, double crit, double evade) {
        this.maxHp = maxHp; this.atk = atk; this.def = def; this.crit = crit; this.evade = evade;
    }

    public static RobotStats base() {
        return new RobotStats(100, 10, 5, 0.05, 0.05);
    }
}
