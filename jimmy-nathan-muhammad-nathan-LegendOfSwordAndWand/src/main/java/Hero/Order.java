package Hero;

public class Order extends Hero {

    public Order(String name) {
        super(name);
        System.out.println("Order hero '" + name + "' created.");
    }

    @Override
    protected void applyLevelUpBonus() {
        if (isHybrid()) {
            switch (hybridClass) {
                case "PRIEST":  // Order + Order: doubled growth
                    maxMana += 10; defense += 4; break;
                case "HERETIC": // Order + Chaos
                    maxMana += 5; defense += 2; attack += 3; maxHp += 5; break;
                case "PALADIN": // Order + Warrior
                    maxMana += 5; defense += 2; attack += 2; defense += 3; break;
                case "PROPHET": // Order + Mage
                    maxMana += 5; defense += 2; maxMana += 5; attack += 1; break;
            }
        } else {
            int multiplier = isSpecialized() ? 2 : 1;
            maxMana += 5 * multiplier;
            defense += 2 * multiplier;
        }
    }

    @Override
    protected void triggerHybrid() {
        // Order's secondary class determines the hybrid
        // secondaryClassLevel hits 5 — the caller sets which class is secondary
        // By default Order + Order = Priest (shouldn't normally happen, but handled)
        hybridClass = "PRIEST";
        System.out.println(name + " has specialised into PRIEST!");
    }

    /** Called externally when we know the second class */
    public void triggerHybridWith(String otherClass) {
        switch (otherClass) {
            case "CHAOS":   hybridClass = "HERETIC"; break;
            case "WARRIOR": hybridClass = "PALADIN"; break;
            case "MAGE":    hybridClass = "PROPHET"; break;
            default:        hybridClass = "PRIEST";  break;
        }
        System.out.println(name + " has become a " + hybridClass + "!");
    }

    @Override
    public void specialAttack(Hero[] targets) {
        if (isHybrid() && hybridClass.equals("HERETIC")) {
            // Heretic: can cast Fire Shield instead of Protect
            fireShield(targets);
        } else {
            protect(targets);
        }
    }

    public void protect(Hero[] party) {
        if (!spendMana(25)) return;
        for (Hero h : party) {
            if (h != null && h.isAlive()) {
                int shield = (int) (h.getMaxHp() * 0.10);
                h.applyShield(shield);
                System.out.println(name + " shields " + h.getName() + " for " + shield + " HP!");
            }
        }
    }

    /**
     * Fire Shield (Heretic hybrid): same as Protect, but if a shielded unit
     * is attacked it returns 10% of the damage back to the attacker.
     * The shield amount is the same — the return damage is handled in takeDamage logic.
     */
    public void fireShield(Hero[] party) {
        if (!spendMana(25)) return;
        for (Hero h : party) {
            if (h != null && h.isAlive()) {
                int shield = (int) (h.getMaxHp() * 0.10);
                h.applyShield(shield);
                System.out.println(name + " casts Fire Shield on " + h.getName() + "!");
            }
        }
    }

    public void heal(Hero[] party) {
        int manaCost = 35;
        // Prophet hybrid: friendly spells double effect
        double healMultiplier = (isHybrid() && hybridClass.equals("PROPHET")) ? 2.0 : 1.0;
        if (!spendMana(manaCost)) return;

        Hero lowest = null;
        for (Hero h : party) {
            if (h != null && h.isAlive()) {
                if (lowest == null || h.getHp() < lowest.getHp()) lowest = h;
            }
        }
        if (lowest != null) {
            // Priest hybrid: Heal applies to ALL party members
            if (isHybrid() && hybridClass.equals("PRIEST")) {
                for (Hero h : party) {
                    if (h != null && h.isAlive()) {
                        double healAmount = h.getMaxHp() * 0.25 * healMultiplier;
                        h.heal(healAmount);
                        System.out.println(name + " heals " + h.getName() + " for " + healAmount + " HP!");
                    }
                }
            } else {
                double healAmount = lowest.getMaxHp() * 0.25 * healMultiplier;
                lowest.heal(healAmount);
                System.out.println(name + " heals " + lowest.getName() + " for " + healAmount + " HP!");
            }
        }
    }
}