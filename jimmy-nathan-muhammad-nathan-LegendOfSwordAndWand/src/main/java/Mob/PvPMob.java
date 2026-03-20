package Mob;

import Hero.Hero;

/**
 * Wraps an opponent Hero so it can participate in the existing Mob-based battle system.
 * Delegates damage, HP and attack to the underlying Hero.
 */
public class PvPMob extends Mob {

    private final Hero heroRef;

    public PvPMob(Hero hero) {
        super(hero.getHp(), hero.getAttack(), 0, 0);
        this.heroRef = hero;
    }

    public Hero getHero() { return heroRef; }

    @Override
    public double getHp()    { return heroRef.getHp(); }
    @Override
    public double getPower() { return heroRef.getAttack(); }
    @Override
    public boolean isAlive() { return heroRef.isAlive(); }

    @Override
    public void takeDamage(double amount) {
        heroRef.takeDamage(amount);
        this.hp = heroRef.getHp();
    }

    @Override
    public void attack(Hero target) {
        int damage = Math.max(0, heroRef.getAttack() - target.getDefense());
        target.takeDamage(damage);
        System.out.println(heroRef.getName() + " attacks " + target.getName() + " for " + damage + "!");
    }

    @Override
    public String toString() {
        return heroRef.getName() + " [" + heroRef.getClassName() + " Lv" + heroRef.getLevel() + "]";
    }
}