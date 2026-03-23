package Mob;

import Hero.Hero;

// default enemy class
public abstract class Mob {
    protected double hp;
    protected double power;
    protected int defense;
    protected int level;
    protected int xpReward;
    protected int goldReward;

    public Mob(double hp, double power, int defense, int level, int xp, int gold) {
        this.hp = hp;
        this.power = power;
        this.defense = defense;
        this.level = level;
        this.xpReward = xp;
        this.goldReward = gold;
    }

    public double getHp() { return hp; }
    public double getPower() { return power; }
    public int getDefense() { return defense; }
    public int getLevel() { return level; }
    public int getXpReward() { return xpReward; }
    public int getGoldReward() { return goldReward; }
    public boolean isAlive() { return hp > 0; }

    public void takeDamage(double amount) {
        this.hp = Math.max(0, this.hp - amount);
        System.out.println(getClass().getSimpleName() + " takes " + amount + " damage. HP remaining: " + this.hp);
    }

    public abstract void attack(Hero target);
}