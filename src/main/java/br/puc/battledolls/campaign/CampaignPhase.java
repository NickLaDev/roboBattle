package br.puc.battledolls.campaign;

import br.puc.battledolls.model.Robot;

/**
 * Representa uma fase da campanha PvC.
 */
public class CampaignPhase {
    private final int phaseNumber;
    private final CPUCharacter cpuCharacter;
    private final Robot cpuRobot;
    private boolean completed = false;
    private boolean playerWon = false;
    
    public CampaignPhase(int phaseNumber, CPUCharacter cpuCharacter, Robot cpuRobot) {
        this.phaseNumber = phaseNumber;
        this.cpuCharacter = cpuCharacter;
        this.cpuRobot = cpuRobot;
    }
    
    public int phaseNumber() { return phaseNumber; }
    public CPUCharacter cpuCharacter() { return cpuCharacter; }
    public Robot cpuRobot() { return cpuRobot; }
    public boolean isCompleted() { return completed; }
    public boolean playerWon() { return playerWon; }
    
    public void markCompleted(boolean playerWon) {
        this.completed = true;
        this.playerWon = playerWon;
    }
    
    /**
     * Retorna os créditos de recompensa se o jogador venceu.
     */
    public int getReward() {
        return playerWon ? cpuCharacter.rewardCredits() : 0;
    }
}

