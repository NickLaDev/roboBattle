package br.puc.robobattle.model;

/**
 * Define os efeitos da habilidade especial de um personagem.
 */
public record AbilityEffect(
        String name,
        String description,
        boolean offensive,
        double damageMultiplier,
        int flatDamageBonus,
        Integer guaranteedBleedTicks,
        Integer guaranteedBleedDamage,
        int extraBleedTicks,
        int extraBleedDamage,
        int selfHeal,
        boolean grantGuard,
        String activationMessage) {

    public AbilityEffect {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Ability name must not be blank");
        if (description == null)
            description = "";
        if (activationMessage == null)
            activationMessage = "";
        if (damageMultiplier <= 0 && offensive)
            throw new IllegalArgumentException("Damage multiplier must be positive for offensive abilities");
        if (!offensive && (flatDamageBonus != 0 || damageMultiplier != 1.0
                || guaranteedBleedTicks != null || guaranteedBleedDamage != null
                || extraBleedTicks != 0 || extraBleedDamage != 0))
            throw new IllegalArgumentException("Non-offensive abilities cannot define offensive modifiers");
        if (selfHeal < 0)
            throw new IllegalArgumentException("Heal cannot be negative");
        if (extraBleedTicks < 0 || extraBleedDamage < 0)
            throw new IllegalArgumentException("Extra bleed values cannot be negative");
        if (guaranteedBleedTicks != null && guaranteedBleedTicks < 0)
            throw new IllegalArgumentException("Guaranteed bleed ticks cannot be negative");
        if (guaranteedBleedDamage != null && guaranteedBleedDamage < 0)
            throw new IllegalArgumentException("Guaranteed bleed damage cannot be negative");
    }
}

