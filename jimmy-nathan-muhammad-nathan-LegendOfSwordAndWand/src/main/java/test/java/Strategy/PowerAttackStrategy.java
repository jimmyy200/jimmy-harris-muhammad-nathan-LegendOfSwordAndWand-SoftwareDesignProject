package Strategy;

import Hero.Hero;
import Mob.Mob;

// Power attack: deals 1.5x normal damage but costs more
public class PowerAttackStrategy implements AttackStrategy {
    @Override
    public void execute(Hero attacker, Mob target) {
        int baseDamage = Math.max(0, attacker.getAttack() - target.getDefense());
        int damage = (int)(baseDamage * 1.5);
        target.takeDamage(damage);
        System.out.println(attacker.getName() + " unleashes a power attack for " + damage + " damage!");
    }

    @Override
    public String getStrategyName() {
        return "Power Attack";
    }
}
