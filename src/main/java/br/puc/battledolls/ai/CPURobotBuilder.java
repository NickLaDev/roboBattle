package br.puc.battledolls.ai;

import br.puc.battledolls.items.Armor;
import br.puc.battledolls.items.Module;
import br.puc.battledolls.items.Weapon;
import br.puc.battledolls.model.CharacterClass;
import br.puc.battledolls.model.Robot;

import java.util.Random;

/**
 * Construtor automático de robôs para a CPU.
 * Gera uma combinação razoável de arma/armadura/módulo respeitando os créditos disponíveis.
 */
public class CPURobotBuilder {
    private final Random rng = new Random();
    
    /**
     * Gera um robô para a CPU com base nos créditos disponíveis.
     * Tenta criar uma build balanceada, priorizando sobrevivência e dano.
     * 
     * @param credits Créditos disponíveis para a CPU
     * @return Um robô montado automaticamente
     */
    public Robot buildRobot(int credits) {
        // Escolhe personagem aleatório
        CharacterClass[] classes = CharacterClass.values();
        CharacterClass chosenClass = classes[rng.nextInt(classes.length)];
        
        // Estratégia: distribuir créditos de forma balanceada
        // Prioriza armadura para sobrevivência, depois arma, depois módulo
        
        int weaponLvl = 0;
        int armorLvl = 0;
        int moduleLvl = 0;
        Module.Type moduleType = Module.Type.CPU; // padrão
        
        int remainingCredits = credits;
        
        // Primeiro, tenta comprar armadura (sobrevivência)
        for (int lvl = 5; lvl >= 1 && remainingCredits > 0; lvl--) {
            int cost = new Armor("CPU Armor", lvl).getCost();
            if (cost <= remainingCredits) {
                armorLvl = lvl;
                remainingCredits -= cost;
                break;
            }
        }
        
        // Depois, tenta comprar arma (dano)
        for (int lvl = 5; lvl >= 1 && remainingCredits > 0; lvl--) {
            int cost = new Weapon("CPU Weapon", lvl).getCost();
            if (cost <= remainingCredits) {
                weaponLvl = lvl;
                remainingCredits -= cost;
                break;
            }
        }
        
        // Por último, módulo (se sobrar créditos)
        if (remainingCredits > 0) {
            // Escolhe tipo de módulo aleatório
            Module.Type[] types = Module.Type.values();
            moduleType = types[rng.nextInt(types.length)];
            
            for (int lvl = 5; lvl >= 1 && remainingCredits > 0; lvl--) {
                int cost = new Module(moduleType, lvl).getCost();
                if (cost <= remainingCredits) {
                    moduleLvl = lvl;
                    break;
                }
            }
        }
        
        // Cria os itens
        Weapon w = (weaponLvl > 0) ? new Weapon("CPU Weapon", weaponLvl) : null;
        Armor a = (armorLvl > 0) ? new Armor("CPU Armor", armorLvl) : null;
        Module m = (moduleLvl > 0) ? new Module(moduleType, moduleLvl) : null;
        
        return new Robot(chosenClass, w, a, m);
    }
    
    /**
     * Calcula o custo total de uma build.
     */
    public int calculateCost(int weaponLvl, int armorLvl, int moduleLvl) {
        int cost = 0;
        if (weaponLvl > 0) cost += new Weapon("", weaponLvl).getCost();
        if (armorLvl > 0) cost += new Armor("", armorLvl).getCost();
        if (moduleLvl > 0) cost += new Module(Module.Type.CPU, moduleLvl).getCost();
        return cost;
    }
}

