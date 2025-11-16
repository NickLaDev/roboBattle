package br.puc.battledolls.items;

/**
 * Representa uma poção que pode ser usada durante a batalha.
 */
public class Potion {
    public enum Type {
        VIDA("Vida", "/assets/items/pocoes/pvida"),
        BARRERA("Barreira", "/assets/items/pocoes/pbarreira"),
        ENERGIA("Energia", "/assets/items/pocoes/penergia"),
        FURIA("Fúria", "/assets/items/pocoes/pfuria");
        
        private final String displayName;
        private final String spritePath;
        
        Type(String displayName, String spritePath) {
            this.displayName = displayName;
            this.spritePath = spritePath;
        }
        
        public String displayName() { return displayName; }
        public String spritePath() { return spritePath; }
    }
    
    private final Type type;
    private final int level; // 1-5
    
    public Potion(Type type, int level) {
        if (level < 1 || level > 5) {
            throw new IllegalArgumentException("Nível da poção deve estar entre 1 e 5");
        }
        this.type = type;
        this.level = level;
    }
    
    public Type type() { return type; }
    public int level() { return level; }
    
    /**
     * Retorna o caminho do sprite da poção.
     */
    public String getSpritePath() {
        return type.spritePath() + level + ".png";
    }
    
    /**
     * Retorna o custo da poção baseado no tipo e nível.
     */
    public int getCost() {
        // Custo base: tipo * nível^2
        int baseCost = switch (type) {
            case VIDA -> 50;
            case BARRERA -> 60;
            case ENERGIA -> 40;
            case FURIA -> 70;
        };
        return baseCost * level * level;
    }
    
    /**
     * Retorna o nome de exibição da poção.
     */
    public String getDisplayName() {
        return type.displayName() + " Nv." + level;
    }
    
    /**
     * Retorna a quantidade de cura da poção de vida.
     */
    public int getHealAmount() {
        if (type != Type.VIDA) return 0;
        return 20 + (level * 15); // 35, 50, 65, 80, 95
    }
    
    /**
     * Retorna a quantidade de escudo da poção de barreira.
     */
    public int getShieldAmount() {
        if (type != Type.BARRERA) return 0;
        return 15 + (level * 10); // 25, 35, 45, 55, 65
    }
    
    /**
     * Retorna a quantidade de carga especial da poção de energia.
     */
    public int getEnergyAmount() {
        if (type != Type.ENERGIA) return 0;
        return 20 + (level * 15); // 35, 50, 65, 80, 95
    }
    
    /**
     * Retorna o multiplicador de ataque da poção de fúria.
     */
    public double getFuryMultiplier() {
        if (type != Type.FURIA) return 1.0;
        return 1.0 + (level * 0.15); // 1.15, 1.30, 1.45, 1.60, 1.75
    }
    
    /**
     * Retorna a duração em turnos da poção de fúria.
     */
    public int getFuryDuration() {
        if (type != Type.FURIA) return 0;
        return 2 + level; // 3, 4, 5, 6, 7 turnos
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Potion potion = (Potion) o;
        return level == potion.level && type == potion.type;
    }
    
    @Override
    public int hashCode() {
        return type.hashCode() * 31 + level;
    }
}

