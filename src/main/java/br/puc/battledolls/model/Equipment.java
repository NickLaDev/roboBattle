package br.puc.battledolls.model;

public interface Equipment {
    String name();
    int getLevel();
    int getCost();
    void apply(RobotStatsBuilder builder);
}
