package Hero;

public class Mage extends Hero {

    public Mage(String name) {
        super(name);
        System.out.println("Mage '" + name + "' created.");
    }

    @Override
    protected void applyLevelUpBonus() {
        if (isHybrid()) {
            switch (hybridClass) {
                case "WIZARD":  // Mage + Mage: doubled growth
                    maxMana += 10; attack += 2; break;
                case "PROPHET": // Mage + Order
                    maxMana += 5; attack += 1; maxMana += 5; defense += 2; break;
                case "SORCERER":// Mage + Chaos
                    maxMana += 5; attack += 1; attack += 3; maxHp += 5; break;
                case "WARLOCK": // Mage + Warrior
                    maxMana += 5; attack += 1; attack += 2; defense += 3; break;
            }
        } else {
            int multiplier = isSpecialized() ? 2 : 1;
            maxMana += 5 * multiplier;
            attack  += 1 * multiplier;
        }
    }

    @Override
    protected void triggerHybrid() {
        hybridClass = "WIZARD";
        System.out.println(name + " has specialised into WIZARD!");
    }

    public void triggerHybridWith(String otherClass) {
        switch (otherClass) {
            case "ORDER":   hybridClass = "PROPHET";  break;
            case "CHAOS":   hybridClass = "SORCERER"; break;
            case "WARRIOR": hybridClass = "WARLOCK";  break;
            default:        hybridClass = "WIZARD";   break;
        }
        System.out.println(name + " has become a " + hybridClass + "!");
    }

    @Override
    public void specialAttack(Hero[] targets) {
        replenish(targets);
    }

    public void replenish(Hero[] party) {
        // Wizard hybrid: Replenish costs only 40 mana instead of 80
        int cost = (isHybrid() && hybridClass.equals("WIZARD")) ? 40 : 80;
        // Prophet hybrid: doubles the effect
        double multiplier = (isHybrid() && hybridClass.equals("PROPHET")) ? 2.0 : 1.0;

        if (!spendMana(cost)) return;
        System.out.println(name + " casts Replenish!");
        for (Hero h : party) {
            if (h != null && h.isAlive()) {
                if (h == this) {
                    int restore = (int) (60 * multiplier);
                    mana = Math.min(maxMana, mana + restore);
                    System.out.println(name + " replenishes " + restore + " mana to self.");
                } else {
                    int restore = (int) (30 * multiplier);
                    h.mana = Math.min(h.maxMana, h.mana + restore);
                    System.out.println(h.getName() + " replenishes " + restore + " mana.");
                }
            }
        }
    }

    /**
     * Mana Burn (Warlock hybrid): every basic attack burns 10% of the
     * target's total mana. Called from the battle system after basicAttack().
     */
    public void manaBurn(Hero target) {
        if (!isHybrid() || !hybridClass.equals("WARLOCK")) return;
        int burn = (int) (target.getMaxMana() * 0.10);
        target.mana = Math.max(0, target.mana - burn);
        System.out.println(name + " burns " + burn + " mana from " + target.getName() + "!");
    }
}