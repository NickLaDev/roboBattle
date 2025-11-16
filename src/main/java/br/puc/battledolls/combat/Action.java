package br.puc.battledolls.combat;

import br.puc.battledolls.items.Potion;

public enum Action {
    ATTACK, DEFEND, SPECIAL, USE_POTION;
    
    private Potion potion;
    
    /**
     * Cria uma ação de usar poção.
     */
    public static Action usePotion(Potion potion) {
        Action action = USE_POTION;
        action.potion = potion;
        return action;
    }
    
    /**
     * Retorna a poção associada (se for USE_POTION).
     */
    public Potion getPotion() {
        return potion;
    }
}
