package Strategy;

import Hero.Hero;
import Mob.Mob;

// Strategy pattern - different attack behaviors can be swapped at runtime
public interface AttackStrategy {
    void execute(Hero attacker, Mob target);
    String getStrategyName();
}
