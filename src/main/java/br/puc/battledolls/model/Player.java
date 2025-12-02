package br.puc.battledolls.model;

import br.puc.battledolls.items.Potion;
import java.util.*;

public class Player {
    private final String name;
    private int credits;
    private Robot robot;
    private final Map<Potion, Integer> potionInventory; // Poção -> quantidade

    public Player(String name, int credits) { 
        this.name = name; 
        this.credits = credits;
        this.potionInventory = new HashMap<>();
    }
    
    public String name() { return name; }
    public int credits() { return credits; }
    public Robot robot() { return robot; }

    public boolean buyAndEquip(Robot r, int totalCost) {
        if (totalCost <= credits) { credits -= totalCost; this.robot = r; return true; }
        return false;
    }
    
    /**
     * Adiciona créditos ao jogador (usado em recompensas de campanha).
     */
    public void addCredits(int amount) {
        if (amount > 0) {
            credits += amount;
        }
    }
    
    /**
     * Compra uma poção e adiciona ao inventário.
     */
    public boolean buyPotion(Potion potion) {
        int cost = potion.getCost();
        if (cost <= credits) {
            credits -= cost;
            potionInventory.put(potion, potionInventory.getOrDefault(potion, 0) + 1);
            return true;
        }
        return false;
    }
    
    /**
     * Remove uma poção do inventário e reembolsa o custo ao jogador.
     */
    public boolean refundPotion(Potion potion) {
        int count = potionInventory.getOrDefault(potion, 0);
        if (count > 0) {
            potionInventory.put(potion, count - 1);
            if (count - 1 == 0) {
                potionInventory.remove(potion);
            }
            credits += potion.getCost();
            return true;
        }
        return false;
    }

    /**
     * Adiciona uma poção diretamente ao inventário sem custo (usado para CPU).
     */
    public void addPotionDirectly(Potion potion) {
        potionInventory.put(potion, potionInventory.getOrDefault(potion, 0) + 1);
    }
    
    /**
     * Usa uma poção do inventário (remove 1 unidade).
     */
    public boolean usePotion(Potion potion) {
        int count = potionInventory.getOrDefault(potion, 0);
        if (count > 0) {
            potionInventory.put(potion, count - 1);
            if (count - 1 == 0) {
                potionInventory.remove(potion);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Retorna a quantidade de uma poção no inventário.
     */
    public int getPotionCount(Potion potion) {
        return potionInventory.getOrDefault(potion, 0);
    }
    
    /**
     * Retorna todas as poções disponíveis no inventário (com quantidade > 0).
     */
    public List<Potion> getAvailablePotions() {
        List<Potion> available = new ArrayList<>();
        for (Map.Entry<Potion, Integer> entry : potionInventory.entrySet()) {
            if (entry.getValue() > 0) {
                available.add(entry.getKey());
            }
        }
        return available;
    }
    
    /**
     * Retorna o inventário completo de poções.
     */
    public Map<Potion, Integer> getPotionInventory() {
        return new HashMap<>(potionInventory);
    }
}
