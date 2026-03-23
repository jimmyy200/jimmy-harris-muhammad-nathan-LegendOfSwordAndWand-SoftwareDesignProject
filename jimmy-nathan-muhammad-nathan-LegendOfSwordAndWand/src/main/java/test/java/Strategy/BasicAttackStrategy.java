package Strategy;

import Hero.Hero;
import Mob.Mob;

// Basic attack: damage = attacker's attack - target's defense
public class BasicAttackStrategy implements AttackStrategy {
    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
        System.out.println(attacker.getName() + " attacks for " + damage + " damage!");
    }

    @Override
    public String getStrategyName() {
        return "Basic Attack";
    }
}
