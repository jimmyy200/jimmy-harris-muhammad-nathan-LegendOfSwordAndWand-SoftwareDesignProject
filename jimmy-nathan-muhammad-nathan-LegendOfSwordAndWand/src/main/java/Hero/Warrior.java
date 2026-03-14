package Hero;

import java.util.Random;

public class Warrior extends Hero {

    private static final Random random = new Random();

    public Warrior(String name) {
        super(name);
        System.out.println("Warrior '" + name + "' created.");
    }

    @Override
    protected void applyLevelUpBonus() {
        if (isHybrid()) {
            switch (hybridClass) {
                case "KNIGHT":  // Warrior + Warrior: doubled growth
                    attack += 4; defense += 6; break;
                case "PALADIN": // Warrior + Order
                    attack += 2; defense += 3; maxMana += 5; defense += 2; break;
                case "ROGUE":   // Warrior + Chaos
                    attack += 2; defense += 3; attack += 3; maxHp += 5; break;
                case "WARLOCK": // Warrior + Mage
                    attack += 2; defense += 3; maxMana += 5; attack += 1; break;
            }
        } else {
            int multiplier = isSpecialized() ? 2 : 1;
            attack  += 2 * multiplier;
            defense += 3 * multiplier;
        }
    }

    @Override
    protected void triggerHybrid() {
        hybridClass = "KNIGHT";
        System.out.println(name + " has specialised into KNIGHT!");
    }

    public void triggerHybridWith(String otherClass) {
        switch (otherClass) {
            case "ORDER":  hybridClass = "PALADIN"; break;
            case "CHAOS":  hybridClass = "ROGUE";   break;
            case "MAGE":   hybridClass = "WARLOCK"; break;
            default:       hybridClass = "KNIGHT";  break;
        }
        System.out.println(name + " has become a " + hybridClass + "!");
    }

    @Override
    public void specialAttack(Hero[] targets) {
        berserkerAttack(targets);
    }

    public void berserkerAttack(Hero[] enemies) {
        if (!spendMana(60)) return;
        if (enemies == null || enemies.length == 0) return;
        System.out.println(name + " goes Berserk!");

        int primaryDamage = Math.max(0, attack - enemies[0].getDefense());

        // Paladin hybrid: heal self for 10% of max HP before attacking
        if (isHybrid() && hybridClass.equals("PALADIN")) {
            double healAmount = maxHp * 0.10;
            heal(healAmount);
            System.out.println(name + " heals for " + healAmount + " before attacking!");
        }

        enemies[0].takeDamage(primaryDamage);

        // Knight hybrid: 50% chance to stun each unit hit
        if (isHybrid() && hybridClass.equals("KNIGHT")) {
            if (random.nextBoolean()) {
                enemies[0].setStunned(true);
                System.out.println(enemies[0].getName() + " is stunned!");
            }
        }

        // Splash hits — up to 2 more targets at 25%
        int splashDamage = (int) (primaryDamage * 0.25);
        for (int i = 1; i < Math.min(3, enemies.length); i++) {
            if (enemies[i] != null && enemies[i].isAlive()) {
                enemies[i].takeDamage(splashDamage);
                System.out.println("Berserker splash hits " + enemies[i].getName() + " for " + splashDamage + "!");
                if (isHybrid() && hybridClass.equals("KNIGHT") && random.nextBoolean()) {
                    enemies[i].setStunned(true);
                    System.out.println(enemies[i].getName() + " is stunned!");
                }
            }
        }
    }

    /**
     * Sneak Attack (Rogue hybrid): every attack has 50% chance to deal
     * an additional hit to a random enemy for 50% of total damage.
     */
    public void sneakAttack(Hero[] enemies) {
        if (enemies == null || enemies.length == 0) return;
        basicAttack(enemies[0]);
        if (random.nextBoolean()) {
            int bonusTarget = random.nextInt(enemies.length);
            int bonusDamage = (int) (Math.max(0, attack - enemies[bonusTarget].getDefense()) * 0.50);
            enemies[bonusTarget].takeDamage(bonusDamage);
            System.out.println("Sneak Attack bonus hit on " + enemies[bonusTarget].getName() + " for " + bonusDamage + "!");
        }
    }
}