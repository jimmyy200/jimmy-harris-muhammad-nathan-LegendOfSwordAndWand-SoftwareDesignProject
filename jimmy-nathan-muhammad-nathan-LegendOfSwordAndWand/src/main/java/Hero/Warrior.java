package Hero;

public class Warrior extends Hero {

    public Warrior(String name) {
        super(20.0, 15.0, 12.0, 150.0);
        System.out.println("Warrior '" + name + "' created.");
    }

    @Override
    public void basicAttack() {
        System.out.println("Warrior swings their sword for " + power + " damage!");
    }

    @Override
    public void specialAttack() {
        double powerSlam = power * 1.8;
        System.out.println("Warrior uses Power Slam! Deals " + powerSlam + " damage!");
    }

    @Override
    public void LevelUp() {
        this.level++;
        this.power += 5;
        this.defense += 3;
        this.hp += 25;
        System.out.println("Warrior leveled up to " + level + "! A well-rounded improvement.");
    }
}