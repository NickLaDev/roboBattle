package br.puc.robobattle.model;

import br.puc.robobattle.items.Module;
import br.puc.robobattle.items.Weapon;
import br.puc.robobattle.items.Armor;

public class Robot {
    private final RobotStats stats;
    private int hp;

    private boolean guarding = false;

    // Especial (BATERIA)
    private boolean specialReady;

    // Sangramento
    private int bleedTicks = 0;
    private int bleedDamage = 0;

    public Robot(Weapon w, Armor a, Module m) {
        RobotStatsBuilder b = new RobotStatsBuilder(); // base e incrementos
        if (w != null) w.apply(b);
        if (a != null) a.apply(b);
        if (m != null) m.apply(b);
        this.stats = b.build();
        this.hp = stats.maxHp;

        this.specialReady = (m != null && m.type() == Module.Type.BATERIA);
    }

    public RobotStats stats() { return stats; }
    public int getHp() { return hp; }
    public boolean isAlive() { return hp > 0; }

    public void takeDamage(int dmg) {
        hp = Math.max(0, hp - Math.max(0, dmg));
    }

    // Guarda
    public void setGuarding(boolean on) { this.guarding = on; }
    public boolean isGuarding() { return guarding; }
    public void clearGuard() { this.guarding = false; }

    // Especial
    public boolean isSpecialAvailable() { return specialReady; }
    public boolean consumeSpecial() {
        if (!specialReady) return false;
        specialReady = false;
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
