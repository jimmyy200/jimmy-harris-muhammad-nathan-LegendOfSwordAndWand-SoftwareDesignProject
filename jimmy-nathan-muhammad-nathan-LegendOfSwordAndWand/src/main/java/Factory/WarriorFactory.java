package Factory;

import Hero.*;

public class WarriorFactory extends HeroFactory {
    @Override
    public Hero createHero(String name) {
        return new Warrior(name);
    }
}
