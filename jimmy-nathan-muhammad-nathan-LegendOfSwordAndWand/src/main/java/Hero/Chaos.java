package Hero;

import java.util.Random;

public class Chaos extends Hero {

    private static final Random random = new Random();

    public Chaos(String name) {
        super(name);
        System.out.println("Chaos hero '" + name + "' created.");
    }

    @Override
    protected void applyLevelUpBonus() {
        if (isHybrid()) {
            switch (hybridClass) {
                case "INVOKER": // Chaos + Chaos: doubled growth
                    attack += 6; maxHp += 10; break;
                case "HERETIC": // Chaos + Order
                    attack += 3; maxHp += 5; maxMana += 5; defense += 2; break;
                case "ROGUE":   // Chaos + Warrior
                    attack += 3; maxHp += 5; attack += 2; defense += 3; break;
                case "SORCERER":// Chaos + Mage
                    attack += 3; maxHp += 5; maxMana += 5; attack += 1; break;
            }
        } else {
            int multiplier = isSpecialized() ? 2 : 1;
            attack  += 3 * multiplier;
            maxHp   += 5 * multiplier;
        }
    }

    @Override
    protected void triggerHybrid() {
        hybridClass = "INVOKER";
        System.out.println(name + " has specialised into INVOKER!");
    }

    public void triggerHybridWith(String otherClass) {
        switch (otherClass) {
            case "ORDER":   hybridClass = "HERETIC";  break;
            case "WARRIOR": hybridClass = "ROGUE";    break;
            case "MAGE":    hybridClass = "SORCERER"; break;
            default:        hybridClass = "INVOKER";  break;
        }
        System.out.println(name + " has become a " + hybridClass + "!");
    }

    @Override
    public void specialAttack(Hero[] targets) {
        fireball(targets);
    }

    public void fireball(Hero[] enemies) {
        if (!spendMana(30)) return;
        int hits = Math.min(3, enemies.length);
        // Sorcerer hybrid: Fireball does double damage to all affected units
        double multiplier = (isHybrid() && hybridClass.equals("SORCERER")) ? 2.0 : 1.0;
        System.out.println(name + " launches a Fireball!");
        for (int i = 0; i < hits; i++) {
            if (enemies[i] != null && enemies[i].isAlive()) {
                int damage = (int) (Math.max(0, attack - enemies[i].getDefense()) * multiplier);
                enemies[i].takeDamage(damage);
            }
        }
    }

    public void chainLightning(Hero[] enemies) {
        if (!spendMana(40)) return;
        System.out.println(name + " casts Chain Lightning!");

        // Invoker hybrid: each subsequent target takes 50% of previous (not 25%)
        double dropOff = (isHybrid() && hybridClass.equals("INVOKER")) ? 0.50 : 0.25;

        // Shuffle randomly
        Hero[] shuffled = enemies.clone();
        for (int i = shuffled.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Hero tmp = shuffled[i]; shuffled[i] = shuffled[j]; shuffled[j] = tmp;
        }

        double currentDamage = Math.max(0, attack - shuffled[0].getDefense());
        for (Hero target : shuffled) {
            if (target != null && target.isAlive()) {
                target.takeDamage(currentDamage);
                currentDamage *= dropOff;
            }
        }
    }
}