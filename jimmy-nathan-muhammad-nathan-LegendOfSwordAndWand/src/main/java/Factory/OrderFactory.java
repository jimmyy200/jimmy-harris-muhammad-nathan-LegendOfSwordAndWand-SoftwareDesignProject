package Factory;

import Hero.*;

public class OrderFactory extends HeroFactory {
    @Override
    public Hero createHero(String name) {
        return new Order(name);
    }
}