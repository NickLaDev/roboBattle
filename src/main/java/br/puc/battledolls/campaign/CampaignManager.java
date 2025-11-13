package br.puc.battledolls.campaign;

import br.puc.battledolls.ai.CPURobotBuilder;
import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.Robot;

import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia a campanha PvC, incluindo fases, progresso e recompensas.
 */
public class CampaignManager {

    // Lista de personagens da CPU na ordem das fases
    private static final CPUCharacter[] CPU_CHARACTERS = {
            // FASE 1: Converted Vampire
            // Configure aqui quantos frames cada animação tem para este personagem
            new CPUCharacter(
                    "Converted Vampire",
                    "/assets/pc-characters/Converted_Vampire",
                    800, // créditos base para o robô
                    200, // recompensa por vitória
                    new CPUCharacter.SpriteFrameConfig(
                            5, // Idle frames
                            5, // Attack_1 frames
                            3, // Attack_2 frames
                            4, // Attack_3 frames
                            8, // Run frames
                            2, // Protect frames
                            1, // Hurt frames
                            8 // Dead frames
                    )),

            // FASE 2: Vampire Girl
            // Configure aqui quantos frames cada animação tem para este personagem
            new CPUCharacter(
                    "Vampire Girl",
                    "/assets/pc-characters/Vampire_Girl",
                    1000,
                    300,
                    new CPUCharacter.SpriteFrameConfig(
                            4, // Idle frames
                            4, // Attack_1 frames
                            4, // Attack_2 frames
                            4, // Attack_3 frames
                            8, // Run frames
                            2, // Protect frames (não tem Protect.png, usa fallback)
                            3, // Hurt frames
                            3 // Dead frames
                    )),

            // FASE 3: Samurai Commander
            // Configure aqui quantos frames cada animação tem para este personagem
            new CPUCharacter(
                    "Samurai Commander",
                    "/assets/pc-characters/Samurai_Commander",
                    1200,
                    400,
                    new CPUCharacter.SpriteFrameConfig(
                            4, // Idle frames
                            4, // Attack_1 frames
                            4, // Attack_2 frames
                            4, // Attack_3 frames
                            8, // Run frames
                            2, // Protect frames
                            3, // Hurt frames
                            3 // Dead frames
                    )),

            // FASE 4: Countess Vampire
            // Configure aqui quantos frames cada animação tem para este personagem
            new CPUCharacter(
                    "Countess Vampire",
                    "/assets/pc-characters/Countess_Vampire",
                    1500,
                    500,
                    new CPUCharacter.SpriteFrameConfig(
                            4, // Idle frames
                            4, // Attack_1 frames
                            4, // Attack_2 frames
                            4, // Attack_3 frames
                            8, // Run frames
                            2, // Protect frames (não tem Protect.png, usa fallback)
                            3, // Hurt frames
                            3 // Dead frames
                    ))
    };

    private final Player player;
    private final List<CampaignPhase> phases;
    private int currentPhaseIndex = 0;
    private int totalCreditsEarned = 0;

    public CampaignManager(Player player) {
        this.player = player;
        this.phases = new ArrayList<>();
        initializePhases();
    }

    /**
     * Inicializa todas as fases da campanha.
     */
    private void initializePhases() {
        CPURobotBuilder builder = new CPURobotBuilder();
        for (int i = 0; i < CPU_CHARACTERS.length; i++) {
            CPUCharacter cpuChar = CPU_CHARACTERS[i];
            // Aumenta a dificuldade progressivamente
            int credits = cpuChar.baseCredits() + (i * 100);
            Robot cpuRobot = builder.buildRobot(credits);
            phases.add(new CampaignPhase(i + 1, cpuChar, cpuRobot));
        }
    }

    /**
     * Retorna a fase atual.
     */
    public CampaignPhase getCurrentPhase() {
        if (currentPhaseIndex >= phases.size()) {
            return null; // Campanha completa
        }
        return phases.get(currentPhaseIndex);
    }

    /**
     * Avança para a próxima fase após vitória.
     */
    public void advanceToNextPhase(boolean playerWon) {
        CampaignPhase current = getCurrentPhase();
        if (current != null) {
            current.markCompleted(playerWon);
            if (playerWon) {
                int reward = current.getReward();
                totalCreditsEarned += reward;
                // Adiciona créditos ao jogador
                player.addCredits(reward);
                currentPhaseIndex++;
            }
        }
    }

    /**
     * Verifica se a campanha foi completada.
     */
    public boolean isCampaignComplete() {
        return currentPhaseIndex >= phases.size();
    }

    /**
     * Verifica se o jogador perdeu (todas as fases completadas mas não venceu
     * todas).
     */
    public boolean hasPlayerLost() {
        CampaignPhase current = getCurrentPhase();
        return current != null && current.isCompleted() && !current.playerWon();
    }

    /**
     * Retorna o número total de fases.
     */
    public int getTotalPhases() {
        return phases.size();
    }

    /**
     * Retorna o índice da fase atual (1-based).
     */
    public int getCurrentPhaseNumber() {
        return currentPhaseIndex + 1;
    }

    /**
     * Retorna o total de créditos ganhos.
     */
    public int getTotalCreditsEarned() {
        return totalCreditsEarned;
    }

    /**
     * Cria um Player para a CPU da fase atual.
     */
    public Player createCPUPlayer() {
        CampaignPhase phase = getCurrentPhase();
        if (phase == null)
            return null;

        Player cpuPlayer = new Player(phase.cpuCharacter().name(), 0);
        cpuPlayer.buyAndEquip(phase.cpuRobot(), 0);
        return cpuPlayer;
    }
}
