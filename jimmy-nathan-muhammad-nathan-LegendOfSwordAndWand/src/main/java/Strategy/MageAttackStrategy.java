package Strategy;

import Hero.Hero;
import Mob.Mob;

// mage attack logic
public class MageAttackStrategy implements AttackStrategy {

    @Override
    public void execute(Hero attacker, Mob target) {
        if (attacker.getMana() >= 10) {
            int damage = Math.max(0, (attacker.getAttack() * 2) - target.getDefense());
            target.takeDamage(damage);
            attacker.changeMana(attacker.getMana() - 10);
        } else {
            int damage = Math.max(0, attacker.getAttack() - target.getDefense());
            target.takeDamage(damage);
        }
    }

    @Override
    public String getStrategyName() {
        return "Mage Attack";
    }
}
