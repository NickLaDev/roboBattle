package br.puc.battledolls.ai;

import br.puc.battledolls.combat.Action;
import br.puc.battledolls.combat.BattleCommand;
import br.puc.battledolls.items.Potion;
import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.Robot;

import java.util.List;
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
    // Chance de usar poção de vida quando HP muito baixo
    private static final double POTION_VIDA_WHEN_CRITICAL = 0.7;
    // Chance de usar poção de barreira quando HP baixo
    private static final double POTION_BARRERA_WHEN_LOW = 0.4;
    // Chance de usar poção de energia quando especial não está disponível
    private static final double POTION_ENERGIA_WHEN_NO_SPECIAL = 0.3;
    // Chance de usar poção de fúria quando HP médio/alto
    private static final double POTION_FURIA_WHEN_HEALTHY = 0.25;
    
    /**
     * Escolhe uma ação para a CPU baseado no estado atual da batalha.
     * @param cpuPlayer O jogador controlado pela CPU
     * @param enemyPlayer O jogador inimigo (humano)
     * @return A ação escolhida pela IA
     */
    public BattleCommand chooseAction(Player cpuPlayer, Player enemyPlayer) {
        Robot cpu = cpuPlayer.robot();
        Robot enemy = enemyPlayer.robot();
        
        double cpuHpPercent = (double) cpu.getHp() / cpu.stats().maxHp;
        double enemyHpPercent = (double) enemy.getHp() / enemy.stats().maxHp;
        
        // Verifica poções disponíveis
        List<Potion> availablePotions = cpuPlayer.getAvailablePotions();
        
        // Se HP crítico (menos de 25%), prioriza poção de vida
        if (cpuHpPercent < 0.25 && rng.nextDouble() < POTION_VIDA_WHEN_CRITICAL) {
            Potion vidaPotion = findBestPotion(availablePotions, Potion.Type.VIDA);
            if (vidaPotion != null) {
                return BattleCommand.usePotion(vidaPotion);
            }
        }
        
        // Se HP baixo (menos de 40%) e não tem escudo, considera poção de barreira
        if (cpuHpPercent < 0.4 && cpu.getShield() == 0 && rng.nextDouble() < POTION_BARRERA_WHEN_LOW) {
            Potion barreiraPotion = findBestPotion(availablePotions, Potion.Type.BARRERA);
            if (barreiraPotion != null) {
                return BattleCommand.usePotion(barreiraPotion);
            }
        }
        
        // Se especial não está disponível e carga está baixa, considera poção de energia
        if (!cpu.isSpecialAvailable() && cpu.getSpecialCharge() < 50 && 
            rng.nextDouble() < POTION_ENERGIA_WHEN_NO_SPECIAL) {
            Potion energiaPotion = findBestPotion(availablePotions, Potion.Type.ENERGIA);
            if (energiaPotion != null) {
                return BattleCommand.usePotion(energiaPotion);
            }
        }
        
        // Se HP médio/alto e não tem fúria, considera poção de fúria
        if (cpuHpPercent > 0.5 && !cpu.hasFury() && rng.nextDouble() < POTION_FURIA_WHEN_HEALTHY) {
            Potion furiaPotion = findBestPotion(availablePotions, Potion.Type.FURIA);
            if (furiaPotion != null) {
                return BattleCommand.usePotion(furiaPotion);
            }
        }
        
        // Se HP muito baixo, há chance de defender
        if (cpuHpPercent < LOW_HP_THRESHOLD && rng.nextDouble() < DEFEND_WHEN_LOW_HP) {
            return BattleCommand.of(Action.DEFEND);
        }
        
        // Se SPECIAL disponível e inimigo com HP baixo, usar SPECIAL
        if (cpu.isSpecialAvailable() && enemyHpPercent < 0.5 && rng.nextDouble() < SPECIAL_WHEN_ENEMY_LOW) {
            return BattleCommand.of(Action.SPECIAL);
        }
        
        // Se SPECIAL disponível e CPU com HP médio/alto, às vezes usar SPECIAL
        if (cpu.isSpecialAvailable() && cpuHpPercent > 0.4 && rng.nextDouble() < 0.3) {
            return BattleCommand.of(Action.SPECIAL);
        }
        
        // Caso padrão: atacar
        return BattleCommand.of(Action.ATTACK);
    }
    
    /**
     * Encontra a melhor poção de um tipo específico (maior nível disponível).
     */
    private Potion findBestPotion(List<Potion> potions, Potion.Type type) {
        Potion best = null;
        int bestLevel = 0;
        for (Potion potion : potions) {
            if (potion.type() == type && potion.level() > bestLevel) {
                best = potion;
                bestLevel = potion.level();
            }
        }
        return best;
    }
}
