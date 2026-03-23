package Mob;

import Hero.Hero;

// regular enemy
public class NormalMob extends Mob {

    public NormalMob(double hp, double power, int defense, int level, int xp, int gold) {
        super(hp, power, defense, level, xp, gold);
    }

    @Override
    public void attack(Hero target) {
        int damage = Math.max(0, (int) power - target.getDefense());
        System.out.println("Enemy (Lv" + level + ") attacks " + target.getName() + " for " + damage + " damage!");
        target.takeDamage(damage);
    }
}
