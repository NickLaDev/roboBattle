package br.puc.battledolls.ai;

import br.puc.battledolls.combat.Action;
import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.Robot;

import java.util.Random;

/**
 * Controlador de IA para o modo Player vs CPU.
 * Escolhe ações baseado em heurísticas simples:
 * - Se HP baixo, prioriza DEFEND
 * - Se SPECIAL disponível e HP do inimigo baixo, usa SPECIAL
 * - Caso contrário, ataca normalmente
 */
public class AIController {
    private final Random rng = new Random();
    
    // Limiar de HP baixo (percentual)
    private static final double LOW_HP_THRESHOLD = 0.35;
    // Chance de defender quando HP baixo
    private static final double DEFEND_WHEN_LOW_HP = 0.4;
    // Chance de usar SPECIAL quando disponível e inimigo com HP baixo
    private static final double SPECIAL_WHEN_ENEMY_LOW = 0.6;
    
    /**
     * Escolhe uma ação para a CPU baseado no estado atual da batalha.
     * @param cpuPlayer O jogador controlado pela CPU
     * @param enemyPlayer O jogador inimigo (humano)
     * @return A ação escolhida pela IA
     */
    public Action chooseAction(Player cpuPlayer, Player enemyPlayer) {
        Robot cpu = cpuPlayer.robot();
        Robot enemy = enemyPlayer.robot();
        
        double cpuHpPercent = (double) cpu.getHp() / cpu.stats().maxHp;
        double enemyHpPercent = (double) enemy.getHp() / enemy.stats().maxHp;
        
        // Se HP muito baixo, há chance de defender
        if (cpuHpPercent < LOW_HP_THRESHOLD && rng.nextDouble() < DEFEND_WHEN_LOW_HP) {
            return Action.DEFEND;
        }
        
        // Se SPECIAL disponível e inimigo com HP baixo, usar SPECIAL
        if (cpu.isSpecialAvailable() && enemyHpPercent < 0.5 && rng.nextDouble() < SPECIAL_WHEN_ENEMY_LOW) {
            return Action.SPECIAL;
        }
        
        // Se SPECIAL disponível e CPU com HP médio/alto, às vezes usar SPECIAL
        if (cpu.isSpecialAvailable() && cpuHpPercent > 0.4 && rng.nextDouble() < 0.3) {
            return Action.SPECIAL;
        }
        
        // Caso padrão: atacar
        return Action.ATTACK;
    }
}

