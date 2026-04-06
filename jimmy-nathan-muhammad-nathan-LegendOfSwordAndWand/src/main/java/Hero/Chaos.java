package Hero;

import Strategy.ChaosAttackStrategy;

// chaos class stats
public class Chaos extends Hero {

    public Chaos(String name) {
        super(name);
        this.attackStrategy = new ChaosAttackStrategy();
    }

    @Override
    protected void applyLevelUpBonus() {
        attack  += 3;
        maxMana += 5;
        maxHp   += 2;
    }

    @Override
    protected void triggerHybrid() {
        if (secondaryClassName == null) return;
        switch (secondaryClassName.toUpperCase()) {
            case "ORDER":   hybridClass = "HERETIC";  break;
            case "WARRIOR": hybridClass = "ROGUE";    break;
            case "MAGE":    hybridClass = "SORCERER"; break;
        }
        if (hybridClass != null) {
            System.out.println(name + " has become a " + hybridClass + "!");
        }
    }

    public void triggerHybridWith(String secondaryClass) {
        if (isHybrid()) return;
        if (primaryClassLevel >= 5 && secondaryClassLevel >= 5) {
            switch (secondaryClass) {
                case "ORDER":   hybridClass = "HERETIC";  break;
                case "WARRIOR": hybridClass = "ROGUE";    break;
                case "MAGE":    hybridClass = "SORCERER"; break;
            }
        }
    }

    @Override
    public String getClassName() {
        if (isHybrid()) return hybridClass;
        if (isSpecialized()) return "INVOKER";
        return "Chaos";
    }

    @Override
    public void specialAttack(Hero[] targets) {
        fireball(targets);
    }

    // shoot fireball
    public void fireball(Hero[] targets) {
        if (!spendMana(30)) return;
        int dmg = attack;
        if ("SORCERER".equals(hybridClass)) dmg *= 2;
        for (int i = 0; i < Math.min(3, targets.length); i++) {
            int finalDmg = Math.max(0, dmg - targets[i].getDefense());
            targets[i].takeDamage(finalDmg);
        }
        System.out.println(name + " launches Fireball!");
    }

    // zap everyone
    public void chainLightning(Hero[] targets) {
        if (!spendMana(40)) return;
        double dmg = attack;
        double dropoff = "INVOKER".equals(getClassName()) ? 0.50 : 0.25;
        for (Hero t : targets) {
            int finalDmg = Math.max(0, (int)dmg - t.getDefense());
            t.takeDamage(finalDmg);
            dmg *= dropoff; // INVOKER: 50% retained per hit, others: 25% retained per hit
        }
        System.out.println(name + " casts Chain Lightning!");
    }
    @Override
    public String[] getSpells() {return new String[] {"Fireball", "Chain Lightning"};}
}
