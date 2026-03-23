package Hero;

import Strategy.OrderAttackStrategy;

// order class stats
public class Order extends Hero {

    public Order(String name) {
        super(name);
        this.attackStrategy = new OrderAttackStrategy();
    }

    @Override
    protected void applyLevelUpBonus() {
        defense += 2;
        maxHp   += 8;
        maxMana += 5;
    }

    @Override
    protected void triggerHybrid() {
        if (secondaryClassName == null) return;
        switch (secondaryClassName.toUpperCase()) {
            case "CHAOS":   hybridClass = "HERETIC"; break;
            case "WARRIOR": hybridClass = "PALADIN"; break;
            case "MAGE":    hybridClass = "PROPHET"; break;
        }
        if (hybridClass != null) {
            System.out.println(name + " has become a " + hybridClass + "!");
        }
    }

    public void triggerHybridWith(String secondaryClass) {
        if (isHybrid()) return;
        if (primaryClassLevel >= 5 && secondaryClassLevel >= 5) {
            switch (secondaryClass) {
                case "CHAOS":   hybridClass = "HERETIC"; break;
                case "WARRIOR": hybridClass = "PALADIN"; break;
                case "MAGE":    hybridClass = "PROPHET"; break;
            }
        }
    }

    @Override
    public String getClassName() {
        if (isHybrid()) return hybridClass;
        if (isSpecialized()) return "PRIEST";
        return "Order";
    }

    @Override
    public void specialAttack(Hero[] targets) {
        heal(targets);
    }

    // give everyone a shield
    public void protect(Hero[] party) {
        if (!spendMana(25)) return;
        int shieldAmt = defense + 10;
        for (Hero h : party) {
            h.applyShield(shieldAmt);
        }
        System.out.println(name + " protects the party! (+" + shieldAmt + " shield each)");
    }

    // fire shield spell
    public void fireShield(Hero[] party) {
        if (!spendMana(25)) return;
        int shieldAmt = defense + 15;
        for (Hero h : party) {
            h.applyShield(shieldAmt);
        }
        System.out.println(name + " casts Fire Shield! (+" + shieldAmt + " burning shield each)");
    }

    // heal the party
    public void heal(Hero[] party) {
        if (!spendMana(35)) return;
        int healAmt = "PRIEST".equals(getClassName()) ? 50 : 30;
        if ("PROPHET".equals(hybridClass)) healAmt *= 2;
        for (Hero h : party) {
            h.heal(healAmt);
        }
        System.out.println(name + " heals the party for " + healAmt + " HP each!");
    }
}