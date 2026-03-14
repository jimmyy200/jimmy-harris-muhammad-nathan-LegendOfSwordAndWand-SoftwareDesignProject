package Mob;

import Hero.Hero;

public abstract class Mob {
    protected double hp;
    protected double power;
    protected int xpReward;
    protected int goldReward;

    public Mob(double hp, double power, int xp, int gold) {
        this.hp = hp;
        this.power = power;
        this.xpReward = xp;
        this.goldReward = gold;
    }

    public double getHp() { return hp; }
    public double getPower() { return power; }
    public int getXpReward() { return xpReward; }
    public int getGoldReward() { return goldReward; }
    public boolean isAlive() { return hp > 0; }

    public void takeDamage(double amount) {
        this.hp = Math.max(0, this.hp - amount);
        System.out.println(getClass().getSimpleName() + " takes " + amount + " damage. HP remaining: " + this.hp);
    }

    public abstract void attack(Hero target);
}