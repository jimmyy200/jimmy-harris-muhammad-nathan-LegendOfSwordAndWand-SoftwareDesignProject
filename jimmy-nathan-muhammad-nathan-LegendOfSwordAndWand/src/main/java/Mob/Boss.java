package Mob;

import Hero.Hero;

public class Boss extends Mob {

    public Boss(double hp, double power, int xp, int gold) {
        super(hp, power, xp, gold);
    }

    @Override
    public void attack(Hero target) {
        double bossAttack = power * 1.5;
        System.out.println("BOSS unleashes a devastating attack for " + bossAttack + " damage!");
        target.takeDamage(bossAttack);
    }
}