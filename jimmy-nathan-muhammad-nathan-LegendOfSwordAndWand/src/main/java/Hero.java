package Hero;

import java.util.ArrayList;
import java.util.List;

import Observer.GameObserver;
import Observer.Subject;
import Strategy.AttackStrategy;
import Strategy.BasicAttackStrategy;

// Base class for all hero types - has stats, levelling, combat, and hybrid class system
public abstract class Hero implements Subject {
    protected String name;
    protected int attack;
    protected int defense;
    protected double hp;
    protected double maxHp;
    protected int mana;
    protected int maxMana;
    protected int level;
    protected int experience;
    protected int shieldHp;
    protected boolean stunned;
    protected boolean waiting;

    // Hybrid tracking
    protected String hybridClass = null;
    protected int primaryClassLevel   = 1;
    protected int secondaryClassLevel = 0;
    protected String secondaryClassName = null; // tracks the name of the second class

    // Strategy pattern - attack behavior can be swapped
    protected AttackStrategy attackStrategy;

    // Observer pattern - notifies on level/XP changes
    private List<GameObserver> observers = new ArrayList<>();

    private static final int MAX_LEVEL = 20;

    public Hero(String name) {
        this.name     = name;
        this.attack   = 5;
        this.defense  = 5;
        this.maxHp    = 100;
        this.hp       = 100;
        this.maxMana  = 50;
        this.mana     = 50;
        this.level    = 1;
        this.experience = 0;
        this.shieldHp = 0;
        this.stunned  = false;
        this.waiting  = false;
        this.attackStrategy = new BasicAttackStrategy();
    }

    // leveling up stuff

    public int expNeededForLevel(int targetLevel) {
        int total = 0;
        for (int l = 2; l <= targetLevel; l++) {
            total += 500 + 75 * l + 20 * l * l;
        }
        return total;
    }

    public boolean gainExperience(int amount) {
        this.experience += amount;
        boolean leveledUp = false;
        while (level < MAX_LEVEL && experience >= expNeededForLevel(level + 1)) {
            levelUp();
            leveledUp = true;
        }
        return leveledUp;
    }

    private void levelUp() {
        level++;
        // base stats per level
        attack  += 1;
        defense += 1;
        maxHp   += 5;
        maxMana += 2;
        applyLevelUpBonus();
        hp   = maxHp;
        mana = maxMana;
        System.out.println(name + " reached level " + level + "! [" + getClassName() + "]");
        notifyObservers();
    }

    // Subclass applies its class-specific bonus
    protected abstract void applyLevelUpBonus();

    // Called when player levels up the secondary class
    public void levelUpSecondaryClass() {
        secondaryClassLevel++;
        if (!isHybrid() && primaryClassLevel >= 5 && secondaryClassLevel >= 5) {
            triggerHybrid();
        }
    }

    public void levelUpPrimaryClass() {
        primaryClassLevel++;
        if (!isHybrid() && primaryClassLevel >= 5 && secondaryClassLevel >= 5) {
            triggerHybrid();
        }
    }

    // Subclass sets hybridClass name when both classes reach 5
    protected abstract void triggerHybrid();

    // Refactor 9 - Long Method (offerLevelUpChoice)
    // Moved triggerHybridWith to base class to enable polymorphic dispatch
    // and eliminate instanceof checks in GamePanel
    public abstract void triggerHybridWith(String secondaryClass);

    public boolean isHybrid()         { return hybridClass != null; }
    public boolean isSpecialized()    { return primaryClassLevel >= 5 && !isHybrid(); }
    public String  getHybridClass()   { return hybridClass; }

    public String getClassName() {
        if (isHybrid()) return hybridClass;
        return getClass().getSimpleName();
    }

    // fighting methods

    public void basicAttack(Hero target) {
        int damage = Math.max(0, this.attack - target.getDefense());
        target.takeDamage(damage);
        System.out.println(name + " attacks " + target.getName() + " for " + damage + " damage!");
    }

    public void takeDamage(double amount) {
        if (shieldHp > 0) {
            double absorbed = Math.min(shieldHp, amount);
            shieldHp -= (int) absorbed;
            amount   -= absorbed;
        }
        this.hp = Math.max(0, this.hp - amount);
        System.out.println(name + " takes " + amount + " damage. HP remaining: " + this.hp);
    }

    public void defend() {
        hp   = Math.min(maxHp,   hp   + 10);
        mana = Math.min(maxMana, mana + 5);
        System.out.println(name + " defends. +10 HP, +5 mana.");
    }

    public boolean spendMana(int cost) {
        if (mana < cost) {
            System.out.println(name + " does not have enough mana!");
            return false;
        }
        mana -= cost;
        return true;
    }

    public void applyShield(int amount) { this.shieldHp += amount; }
    public void heal(double amount)     { this.hp = Math.min(maxHp, hp + amount); }
    public boolean isAlive()            { return hp > 0; }
    public void fullRestore()           { hp = maxHp; mana = maxMana; shieldHp = 0; }

    public abstract void specialAttack(Hero[] targets);

    // set and get attack strategies

    public void setAttackStrategy(AttackStrategy strategy) {
        this.attackStrategy = strategy;
    }

    public AttackStrategy getAttackStrategy() {
        return attackStrategy;
    }

    // observer pattern methods

    @Override
    public void attach(GameObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(GameObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (GameObserver observer : observers) {
            observer.onHeroLevelUp(name, level, getClassName());
        }
    }

    // basic getters and setters

    public String  getName()       { return name; }
    public int     getAttack()     { return attack; }
    public int     getPower()      { return attack; }  // Alias for consistency with Mob
    public int     getDefense()    { return defense; }
    public double  getHp()         { return hp; }
    public double  getMaxHp()      { return maxHp; }
    public int     getMana()       { return mana; }
    public int     getMaxMana()    { return maxMana; }
    public int     getLevel()      { return level; }
    public int     getExperience() { return experience; }
    public int     getShieldHp()   { return shieldHp; }
    public boolean isStunned()     { return stunned; }
    public boolean isWaiting()     { return waiting; }
    public void setStunned(boolean stunned) { this.stunned = stunned; }
    public void setWaiting(boolean waiting) { this.waiting = waiting; }
    public void setLevel(int level)         { this.level = level; }
    public int getPrimaryClassLevel()       { return primaryClassLevel; }
    public int getSecondaryClassLevel()     { return secondaryClassLevel; }
    public String getSecondaryClassName()   { return secondaryClassName; }
    public void setSecondaryClassName(String name) { this.secondaryClassName = name; }
    public void setPrimaryClassLevel(int lvl) { this.primaryClassLevel = lvl; }
    public void setSecondaryClassLevel(int lvl) { this.secondaryClassLevel = lvl; }
    public void setHybridClass(String hc)   { this.hybridClass = hc; }
    public void setExperience(int exp)      { this.experience = exp; }
    public void setMaxHp(double mhp)        { this.maxHp = mhp; }
    public void setMaxMana(int mm)          { this.maxMana = mm; }

    @Override
    public String toString() {
        return String.format("[%s | %s | Lv%d | HP: %.0f/%.0f | Mana: %d/%d | ATK: %d | DEF: %d]",
                name, getClassName(), level, hp, maxHp, mana, maxMana, attack, defense);
    }

    // Additional setters needed by GamePanel
    public void changeAttack(int attack)   { this.attack = attack; }
    public void changeDefense(int defense) { this.defense = defense; }
    public void changeHp(double hp)        { this.hp = Math.min(maxHp, hp); }
    public void changeMana(int mana)       { this.mana = Math.min(maxMana, mana); }
    public void restoreMana(int amount)    { this.mana = Math.min(maxMana, mana + amount); }
    public abstract String[] getSpells();
}