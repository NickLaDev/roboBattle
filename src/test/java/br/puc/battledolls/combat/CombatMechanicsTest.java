package br.puc.battledolls.combat;

import br.puc.battledolls.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para validar os mecanismos de combate: ataque, defesa, crítico, esquiva e sangramento.
 */
public class CombatMechanicsTest {

    private DamageCalculator calculator;
    private Robot attacker;
    private Robot defender;

    @BeforeEach
    public void setup() {
        calculator = new DamageCalculator();
    }

    @Test
    public void testBasicAttack() {
        // Cria atacante com 50 de ataque e defensor com 10 de defesa
        attacker = createRobot(100, 50, 10, 0.0, 0.0);
        defender = createRobot(100, 10, 10, 0.0, 0.0);

        DamageResult result = calculator.compute(attacker, defender, false);

        // Verifica que o dano foi calculado (ataque - defesa com variação)
        assertTrue(result.finalDamage > 0, "O dano básico deve ser maior que 0");
        assertTrue(result.finalDamage >= 35 && result.finalDamage <= 45,
            "Dano esperado ~40 (50-10), obtido: " + result.finalDamage);
        assertFalse(result.critical, "Não deve ser crítico com 0% de chance");
        assertFalse(result.evaded, "Não deve esquivar com 0% de chance");
        assertFalse(result.usedSpecial, "Não deve usar especial quando não solicitado");
    }

    @Test
    public void testDefenseReduction() {
        // Cria robôs com defesa alta
        attacker = createRobot(100, 30, 10, 0.0, 0.0);
        defender = createRobot(100, 10, 25, 0.0, 0.0);

        DamageResult result = calculator.compute(attacker, defender, false);

        // Com defesa alta, o dano deve ser reduzido
        assertTrue(result.finalDamage > 0, "Sempre deve causar pelo menos 1 de dano");
        assertTrue(result.finalDamage < 10,
            "Dano deve ser reduzido pela defesa alta, obtido: " + result.finalDamage);
    }

    @Test
    public void testCriticalHit() {
        // Cria atacante com 100% de chance de crítico
        attacker = createRobot(100, 50, 10, 1.0, 0.0);
        defender = createRobot(100, 10, 10, 0.0, 0.0);

        DamageResult result = calculator.compute(attacker, defender, false);

        // Verifica que foi crítico
        assertTrue(result.critical, "Deve ser crítico com 100% de chance");
        assertTrue(result.applyBleed, "Crítico deve aplicar sangramento");
        // Crítico multiplica dano por 1.5
        assertTrue(result.finalDamage >= 50 && result.finalDamage <= 70,
            "Dano crítico esperado ~60 ((50-10)*1.5), obtido: " + result.finalDamage);
    }

    @Test
    public void testEvasion() {
        // Cria defensor com 100% de chance de esquiva
        attacker = createRobot(100, 50, 10, 0.0, 0.0);
        defender = createRobot(100, 10, 10, 0.0, 1.0);

        DamageResult result = calculator.compute(attacker, defender, false);

        // Verifica que esquivou
        assertTrue(result.evaded, "Deve esquivar com 100% de chance");
        assertEquals(0, result.finalDamage, "Dano deve ser 0 quando esquiva");
        assertFalse(result.critical, "Não pode ser crítico se esquivou");
        assertFalse(result.applyBleed, "Não deve aplicar sangramento se esquivou");
    }

    @Test
    public void testSpecialAttack() {
        // Cria robôs básicos
        attacker = createRobot(100, 50, 10, 0.0, 0.0);
        defender = createRobot(100, 10, 10, 0.0, 0.0);

        DamageResult result = calculator.compute(attacker, defender, true);

        // Verifica que o especial multiplica o dano
        assertTrue(result.usedSpecial, "Deve marcar como especial usado");
        // Especial multiplica dano por 1.5
        assertTrue(result.finalDamage >= 50 && result.finalDamage <= 70,
            "Dano especial esperado ~60 ((50-10)*1.5), obtido: " + result.finalDamage);
    }

    @Test
    public void testGuardMechanism() {
        // Testa o mecanismo de guarda
        Robot robot = createRobot(100, 30, 20, 0.0, 0.0);

        assertFalse(robot.isGuarding(), "Robô não deve estar guardando inicialmente");

        robot.setGuarding(true);
        assertTrue(robot.isGuarding(), "Robô deve estar guardando após ativar");

        robot.clearGuard();
        assertFalse(robot.isGuarding(), "Robô não deve estar guardando após limpar");
    }

    @Test
    public void testBleedingMechanism() {
        Robot robot = createRobot(100, 30, 20, 0.0, 0.0);

        assertFalse(robot.isBleeding(), "Robô não deve estar sangrando inicialmente");

        robot.applyBleed(3, 5);
        assertTrue(robot.isBleeding(), "Robô deve estar sangrando após aplicar");

        // Testa ticks de sangramento
        int initialHp = robot.getHp();
        int damage1 = robot.tickBleed();
        assertEquals(5, damage1, "Primeiro tick deve causar 5 de dano");
        assertEquals(initialHp - 5, robot.getHp(), "HP deve reduzir em 5");
        assertTrue(robot.isBleeding(), "Robô ainda deve estar sangrando (2 ticks restantes)");

        int damage2 = robot.tickBleed();
        assertEquals(5, damage2, "Segundo tick deve causar 5 de dano");
        assertTrue(robot.isBleeding(), "Robô ainda deve estar sangrando (1 tick restante)");

        int damage3 = robot.tickBleed();
        assertEquals(5, damage3, "Terceiro tick deve causar 5 de dano");
        assertFalse(robot.isBleeding(), "Robô não deve estar mais sangrando após 3 ticks");

        int damage4 = robot.tickBleed();
        assertEquals(0, damage4, "Não deve causar dano quando não está sangrando");
    }

    @Test
    public void testSpecialCharges() {
        // Sem módulo de bateria (1 carga)
        Robot robotNoBattery = new Robot(CharacterClass.YURI, null, null, null);
        assertTrue(robotNoBattery.isSpecialAvailable(), "Deve ter especial disponível");
        assertEquals(1, robotNoBattery.specialCharges(), "Deve ter 1 carga sem bateria");

        assertTrue(robotNoBattery.consumeSpecial(), "Deve conseguir consumir a carga");
        assertFalse(robotNoBattery.isSpecialAvailable(), "Não deve ter mais especial disponível");
        assertEquals(0, robotNoBattery.specialCharges(), "Deve ter 0 cargas após consumir");

        assertFalse(robotNoBattery.consumeSpecial(), "Não deve conseguir consumir sem cargas");
    }

    @Test
    public void testMinimumDamage() {
        // Defesa muito alta (maior que ataque)
        attacker = createRobot(100, 10, 10, 0.0, 0.0);
        defender = createRobot(100, 10, 100, 0.0, 0.0);

        DamageResult result = calculator.compute(attacker, defender, false);

        // Deve sempre causar pelo menos 1 de dano
        assertEquals(1, result.finalDamage, "Dano mínimo deve ser 1 mesmo com defesa muito alta");
    }

    @Test
    public void testCriticalAndSpecialStack() {
        // Cria atacante com 100% de crítico e usa especial
        attacker = createRobot(100, 50, 10, 1.0, 0.0);
        defender = createRobot(100, 10, 10, 0.0, 0.0);

        DamageResult result = calculator.compute(attacker, defender, true);

        // Crítico e especial devem acumular (1.5 * 1.5 = 2.25)
        assertTrue(result.critical, "Deve ser crítico");
        assertTrue(result.usedSpecial, "Deve usar especial");
        // Dano esperado: (50-10) * 1.5 (crit) * 1.5 (special) = 40 * 2.25 = 90
        assertTrue(result.finalDamage >= 80 && result.finalDamage <= 100,
            "Dano com crítico+especial esperado ~90, obtido: " + result.finalDamage);
    }

    // Método auxiliar para criar robôs de teste
    private Robot createRobot(int hp, int atk, int def, double crit, double evade) {
        // Builder começa com valores base (100 HP, 10 ATK, 5 DEF, 0.05 crit, 0.05 evade)
        // Precisamos ajustar para os valores desejados
        RobotStatsBuilder builder = new RobotStatsBuilder();
        builder.addHp(hp - 100);      // Ajusta HP
        builder.addAtk(atk - 10);     // Ajusta ATK
        builder.addDef(def - 5);      // Ajusta DEF
        builder.addCrit(crit - 0.05); // Ajusta crit
        builder.addEvade(evade - 0.05); // Ajusta evade

        // Cria um robô sem personagem/equipamento específico, usando stats customizados
        return new Robot(null, null, null, null) {
            @Override
            public RobotStats stats() {
                return builder.build();
            }
        };
    }
}
