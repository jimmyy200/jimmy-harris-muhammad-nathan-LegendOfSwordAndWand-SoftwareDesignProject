package Hero;

import Strategy.MageAttackStrategy;

// mage class stats
public class Mage extends Hero {

    public Mage(String name) {
        super(name);
        this.attackStrategy = new MageAttackStrategy();
    }

    @Override
    protected void applyLevelUpBonus() {
        attack  += 2;
        maxMana += 10;
        maxHp   += 3;
    }

    @Override
    protected void triggerHybrid() {
        if (secondaryClassName == null) return;
        switch (secondaryClassName.toUpperCase()) {
            case "ORDER":   hybridClass = "PROPHET";  break;
            case "CHAOS":   hybridClass = "SORCERER"; break;
            case "WARRIOR": hybridClass = "WARLOCK";  break;
        }
        if (hybridClass != null) {
            System.out.println(name + " has become a " + hybridClass + "!");
        }
    }

    @Override
    public void triggerHybridWith(String secondaryClass) {
        if (isHybrid()) return;
        if (primaryClassLevel >= 5 && secondaryClassLevel >= 5) {
            switch (secondaryClass) {
                case "ORDER":   hybridClass = "PROPHET";  break;
                case "CHAOS":   hybridClass = "SORCERER"; break;
                case "WARRIOR": hybridClass = "WARLOCK";  break;
            }
        }
    }

    @Override
    public String getClassName() {
        if (isHybrid()) return hybridClass;
        if (isSpecialized()) return "WIZARD";
        return "Mage";
    }

    @Override
    public void specialAttack(Hero[] targets) {
        replenish(targets);
    }

    // give mana back
    public void replenish(Hero[] party) {
        int cost = "WIZARD".equals(getClassName()) ? 40 : 80;
        if (!spendMana(cost)) return;
        int restore = "PROPHET".equals(hybridClass) ? 60 : 30;
        for (Hero h : party) {
            h.restoreMana(restore);
        }
        System.out.println(name + " replenishes " + restore + " mana to all allies!");
    }

    // take mana from them
    public void manaBurn(Hero target) {
        int drain = 10;
        if (target.getMana() >= drain) {
            target.changeMana(target.getMana() - drain);
            restoreMana(drain);
            System.out.println(name + " drains " + drain + " mana from " + target.getName() + "!");
        }
    }
}