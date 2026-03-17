package Factory;

import Hero.*;

public class ChaosFactory extends HeroFactory {
    @Override
    public Hero createHero(String name) {
        return new Chaos(name);
    }
}
