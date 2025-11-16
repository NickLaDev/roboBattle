package br.puc.battledolls.campaign;

import br.puc.battledolls.ai.CPURobotBuilder;
import br.puc.battledolls.items.Potion;
import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.Robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

        // Adiciona poções para a CPU baseado na fase
        equipCPUWithPotions(cpuPlayer, currentPhaseIndex);

        return cpuPlayer;
    }

    /**
     * Equipa a CPU com poções baseado na dificuldade da fase.
     */
    private void equipCPUWithPotions(Player cpuPlayer, int phaseIndex) {
        Random rng = new Random();

        // Aumenta quantidade e qualidade das poções conforme a fase avança
        int baseQuantity = 1 + phaseIndex; // Fase 0 = 1, Fase 1 = 2, etc.
        int maxLevel = Math.min(5, 2 + phaseIndex); // Fase 0 = nível 2, aumenta gradualmente

        // Adiciona poções de vida (sempre)
        int vidaCount = baseQuantity;
        int vidaLevel = Math.min(maxLevel, 1 + phaseIndex);
        for (int i = 0; i < vidaCount; i++) {
            Potion vidaPotion = new Potion(Potion.Type.VIDA, vidaLevel);
            cpuPlayer.addPotionDirectly(vidaPotion);
        }

        // Adiciona poções de barreira (50% de chance por fase)
        if (phaseIndex >= 1 && rng.nextBoolean()) {
            int barreraCount = 1 + (phaseIndex / 2);
            int barreraLevel = Math.min(maxLevel, phaseIndex);
            for (int i = 0; i < barreraCount; i++) {
                Potion barreraPotion = new Potion(Potion.Type.BARRERA, Math.max(1, barreraLevel));
                cpuPlayer.addPotionDirectly(barreraPotion);
            }
        }

        // Adiciona poções de energia (a partir da fase 2)
        if (phaseIndex >= 2) {
            int energiaCount = 1 + (phaseIndex / 3);
            int energiaLevel = Math.min(maxLevel, phaseIndex - 1);
            for (int i = 0; i < energiaCount; i++) {
                Potion energiaPotion = new Potion(Potion.Type.ENERGIA, Math.max(1, energiaLevel));
                cpuPlayer.addPotionDirectly(energiaPotion);
            }
        }

        // Adiciona poções de fúria (a partir da fase 3)
        if (phaseIndex >= 3) {
            int furiaCount = 1 + (phaseIndex / 4);
            int furiaLevel = Math.min(maxLevel, phaseIndex - 2);
            for (int i = 0; i < furiaCount; i++) {
                Potion furiaPotion = new Potion(Potion.Type.FURIA, Math.max(1, furiaLevel));
                cpuPlayer.addPotionDirectly(furiaPotion);
            }
        }
    }
}
