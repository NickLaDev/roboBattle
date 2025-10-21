package br.puc.robobattle.combat;

import br.puc.robobattle.model.Player;
import br.puc.robobattle.model.Robot;

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

            // Tick de sangramento NO INÍCIO do turno do current
            int bleedTick = current.robot().tickBleed();
            if (bleedTick > 0) {
                System.out.printf("(SANGRAMENTO) %s sofre %d de dano. HP agora = %d%n",
                        current.name(), bleedTick, current.robot().getHp());
                if (!current.robot().isAlive()) break;
            }

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
                    DamageResult res = calc.compute(current.robot(), enemy.robot(), useSpecial);

                    if (res.evaded) {
                        System.out.printf("%s atacou, mas %s ESQUIVOU! (0 dano)%n",
                                current.name(), enemy.name());
                    } else {
                        int dmg = res.finalDamage;

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

                        if (res.applyBleed && enemy.robot().isAlive()) {
                            enemy.robot().applyBleed(2, 3); // 2 turnos, 3 de dano por tick
                            System.out.printf("(SANGRAMENTO) %s foi afligido e sofrerá dano por %d turnos.%n",
                                    enemy.name(), 2);
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
}
