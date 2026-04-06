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
        boolean healsAll = "PRIEST".equals(getClassName()) || "PROPHET".equals(hybridClass);
        double multiplier = "PROPHET".equals(hybridClass) ? 0.50 : 0.25;
        if (healsAll) {
            for (Hero h : party) {
                double healAmt = h.getMaxHp() * multiplier;
                h.heal(healAmt);
            }
            System.out.println(name + " heals the entire party!");
        } else {
            Hero lowest = null;
            for (Hero h : party) {
                if (h.isAlive() && (lowest == null || h.getHp() < lowest.getHp())) {
                    lowest = h;
                }
            }
            if (lowest != null) {
                double healAmt = lowest.getMaxHp() * multiplier;
                lowest.heal(healAmt);
                System.out.println(name + " heals " + lowest.getName() + " for " + (int)healAmt + " HP!");
            }
        }
    }
    @Override
    public String[] getSpells(){
        if (isHybrid() && "HERETIC".equals(hybridClass)) {
            return new String[] {"Fire Shield", "Heal"};
        }
        return new String[] {"Protect", "Heal"};
    }
}
