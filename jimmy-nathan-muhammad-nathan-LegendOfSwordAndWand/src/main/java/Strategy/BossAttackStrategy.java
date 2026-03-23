package Strategy;

import Hero.Hero;
import Mob.Mob;

// boss attack logic
public class BossAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        int damage = Math.max(0, (int)(attacker.getPower() * 1.5) - target.getDefense());
        target.takeDamage(damage);
    }

    @Override
    public String getStrategyName() {
        return "Boss Attack";
    }
}
