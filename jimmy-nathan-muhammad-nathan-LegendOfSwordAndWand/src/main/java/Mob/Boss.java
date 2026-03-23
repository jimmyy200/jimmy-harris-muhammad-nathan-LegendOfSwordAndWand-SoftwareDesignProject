package Mob;

import Hero.Hero;

// boss enemy logic
public class Boss extends Mob {

    public Boss(double hp, double power, int defense, int level, int xp, int gold) {
        super(hp, power, defense, level, xp, gold);
    }

    @Override
    public void attack(Hero target) {
        int damage = (int) Math.max(0, (power * 1.5) - target.getDefense());
        System.out.println("BOSS (Lv" + level + ") unleashes a devastating attack for " + damage + " damage!");
        target.takeDamage(damage);
    }
}