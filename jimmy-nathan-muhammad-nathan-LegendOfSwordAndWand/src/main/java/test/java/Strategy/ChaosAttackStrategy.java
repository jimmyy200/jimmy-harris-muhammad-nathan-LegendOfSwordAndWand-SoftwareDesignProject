package Strategy;

import Hero.Hero;
import Mob.Mob;

// Strategy for Chaos class attack - high risk high reward with self-damage
public class ChaosAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, (attacker.getAttack() * 3) - target.getDefense());
        target.takeDamage(damage);
        // Chaos penalty: attack costs health
        int selfDamage = Math.max(1, damage / 4);
        attacker.takeDamage(selfDamage);
    }

    @Override
    public String getStrategyName() {
        return "Chaos Attack";
    }
}
