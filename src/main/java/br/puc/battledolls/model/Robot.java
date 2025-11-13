package br.puc.battledolls.model;

import br.puc.battledolls.items.Weapon;
import br.puc.battledolls.items.Module;
import br.puc.battledolls.items.Armor;

public class Robot {
    private final CharacterClass characterClass;
    private final RobotStats stats;
    private int hp;

    private boolean guarding = false;

    // Habilidade especial — número de cargas disponíveis
    private int specialCharges;

    // Sangramento
    private int bleedTicks = 0;
    private int bleedDamage = 0;

    public Robot(CharacterClass characterClass, Weapon w, Armor a, Module m) {
        this.characterClass = characterClass;
        RobotStatsBuilder b = new RobotStatsBuilder(); // base e incrementos
        if (characterClass != null) characterClass.applyBaseStats(b);
        if (w != null) w.apply(b);
        if (a != null) a.apply(b);
        if (m != null) m.apply(b);
        this.stats = b.build();
        this.hp = stats.maxHp;

        this.specialCharges = (m != null && m.type() == Module.Type.BATERIA) ? 2 : 1;
    }

    public RobotStats stats() { return stats; }
    public int getHp() { return hp; }
    public boolean isAlive() { return hp > 0; }
    public CharacterClass characterClass() { return characterClass; }

    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - Math.max(0, dmg));
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        hp = Math.min(stats.maxHp, hp + amount);
    }

    // Guarda
    public void setGuarding(boolean on) { this.guarding = on; }
    public boolean isGuarding() { return guarding; }
    public void clearGuard() { this.guarding = false; }

    // Especial
    public boolean isSpecialAvailable() { return specialCharges > 0; }
    public int specialCharges() { return specialCharges; }
    public boolean consumeSpecial() {
        if (specialCharges <= 0) return false;
        specialCharges--;
        return true;
    }

    // Sangramento
    public void applyBleed(int ticks, int damagePerTick) {
        // Acumula re-aplicação: renova com o maior entre os valores
        this.bleedTicks = Math.max(this.bleedTicks, ticks);
        this.bleedDamage = Math.max(this.bleedDamage, damagePerTick);
    }

    /** Aplica um tick de bleed, se houver. Retorna o dano aplicado. */
    public int tickBleed() {
        if (bleedTicks <= 0) return 0;
        takeDamage(bleedDamage);
        bleedTicks--;
        return bleedDamage;
    }

    public boolean isBleeding() { return bleedTicks > 0; }
}
