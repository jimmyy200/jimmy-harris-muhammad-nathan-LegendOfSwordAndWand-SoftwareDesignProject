package Hero;

public class Chaos extends Hero {

    public Chaos(String name) {
        super(25.0, 8.0, 18.0, 90.0);
        System.out.println("Chaos hero '" + name + "' created.");
    }

    @Override
    public void basicAttack() {
        System.out.println("Chaos unleashes a wild strike for " + power + " damage!");
    }

    @Override
    public void specialAttack() {
        double chaosBlast = power * 2.0;
        System.out.println("Chaos uses Chaos Blast! Deals " + chaosBlast + " damage at the cost of 10 HP!");
        this.hp -= 10;
    }

    @Override
    public void LevelUp() {
        this.level++;
        this.power += 7;
        this.speed += 3;
        this.hp += 10;
        System.out.println("Chaos leveled up to " + level + "! Power and speed surged!");
    }
}