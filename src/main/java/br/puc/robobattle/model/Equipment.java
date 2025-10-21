package br.puc.robobattle.model;

public interface Equipment {
    String name();
    int getLevel();
    int getCost();
    void apply(RobotStatsBuilder builder);
}
