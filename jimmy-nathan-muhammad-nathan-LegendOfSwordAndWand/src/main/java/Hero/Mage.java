package Hero;

public class Mage extends Hero {

    public Mage(String name) {
        super(30.0, 5.0, 14.0, 80.0);
        System.out.println("Mage '" + name + "' created.");
    }

    @Override
    public void basicAttack() {
        System.out.println("Mage fires a magic bolt for " + power + " damage!");
    }

    @Override
    public void specialAttack() {
        double arcaneBlast = power * 2.5;
        System.out.println("Mage casts Arcane Explosion! Deals " + arcaneBlast + " damage to all enemies!");
    }

    @Override
    public void LevelUp() {
        this.level++;
        this.power += 8;
        this.hp += 8;
        System.out.println("Mage leveled up to " + level + "! Magical power greatly increased.");
    }
}