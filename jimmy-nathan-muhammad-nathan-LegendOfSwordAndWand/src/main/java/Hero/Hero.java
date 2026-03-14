package Hero;

public abstract class Hero {
    protected double power;
    protected double defense;
    protected double speed;
    protected double hp;
    protected int level;

    public Hero(double power, double defense, double speed, double hp) {
        this.power = power;
        this.defense = defense;
        this.speed = speed;
        this.hp = hp;
        this.level = 1;
    }

    public double getPower() { return power; }
    public double getDefense() { return defense; }
    public double getSpeed() { return speed; }
    public double getHp() { return hp; }
    public int getLevel() { return level; }

    public void takeDamage(double amount) {
        double reduced = Math.max(0, amount - defense);
        this.hp = Math.max(0, this.hp - reduced);
        System.out.println("Hero takes " + reduced + " damage. HP remaining: " + this.hp);
    }

    public void changeSpeed(double speed) { this.speed = speed; }
    public void changePower(double power) { this.power = power; }
    public void changeDefense(double defense) { this.defense = defense; }
    public void changeHp(double hp) { this.hp = hp; }

    public void basicAttack() {
        System.out.println(getClass().getSimpleName() + " performs a basic attack for " + power + " damage!");
    }

    public void setLevel(int level) { this.level = level; }

    public abstract void specialAttack();
    public abstract void LevelUp();
}