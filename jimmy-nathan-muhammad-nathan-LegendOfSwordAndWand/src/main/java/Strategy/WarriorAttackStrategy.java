package Strategy;

import Hero.Hero;
import Mob.Mob;

// warrior attack logic
public class WarriorAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, attacker.getAttack() - target.getDefense() + 2);
        target.takeDamage(damage);
    }

    @Override
    public String getStrategyName() {
        return "Warrior Attack";
    }
}
