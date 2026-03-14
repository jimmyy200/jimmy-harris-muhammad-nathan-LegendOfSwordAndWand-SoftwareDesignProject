package Hero;

public class Order extends Hero {

    public Order(String name) {
        super(15.0, 20.0, 10.0, 120.0);
        System.out.println("Order hero '" + name + "' created.");
    }

    @Override
    public void basicAttack() {
        System.out.println("Order strikes with disciplined precision for " + power + " damage!");
    }

    @Override
    public void specialAttack() {
        double shieldBurst = power * 1.5;
        System.out.println("Order uses Shield Burst! Deals " + shieldBurst + " damage and boosts defense!");
        this.defense += 5;
    }

    @Override
    public void LevelUp() {
        this.level++;
        this.power += 3;
        this.defense += 5;
        this.hp += 20;
        System.out.println("Order leveled up to " + level + "! Defense increased significantly.");
    }
}