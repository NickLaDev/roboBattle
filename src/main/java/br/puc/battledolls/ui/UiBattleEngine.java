package br.puc.battledolls.ui;

import br.puc.battledolls.combat.Action;
import br.puc.battledolls.combat.DamageCalculator;
import br.puc.battledolls.combat.DamageResult;
import br.puc.battledolls.model.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UiBattleEngine {

    public static class Snapshot {
        public final String currentName, enemyName;
        public final int currentHp, currentMaxHp, enemyHp, enemyMaxHp;
        public final boolean currentGuarding, enemyGuarding;
        public final boolean currentBleeding, enemyBleeding;
        public final boolean currentSpecial, enemySpecial;
        public final int round;
        public final boolean finished;
        public final String winner;

        Snapshot(String currentName, String enemyName,
                int currentHp, int currentMaxHp, int enemyHp, int enemyMaxHp,
                boolean currentGuarding, boolean enemyGuarding,
                boolean currentBleeding, boolean enemyBleeding,
                boolean currentSpecial, boolean enemySpecial,
                int round, boolean finished, String winner) {
            this.currentName = currentName;
            this.enemyName = enemyName;
            this.currentHp = currentHp;
            this.currentMaxHp = currentMaxHp;
            this.enemyHp = enemyHp;
            this.enemyMaxHp = enemyMaxHp;
            this.currentGuarding = currentGuarding;
            this.enemyGuarding = enemyGuarding;
            this.currentBleeding = currentBleeding;
            this.enemyBleeding = enemyBleeding;
            this.currentSpecial = currentSpecial;
            this.enemySpecial = enemySpecial;
            this.round = round;
            this.finished = finished;
            this.winner = winner;
        }
    }

    public static class StepResult {
        public final List<String> logs = new ArrayList<>();
        public final Snapshot snapshot;
        public final BattleEvent event; // Evento visual do último ataque

        StepResult(Snapshot s, BattleEvent event) {
            this.snapshot = s;
            this.event = event;
        }
    }
    
    /**
     * Representa um evento visual que aconteceu na batalha.
     */
    public static class BattleEvent {
        public final boolean isCritical;
        public final boolean isEvaded;
        public final boolean isDefended;
        public final boolean isSpecial;
        public final boolean isBleeding;
        public final int damage;
        public final String attackerName;
        public final String defenderName;
        
        public BattleEvent(boolean isCritical, boolean isEvaded, boolean isDefended, 
                          boolean isSpecial, boolean isBleeding, int damage,
                          String attackerName, String defenderName) {
            this.isCritical = isCritical;
            this.isEvaded = isEvaded;
            this.isDefended = isDefended;
            this.isSpecial = isSpecial;
            this.isBleeding = isBleeding;
            this.damage = damage;
            this.attackerName = attackerName;
            this.defenderName = defenderName;
        }
        
        public static BattleEvent none() {
            return new BattleEvent(false, false, false, false, false, 0, "", "");
        }
    }

    private final DamageCalculator calc = new DamageCalculator();
    private final Random rng = new Random();
    private final Player p1, p2;
    private final boolean isPVC; // true se modo Player vs CPU
    private Player current, enemy;
    private int round = 1;
    private boolean finished = false;
    private String winnerName = null;

    public UiBattleEngine(Player p1, Player p2) {
        this(p1, p2, false);
    }
    
    public UiBattleEngine(Player p1, Player p2, boolean isPVC) {
        this.p1 = p1;
        this.p2 = p2;
        this.isPVC = isPVC;
        this.current = rng.nextBoolean() ? p1 : p2;
        this.enemy = (current == p1) ? p2 : p1;
    }
    
    /**
     * Verifica se o jogador atual é a CPU (no modo PvC, p2 é sempre CPU).
     */
    public boolean isCurrentPlayerCPU() {
        return isPVC && current == p2;
    }
    
    /**
     * Retorna o nome do jogador que é CPU (null se não houver).
     */
    public String getCPUPlayerName() {
        return isPVC ? p2.name() : null;
    }
    
    /**
     * Retorna o jogador 1 (sempre humano no modo PvC).
     */
    public Player getPlayer1() {
        return p1;
    }
    
    /**
     * Retorna o jogador 2 (CPU no modo PvC, humano no modo PvP).
     */
    public Player getPlayer2() {
        return p2;
    }
    
    /**
     * Retorna o jogador atual (pode ser CPU no modo PvC).
     */
    public Player getCurrentPlayer() {
        return current;
    }
    
    /**
     * Retorna o jogador inimigo do atual.
     */
    public Player getEnemyPlayer() {
        return enemy;
    }

    public Snapshot snapshot() {
        return new Snapshot(
                current.name(), enemy.name(),
                current.robot().getHp(), current.robot().stats().maxHp,
                enemy.robot().getHp(), enemy.robot().stats().maxHp,
                current.robot().isGuarding(), enemy.robot().isGuarding(),
                current.robot().isBleeding(), enemy.robot().isBleeding(),
                current.robot().isSpecialAvailable(), enemy.robot().isSpecialAvailable(),
                round, finished, winnerName);
    }

    public StepResult perform(Action action) {
        List<String> logs = new ArrayList<>();
        BattleEvent event = BattleEvent.none();
        
        if (finished)
            return pack(logs, event);

        // Tick de sangramento no INÍCIO do turno do current
        int bleedTick = current.robot().tickBleed();
        if (bleedTick > 0) {
            logs.add(String.format("(SANGRAMENTO) %s sofre %d de dano. HP=%d",
                    current.name(), bleedTick, current.robot().getHp()));
            if (!current.robot().isAlive()) {
                finish(enemy.name(), logs);
                return pack(logs, event);
            }
        }

        logs.add(String.format("[Round %d] %s (%d HP) vs %s (%d HP)",
                round, p1.name(), p1.robot().getHp(), p2.name(), p2.robot().getHp()));

        switch (action) {
            case DEFEND -> {
                current.robot().setGuarding(true);
                logs.add(current.name() + " adotou postura DEFENSIVA (−50% no próximo dano).");
            }
            case ATTACK, SPECIAL -> {
                boolean useSpecial = (action == Action.SPECIAL) && current.robot().consumeSpecial();
                boolean wasGuarding = enemy.robot().isGuarding();
                DamageResult res = calc.compute(current.robot(), enemy.robot(), useSpecial);

                if (res.evaded) {
                    logs.add(String.format("%s atacou, mas %s ESQUIVOU! (0 dano)", current.name(), enemy.name()));
                    event = new BattleEvent(false, true, false, useSpecial, false, 0, current.name(), enemy.name());
                } else {
                    int dmg = res.finalDamage;
                    boolean wasDefended = false;

                    if (wasGuarding) {
                        int original = dmg;
                        dmg = Math.max(1, (int) Math.round(dmg * 0.5));
                        enemy.robot().clearGuard();
                        wasDefended = true;
                        logs.add(String.format("(GUARDA) Dano reduzido de %d para %d.", original, dmg));
                    }

                    enemy.robot().takeDamage(dmg);
                    logs.add(String.format("%s causou %d de dano%s%s. HP de %s = %d",
                            current.name(), dmg,
                            (res.critical ? " (CRIT!)" : ""),
                            (useSpecial ? " (SPECIAL!)" : ""),
                            enemy.name(), enemy.robot().getHp()));

                    boolean appliedBleed = false;
                    if (res.applyBleed && enemy.robot().isAlive()) {
                        enemy.robot().applyBleed(2, 3);
                        appliedBleed = true;
                        logs.add(String.format("(SANGRAMENTO) %s foi afligido por 2 turnos.", enemy.name()));
                    }
                    
                    event = new BattleEvent(res.critical, false, wasDefended, useSpecial, 
                                          appliedBleed, dmg, current.name(), enemy.name());
                }
            }
        }

        if (!enemy.robot().isAlive()) {
            finish(current.name(), logs);
            return pack(logs, event);
        }

        // troca turno
        Player tmp = current;
        current = enemy;
        enemy = tmp;
        round++;
        return pack(logs, event);
    }

    private void finish(String winner, List<String> logs) {
        finished = true;
        winnerName = winner;
        logs.add("\n*** VENCEDOR: " + winner + " ***");
    }

    private StepResult pack(List<String> logs, BattleEvent event) {
        StepResult sr = new StepResult(snapshot(), event);
        sr.logs.addAll(logs);
        return sr;
    }
}
