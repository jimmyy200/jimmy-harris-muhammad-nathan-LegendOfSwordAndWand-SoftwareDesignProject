package Strategy;

import Hero.Hero;
import Mob.Mob;

// order attack logic
public class OrderAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, attacker.getAttack() - target.getDefense());
        target.takeDamage(damage);
        // give a shield
        attacker.applyShield(Math.max(1, damage / 5));
    }

    @Override
    public String getStrategyName() {
        return "Order Attack";
    }
}
