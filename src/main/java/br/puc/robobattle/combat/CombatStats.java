package br.puc.robobattle.combat;

public class CombatStats {
    public int rounds = 0;

    public static class PerPlayer {
        public int totalDamage = 0;
        public int maxHit = 0;
        public int crits = 0;
        public int evades = 0;
        public int specials = 0;
        public int bleedsApplied = 0;
        public int bleedTicksDealt = 0;
    }

    public final PerPlayer p1 = new PerPlayer();
    public final PerPlayer p2 = new PerPlayer();
}
