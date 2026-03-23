package Strategy;

import Hero.Hero;
import Mob.Mob;

// Strategy for Order class attack - defensive damage with shield generation
public class OrderAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
        // Order bonus: small shield on hit
        attacker.applyShield(Math.max(1, damage / 5));
    }

    @Override
    public String getStrategyName() {
        return "Order Attack";
    }
}
