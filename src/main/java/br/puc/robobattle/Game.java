package br.puc.robobattle;

import br.puc.robobattle.combat.BattleEngine;
import br.puc.robobattle.items.Module;
import br.puc.robobattle.items.Weapon;
import br.puc.robobattle.items.Armor;
import br.puc.robobattle.model.Player;
import br.puc.robobattle.model.CharacterClass;
import br.puc.robobattle.model.AbilityEffect;
import br.puc.robobattle.model.Robot;

import java.util.Scanner;

public class Game {

    // DTO simples para compra
    public static class Purchase {
        public final Robot robot;
        public final int totalCost;
        public Purchase(Robot robot, int totalCost) {
            this.robot = robot;
            this.totalCost = totalCost;
        }
    }

    // ======== NOVO: custos lineares por nível (0..5) ========
    private static int costLinear(int lvl) {
        if (lvl < 0 || lvl > 5) throw new IllegalArgumentException("Nível inválido");
        return 100 * lvl; // 0,100,200,300,400,500
    }
    private static int costWeapon(int lvl) { return costLinear(lvl); } // sobrescreve custo da loja
    private static int costArmor (int lvl) { return costLinear(lvl); } // sobrescreve custo da loja
    private static int costModule(int lvl) { return costLinear(lvl); } // sobrescreve custo da loja
    // =========================================================

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.print("Nome do Jogador 1: ");
        String n1 = in.nextLine().trim();
        System.out.print("Nome do Jogador 2: ");
        String n2 = in.nextLine().trim();

        // ======== NOVO: créditos iniciais = 100 ========
        Player a = new Player(n1, 1000);
        Player b = new Player(n2, 1000);
        // =================================================

        // Compra segura do Player A
        while (true) {
            Purchase p = buildRobotViaShop(in, a);
            if (p != null && a.buyAndEquip(p.robot, p.totalCost)) break;
            System.out.println("Compra inválida. Tente novamente.\n");
        }

        // Compra segura do Player B
        while (true) {
            Purchase p = buildRobotViaShop(in, b);
            if (p != null && b.buyAndEquip(p.robot, p.totalCost)) break;
            System.out.println("Compra inválida. Tente novamente.\n");
        }

        // Luta (usa DamageResult/bleed/esquiva/crítico já implementados no seu motor)
        new BattleEngine(in).fight(a, b);
    }

    private static int askInt(Scanner in, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v < min || v > max) throw new NumberFormatException();
                return v;
            } catch (NumberFormatException e) {
                System.out.println("Valor inválido.");
            }
        }
    }

    private static Module.Type askModuleType(Scanner in) {
        while (true) {
            System.out.println("Tipo de Módulo: 1) MOBILIDADE  2) CPU  3) BATERIA");
            System.out.print("Escolha: ");
            String s = in.nextLine().trim();
            switch (s) {
                case "1": return Module.Type.MOBILIDADE;
                case "2": return Module.Type.CPU;
                case "3": return Module.Type.BATERIA;
                default: System.out.println("Opção inválida.");
            }
        }
    }

    private static CharacterClass askCharacterClass(Scanner in) {
        CharacterClass[] classes = CharacterClass.values();
        while (true) {
            System.out.println("\nPersonagens disponíveis:");
            for (int i = 0; i < classes.length; i++) {
                CharacterClass cc = classes[i];
                AbilityEffect ability = cc.ability();
                System.out.printf("%d) %s%n", i + 1, cc.displayName());
                System.out.printf("   %s%n", cc.description());
                System.out.printf("   Especial: %s — %s%n", ability.name(), ability.description());
            }
            System.out.print("Escolha o personagem (1-" + classes.length + "): ");
            String s = in.nextLine().trim();
            try {
                int idx = Integer.parseInt(s) - 1;
                if (idx >= 0 && idx < classes.length)
                    return classes[idx];
            } catch (NumberFormatException ignored) {}
            System.out.println("Opção inválida.");
        }
    }

    // ======== NOVO: imprime a tabela de preço por nível ANTES da escolha ========
    private static void printPriceTable(String label, int credits, int subtotal) {
        System.out.printf("%nTabela de preços — %s (Créditos atuais: %d)%n", label, credits);
        System.out.println("Nível | Preço | Saldo se comprar este nível");
        System.out.println("----- | ----- | ---------------------------");
        for (int lvl = 0; lvl <= 5; lvl++) {
            int cost = costLinear(lvl);
            int remaining = credits - (subtotal + cost);
            System.out.printf("%5d | %5d | %d%n", lvl, cost, remaining);
        }
    }
    // ============================================================================

    private static void printBudgetLine(String label, int lvl, int cost, int credits, int subtotal) {
        int remaining = credits - (subtotal + cost);
        System.out.printf("%s (nível %d): preço = %d | saldo restante se comprar = %d%n",
                label, lvl, cost, remaining);
    }

    // Retorna Purchase (robot + totalCost)
    private static Purchase buildRobotViaShop(Scanner in, Player p) {
        System.out.printf("%n=== Loja do(a) %s | Créditos: %d ===%n", p.name(), p.credits());

        int subtotal = 0;

        // ======== PERSONAGEM ========
        CharacterClass character = askCharacterClass(in);

        // ======== ARMA: mostrar preços antes de escolher ========
        printPriceTable("ARMA", p.credits(), subtotal);
        int wLvl = askInt(in, "Escolha o nível da ARMA (0-5): ", 0, 5);
        int weaponCost = costWeapon(wLvl);
        subtotal += weaponCost;
        printBudgetLine("ARMA", wLvl, weaponCost, p.credits(), 0);

        // ======== ARMADURA: mostrar preços antes de escolher ========
        printPriceTable("ARMADURA", p.credits(), subtotal);
        int aLvl = askInt(in, "Escolha o nível da ARMADURA (0-5): ", 0, 5);
        int armorCost = costArmor(aLvl);
        subtotal += armorCost;
        printBudgetLine("ARMADURA", aLvl, armorCost, p.credits(), weaponCost);

        // ======== MÓDULO: mostrar preços antes de escolher ========
        printPriceTable("MÓDULO", p.credits(), subtotal);
        int mLvl = askInt(in, "Escolha o nível do MÓDULO (0-5): ", 0, 5);
        Module.Type mType = askModuleType(in);
        int moduleCost = costModule(mLvl);
        subtotal += moduleCost;
        printBudgetLine("MÓDULO " + mType.name(), mLvl, moduleCost, p.credits(), weaponCost + armorCost);

        int total = weaponCost + armorCost + moduleCost;

        // Prévia de stats antes de confirmar
        Weapon w = new Weapon("Arma N" + wLvl, wLvl);
        Armor  a = new Armor("Armadura N" + aLvl, aLvl);
        Module m = new Module(mType, mLvl);
        Robot preview = new Robot(character, w, a, m);

        System.out.printf("%nResumo da compra (total = %d, saldo pós-compra = %d):%n", total, p.credits() - total);
        AbilityEffect ability = character.ability();
        System.out.printf("Personagem: %s — Especial: %s%n", character.displayName(), ability.name());
        System.out.printf("Descrição do especial: %s%n", ability.description());
        System.out.printf("HP=%d  ATK=%d  DEF=%d  CRIT=%.1f%%  EVADE=%.1f%%  Cargas de especial=%d%n",
                preview.stats().maxHp, preview.stats().atk, preview.stats().def,
                preview.stats().crit * 100.0, preview.stats().evade * 100.0,
                preview.specialCharges());

        if (total > p.credits()) {
            System.out.println("Créditos insuficientes para esta configuração.");
            return null;
        }

        System.out.print("Confirmar compra? (s/n): ");
        if (!in.nextLine().trim().equalsIgnoreCase("s")) {
            System.out.println("Compra cancelada.");
            return null;
        }

        return new Purchase(preview, total);
    }
}
