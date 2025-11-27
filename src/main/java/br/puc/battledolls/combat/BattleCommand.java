package br.puc.battledolls.combat;

import br.puc.battledolls.items.Potion;

/**
 * Representa um comando de batalha com a ação e, opcionalmente, a poção a ser usada
 * ou o multiplicador customizado para o especial (da timing bar).
 */
public record BattleCommand(Action action, Potion potion, Double specialMultiplier) {

    public BattleCommand {
        if (action == null) {
            throw new IllegalArgumentException("Action não pode ser nula.");
        }
        if (action == Action.USE_POTION && potion == null) {
            throw new IllegalArgumentException("Poção obrigatória para USE_POTION.");
        }
        if (action != Action.USE_POTION && potion != null) {
            throw new IllegalArgumentException("Poção só deve ser informada em USE_POTION.");
        }
    }

    /**
     * Cria um comando simples (ATTACK, DEFEND ou SPECIAL).
     */
    public static BattleCommand of(Action action) {
        return new BattleCommand(action, null, null);
    }
    
    /**
     * Cria um comando para SPECIAL com multiplicador customizado (da timing bar).
     */
    public static BattleCommand of(Action action, double specialMultiplier) {
        return new BattleCommand(action, null, specialMultiplier);
    }

    /**
     * Cria um comando para uso de poção.
     */
    public static BattleCommand usePotion(Potion potion) {
        return new BattleCommand(Action.USE_POTION, potion, null);
    }
}
