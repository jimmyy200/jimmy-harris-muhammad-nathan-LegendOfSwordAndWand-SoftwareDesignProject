package Factory;

import Hero.*;

public class MageFactory extends HeroFactory {
    @Override
    public Hero createHero(String name) {
        return new Mage(name);
    }
}