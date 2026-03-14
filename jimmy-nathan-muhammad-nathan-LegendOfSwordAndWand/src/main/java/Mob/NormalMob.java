package Mob;

import Hero.Hero;

public class NormalMob extends Mob {
    protected double spawnRate;

    public NormalMob(double hp, double power, int xp, int gold, double spawnRate) {
        super(hp, power, xp, gold);
        this.spawnRate = spawnRate;
    }

    public double getSpawnRate() { return spawnRate; }

    @Override
    public void attack(Hero target) {
        System.out.println("NormalMob attacks for " + power + " damage!");
        target.takeDamage(power);
    }
}
