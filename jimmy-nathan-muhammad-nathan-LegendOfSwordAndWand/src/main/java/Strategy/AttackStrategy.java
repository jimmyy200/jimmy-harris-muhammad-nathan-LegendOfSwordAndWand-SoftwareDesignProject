package Strategy;

import Hero.Hero;
import Mob.Mob;

// different attack logic
public interface AttackStrategy {
    void execute(Hero attacker, Mob target);
    String getStrategyName();
}
