package Mob;

import Hero.Hero;

// makes a hero fight like a monster
// used for pvp
public class PvPMob extends Mob {

    private final Hero heroRef;

    public PvPMob(Hero hero) {
        super(hero.getHp(), hero.getAttack(), hero.getDefense(), hero.getLevel(), 0, 0);
        this.heroRef = hero;
    }

    public Hero getHero() { return heroRef; }

    @Override
    public double getHp()    { return heroRef.getHp(); }
    @Override
    public double getPower() { return heroRef.getAttack(); }
    @Override
    public int getDefense()  { return heroRef.getDefense(); }
    @Override
    public int getLevel()    { return heroRef.getLevel(); }
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