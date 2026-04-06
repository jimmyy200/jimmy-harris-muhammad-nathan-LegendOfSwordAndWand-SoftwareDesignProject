package Hero;

import Strategy.WarriorAttackStrategy;

// warrior class stats
public class Warrior extends Hero {

    public Warrior(String name) {
        super(name);
        this.attackStrategy = new WarriorAttackStrategy();
    }

    @Override
    protected void applyLevelUpBonus() {
        attack  += 1;
        defense += 1;
        maxHp   += 10;
        maxMana += 3;
    }

    @Override
    protected void triggerHybrid() {
        // make them a hybrid class
        if (secondaryClassName == null) return;
        switch (secondaryClassName.toUpperCase()) {
            case "ORDER": hybridClass = "PALADIN"; break;
            case "CHAOS": hybridClass = "ROGUE";   break;
            case "MAGE":  hybridClass = "WARLOCK"; break;
        }
        if (hybridClass != null) {
            System.out.println(name + " has become a " + hybridClass + "!");
        }
    }

    // set the second class they picked
    @Override
    public void triggerHybridWith(String secondaryClass) {
        if (isHybrid()) return;
        if (primaryClassLevel >= 5 && secondaryClassLevel >= 5) {
            switch (secondaryClass) {
                case "ORDER": hybridClass = "PALADIN"; break;
                case "CHAOS": hybridClass = "ROGUE";   break;
                case "MAGE":  hybridClass = "WARLOCK"; break;
            }
        }
    }

    // knight logic
    @Override
    public String getClassName() {
        if (isHybrid()) return hybridClass;
        if (isSpecialized()) return "KNIGHT";
        return "Warrior";
    }

    @Override
    public void specialAttack(Hero[] targets) {
        berserkerAttack(targets);
    }

    // attack method
    public void berserkerAttack(Hero[] targets) {
        if (!spendMana(60)) return;
        System.out.println(name + " goes Berserk!");
        if (targets.length > 0) {
            int primary = Math.max(0, attack - targets[0].getDefense()) * 2;
            targets[0].takeDamage(primary);
            int splash = (int)(primary * 0.25);
            for (int i = 1; i < Math.min(3, targets.length); i++) {
                targets[i].takeDamage(splash);
            }
        }
        // stun bonus
        if ("KNIGHT".equals(getClassName()) && targets.length > 0) {
            if (Math.random() < 0.25) {
                targets[0].setStunned(true);
                System.out.println("Stunning blow! " + targets[0].getName() + " is stunned!");
            }
        }
        // healing bonus
        if ("PALADIN".equals(hybridClass)) {
            heal(maxHp * 0.10);
            System.out.println(name + " heals from righteous fury!");
        }
    }
    @Override
    public String[] getSpells() {return new String[] {"Berserker Attack"};}
}