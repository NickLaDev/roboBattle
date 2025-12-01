package br.puc.battledolls.ui;

import br.puc.battledolls.combat.Action;
import br.puc.battledolls.combat.BattleCommand;
import br.puc.battledolls.combat.DamageCalculator;
import br.puc.battledolls.combat.DamageResult;
import br.puc.battledolls.items.Potion;
import br.puc.battledolls.model.Player;
import br.puc.battledolls.model.AbilityEffect;

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
        public final int currentSpecialCharge, enemySpecialCharge;
        public final int round;
        public final boolean finished;
        public final String winner;

        Snapshot(String currentName, String enemyName,
                int currentHp, int currentMaxHp, int enemyHp, int enemyMaxHp,
                boolean currentGuarding, boolean enemyGuarding,
                boolean currentBleeding, boolean enemyBleeding,
                boolean currentSpecial, boolean enemySpecial,
                int currentSpecialCharge, int enemySpecialCharge,
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
            this.currentSpecialCharge = currentSpecialCharge;
            this.enemySpecialCharge = enemySpecialCharge;
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
        public final String statusLabel;
        
        public BattleEvent(boolean isCritical, boolean isEvaded, boolean isDefended, 
                          boolean isSpecial, boolean isBleeding, int damage,
                          String attackerName, String defenderName, String statusLabel) {
            this.isCritical = isCritical;
            this.isEvaded = isEvaded;
            this.isDefended = isDefended;
            this.isSpecial = isSpecial;
            this.isBleeding = isBleeding;
            this.damage = damage;
            this.attackerName = attackerName;
            this.defenderName = defenderName;
            this.statusLabel = statusLabel;
        }
        
        public static BattleEvent none() {
            return new BattleEvent(false, false, false, false, false, 0, "", "", "");
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
                current.robot().getSpecialCharge(), enemy.robot().getSpecialCharge(),
                round, finished, winnerName);
    }

    public StepResult perform(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action não pode ser nula.");
        }
        if (action == Action.USE_POTION) {
            throw new IllegalArgumentException("Use perform(BattleCommand.usePotion(...)) para poções.");
        }
        return perform(BattleCommand.of(action));
    }

    public StepResult perform(BattleCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Comando não pode ser nulo.");
        }
        Action action = command.action();
        List<String> logs = new ArrayList<>();
        BattleEvent event = BattleEvent.none();
        
        if (finished)
            return pack(logs, event);

        // Atualiza efeitos temporários (fúria) no início do turno
        current.robot().tickTemporaryEffects();

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

        boolean isDefending = false;
        var potion = command.potion();
        switch (action) {
            case USE_POTION -> {
                if (potion == null || !current.usePotion(potion)) {
                    logs.add(current.name() + " tentou usar uma poção, mas não possui.");
                    return pack(logs, event);
                }
                
                // Aplica efeito da poção
                switch (potion.type()) {
                    case VIDA -> {
                        int heal = potion.getHealAmount();
                        current.robot().heal(heal);
                        logs.add(String.format("%s usou %s e recuperou %d HP! HP atual: %d",
                            current.name(), potion.getDisplayName(), heal, current.robot().getHp()));
                        event = new BattleEvent(false, false, false, false, false, -heal, 
                            current.name(), current.name(), ""); // negativo indica cura
                    }
                    case BARRERA -> {
                        int shield = potion.getShieldAmount();
                        current.robot().addShield(shield);
                        logs.add(String.format("%s usou %s e ganhou %d de escudo!",
                            current.name(), potion.getDisplayName(), shield));
                        event = new BattleEvent(false, false, false, false, false, -shield - 1000, 
                            current.name(), current.name(), ""); // -1000+ indica escudo
                    }
                    case ENERGIA -> {
                        int energy = potion.getEnergyAmount();
                        current.robot().addSpecialChargeAmount(energy);
                        logs.add(String.format("%s usou %s e ganhou %d de carga especial! Carga: %d/100",
                            current.name(), potion.getDisplayName(), energy, current.robot().getSpecialCharge()));
                        event = new BattleEvent(false, false, false, false, false, -energy - 2000, 
                            current.name(), current.name(), ""); // -2000+ indica energia
                    }
                    case FURIA -> {
                        double multiplier = potion.getFuryMultiplier();
                        int duration = potion.getFuryDuration();
                        current.robot().applyFury(multiplier, duration);
                        logs.add(String.format("%s usou %s! Ataque aumentado em %.0f%% por %d turnos!",
                            current.name(), potion.getDisplayName(), (multiplier - 1.0) * 100, duration));
                        event = new BattleEvent(false, false, false, false, false, -3000, 
                            current.name(), current.name(), ""); // -3000 indica fúria
                    }
                }
            }
            case DEFEND -> {
                current.robot().setGuarding(true);
                isDefending = true;
                logs.add(current.name() + " adotou postura DEFENSIVA (−50% no próximo dano).");
            }
            case ATTACK, SPECIAL -> {
                boolean useSpecial = (action == Action.SPECIAL) && current.robot().consumeSpecial();
                boolean wasGuarding = enemy.robot().isGuarding();
                AbilityEffect ability = (useSpecial && current.robot().characterClass() != null)
                        ? current.robot().characterClass().ability()
                        : null;
                
                if (useSpecial && ability != null && !ability.offensive()) {
                    // Habilidade defensiva (sem ataque direto)
                    if (!ability.activationMessage().isBlank()) {
                        logs.add(ability.activationMessage());
                    } else {
                        logs.add(current.name() + " usou o especial " + ability.name() + "!");
                    }
                    
                    // Cura
                    int heal = ability.selfHeal();
                    if (heal > 0) {
                        current.robot().heal(heal);
                        logs.add(String.format("%s recuperou %d de HP. HP atual: %d",
                                current.name(), heal, current.robot().getHp()));
                        event = new BattleEvent(false, false, false, true, false, -heal,
                                current.name(), current.name(), "");
                    } else {
                        event = new BattleEvent(false, false, false, true, false, 0,
                                current.name(), current.name(), "");
                    }
                    
                    // Ativa guarda se aplicável
                    if (ability.grantGuard()) {
                        current.robot().setGuarding(true);
                        logs.add(current.name() + " entrou em postura defensiva!");
                    }
                } else {
                
                    // Pega multiplicador customizado se for especial (da timing bar) e ajusta pelo personagem
                    double skillMultiplier = (useSpecial && command.specialMultiplier() != null)
                            ? command.specialMultiplier()
                            : (useSpecial ? 1.5 : 1.0);
                    double abilityMultiplier = (useSpecial && ability != null) ? ability.damageMultiplier() : 1.0;
                    double totalMultiplier = useSpecial ? skillMultiplier * abilityMultiplier : 1.0;
                    
                    DamageResult res = calc.compute(current.robot(), enemy.robot(), useSpecial, totalMultiplier);

                    if (res.evaded) {
                        logs.add(String.format("%s atacou, mas %s ESQUIVOU! (0 dano)", current.name(), enemy.name()));
                        event = new BattleEvent(false, true, false, useSpecial, false, 0, current.name(), enemy.name(), "");
                    } else {
                        int dmg = res.finalDamage;
                        if (useSpecial && ability != null) {
                            dmg += ability.flatDamageBonus();
                        }
                        dmg = Math.max(1, dmg);
                        boolean wasDefended = false;
                        boolean guaranteedPoison = useSpecial && ability != null
                                && ability.guaranteedBleedTicks() != null
                                && ability.guaranteedBleedDamage() != null;

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

                        int bleedTicks = 0;
                        int bleedDamage = 0;
                        boolean appliedBleed = false;

                        // Crítico padrão aplica 2x3 se algum dano passou
                        if (res.applyBleed && dmg > 0) {
                            bleedTicks = 2;
                            bleedDamage = 3;
                            appliedBleed = true;
                        }

                        // Habilidade especial pode garantir/estender sangramento mesmo sem crítico
                        if (useSpecial && ability != null) {
                            Integer guaranteedTicks = ability.guaranteedBleedTicks();
                            Integer guaranteedDamage = ability.guaranteedBleedDamage();
                            if (guaranteedTicks != null && guaranteedDamage != null) {
                                bleedTicks = Math.max(bleedTicks, guaranteedTicks);
                                bleedDamage = Math.max(bleedDamage, guaranteedDamage);
                                appliedBleed = true;
                            }

                            if (appliedBleed && (ability.extraBleedTicks() > 0 || ability.extraBleedDamage() > 0)) {
                                bleedTicks += ability.extraBleedTicks();
                                bleedDamage += ability.extraBleedDamage();
                            }
                        }
                        
                        if (appliedBleed && enemy.robot().isAlive()) {
                            enemy.robot().applyBleed(bleedTicks, bleedDamage);
                            logs.add(String.format("(SANGRAMENTO) %s foi afligido por %d turnos.",
                                    enemy.name(), bleedTicks));
                        }
                        
                        // Cura ou bônus defensivo após o ataque, se existir
                        if (useSpecial && ability != null) {
                            if (ability.selfHeal() > 0) {
                                current.robot().heal(ability.selfHeal());
                                logs.add(String.format("%s se curou em %d HP. HP atual: %d",
                                        current.name(), ability.selfHeal(), current.robot().getHp()));
                            }
                            if (ability.grantGuard()) {
                                current.robot().setGuarding(true);
                                logs.add(current.name() + " entrou em postura defensiva!");
                            }
                            if (!ability.activationMessage().isBlank()) {
                                logs.add(0, ability.activationMessage());
                            }
                        }
                        
                        String statusLabel = "";
                        if (appliedBleed) {
                            statusLabel = (guaranteedPoison) ? "ENVENENADO" : "SANGRAMENTO";
                        }
                        event = new BattleEvent(res.critical, false, wasDefended, useSpecial, 
                                              appliedBleed, dmg, current.name(), enemy.name(), statusLabel);
                    }
                }
            }
        }

        if (!enemy.robot().isAlive()) {
            finish(current.name(), logs);
            return pack(logs, event);
        }

        // Adiciona carga ao especial do jogador atual
        current.robot().addSpecialCharge(isDefending);
        int chargeGained = isDefending ? 40 : 20;
        int newCharge = current.robot().getSpecialCharge();
        if (newCharge >= 100) {
            logs.add(String.format("(ESPECIAL CARREGADO!) %s está pronto para usar o ataque especial!", current.name()));
        } else {
            logs.add(String.format("(CARGA) %s ganhou %d de carga especial. Total: %d/100",
                current.name(), chargeGained, newCharge));
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
