package br.puc.battledolls.combat;

public class DamageResult {
    public final int rolledAttack;
    public final int rolledDefense;
    public final boolean evaded;
    public final boolean critical;
    public final boolean usedSpecial;
    public final int finalDamage;
    public final boolean applyBleed; // sangramento

    public DamageResult(int rolledAttack, int rolledDefense, boolean evaded, boolean critical,
                        boolean usedSpecial, int finalDamage, boolean applyBleed) {
        this.rolledAttack = rolledAttack;
        this.rolledDefense = rolledDefense;
        this.evaded = evaded;
        this.critical = critical;
        this.usedSpecial = usedSpecial;
        this.finalDamage = finalDamage;
        this.applyBleed = applyBleed;
    }
}
