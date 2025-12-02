package br.puc.battledolls.model;

import br.puc.battledolls.items.Armor;
import br.puc.battledolls.items.Module;
import br.puc.battledolls.items.Weapon;

public class Robot {
    private final CharacterClass characterClass;
    private final RobotStats stats;
    private final Weapon weapon;
    private final Armor armor;
    private final Module module;
    private int hp;

    private boolean guarding = false;

    // Habilidade especial — número de cargas disponíveis (sistema antigo)
    private int specialCharges;

    // Sistema de carga de especial
    private static final int MAX_SPECIAL_CHARGE = 100;
    private int specialCharge = 0; // 0-100
    private int chargePerTurn = 20; // carga base por turno
    private int chargeOnDefend = 40; // carga quando defende (2x)

    // Sangramento
    private int bleedTicks = 0;
    private int bleedDamage = 0;
    private boolean poisoned = false;
    private String bleedLabel = null;
    
    // Escudo/Barreira (absorve dano antes do HP)
    private int shield = 0;
    
    // Fúria (multiplicador de ataque temporário)
    private double furyMultiplier = 1.0;
    private int furyTurns = 0;

    public Robot(CharacterClass characterClass, Weapon w, Armor a, Module m) {
        this.characterClass = characterClass;
        this.weapon = w;
        this.armor = a;
        this.module = m;
        RobotStatsBuilder b = new RobotStatsBuilder(); // base e incrementos
        if (characterClass != null) characterClass.applyBaseStats(b);
        if (w != null) w.apply(b);
        if (a != null) a.apply(b);
        if (m != null) m.apply(b);
        this.stats = b.build();
        this.hp = stats.maxHp;

        this.specialCharges = (m != null && m.type() == Module.Type.BATERIA) ? 2 : 1;
    }

    public Weapon weapon() {
        return weapon;
    }

    public Armor armor() {
        return armor;
    }

    public Module module() {
        return module;
    }

    public RobotStats stats() { return stats; }
    public int getHp() { return hp; }
    public boolean isAlive() { return hp > 0; }
    public CharacterClass characterClass() { return characterClass; }

    public void takeDamage(int dmg) {
        if (dmg <= 0) return;
        
        // Escudo absorve dano primeiro
        if (shield > 0) {
            int absorbed = Math.min(shield, dmg);
            shield -= absorbed;
            dmg -= absorbed;
        }
        
        // Dano restante vai para o HP
        if (dmg > 0) {
            hp = Math.max(0, hp - dmg);
        }
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        hp = Math.min(stats.maxHp, hp + amount);
    }

    // Guarda
    public void setGuarding(boolean on) { this.guarding = on; }
    public boolean isGuarding() { return guarding; }
    public void clearGuard() { this.guarding = false; }

    // Especial (sistema antigo - mantido por compatibilidade)
    public boolean isSpecialAvailable() { return specialCharge >= MAX_SPECIAL_CHARGE; }
    public int specialCharges() { return specialCharges; }
    public boolean consumeSpecial() {
        if (specialCharge < MAX_SPECIAL_CHARGE) return false;
        specialCharge = 0; // reseta a carga ao usar
        return true;
    }

    // Sistema de carga de especial
    public int getSpecialCharge() { return specialCharge; }
    public int getMaxSpecialCharge() { return MAX_SPECIAL_CHARGE; }
    public double getSpecialChargePercent() {
        return (double) specialCharge / MAX_SPECIAL_CHARGE;
    }

    /**
     * Adiciona carga ao especial. Chamado ao final de cada turno.
     * @param defending se o robô está defendendo neste turno (carga 2x)
     */
    public void addSpecialCharge(boolean defending) {
        if (specialCharge >= MAX_SPECIAL_CHARGE) return;
        int charge = defending ? chargeOnDefend : chargePerTurn;
        specialCharge = Math.min(MAX_SPECIAL_CHARGE, specialCharge + charge);
    }
    
    /**
     * Adiciona uma quantidade específica de carga ao especial (usado por poções).
     */
    public void addSpecialChargeAmount(int amount) {
        if (amount > 0) {
            specialCharge = Math.min(MAX_SPECIAL_CHARGE, specialCharge + amount);
        }
    }

    /**
     * Reseta a carga do especial para 0.
     */
    public void resetSpecialCharge() {
        specialCharge = 0;
    }

    // Sangramento
    public void applyBleed(int ticks, int damagePerTick) {
        applyBleed(ticks, damagePerTick, false);
    }

    public void applyBleed(int ticks, int damagePerTick, boolean asPoison) {
        if (asPoison) {
            // Veneno do Shinobi: aplica contagem exata, sem acumular
            this.bleedTicks = Math.max(0, ticks);
            this.bleedDamage = Math.max(0, damagePerTick);
            this.poisoned = true;
            this.bleedLabel = "ENVENENAMENTO";
            return;
        }

        // Acumula re-aplicação: renova com o maior entre os valores
        this.bleedTicks = Math.max(this.bleedTicks, ticks);
        this.bleedDamage = Math.max(this.bleedDamage, damagePerTick);
        boolean hadPoison = this.poisoned && this.bleedTicks > 0;
        if (asPoison) {
            this.poisoned = true;
            this.bleedLabel = "ENVENENAMENTO";
        } else {
            // Se já estava envenenado, mantém o rótulo; caso contrário vira sangramento
            this.poisoned = hadPoison;
            this.bleedLabel = hadPoison ? "ENVENENAMENTO" : "SANGRAMENTO";
        }
    }

    /** Aplica um tick de bleed, se houver. Retorna o dano aplicado. */
    public int tickBleed() {
        if (bleedTicks <= 0) {
            poisoned = false;
            bleedLabel = null;
            return 0;
        }
        takeDamage(bleedDamage);
        bleedTicks--;
        if (bleedTicks <= 0) {
            poisoned = false;
            bleedLabel = null;
        }
        return bleedDamage;
    }

    public boolean isBleeding() { return bleedTicks > 0; }
    public boolean isPoisoned() { return bleedTicks > 0 && poisoned; }
    public String getBleedLabel() {
        if (!isBleeding()) return null;
        return (bleedLabel == null || bleedLabel.isBlank()) ? "SANGRAMENTO" : bleedLabel;
    }
    
    // Escudo/Barreira
    public int getShield() { return shield; }
    public void addShield(int amount) {
        if (amount > 0) {
            shield += amount;
        }
    }
    
    // Fúria
    public double getFuryMultiplier() { return furyMultiplier; }
    public void applyFury(double multiplier, int turns) {
        if (multiplier > 1.0 && turns > 0) {
            furyMultiplier = multiplier;
            furyTurns = Math.max(furyTurns, turns); // Mantém o maior se já tiver fúria
        }
    }
    
    /**
     * Atualiza efeitos temporários (fúria). Chamado no início de cada turno.
     */
    public void tickTemporaryEffects() {
        if (furyTurns > 0) {
            furyTurns--;
            if (furyTurns <= 0) {
                furyMultiplier = 1.0;
            }
        }
    }
    
    public boolean hasFury() { return furyTurns > 0; }
}
