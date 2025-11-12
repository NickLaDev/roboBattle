package br.puc.robobattle.model;

import java.util.function.Consumer;

/**
 * Representa os personagens jogáveis, cada um com atributos base e habilidade especial.
 */
public enum CharacterClass {

    BEATRIZ(
            "Beatriz",
            "Espadachim solar focada em golpes críticos e agressivos.",
            "/assets/chars/beatriz.png",
            builder -> builder.addAtk(8).addCrit(0.06),
            new AbilityEffect(
                    "Golpe Solar",
                    "Ataque especial com 70% de dano extra. Se o golpe for crítico, causa sangramento como de costume.",
                    true,
                    1.7,
                    0,
                    null,
                    null,
                    0,
                    0,
                    0,
                    false,
                    "Beatriz canaliza o Golpe Solar!"
            )
    ),

    YURI(
            "Yuri",
            "Guardião prismático capaz de aguentar pancadas e proteger aliados.",
            "/assets/chars/yuri.png",
            builder -> builder.addDef(8).addHp(30),
            new AbilityEffect(
                    "Escudo Prismático",
                    "Restaura 35 de HP e ativa postura defensiva imediatamente.",
                    false,
                    1.0,
                    0,
                    null,
                    null,
                    0,
                    0,
                    35,
                    true,
                    "Yuri ergue o Escudo Prismático!"
            )
    ),

    SHINOBI(
            "Shinobi",
            "Assassino ágil que aposta em esquivas e veneno duradouro.",
            "/assets/robots/Shinobi/Idle.png",
            builder -> builder.addAtk(4).addEvade(0.08),
            new AbilityEffect(
                    "Lâminas Envenenadas",
                    "Ataque que aplica veneno garantido (sangramento de 4 turnos causando 4 de dano).",
                    true,
                    1.0,
                    0,
                    4,
                    4,
                    0,
                    0,
                    0,
                    false,
                    "Shinobi perfura o alvo com lâminas envenenadas!"
            )
    );

    private final String displayName;
    private final String description;
    private final String portraitPath;
    private final Consumer<RobotStatsBuilder> baseStatsApplier;
    private final AbilityEffect ability;

    CharacterClass(String displayName, String description, String portraitPath,
                   Consumer<RobotStatsBuilder> baseStatsApplier, AbilityEffect ability) {
        this.displayName = displayName;
        this.description = description;
        this.portraitPath = portraitPath;
        this.baseStatsApplier = baseStatsApplier;
        this.ability = ability;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public String portraitPath() {
        return portraitPath;
    }

    public AbilityEffect ability() {
        return ability;
    }

    public void applyBaseStats(RobotStatsBuilder builder) {
        baseStatsApplier.accept(builder);
    }
}

