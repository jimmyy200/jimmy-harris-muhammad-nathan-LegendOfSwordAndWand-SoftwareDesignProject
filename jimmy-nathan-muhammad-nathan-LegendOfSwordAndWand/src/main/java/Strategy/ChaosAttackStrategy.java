package Strategy;

import Hero.Hero;
import Mob.Mob;

// chaos attack logic
public class ChaosAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, (attacker.getAttack() * 3) - target.getDefense());
        target.takeDamage(damage);
        // costs health
        int selfDamage = Math.max(1, damage / 4);
        attacker.takeDamage(selfDamage);
    }

    @Override
    public String getStrategyName() {
        return "Chaos Attack";
    }
}
