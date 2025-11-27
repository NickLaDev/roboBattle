package br.puc.battledolls.combat;

import br.puc.battledolls.model.Robot;
import br.puc.battledolls.model.RobotStats;

import java.util.Random;

public class DamageCalculator {
    private final Random rng = new Random();

    public DamageResult compute(Robot attacker, Robot defender, boolean useSpecial) {
        return compute(attacker, defender, useSpecial, 1.5); // Multiplicador padrão
    }
    
    public DamageResult compute(Robot attacker, Robot defender, boolean useSpecial, double specialMultiplier) {
        RobotStats a = attacker.stats(), d = defender.stats();

        int rolledAttack = (int) Math.round(a.atk * (0.95 + 0.10 * rng.nextDouble())); // ±5%
        int rolledDefense = (int) Math.round(d.def * (0.99 + 0.02 * rng.nextDouble())); // ±1%

        // Evade
        if (rng.nextDouble() < d.evade) {
            return new DamageResult(rolledAttack, rolledDefense, true, false, useSpecial, 0, false);
        }

        double raw = rolledAttack - rolledDefense;

        // Fúria (multiplicador de ataque)
        double furyMultiplier = attacker.getFuryMultiplier();
        if (furyMultiplier > 1.0) {
            raw *= furyMultiplier;
        }

        // Crit
        boolean critical = rng.nextDouble() < a.crit;
        if (critical) raw *= 1.5;

        // Special (Bateria) - usa multiplicador customizado da timing bar
        if (useSpecial) {
            raw *= specialMultiplier;
        }

        int finalDamage = (int) Math.max(1, Math.round(raw));

        // Regra de sangramento: se crítico, aplica bleed (2 ticks de 3 dmg)
        boolean applyBleed = critical;

        return new DamageResult(rolledAttack, rolledDefense, false, critical, useSpecial, finalDamage, applyBleed);
    }
}
