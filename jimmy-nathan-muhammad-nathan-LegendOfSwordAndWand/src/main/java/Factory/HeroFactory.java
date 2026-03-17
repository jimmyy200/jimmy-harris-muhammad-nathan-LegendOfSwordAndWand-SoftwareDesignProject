package Factory;

import Hero.*;

public abstract class HeroFactory {

    /** Factory method — subclasses decide which Hero to create */
    public abstract Hero createHero(String name);

    /** Convenience static method to get the right factory by class name */
    public static HeroFactory getFactory(String heroClass) {
        switch (heroClass.toUpperCase()) {
            case "WARRIOR": return new WarriorFactory();
            case "MAGE":    return new MageFactory();
            case "ORDER":   return new OrderFactory();
            case "CHAOS":   return new ChaosFactory();
            default:        return new WarriorFactory();
        }
    }

    /** Create a hero at a specific level */
    public Hero createHeroAtLevel(String name, int targetLevel) {
        Hero hero = createHero(name);
        for (int i = 1; i < targetLevel; i++) {
            hero.gainExperience(hero.expNeededForLevel(i + 1));
        }
        return hero;
    }
}