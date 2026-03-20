package Panels;

import Factory.HeroFactory;
import Hero.*;
import Mob.*;
import Singleton.DatabaseManager;
import State.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

public class GamePanel extends JPanel {

    private static final int TOTAL_ROOMS = 30;
    private static final Random random = new Random();

    // ── Game State ────────────────────────────────────────────
    private Hero       hero;
    private List<Hero> party;
    private int        gold;
    private int        currentRoom;
    private int        baseEncounterChance = 60;
    private List<Mob>  currentMobs;
    private boolean    inBattle;

    // ── Inventory ─────────────────────────────────────────────
    // Maps item name -> quantity
    private Map<String, Integer> inventory = new LinkedHashMap<>();

    // ── Turn tracking ─────────────────────────────────────────
    private Queue<Hero>    turnQueue;      // heroes still to choose their action
    private Hero           activeHero;    // hero currently choosing
    private List<Runnable> pendingActions; // queued hero actions to resolve together

    // ── State ─────────────────────────────────────────────────
    private RoomContext roomContext;

    // ── UI ────────────────────────────────────────────────────
    private JLabel    lblRoom, lblGold, lblHeroStats, lblTurn;
    private JTextArea logArea;
    private JButton   btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems;
    private JPanel    mobPanel;
    private final String[]   currentUser;
    private final JPanel     container;
    private final CardLayout cl;

    public GamePanel(JPanel container, CardLayout cl, String[] currentUser) {
        this.container   = container;
        this.cl          = cl;
        this.currentUser = currentUser;
        setLayout(new BorderLayout(5, 5));
        buildUI();
    }

    // ── Build UI ──────────────────────────────────────────────

    private void buildUI() {
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        lblHeroStats = new JLabel("", SwingConstants.LEFT);
        lblRoom      = new JLabel("", SwingConstants.CENTER);
        lblGold      = new JLabel("", SwingConstants.RIGHT);
        topPanel.add(lblHeroStats);
        topPanel.add(lblRoom);
        topPanel.add(lblGold);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        mobPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        mobPanel.setBorder(BorderFactory.createTitledBorder("Enemies"));
        mobPanel.setPreferredSize(new Dimension(0, 100));
        centerPanel.add(mobPanel, BorderLayout.NORTH);

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        centerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        lblTurn = new JLabel(" ", SwingConstants.CENTER);
        lblTurn.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTurn.setForeground(new Color(0, 100, 0));
        bottomPanel.add(lblTurn, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout());
        btnAttack   = new JButton("Attack");
        btnDefend   = new JButton("Defend");
        btnWait     = new JButton("Wait");
        btnCast     = new JButton("Cast Spell");
        btnNextRoom = new JButton("Next Room");
        btnUseItems = new JButton("Use Items");
        actionPanel.add(btnAttack);
        actionPanel.add(btnDefend);
        actionPanel.add(btnWait);
        actionPanel.add(btnCast);
        actionPanel.add(btnNextRoom);
        actionPanel.add(btnUseItems);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAttack.addActionListener(e   -> queueAttack());
        btnDefend.addActionListener(e   -> queueDefend());
        btnWait.addActionListener(e     -> queueWait());
        btnCast.addActionListener(e     -> queueCast());
        btnNextRoom.addActionListener(e -> enterNextRoom());
        btnUseItems.addActionListener(e -> useItems());

        // Initialise state machine
        roomContext = new RoomContext(btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems);
        roomContext.setState(new InnState()); // start disabled until game begins
    }

    // ── PvP State ─────────────────────────────────────────────
    private boolean isPvP = false;
    private String  pvpPlayer1Name;
    private String  pvpPlayer2Name;
    private List<Hero> pvpPlayer2Party;
    private java.util.function.BiConsumer<String, String> pvpResultCallback;

    // ── Init / Load ───────────────────────────────────────────

    /** Called from PvPPanel to launch a PvP battle */
    public void startPvPBattle(List<Hero> p1Party, String p1Name,
                               List<Hero> p2Party, String p2Name,
                               java.util.function.BiConsumer<String, String> onResult) {
        isPvP            = true;
        pvpPlayer1Name   = p1Name;
        pvpPlayer2Name   = p2Name;
        pvpPlayer2Party  = new ArrayList<>(p2Party);
        pvpResultCallback = onResult;

        party       = new ArrayList<>(p1Party);
        hero        = party.get(0);
        gold        = 0;
        currentRoom = 0;
        logArea.setText("");
        log("=== PvP Battle: " + p1Name + " vs " + p2Name + " ===");
        log(p1Name + "'s party: " + party.stream()
                .map(h -> h.getName() + " [" + h.getClassName() + " Lv" + h.getLevel() + "]")
                .collect(Collectors.joining(", ")));
        log(p2Name + "'s party: " + pvpPlayer2Party.stream()
                .map(h -> h.getName() + " [" + h.getClassName() + " Lv" + h.getLevel() + "]")
                .collect(Collectors.joining(", ")));

        // Spawn the opponent party as the "mobs" for this battle
        currentMobs = new ArrayList<>();
        for (Hero opponent : pvpPlayer2Party) {
            currentMobs.add(new PvPMob(opponent));
        }
        inBattle = true;
        roomContext.setState(new State.BattleState());
        lblRoom.setText("PvP Battle");
        refreshStats();
        refreshMobPanel();
        startNewRound();
    }

    public void startNewGame(List<Hero> startingParty) {
        party       = new ArrayList<>(startingParty);
        hero        = party.get(0);
        gold        = 0;
        currentRoom = 0;
        logArea.setText("");
        log("=== Game Started! Party: " + party.stream()
                .map(h -> h.getName() + " (" + h.getClassName() + ")")
                .collect(Collectors.joining(", ")) + " ===");
        enterNextRoom();
    }

    /** Load a full saved party */
    public void loadGame(List<Hero> savedParty, int savedGold, int savedRoom) {
        party       = new ArrayList<>(savedParty);
        hero        = party.get(0);
        gold        = savedGold;
        currentRoom = savedRoom;
        logArea.setText("");
        log("=== Save Loaded! Party: " + party.stream()
                .map(h -> h.getName() + " (" + h.getClassName() + ")")
                .collect(Collectors.joining(", ")) + " ===");
        enterNextRoom();
    }

    public void loadGame(String heroClass, String heroName, int level, double hp,
                         int attack, int defense, int mana, int goldAmt, int room) {
        hero = createHero(heroClass, heroName);
        party = new ArrayList<>();
        party.add(hero);
        hero.setLevel(level);
        hero.changeHp(hp);
        hero.changeAttack(attack);
        hero.changeDefense(defense);
        hero.changeMana(mana);
        this.gold        = goldAmt;
        this.currentRoom = room;
        logArea.setText("");
        log("=== Save Loaded! Room " + room + " ===");
        enterNextRoom();
    }

    private Hero createHero(String heroClass, String heroName) {
        return HeroFactory.getFactory(heroClass).createHero(heroName);
    }

    // ── Room Logic ────────────────────────────────────────────

    private void enterNextRoom() {
        currentRoom++;
        if (currentRoom > TOTAL_ROOMS) { endCampaign(); return; }

        currentMobs = new ArrayList<>();
        inBattle    = false;

        int cumLevel = party.stream().mapToInt(Hero::getLevel).sum();
        int shift    = (cumLevel / 10) * 3;
        int encounterChance = Math.min(90, baseEncounterChance + shift);

        lblRoom.setText("Room " + currentRoom + " / " + TOTAL_ROOMS);
        refreshStats();

        if (random.nextInt(100) < encounterChance) {
            spawnMobs();
            startBattle();
        } else {
            visitInn();
        }
    }

    private void spawnMobs() {
        int numMobs  = 1 + random.nextInt(3);
        int mobLevel = Math.max(1, hero.getLevel() * 2 + random.nextInt(3) - 1);
        for (int i = 0; i < numMobs; i++) {
            double mobHp  = 50  + mobLevel * 10;
            double power  = 3   + mobLevel * 2;
            int xp        = 50  * mobLevel;
            int g         = 75  * mobLevel;
            currentMobs.add(new NormalMob(mobHp, power, xp, g, 0.75));
        }
        log("--- " + numMobs + " enemy mob(s) (Lv" + mobLevel + ") appeared! ---");
    }

    private void startBattle() {
        inBattle = true;
        roomContext.setState(new BattleState());
        startNewRound();
    }

    // ── Round / Turn System ───────────────────────────────────

    /**
     * A round works like this:
     * 1. Each living hero picks an action (queued, no damage applied yet)
     * 2. Once all heroes have chosen, resolveRound() is called
     * 3. Hero actions are applied first, then mob actions
     * 4. Damage and effects are calculated and displayed
     */
    private void startNewRound() {
        turnQueue      = new LinkedList<>();
        pendingActions = new ArrayList<>();

        for (Hero h : party) {
            if (h.isAlive() && !h.isStunned()) turnQueue.add(h);
            else if (h.isStunned()) {
                h.setStunned(false);
                log(h.getName() + " recovers from stun.");
            }
        }
        log("--- New round. " + turnQueue.size() + " hero(es) to act. ---");
        promptNextHero();
    }

    /** Prompt the next hero in the queue to choose an action */
    private void promptNextHero() {
        if (turnQueue.isEmpty()) {
            // All heroes have chosen — resolve the round
            resolveRound();
            return;
        }
        activeHero = turnQueue.poll();
        lblTurn.setText("▶ " + activeHero.getName() + "'s turn (" + activeHero.getClassName() + ")");
        roomContext.setState(new BattleState());
        refreshStats();
    }

    // ── Queue Actions (no damage yet) ─────────────────────────

    private void queueAttack() {
        if (activeHero == null) return;
        final Hero attacker = activeHero;
        pendingActions.add(() -> {
            Mob target = pickLivingMob();
            if (target != null) {
                int damage = Math.max(0, attacker.getAttack() - (int)(target.getPower() * 0.5));
                target.takeDamage(damage);
                log(attacker.getName() + " attacks for " + damage + " damage!");
            }
        });
        log(activeHero.getName() + " chose: Attack");
        activeHero = null;
        promptNextHero();
    }

    private void queueDefend() {
        if (activeHero == null) return;
        final Hero defender = activeHero;
        pendingActions.add(() -> {
            defender.defend();
            log(defender.getName() + " defends. +10 HP, +5 Mana.");
        });
        log(activeHero.getName() + " chose: Defend");
        activeHero = null;
        promptNextHero();
    }

    private void queueWait() {
        if (activeHero == null) return;
        // Re-add to end of queue so they act last
        turnQueue.add(activeHero);
        log(activeHero.getName() + " chose: Wait (acts last)");
        // Prevent infinite loop — if only waiters remain, just move on
        boolean allWaiting = turnQueue.stream().allMatch(h -> pendingActions.isEmpty());
        if (turnQueue.size() == 1 && turnQueue.peek() == activeHero) {
            // Only this hero left and they chose wait — force them to act
            final Hero waiter = turnQueue.poll();
            pendingActions.add(() -> {
                Mob target = pickLivingMob();
                if (target != null) {
                    int damage = Math.max(0, waiter.getAttack() - (int)(target.getPower() * 0.5));
                    target.takeDamage(damage);
                    log(waiter.getName() + " (waited) attacks for " + damage + " damage!");
                }
            });
            activeHero = null;
            resolveRound();
            return;
        }
        activeHero = null;
        promptNextHero();
    }

    private void queueCast() {
        if (activeHero == null) return;
        String[] spells = getSpellOptions(activeHero);
        if (spells.length == 0) { log("No spells available!"); return; }

        String chosen = (String) JOptionPane.showInputDialog(
                this, "Choose a spell for " + activeHero.getName() + ":", "Cast Spell",
                JOptionPane.PLAIN_MESSAGE, null, spells, spells[0]);
        if (chosen == null) return;

        final Hero caster      = activeHero;
        final String spell     = chosen;
        final Hero[] partySnap = party.toArray(new Hero[0]);

        pendingActions.add(() -> {
            Mob[] mobArr = currentMobs.toArray(new Mob[0]);
            castSpell(caster, spell, partySnap, mobArr);
        });
        log(activeHero.getName() + " chose: " + spell);
        activeHero = null;
        promptNextHero();
    }

    // ── Resolve Round ─────────────────────────────────────────

    /**
     * All heroes have chosen. Now apply:
     * 1. Hero actions (in order chosen)
     * 2. Mob actions
     * Then check for battle end.
     */
    private void resolveRound() {
        roomContext.setState(new ResolvingState());
        lblTurn.setText("Resolving round...");

        log("=== Heroes act ===");
        for (Runnable action : pendingActions) {
            action.run();
        }
        refreshStats();
        refreshMobPanel();

        if (checkBattleEnd()) return;

        log("=== Enemies act ===");
        mobsTurn();
    }

    private void castSpell(Hero caster, String spell, Hero[] partyArr, Mob[] mobs) {
        switch (spell) {
            case "Protect": ((Order) caster).protect(partyArr); break;
            case "Heal":    ((Order) caster).heal(partyArr);    break;
            case "Fireball":
                if (!caster.spendMana(30)) return;
                log(caster.getName() + " launches Fireball!");
                for (int i = 0; i < Math.min(3, mobs.length); i++) {
                    if (mobs[i].isAlive()) {
                        int dmg = Math.max(0, caster.getAttack() - (int)(mobs[i].getPower() * 0.3));
                        mobs[i].takeDamage(dmg);
                    }
                }
                break;
            case "Chain Lightning":
                if (!caster.spendMana(40)) return;
                log(caster.getName() + " casts Chain Lightning!");
                double dmg = caster.getAttack();
                for (Mob m : mobs) { if (m.isAlive()) { m.takeDamage(dmg); dmg *= 0.25; } }
                break;
            case "Berserker Attack":
                if (!caster.spendMana(60)) return;
                log(caster.getName() + " goes Berserk!");
                if (mobs.length > 0 && mobs[0].isAlive()) {
                    int primary = Math.max(0, caster.getAttack() - (int)(mobs[0].getPower() * 0.3));
                    mobs[0].takeDamage(primary);
                    int splash = (int)(primary * 0.25);
                    for (int i = 1; i < Math.min(3, mobs.length); i++)
                        if (mobs[i].isAlive()) mobs[i].takeDamage(splash);
                }
                break;
            case "Replenish": ((Mage) caster).replenish(partyArr); break;
        }
        refreshMobPanel();
    }

    private String[] getSpellOptions(Hero h) {
        switch (h.getClass().getSimpleName().toUpperCase()) {
            case "ORDER":   return new String[]{"Protect", "Heal"};
            case "CHAOS":   return new String[]{"Fireball", "Chain Lightning"};
            case "WARRIOR": return new String[]{"Berserker Attack"};
            case "MAGE":    return new String[]{"Replenish"};
            default:        return new String[]{};
        }
    }

    private void mobsTurn() {
        List<Hero> living = new ArrayList<>();
        for (Hero h : party) { if (h.isAlive()) living.add(h); }
        if (living.isEmpty()) { playerDied(); return; }

        for (Mob mob : currentMobs) {
            if (mob.isAlive()) {
                int action = random.nextInt(3);
                if (action == 0) {
                    Hero target = living.get(random.nextInt(living.size()));
                    mob.attack(target);
                    log("Enemy attacks " + target.getName() + "!");
                } else if (action == 1) {
                    log("Enemy defends.");
                } else {
                    log("Enemy waits.");
                }
            }
        }
        refreshStats();
        refreshMobPanel();

        if (party.stream().noneMatch(Hero::isAlive)) { playerDied(); return; }

        // Start the next round
        if (inBattle) startNewRound();
    }

    // ── Battle Resolution ─────────────────────────────────────

    private boolean checkBattleEnd() {
        if (currentMobs.stream().anyMatch(Mob::isAlive)) return false;

        inBattle       = false;
        turnQueue      = null;
        activeHero     = null;
        pendingActions = null;
        lblTurn.setText(" ");

        int totalXp   = currentMobs.stream().mapToInt(Mob::getXpReward).sum();
        int totalGold = currentMobs.stream().mapToInt(Mob::getGoldReward).sum();
        gold += totalGold;

        List<Hero> survivors = new ArrayList<>();
        for (Hero h : party) { if (h.isAlive()) survivors.add(h); }
        int xpEach = survivors.isEmpty() ? 0 : totalXp / survivors.size();

        log("--- Victory! +" + totalXp + " XP (each survivor gets " + xpEach + "), +" + totalGold + " Gold ---");
        for (Hero h : survivors) {
            int before = h.getLevel();
            h.gainExperience(xpEach);
            if (h.getLevel() > before) log(h.getName() + " levelled up to Lv" + h.getLevel() + "!");
            if (h.getLevel() < 20)
                log(h.getName() + ": " + h.getExperience() + " XP | "
                        + (h.expNeededForLevel(h.getLevel() + 1) - h.getExperience()) + " to next level");
        }

        refreshStats();
        refreshMobPanel();
        roomContext.setState(new VictoryState());

        if (isPvP) {
            isPvP = false;
            log("=== " + pvpPlayer1Name + " wins the PvP battle! ===");
            if (pvpResultCallback != null)
                pvpResultCallback.accept(pvpPlayer1Name, pvpPlayer2Name);
            return true;
        }

        saveProgress();
        return true;
    }

    private void playerDied() {
        inBattle       = false;
        turnQueue      = null;
        activeHero     = null;
        pendingActions = null;
        lblTurn.setText(" ");

        if (isPvP) {
            isPvP = false;
            log("=== " + pvpPlayer2Name + " wins the PvP battle! ===");
            if (pvpResultCallback != null)
                pvpResultCallback.accept(pvpPlayer2Name, pvpPlayer1Name);
            roomContext.setState(new DefeatedState());
            return;
        }

        int lostGold = (int)(gold * 0.10);
        gold -= lostGold;
        log("--- Defeated! Lost " + lostGold + " gold. ---");
        for (Hero h : party) h.fullRestore();
        refreshStats();
        roomContext.setState(new DefeatedState());
        JOptionPane.showMessageDialog(this, "Defeated! Lost " + lostGold + " gold.", "Defeated", JOptionPane.WARNING_MESSAGE);
    }

    // ── Inn ───────────────────────────────────────────────────

    private void visitInn() {
        inBattle = false;
        for (Hero h : party) h.fullRestore();
        log("--- You found an Inn! All party members fully restored. ---");
        refreshStats();
        if (currentRoom <= 10 && party.size() < 5) offerRecruitment();
        showInnShop();
        roomContext.setState(new InnState());
    }

    private static final String[] HERO_CLASSES = {"Warrior", "Mage", "Order", "Chaos"};

    private void offerRecruitment() {
        String recruitClass = HERO_CLASSES[random.nextInt(HERO_CLASSES.length)];
        int recruitLevel    = 1 + random.nextInt(4);
        int cost            = recruitLevel == 1 ? 0 : 200 * recruitLevel;
        String heroName     = recruitClass + "-" + (party.size() + 1);

        String msg = "<html>A wandering <b>" + recruitClass + "</b> (Lv" + recruitLevel + ") is looking for work!<br>"
                + (cost == 0 ? "They will join for FREE!" : "Hiring cost: <b>" + cost + "g</b>") + "</html>";

        if (JOptionPane.showConfirmDialog(this, msg, "Recruit Hero?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (gold < cost) { JOptionPane.showMessageDialog(this, "Not enough gold!", "Recruit", JOptionPane.WARNING_MESSAGE); return; }

        gold -= cost;
        Hero recruit = createHero(recruitClass, heroName);
        for (int i = 1; i < recruitLevel; i++) recruit.gainExperience(recruit.expNeededForLevel(i + 1));
        party.add(recruit);
        log("--- " + heroName + " the " + recruitClass + " (Lv" + recruit.getLevel() + ") joined! ---");
        refreshStats();
    }

    // Item definitions: name -> [cost, hp, mana, fullRestore]
    private static final Object[][] ITEM_DEFS = {
            {"Bread",  200,  20,   0,  false},
            {"Cheese", 500,  50,   0,  false},
            {"Steak",  1000, 200,  0,  false},
            {"Water",  150,  0,    10, false},
            {"Juice",  400,  0,    30, false},
            {"Wine",   750,  0,    100,false},
            {"Elixir", 2000, 0,    0,  true },
    };

    private void showInnShop() {
        while (true) {
            // Build shop display with costs and current stock
            String[] options = new String[ITEM_DEFS.length + 1];
            for (int i = 0; i < ITEM_DEFS.length; i++) {
                String name = (String) ITEM_DEFS[i][0];
                int cost    = (int)    ITEM_DEFS[i][1];
                int hp      = (int)    ITEM_DEFS[i][2];
                int mana    = (int)    ITEM_DEFS[i][3];
                boolean full= (boolean)ITEM_DEFS[i][4];
                int qty     = inventory.getOrDefault(name, 0);
                String effect = full ? "Full restore" : (hp > 0 ? "+" + hp + " HP" : "") + (mana > 0 ? " +" + mana + " MP" : "");
                options[i] = name + " - " + cost + "g (" + effect + ") [owned: " + qty + "]";
            }
            options[ITEM_DEFS.length] = "Leave Shop";

            String pick = (String) JOptionPane.showInputDialog(this,
                    "Gold: " + gold + "  |  Inventory: " + inventorySummary(),
                    "Inn Shop", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (pick == null || pick.startsWith("Leave")) break;

            // Find which item was picked
            for (Object[] def : ITEM_DEFS) {
                if (pick.startsWith((String) def[0])) {
                    int cost = (int) def[1];
                    if (gold < cost) {
                        JOptionPane.showMessageDialog(this, "Not enough gold!", "Shop", JOptionPane.WARNING_MESSAGE);
                    } else {
                        gold -= cost;
                        String name = (String) def[0];
                        inventory.put(name, inventory.getOrDefault(name, 0) + 1);
                        log("Bought " + name + " for " + cost + "g. [" + name + " x" + inventory.get(name) + "]");
                        refreshStats();
                    }
                    break;
                }
            }
        }
        refreshStats();
    }

    /** Use items from inventory between battles */
    public void useItems() {
        if (inBattle) {
            JOptionPane.showMessageDialog(this, "Can't use items during battle!", "Items", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (inventory.isEmpty() || inventory.values().stream().allMatch(q -> q == 0)) {
            JOptionPane.showMessageDialog(this, "Your inventory is empty!", "Items", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        while (true) {
            // Build inventory list
            List<String> available = new ArrayList<>();
            for (Map.Entry<String, Integer> e : inventory.entrySet()) {
                if (e.getValue() > 0) available.add(e.getKey() + " x" + e.getValue());
            }
            if (available.isEmpty()) break;
            available.add("Close");

            String pick = (String) JOptionPane.showInputDialog(this,
                    "Select item to use:", "Inventory",
                    JOptionPane.PLAIN_MESSAGE, null, available.toArray(), available.get(0));
            if (pick == null || pick.equals("Close")) break;

            String itemName = pick.split(" x")[0];

            // Pick target hero
            String[] heroNames = party.stream().map(Hero::getName).toArray(String[]::new);
            String targetName = (String) JOptionPane.showInputDialog(this,
                    "Use " + itemName + " on which hero?", "Use Item",
                    JOptionPane.PLAIN_MESSAGE, null, heroNames, heroNames[0]);
            if (targetName == null) continue;

            Hero target = party.stream().filter(h -> h.getName().equals(targetName)).findFirst().orElse(hero);

            // Apply effect
            for (Object[] def : ITEM_DEFS) {
                if (def[0].equals(itemName)) {
                    int hp      = (int)    def[2];
                    int mana    = (int)    def[3];
                    boolean full= (boolean)def[4];
                    if (full)       target.fullRestore();
                    else {
                        if (hp   > 0) target.heal(hp);
                        if (mana > 0) target.restoreMana(mana);
                    }
                    inventory.put(itemName, inventory.get(itemName) - 1);
                    log(target.getName() + " used " + itemName + "!");
                    refreshStats();
                    break;
                }
            }
        }
    }

    private String inventorySummary() {
        if (inventory.isEmpty()) return "empty";
        return inventory.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> e.getKey() + "x" + e.getValue())
                .collect(Collectors.joining(", "));
    }

    private void buyItem(int cost, Runnable effect) {
        if (gold >= cost) { gold -= cost; effect.run(); log("Purchased item for " + cost + "g."); refreshStats(); }
        else JOptionPane.showMessageDialog(this, "Not enough gold!", "Shop", JOptionPane.WARNING_MESSAGE);
    }

    // ── End Campaign ──────────────────────────────────────────

    private void endCampaign() {
        int totalLevels = party.stream().mapToInt(Hero::getLevel).sum();
        int score = totalLevels * 100 + gold * 10;
        log("=== Campaign Complete! Final Score: " + score + " ===");
        roomContext.setState(new ResolvingState());
        DatabaseManager.getInstance().saveScore(currentUser[0], score);
        JOptionPane.showMessageDialog(this,
                "Campaign complete!\nFinal Score: " + score + "\nGold: " + gold
                        + "\nParty size: " + party.size() + "\nTotal levels: " + totalLevels,
                "Game Over", JOptionPane.INFORMATION_MESSAGE);
        cl.show(container, "Menu");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void saveProgress() {
        DatabaseManager.getInstance().saveParty(currentUser[0], party, gold, currentRoom);
    }

    private Mob pickLivingMob() {
        return currentMobs.stream().filter(Mob::isAlive).findFirst().orElse(null);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void refreshStats() {
        if (party == null) return;
        StringBuilder sb = new StringBuilder("<html>");
        for (Hero h : party) {
            String color = h == activeHero ? "green" : "black";
            sb.append("<font color='").append(color).append("'>")
                    .append(h.getName()).append(" [").append(h.getClassName()).append(" Lv").append(h.getLevel()).append("] ")
                    .append("HP:").append((int)h.getHp()).append("/").append((int)h.getMaxHp())
                    .append(" MP:").append(h.getMana()).append("/").append(h.getMaxMana());
            if (!h.isAlive()) sb.append(" <b>DEAD</b>");
            sb.append("</font><br>");
        }
        sb.append("</html>");
        lblHeroStats.setText(sb.toString());
        lblGold.setText("Gold: " + gold);
    }

    private void refreshMobPanel() {
        mobPanel.removeAll();
        for (Mob mob : currentMobs) {
            String label = (mob instanceof PvPMob)
                    ? ((PvPMob) mob).getHero().getName() + " [" + ((PvPMob) mob).getHero().getClassName() + "]"
                    : "Enemy";
            String txt = mob.isAlive()
                    ? "<html>" + label + "<br>HP: " + (int)mob.getHp() + "</html>"
                    : "<html><strike>" + label + "</strike><br>Defeated</html>";
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createLineBorder(mob.isAlive() ? Color.RED : Color.GRAY));
            lbl.setPreferredSize(new Dimension(100, 60));
            mobPanel.add(lbl);
        }
        mobPanel.revalidate();
        mobPanel.repaint();
    }

    // button state is now managed by RoomContext / State pattern
}