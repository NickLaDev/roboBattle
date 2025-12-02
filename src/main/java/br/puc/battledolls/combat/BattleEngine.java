package br.puc.battledolls.combat;

import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.Robot;
import br.puc.battledolls.model.AbilityEffect;
import br.puc.battledolls.model.CharacterClass;

import java.util.Random;
import java.util.Scanner;

public class BattleEngine {
    private final DamageCalculator calc = new DamageCalculator();
    private final Random rng = new Random();
    private final Scanner in; // ler ações dos jogadores

    public BattleEngine(Scanner in) { this.in = in; }

    public Player fight(Player p1, Player p2) {
        Robot r1 = p1.robot(), r2 = p2.robot();
        Player current = rng.nextBoolean() ? p1 : p2;
        int round = 1;

        System.out.println("\n--- BATALHA ---");
        while (r1.isAlive() && r2.isAlive()) {
            Player enemy = (current == p1) ? p2 : p1;

            // Tick de sangramento/veneno para AMBOS no início de cada turno (independente de quem joga)
            if (applyOngoingDamage(current, enemy)) break; // se alguém morreu, fim
            if (applyOngoingDamage(enemy, current)) break;

            System.out.printf("%n[Round %d] %s (%d HP) vs %s (%d HP)%n",
                    round, p1.name(), r1.getHp(), p2.name(), r2.getHp());

            Action action = chooseAction(current);
            switch (action) {
                case DEFEND -> {
                    current.robot().setGuarding(true);
                    System.out.printf("%s adotou postura DEFENSIVA (−50%% no próximo dano recebido).%n",
                            current.name());
                }
                case ATTACK, SPECIAL -> {
                    boolean useSpecial = (action == Action.SPECIAL) && current.robot().consumeSpecial();
                    AbilityEffect ability = (useSpecial && current.robot().characterClass() != null)
                            ? current.robot().characterClass().ability()
                            : null;

                    // Habilidade defensiva (ex.: cura/guarda)
                    if (useSpecial && ability != null && !ability.offensive()) {
                        if (!ability.activationMessage().isBlank()) {
                            System.out.println(ability.activationMessage());
                        }
                        if (ability.selfHeal() > 0) {
                            current.robot().heal(ability.selfHeal());
                            System.out.printf("%s recuperou %d de HP. HP agora = %d%n",
                                    current.name(), ability.selfHeal(), current.robot().getHp());
                        }
                        if (ability.grantGuard()) {
                            current.robot().setGuarding(true);
                            System.out.printf("%s entrou em postura defensiva!%n", current.name());
                        }
                        break;
                    }

                    double baseSpecialMultiplier = useSpecial ? 1.5 : 1.0;
                    double abilityMultiplier = (useSpecial && ability != null) ? ability.damageMultiplier() : 1.0;
                    double totalMultiplier = useSpecial ? baseSpecialMultiplier * abilityMultiplier : 1.0;

                    DamageResult res = calc.compute(current.robot(), enemy.robot(), useSpecial, totalMultiplier);

                    if (res.evaded) {
                        System.out.printf("%s atacou, mas %s ESQUIVOU! (0 dano)%n",
                                current.name(), enemy.name());
                    } else {
                        int dmg = res.finalDamage;
                        if (useSpecial && ability != null) {
                            dmg += ability.flatDamageBonus();
                        }
                        boolean guaranteedPoison = useSpecial && ability != null
                                && ability.guaranteedBleedTicks() != null
                                && ability.guaranteedBleedDamage() != null;

                        // redução por guarda
                        if (enemy.robot().isGuarding()) {
                            int original = dmg;
                            dmg = Math.max(1, (int) Math.round(dmg * 0.5)); // -50%
                            enemy.robot().clearGuard();
                            System.out.printf("(GUARDA) Dano reduzido de %d para %d.%n", original, dmg);
                        }

                        enemy.robot().takeDamage(dmg);
                        System.out.printf("%s causou %d de dano%s%s. HP de %s = %d%n",
                                current.name(), dmg,
                                (res.critical ? " (CRIT!)" : ""),
                                (useSpecial ? " (SPECIAL!)" : ""),
                                enemy.name(), enemy.robot().getHp());

                        int bleedTicks = 0;
                        int bleedDamage = 0;
                        boolean appliedBleed = false;
                        boolean shinobiPoison = false;
                        boolean isShinobi = current.robot().characterClass() == CharacterClass.SHINOBI;

                        // Crítico padrão aplica 2x3 se algum dano passou
                        if (res.applyBleed && dmg > 0) {
                            bleedTicks = 1; // sangramento crítico dura apenas 1 turno
                            bleedDamage = 3;
                            appliedBleed = true;
                        }
                        // Habilidade especial pode garantir/estender sangramento mesmo sem crítico
                        if (useSpecial && ability != null) {
                            Integer guaranteedTicks = ability.guaranteedBleedTicks();
                            Integer guaranteedDamage = ability.guaranteedBleedDamage();
                            if (isShinobi && guaranteedTicks != null && guaranteedDamage != null) {
                                // Apenas o Shinobi tem veneno garantido
                                bleedTicks = guaranteedTicks;
                                bleedDamage = guaranteedDamage;
                                appliedBleed = true;
                            }
                            // Extra bleed só faz sentido se já aplicamos algo
                            if (appliedBleed && (ability.extraBleedTicks() > 0 || ability.extraBleedDamage() > 0)) {
                                bleedTicks += ability.extraBleedTicks();
                                bleedDamage += ability.extraBleedDamage();
                            }
                        }
                        if (appliedBleed && enemy.robot().isAlive()) {
                            shinobiPoison = isShinobi && guaranteedPoison && useSpecial;
                            enemy.robot().applyBleed(bleedTicks, bleedDamage, shinobiPoison);
                            String status = shinobiPoison ? "ENVENENAMENTO" : "SANGRAMENTO";
                            System.out.printf("(%s) %s foi afligido e sofrerá dano por %d turnos.%n",
                                    status, enemy.name(), bleedTicks);
                            // Veneno do Shinobi aplica dano imediato + mais 3 turnos
                            if (shinobiPoison) {
                                int poisonHit = enemy.robot().tickBleed();
                                if (poisonHit > 0) {
                                    System.out.printf("(ENVENENAMENTO) %s sofre %d de dano imediato. HP agora = %d%n",
                                            enemy.name(), poisonHit, enemy.robot().getHp());
                                    if (!enemy.robot().isAlive()) {
                                        System.out.printf("%n*** VENCEDOR: %s ***%n", current.name());
                                        return current;
                                    }
                                }
                            }
                        }

                        if (useSpecial && ability != null) {
                            if (ability.selfHeal() > 0) {
                                current.robot().heal(ability.selfHeal());
                                System.out.printf("%s se curou em %d HP. HP agora = %d%n",
                                        current.name(), ability.selfHeal(), current.robot().getHp());
                            }
                            if (ability.grantGuard()) {
                                current.robot().setGuarding(true);
                                System.out.printf("%s entrou em postura defensiva!%n", current.name());
                            }
                            if (!ability.activationMessage().isBlank()) {
                                System.out.println(ability.activationMessage());
                            }
                        }
                    }
                }
            }
            // troca turno
            current = enemy;
            round++;
        }
        Player winner = r1.isAlive() ? p1 : p2;
        System.out.printf("%n*** VENCEDOR: %s ***%n", winner.name());
        return winner;
    }

    private Action chooseAction(Player p) {
        while (true) {
            boolean canSpecial = p.robot().isSpecialAvailable();
            System.out.printf("%nAção de %s: 1) ATTACK  2) DEFEND  %s%n",
                    p.name(), canSpecial ? "3) SPECIAL" : "(SPECIAL indisponível)");
            System.out.print("Escolha: ");
            String s = in.nextLine().trim();
            if ("1".equals(s)) return Action.ATTACK;
            if ("2".equals(s)) return Action.DEFEND;
            if ("3".equals(s) && canSpecial) return Action.SPECIAL;
            System.out.println("Opção inválida.");
        }
    }

    /**
     * Aplica dano recorrente (sangramento/veneno) ao jogador alvo.
     * @return true se o alvo morreu, encerrando o combate.
     */
    private boolean applyOngoingDamage(Player target, Player other) {
        String bleedLabel = target.robot().getBleedLabel();
        int bleedTick = target.robot().tickBleed();
        if (bleedTick > 0) {
            String statusLabel = (bleedLabel != null) ? bleedLabel : "SANGRAMENTO";
            System.out.printf("(%s) %s sofre %d de dano. HP agora = %d%n",
                    statusLabel, target.name(), bleedTick, target.robot().getHp());
            if (!target.robot().isAlive()) {
                System.out.printf("%n*** VENCEDOR: %s ***%n", other.name());
                return true;
            }
        }
        return false;
    }
}
