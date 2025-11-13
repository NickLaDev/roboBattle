package br.puc.battledolls.model;

public class Player {
    private final String name;
    private int credits;
    private Robot robot;

    public Player(String name, int credits) { this.name = name; this.credits = credits; }
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
}
