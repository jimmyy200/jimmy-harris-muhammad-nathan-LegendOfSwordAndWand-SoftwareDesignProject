package Hero;

public abstract class Hero {
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
    protected String hybridClass = null;  // e.g. "WARLOCK", "PALADIN", etc.
    protected int primaryClassLevel   = 1;
    protected int secondaryClassLevel = 0;

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
    }

    // ── Levelling ─────────────────────────────────────────────

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
        attack  += 1;
        defense += 1;
        maxHp   += 5;
        maxMana += 2;
        applyLevelUpBonus();
        hp   = maxHp;
        mana = maxMana;
        System.out.println(name + " reached level " + level + "! [" + getClassName() + "]");
    }

    /** Subclass applies its class bonus. If hybrid, combines both class growths. */
    protected abstract void applyLevelUpBonus();

    /**
     * Called when player chooses to level up the secondary class.
     * Once both primary and secondary hit level 5, hybrid is triggered.
     */
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

    /** Subclass sets hybridClass name when conditions are met */
    protected abstract void triggerHybrid();

    public boolean isHybrid()         { return hybridClass != null; }
    public boolean isSpecialized()    { return primaryClassLevel >= 5 && !isHybrid(); }
    public String  getHybridClass()   { return hybridClass; }

    public String getClassName() {
        if (isHybrid()) return hybridClass;
        return getClass().getSimpleName();
    }

    // ── Combat ────────────────────────────────────────────────

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
    public void fullRestore()           { hp = maxHp; mana = maxMana; }

    public abstract void specialAttack(Hero[] targets);

    // ── Getters / Setters ─────────────────────────────────────

    public String  getName()       { return name; }
    public int     getAttack()     { return attack; }
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
}