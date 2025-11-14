package br.puc.battledolls.validation;

import br.puc.battledolls.combat.*;
import br.puc.battledolls.model.*;
import br.puc.battledolls.items.*;

/**
 * Validador manual dos mecanismos de combate.
 * Executa testes para verificar ataque, defesa, crítico, esquiva e sangramento.
 */
public class CombatMechanicsValidator {

    private static int testsRun = 0;
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=".repeat(70));
        System.out.println("VALIDAÇÃO DOS MECANISMOS DE COMBATE");
        System.out.println("=".repeat(70));

        testBasicAttack();
        testDefenseReduction();
        testCriticalHit();
        testEvasion();
        testSpecialAttack();
        testGuardMechanism();
        testBleedingMechanism();
        testSpecialCharges();
        testMinimumDamage();
        testCriticalAndSpecialStack();

        System.out.println("\n" + "=".repeat(70));
        System.out.println("RESUMO DOS TESTES");
        System.out.println("=".repeat(70));
        System.out.printf("Total de testes executados: %d%n", testsRun);
        System.out.printf("Testes aprovados: %d ✓%n", testsPassed);
        System.out.printf("Testes falhados: %d ✗%n", testsFailed);

        if (testsFailed == 0) {
            System.out.println("\n✓ TODOS OS MECANISMOS DE COMBATE ESTÃO FUNCIONANDO CORRETAMENTE!");
        } else {
            System.out.println("\n✗ ALGUNS TESTES FALHARAM - REVISAR MECANISMOS");
        }
    }

    private static void testBasicAttack() {
        System.out.println("\n[TEST 1] Ataque Básico");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 50, 10, 0.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 10, 0.0, 0.0);

        DamageResult result = calc.compute(attacker, defender, false);

        System.out.printf("  Ataque: %d | Defesa: %d | Dano final: %d%n",
                attacker.stats().atk, defender.stats().def, result.finalDamage);

        assertTrue("Dano > 0", result.finalDamage > 0);
        assertTrue("Dano entre 35-45", result.finalDamage >= 35 && result.finalDamage <= 45);
        assertFalse("Não é crítico", result.critical);
        assertFalse("Não esquivou", result.evaded);
        assertFalse("Não usou especial", result.usedSpecial);
    }

    private static void testDefenseReduction() {
        System.out.println("\n[TEST 2] Redução por Defesa");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 30, 10, 0.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 25, 0.0, 0.0);

        DamageResult result = calc.compute(attacker, defender, false);

        System.out.printf("  Ataque: %d | Defesa alta: %d | Dano final: %d%n",
                attacker.stats().atk, defender.stats().def, result.finalDamage);

        assertTrue("Dano mínimo 1", result.finalDamage >= 1);
        assertTrue("Dano reduzido < 10", result.finalDamage < 10);
    }

    private static void testCriticalHit() {
        System.out.println("\n[TEST 3] Golpe Crítico");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 50, 10, 1.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 10, 0.0, 0.0);

        DamageResult result = calc.compute(attacker, defender, false);

        System.out.printf("  Chance crítico: 100%% | Crítico: %s | Dano: %d%n",
                result.critical ? "SIM" : "NÃO", result.finalDamage);
        System.out.printf("  Sangramento aplicado: %s%n", result.applyBleed ? "SIM" : "NÃO");

        assertTrue("É crítico", result.critical);
        assertTrue("Aplica sangramento", result.applyBleed);
        assertTrue("Dano crítico 50-70", result.finalDamage >= 50 && result.finalDamage <= 70);
    }

    private static void testEvasion() {
        System.out.println("\n[TEST 4] Esquiva");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 50, 10, 0.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 10, 0.0, 1.0);

        DamageResult result = calc.compute(attacker, defender, false);

        System.out.printf("  Chance esquiva: 100%% | Esquivou: %s | Dano: %d%n",
                result.evaded ? "SIM" : "NÃO", result.finalDamage);

        assertTrue("Esquivou", result.evaded);
        assertTrue("Dano é 0", result.finalDamage == 0);
        assertFalse("Não é crítico", result.critical);
        assertFalse("Não aplica sangramento", result.applyBleed);
    }

    private static void testSpecialAttack() {
        System.out.println("\n[TEST 5] Ataque Especial");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 50, 10, 0.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 10, 0.0, 0.0);

        DamageResult result = calc.compute(attacker, defender, true);

        System.out.printf("  Especial usado: %s | Dano: %d (esperado ~60)%n",
                result.usedSpecial ? "SIM" : "NÃO", result.finalDamage);

        assertTrue("Usou especial", result.usedSpecial);
        assertTrue("Dano 50-70", result.finalDamage >= 50 && result.finalDamage <= 70);
    }

    private static void testGuardMechanism() {
        System.out.println("\n[TEST 6] Mecanismo de Guarda");
        System.out.println("-".repeat(70));

        Robot robot = createRobotWithStats(100, 30, 20, 0.0, 0.0);

        System.out.printf("  Estado inicial: guardando = %s%n", robot.isGuarding());
        assertFalse("Não está guardando", robot.isGuarding());

        robot.setGuarding(true);
        System.out.printf("  Após ativar: guardando = %s%n", robot.isGuarding());
        assertTrue("Está guardando", robot.isGuarding());

        robot.clearGuard();
        System.out.printf("  Após limpar: guardando = %s%n", robot.isGuarding());
        assertFalse("Não está mais guardando", robot.isGuarding());
    }

    private static void testBleedingMechanism() {
        System.out.println("\n[TEST 7] Mecanismo de Sangramento");
        System.out.println("-".repeat(70));

        Robot robot = createRobotWithStats(100, 30, 20, 0.0, 0.0);

        System.out.printf("  Estado inicial: sangrando = %s%n", robot.isBleeding());
        assertFalse("Não está sangrando", robot.isBleeding());

        robot.applyBleed(3, 5);
        System.out.printf("  Após aplicar (3 ticks, 5 dmg): sangrando = %s%n", robot.isBleeding());
        assertTrue("Está sangrando", robot.isBleeding());

        int initialHp = robot.getHp();
        int damage1 = robot.tickBleed();
        System.out.printf("  Tick 1: dano = %d | HP: %d → %d%n", damage1, initialHp, robot.getHp());
        assertTrue("Dano = 5", damage1 == 5);
        assertTrue("HP reduziu 5", robot.getHp() == initialHp - 5);
        assertTrue("Ainda sangr ando", robot.isBleeding());

        robot.tickBleed();
        robot.tickBleed();
        System.out.printf("  Após 3 ticks: sangrando = %s%n", robot.isBleeding());
        assertFalse("Não sangra mais", robot.isBleeding());

        int damage4 = robot.tickBleed();
        System.out.printf("  Tick sem sangramento: dano = %d%n", damage4);
        assertTrue("Dano = 0", damage4 == 0);
    }

    private static void testSpecialCharges() {
        System.out.println("\n[TEST 8] Cargas de Especial");
        System.out.println("-".repeat(70));

        Robot robot = new Robot(CharacterClass.YURI, null, null, null);

        System.out.printf("  Cargas iniciais: %d%n", robot.specialCharges());
        System.out.printf("  Disponível: %s%n", robot.isSpecialAvailable());
        assertTrue("Tem especial", robot.isSpecialAvailable());
        assertTrue("1 carga", robot.specialCharges() == 1);

        boolean consumed = robot.consumeSpecial();
        System.out.printf("  Após consumir: sucesso = %s | cargas = %d%n",
                consumed, robot.specialCharges());
        assertTrue("Consumiu com sucesso", consumed);
        assertTrue("0 cargas", robot.specialCharges() == 0);
        assertFalse("Sem especial", robot.isSpecialAvailable());

        boolean consumed2 = robot.consumeSpecial();
        System.out.printf("  Tentativa sem cargas: sucesso = %s%n", consumed2);
        assertFalse("Não pode consumir", consumed2);
    }

    private static void testMinimumDamage() {
        System.out.println("\n[TEST 9] Dano Mínimo");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 10, 10, 0.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 100, 0.0, 0.0);

        DamageResult result = calc.compute(attacker, defender, false);

        System.out.printf("  Ataque: %d | Defesa muito alta: %d | Dano: %d%n",
                attacker.stats().atk, defender.stats().def, result.finalDamage);

        assertTrue("Dano mínimo = 1", result.finalDamage == 1);
    }

    private static void testCriticalAndSpecialStack() {
        System.out.println("\n[TEST 10] Crítico + Especial (Acumulativo)");
        System.out.println("-".repeat(70));

        DamageCalculator calc = new DamageCalculator();
        Robot attacker = createRobotWithStats(100, 50, 10, 1.0, 0.0);
        Robot defender = createRobotWithStats(100, 10, 10, 0.0, 0.0);

        DamageResult result = calc.compute(attacker, defender, true);

        System.out.printf("  Crítico: %s | Especial: %s | Dano: %d (esperado ~90)%n",
                result.critical ? "SIM" : "NÃO",
                result.usedSpecial ? "SIM" : "NÃO",
                result.finalDamage);

        assertTrue("É crítico", result.critical);
        assertTrue("Usou especial", result.usedSpecial);
        assertTrue("Dano 80-100", result.finalDamage >= 80 && result.finalDamage <= 100);
    }

    // ========== Utilitários ==========

    private static Robot createRobotWithStats(int hp, int atk, int def, double crit, double evade) {
        RobotStatsBuilder builder = new RobotStatsBuilder();
        builder.addHp(hp - 100);
        builder.addAtk(atk - 10);
        builder.addDef(def - 5);
        builder.addCrit(crit - 0.05);
        builder.addEvade(evade - 0.05);

        return new Robot(null, null, null, null) {
            @Override
            public RobotStats stats() {
                return builder.build();
            }
        };
    }

    private static void assertTrue(String description, boolean condition) {
        testsRun++;
        if (condition) {
            testsPassed++;
            System.out.printf("    ✓ %s%n", description);
        } else {
            testsFailed++;
            System.out.printf("    ✗ %s [FALHOU]%n", description);
        }
    }

    private static void assertFalse(String description, boolean condition) {
        assertTrue(description, !condition);
    }
}
