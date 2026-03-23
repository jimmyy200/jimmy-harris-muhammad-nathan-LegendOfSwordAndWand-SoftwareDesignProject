package Panels;

import Factory.HeroFactory;
import Hero.*;
import Mob.*;
import Observer.GameObserver;
import Observer.GameSubject;
import Singleton.DatabaseManager;
import State.*;
import Strategy.*;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

// main game screen
public class GamePanel extends JPanel implements GameObserver {

    private static final int TOTAL_ROOMS = 30;
    private static final Random random = new Random();

    // game state variables
    private List<Hero> party;
    private int        gold;
    private int        currentRoom;
    private List<Mob>  currentMobs;
    private boolean    inBattle;

    // inventory variables
    private Map<String, Integer> inventory = new LinkedHashMap<>();

    // keeping track of turns
    private Queue<Hero>    turnQueue;
    private Hero           activeHero;
    private List<Runnable> pendingActions;
    private List<Hero>     waitQueue; // line for waiting heroes

    // state design pattern
    private RoomContext roomContext;

    // observer pattern setup
    private GameSubject gameSubject;

    // pvp state
    private boolean isPvP = false;
    private String  pvpPlayer1Name;
    private String  pvpPlayer2Name;
    private List<Hero> pvpPlayer2Party;
    private java.util.function.BiConsumer<String, String> pvpResultCallback;

    // ui elements
    private JLabel    lblRoom, lblGold, lblHeroStats, lblTurn, lblQueue;
    private JTextArea logArea;
    private JButton   btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems, btnParty, btnExit;
    private JPanel    mobPanel;
    private final String[]   currentUser;
    private final JPanel     container;
    private final CardLayout cl;

    public GamePanel(JPanel container, CardLayout cl, String[] currentUser) {
        this.container   = container;
        this.cl          = cl;
        this.currentUser = currentUser;
        this.gameSubject = new GameSubject();
        this.gameSubject.addObserver(this);
        setLayout(new BorderLayout(5, 5));
        buildUI();
    }

    // callbacks for observer

    @Override
    public void onHeroLevelUp(String heroName, int newLevel, String className) {
        log(heroName + " levelled up to Lv" + newLevel + "! [" + className + "]");
    }

    @Override
    public void onHeroDefeated(String heroName) {
        log(heroName + " has been defeated!");
    }

    @Override
    public void onGoldChanged(int newGold) {
        lblGold.setText("Gold: " + newGold);
    }

    @Override
    public void onExperienceGained(String heroName, int xpGained) {
        log(heroName + " gained " + xpGained + " XP.");
    }

    // building the screen

    private void buildUI() {
        // stats room and gold
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        lblHeroStats = new JLabel("", SwingConstants.LEFT);
        lblRoom      = new JLabel("", SwingConstants.CENTER);
        lblGold      = new JLabel("", SwingConstants.RIGHT);
        topPanel.add(lblHeroStats);
        topPanel.add(lblRoom);
        topPanel.add(lblGold);
        add(topPanel, BorderLayout.NORTH);

        // enemies and text
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        mobPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        mobPanel.setBorder(BorderFactory.createTitledBorder("Enemies"));
        mobPanel.setPreferredSize(new Dimension(0, 110));
        centerPanel.add(mobPanel, BorderLayout.NORTH);

        logArea = new JTextArea(8, 30);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        centerPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // buttons and stuff
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel turnPanel = new JPanel(new GridLayout(2, 1));
        lblTurn  = new JLabel(" ", SwingConstants.CENTER);
        lblTurn.setFont(new Font("SansSerif", Font.BOLD, 13));
        lblTurn.setForeground(new Color(0, 100, 0));
        lblQueue = new JLabel(" ", SwingConstants.CENTER);
        lblQueue.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblQueue.setForeground(Color.DARK_GRAY);
        turnPanel.add(lblTurn);
        turnPanel.add(lblQueue);
        bottomPanel.add(turnPanel, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout());
        btnAttack   = new JButton("Attack");
        btnDefend   = new JButton("Defend");
        btnWait     = new JButton("Wait");
        btnCast     = new JButton("Cast Spell");
        btnNextRoom = new JButton("Next Room");
        btnUseItems = new JButton("Use Items");
        btnParty    = new JButton("Party / Inventory");
        btnExit     = new JButton("Exit Campaign");
        actionPanel.add(btnAttack);
        actionPanel.add(btnDefend);
        actionPanel.add(btnWait);
        actionPanel.add(btnCast);
        actionPanel.add(btnNextRoom);
        actionPanel.add(btnUseItems);
        actionPanel.add(btnParty);
        actionPanel.add(btnExit);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        btnAttack.addActionListener(e   -> queueAttack());
        btnDefend.addActionListener(e   -> queueDefend());
        btnWait.addActionListener(e     -> queueWait());
        btnCast.addActionListener(e     -> queueCast());
        btnNextRoom.addActionListener(e -> enterNextRoom());
        btnUseItems.addActionListener(e -> useItems());
        btnParty.addActionListener(e    -> showPartyView());
        btnExit.addActionListener(e     -> exitCampaign());

        roomContext = new RoomContext(btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems, btnParty, btnExit);
        roomContext.setState(new InnState());
    }

    // initial setup and loading

    public void startPvPBattle(List<Hero> p1Party, String p1Name,
                               List<Hero> p2Party, String p2Name,
                               java.util.function.BiConsumer<String, String> onResult) {
        isPvP             = true;
        pvpPlayer1Name    = p1Name;
        pvpPlayer2Name    = p2Name;
        pvpPlayer2Party   = new ArrayList<>(p2Party);
        pvpResultCallback = onResult;

        party       = new ArrayList<>(p1Party);
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

        currentMobs = new ArrayList<>();
        for (Hero opponent : pvpPlayer2Party) currentMobs.add(new PvPMob(opponent));
        inBattle = true;
        roomContext.setState(new BattleState());
        lblRoom.setText("PvP Battle");
        refreshStats();
        refreshMobPanel();
        startNewRound();
    }

    public void startNewGame(List<Hero> startingParty) {
        isPvP       = false;
        party       = new ArrayList<>(startingParty);
        gold        = 0;
        currentRoom = 0;
        inventory   = new LinkedHashMap<>();
        logArea.setText("");
        log("=== Game Started! Party: " + party.stream()
                .map(h -> h.getName() + " (" + h.getClassName() + ")")
                .collect(Collectors.joining(", ")) + " ===");
        enterNextRoom();
    }

    public void loadGame(List<Hero> savedParty, int savedGold, int savedRoom) {
        isPvP       = false;
        party       = new ArrayList<>(savedParty);
        gold        = savedGold;
        currentRoom = savedRoom;
        // load inventory from DB
        inventory = DatabaseManager.getInstance().loadInventory(currentUser[0]);
        logArea.setText("");
        log("=== Save Loaded! Party: " + party.stream()
                .map(h -> h.getName() + " (" + h.getClassName() + ")")
                .collect(Collectors.joining(", ")) + " ===");
        enterNextRoom();
    }

    public void loadGame(String heroClass, String heroName, int level, double hp,
                         int attack, int defense, int mana, int goldAmt, int room) {
        Hero hero = HeroFactory.getFactory(heroClass).createHero(heroName);
        party = new ArrayList<>();
        party.add(hero);
        hero.setLevel(level);
        hero.changeHp(hp);
        hero.changeAttack(attack);
        hero.changeDefense(defense);
        hero.changeMana(mana);
        this.gold        = goldAmt;
        this.currentRoom = room;
        inventory        = new LinkedHashMap<>();
        logArea.setText("");
        log("=== Save Loaded! Room " + room + " ===");
        enterNextRoom();
    }

    // room mechanics

    private void enterNextRoom() {
        currentRoom++;
        if (currentRoom > TOTAL_ROOMS) { endCampaign(); return; }

        currentMobs = new ArrayList<>();
        inBattle    = false;

        // encounter probability per spec: 60% base, +3% per 10 cumulative levels
        int cumLevel = party.stream().mapToInt(Hero::getLevel).sum();
        int shift    = (cumLevel / 10) * 3;
        int encounterChance = Math.min(90, 60 + shift);

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
        int cumLevel = party.stream().mapToInt(Hero::getLevel).sum();
        // 1-5 enemy units per spec
        int numMobs = 1 + random.nextInt(5);
        // enemy cumulative level scales with player cumulative level
        int maxEnemyCumLevel = Math.max(numMobs, cumLevel);
        int minEnemyCumLevel = Math.max(numMobs, cumLevel - 10);
        int enemyCumLevel = minEnemyCumLevel + random.nextInt(Math.max(1, maxEnemyCumLevel - minEnemyCumLevel + 1));

        // distribute levels among mobs
        int[] mobLevels = new int[numMobs];
        Arrays.fill(mobLevels, 1);
        int remaining = enemyCumLevel - numMobs;
        for (int i = 0; i < remaining; i++) {
            mobLevels[random.nextInt(numMobs)]++;
        }
        // cap at level 10
        for (int i = 0; i < numMobs; i++) {
            mobLevels[i] = Math.min(10, mobLevels[i]);
        }

        for (int i = 0; i < numMobs; i++) {
            int lvl = mobLevels[i];
            // scale stats based on level
            double mobHp  = 80 + lvl * 15;
            int power     = 4 + lvl * 2;
            int mobDef    = 2 + lvl;
            int xp        = 50 * lvl;
            int g         = 75 * lvl;
            currentMobs.add(new NormalMob(mobHp, power, mobDef, lvl, xp, g));
        }
        log("--- " + numMobs + " enemy mob(s) appeared! ---");
        for (Mob m : currentMobs) {
            NormalMob nm = (NormalMob) m;
            log("  Enemy Lv" + nm.getLevel() + " | HP:" + (int)nm.getHp()
                    + " ATK:" + (int)nm.getPower() + " DEF:" + nm.getDefense());
        }
    }

    private void startBattle() {
        inBattle = true;
        roomContext.setState(new BattleState());
        refreshMobPanel();
        startNewRound();
    }

    // turn based mechanics

    private void startNewRound() {
        pendingActions = new ArrayList<>();
        waitQueue      = new ArrayList<>();

        // build turn order: sort heroes by level desc, then attack desc
        List<Hero> sortedHeroes = party.stream()
                .filter(h -> h.isAlive() && !h.isStunned())
                .sorted((a, b) -> {
                    if (b.getLevel() != a.getLevel()) return b.getLevel() - a.getLevel();
                    return b.getAttack() - a.getAttack();
                })
                .collect(Collectors.toList());

        // handle stunned heroes
        for (Hero h : party) {
            if (h.isStunned()) {
                h.setStunned(false);
                log(h.getName() + " recovers from stun.");
            }
        }

        turnQueue = new LinkedList<>(sortedHeroes);

        log("--- New round. Turn order: " + sortedHeroes.stream()
                .map(Hero::getName).collect(Collectors.joining(" -> ")) + " ---");
        promptNextHero();
    }

    private void promptNextHero() {
        if (turnQueue.isEmpty()) {
            // process waiting heroes in FIFO order
            if (!waitQueue.isEmpty()) {
                Hero waiter = waitQueue.remove(0);
                activeHero = waiter;
                lblTurn.setText("▶ " + waiter.getName() + "'s turn (waited)");
                lblQueue.setText("Remaining waiters: " + (waitQueue.isEmpty() ? "none" :
                        waitQueue.stream().map(Hero::getName).collect(Collectors.joining(" -> "))));
                roomContext.setState(new BattleState());
                refreshStats();
                return;
            }
            resolveRound();
            return;
        }
        activeHero = turnQueue.poll();
        lblTurn.setText("▶ " + activeHero.getName() + "'s turn (" + activeHero.getClassName() + ")");
        String remaining = turnQueue.isEmpty() ? "none"
                : turnQueue.stream().map(Hero::getName).collect(Collectors.joining(" -> "));
        lblQueue.setText("Up next: " + remaining);
        roomContext.setState(new BattleState());
        refreshStats();
    }

    // queued combat actions

    private void queueAttack() {
        if (activeHero == null) return;
        final Hero attacker = activeHero;

        // let player pick a target
        List<String> targets = new ArrayList<>();
        for (int i = 0; i < currentMobs.size(); i++) {
            Mob m = currentMobs.get(i);
            if (m.isAlive()) {
                if (m instanceof PvPMob) {
                    targets.add(((PvPMob) m).getHero().getName());
                } else {
                    targets.add("Enemy " + (i + 1) + " (Lv" + m.getLevel() + ")");
                }
            }
        }
        if (targets.isEmpty()) return;

        String chosen = null;
        if (targets.size() == 1) {
            chosen = targets.get(0);
        } else {
            chosen = (String) JOptionPane.showInputDialog(
                    this, "Choose target for " + attacker.getName() + ":", "Attack Target",
                    JOptionPane.PLAIN_MESSAGE, null, targets.toArray(), targets.get(0));
        }
        if (chosen == null) return;

        final int targetIdx = targets.indexOf(chosen);
        final List<Mob> livingMobs = currentMobs.stream().filter(Mob::isAlive).collect(Collectors.toList());

        pendingActions.add(() -> {
            if (targetIdx >= 0 && targetIdx < livingMobs.size()) {
                Mob target = livingMobs.get(targetIdx);
                if (target.isAlive()) {
                    // damage math
                    int damage = Math.max(0, attacker.getAttack() - target.getDefense());
                    target.takeDamage(damage);
                    log(attacker.getName() + " attacks for " + damage + " damage!");

                    // extra hit chance for rogue
                    if (attacker instanceof Warrior && attacker.isHybrid()
                            && "ROGUE".equals(attacker.getHybridClass())) {
                        if (random.nextBoolean()) {
                            List<Mob> alive = currentMobs.stream().filter(Mob::isAlive).collect(Collectors.toList());
                            if (!alive.isEmpty()) {
                                Mob bonusTarget = alive.get(random.nextInt(alive.size()));
                                int bonusDmg = (int)(Math.max(0, attacker.getAttack() - bonusTarget.getDefense()) * 0.5);
                                bonusTarget.takeDamage(bonusDmg);
                                log("Sneak Attack! Bonus hit for " + bonusDmg + "!");
                            }
                        }
                    }

                    // mana burn for warlock
                    if (attacker instanceof Mage && attacker.isHybrid()
                            && "WARLOCK".equals(attacker.getHybridClass()) && target instanceof PvPMob) {
                        ((Mage) attacker).manaBurn(((PvPMob) target).getHero());
                    }
                }
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
        log(activeHero.getName() + " chose: Wait (acts at end of round)");
        waitQueue.add(activeHero);
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

        // check mana before committing
        int cost = getSpellCost(chosen, activeHero);
        if (activeHero.getMana() < cost) {
            JOptionPane.showMessageDialog(this, "Not enough mana! Need " + cost + ", have " + activeHero.getMana(),
                    "Cast Spell", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final Hero caster      = activeHero;
        final String spell     = chosen;
        final Hero[] partySnap = party.toArray(new Hero[0]);
        pendingActions.add(() -> castSpell(caster, spell, partySnap, currentMobs.toArray(new Mob[0])));
        log(activeHero.getName() + " chose: " + spell);
        activeHero = null;
        promptNextHero();
    }

    // resolving the round

    private void resolveRound() {
        roomContext.setState(new ResolvingState());
        lblTurn.setText("Resolving round...");
        lblQueue.setText(" ");

        log("=== Heroes act ===");
        for (Runnable action : pendingActions) action.run();
        refreshStats();
        refreshMobPanel();
        if (checkBattleEnd()) return;

        log("=== Enemies act ===");
        mobsTurn();
    }

    private void castSpell(Hero caster, String spell, Hero[] partyArr, Mob[] mobs) {
        switch (spell) {
            case "Protect":
                ((Order) caster).protect(partyArr);
                break;
            case "Fire Shield":
                ((Order) caster).fireShield(partyArr);
                break;
            case "Heal":
                ((Order) caster).heal(partyArr);
                break;
            case "Fireball":
                ((Chaos) caster).fireball(getMobsAsHeroArray(mobs));
                break;
            case "Chain Lightning":
                ((Chaos) caster).chainLightning(getMobsAsHeroArray(mobs));
                break;
            case "Berserker Attack":
                ((Warrior) caster).berserkerAttack(getMobsAsHeroArray(mobs));
                break;
            case "Replenish":
                ((Mage) caster).replenish(partyArr);
                break;
        }
        refreshMobPanel();
    }

    // Mobs don't extend Hero, so for spells that target Hero[], we apply damage directly
    private Hero[] getMobsAsHeroArray(Mob[] mobs) {
        // for PvP mobs we can get the hero reference
        // for normal mobs we apply damage separately
        List<Hero> targets = new ArrayList<>();
        for (Mob m : mobs) {
            if (m instanceof PvPMob && m.isAlive()) {
                targets.add(((PvPMob) m).getHero());
            }
        }
        if (!targets.isEmpty()) return targets.toArray(new Hero[0]);

        // for normal mobs, create temp wrapper — spells that directly damage mobs are handled separately
        return new Hero[0];
    }

    private String[] getSpellOptions(Hero h) {
        String className = h.getClass().getSimpleName().toUpperCase();
        if (className.equals("ORDER")) {
            if (h.isHybrid() && "HERETIC".equals(h.getHybridClass())) {
                return new String[]{"Fire Shield", "Heal"};
            }
            return new String[]{"Protect", "Heal"};
        }
        if (className.equals("CHAOS")) {
            return new String[]{"Fireball", "Chain Lightning"};
        }
        if (className.equals("WARRIOR")) {
            return new String[]{"Berserker Attack"};
        }
        if (className.equals("MAGE")) {
            return new String[]{"Replenish"};
        }
        return new String[]{};
    }

    private int getSpellCost(String spell, Hero caster) {
        switch (spell) {
            case "Protect": case "Fire Shield": return 25;
            case "Heal":              return 35;
            case "Fireball":          return 30;
            case "Chain Lightning":   return 40;
            case "Berserker Attack":  return 60;
            case "Replenish":
                if (caster.isHybrid() && "WIZARD".equals(caster.getHybridClass())) return 40;
                return 80;
            default: return 0;
        }
    }

    private void mobsTurn() {
        List<Hero> living = party.stream().filter(Hero::isAlive).collect(Collectors.toList());
        if (living.isEmpty()) { playerDied(); return; }

        for (Mob mob : currentMobs) {
            if (!mob.isAlive()) continue;
            int action = random.nextInt(3); // 0=attack, 1=defend, 2=wait
            if (action == 0) {
                Hero target = living.get(random.nextInt(living.size()));
                mob.attack(target);
                if (!target.isAlive()) {
                    gameSubject.notifyHeroDefeated(target.getName());
                    living.remove(target);
                    if (living.isEmpty()) break;
                }
            } else if (action == 1) {
                log("Enemy defends.");
            } else {
                log("Enemy waits.");
            }
        }
        refreshStats();
        refreshMobPanel();
        if (party.stream().noneMatch(Hero::isAlive)) { playerDied(); return; }
        if (inBattle) startNewRound();
    }

    // battle ending logic

    private boolean checkBattleEnd() {
        if (currentMobs.stream().anyMatch(Mob::isAlive)) return false;

        inBattle       = false;
        turnQueue      = null;
        activeHero     = null;
        pendingActions = null;
        waitQueue      = null;
        lblTurn.setText(" ");
        lblQueue.setText(" ");

        // PvP: no XP or gold gained
        if (isPvP) {
            isPvP = false;
            log("=== " + pvpPlayer1Name + " wins the PvP battle! ===");
            roomContext.setState(new VictoryState());
            if (pvpResultCallback != null) pvpResultCallback.accept(pvpPlayer1Name, pvpPlayer2Name);
            return true;
        }

        // PvE: calculate rewards
        int totalXp   = currentMobs.stream().mapToInt(Mob::getXpReward).sum();
        int totalGold = currentMobs.stream().mapToInt(Mob::getGoldReward).sum();
        gold += totalGold;
        gameSubject.notifyGoldChanged(gold);

        List<Hero> survivors = party.stream().filter(Hero::isAlive).collect(Collectors.toList());
        int xpEach = survivors.isEmpty() ? 0 : totalXp / survivors.size();

        log("--- Victory! +" + totalXp + " XP (each survivor gets " + xpEach + "), +" + totalGold + " Gold ---");

        // grant XP and handle level-ups
        for (Hero h : survivors) {
            int before = h.getLevel();
            gameSubject.notifyExperienceGained(h.getName(), xpEach);
            h.gainExperience(xpEach);
            if (h.getLevel() > before) {
                gameSubject.notifyLevelUp(h.getName(), h.getLevel(), h.getClassName());
                // offer class level-up choice
                offerLevelUpChoice(h);
            }
            if (h.getLevel() < 20) {
                log(h.getName() + ": " + h.getExperience() + " XP | "
                        + (h.expNeededForLevel(h.getLevel() + 1) - h.getExperience()) + " to next level");
            }
        }

        refreshStats();
        refreshMobPanel();
        roomContext.setState(new VictoryState());
        saveProgress();
        return true;
    }

    // let player choose which class to put a level into
    private void offerLevelUpChoice(Hero h) {
        String primaryClass = h.getClass().getSimpleName();
        if (h.getSecondaryClassName() != null) {
            // already has two classes - let them pick which to level
            String[] options = {primaryClass, h.getSecondaryClassName()};
            String pick = (String) JOptionPane.showInputDialog(this,
                    h.getName() + " levelled up! Choose which class to increase:",
                    "Level Up", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (pick != null) {
                if (pick.equalsIgnoreCase(primaryClass)) {
                    h.levelUpPrimaryClass();
                    log(h.getName() + " increased " + primaryClass + " class level to " + h.getPrimaryClassLevel());
                } else {
                    h.levelUpSecondaryClass();
                    log(h.getName() + " increased " + h.getSecondaryClassName() + " class level to " + h.getSecondaryClassLevel());
                }
            }
        } else {
            // no secondary class yet - offer to pick one or stick with primary
            String[] allClasses = {"Warrior", "Mage", "Order", "Chaos"};
            List<String> options = new ArrayList<>();
            options.add(primaryClass + " (current)");
            for (String c : allClasses) {
                if (!c.equalsIgnoreCase(primaryClass)) {
                    options.add(c + " (new secondary)");
                }
            }
            String pick = (String) JOptionPane.showInputDialog(this,
                    h.getName() + " levelled up! Level up current class or pick a secondary:",
                    "Level Up", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
            if (pick != null) {
                if (pick.contains("current")) {
                    h.levelUpPrimaryClass();
                    log(h.getName() + " increased " + primaryClass + " class level to " + h.getPrimaryClassLevel());
                } else {
                    String newClass = pick.split(" \\(")[0];
                    h.setSecondaryClassName(newClass);
                    h.setSecondaryClassLevel(1);
                    // trigger hybrid check
                    if (h instanceof Order) ((Order) h).triggerHybridWith(newClass.toUpperCase());
                    else if (h instanceof Chaos) ((Chaos) h).triggerHybridWith(newClass.toUpperCase());
                    else if (h instanceof Warrior) ((Warrior) h).triggerHybridWith(newClass.toUpperCase());
                    else if (h instanceof Mage) ((Mage) h).triggerHybridWith(newClass.toUpperCase());
                    log(h.getName() + " started learning " + newClass + " as secondary class!");
                }
            }
        }
    }

    private void playerDied() {
        inBattle       = false;
        turnQueue      = null;
        activeHero     = null;
        pendingActions = null;
        waitQueue      = null;
        lblTurn.setText(" ");
        lblQueue.setText(" ");

        if (isPvP) {
            isPvP = false;
            log("=== " + pvpPlayer2Name + " wins the PvP battle! ===");
            if (pvpResultCallback != null) pvpResultCallback.accept(pvpPlayer2Name, pvpPlayer1Name);
            roomContext.setState(new DefeatedState());
            return;
        }

        // lose 10% gold
        int lostGold = (int)(gold * 0.10);
        gold -= lostGold;
        gameSubject.notifyGoldChanged(gold);

        // lose 30% of current-level XP for each hero
        for (Hero h : party) {
            int xpForCurrentLevel = h.getExperience() - h.expNeededForLevel(h.getLevel());
            int xpLost = (int)(xpForCurrentLevel * 0.30);
            if (xpLost > 0) {
                h.setExperience(h.getExperience() - xpLost);
                log(h.getName() + " lost " + xpLost + " XP.");
            }
        }

        log("--- Defeated! Lost " + lostGold + " gold. ---");
        for (Hero h : party) h.fullRestore();
        refreshStats();
        roomContext.setState(new DefeatedState());
        JOptionPane.showMessageDialog(this, "Defeated! Lost " + lostGold + " gold.\nReturning to last inn.",
                "Defeated", JOptionPane.WARNING_MESSAGE);
    }

    // viewing party and items

    private void showPartyView() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PARTY ===\n");
        for (Hero h : party) {
            sb.append(String.format("%-20s [%s Lv%d]\n", h.getName(), h.getClassName(), h.getLevel()));
            sb.append(String.format("  HP: %d/%d  MP: %d/%d  ATK: %d  DEF: %d\n",
                    (int)h.getHp(), (int)h.getMaxHp(), h.getMana(), h.getMaxMana(), h.getAttack(), h.getDefense()));
            sb.append(String.format("  Class Levels: %s Lv%d", h.getClass().getSimpleName(), h.getPrimaryClassLevel()));
            if (h.getSecondaryClassName() != null) {
                sb.append(String.format(" | %s Lv%d", h.getSecondaryClassName(), h.getSecondaryClassLevel()));
            }
            if (h.isHybrid()) sb.append(" | Hybrid: " + h.getHybridClass());
            sb.append("\n");
            sb.append(String.format("  XP: %d | To next level: %s\n",
                    h.getExperience(),
                    h.getLevel() < 20 ? String.valueOf(h.expNeededForLevel(h.getLevel() + 1) - h.getExperience()) : "MAX"));
            if (!h.isAlive()) sb.append("  *** DEAD ***\n");
            sb.append("\n");
        }
        sb.append("=== INVENTORY ===\n");
        boolean hasItems = false;
        for (Map.Entry<String, Integer> e : inventory.entrySet()) {
            if (e.getValue() > 0) { sb.append("  ").append(e.getKey()).append(" x").append(e.getValue()).append("\n"); hasItems = true; }
        }
        if (!hasItems) sb.append("  (empty)\n");
        sb.append("\nGold: ").append(gold);

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(450, 400));
        JOptionPane.showMessageDialog(this, scroll, "Party & Inventory", JOptionPane.PLAIN_MESSAGE);
    }

    // quitting campaign

    private void exitCampaign() {
        if (inBattle) {
            JOptionPane.showMessageDialog(this, "You cannot exit during a battle!",
                    "Exit", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Exit the campaign? Your progress will be saved.",
                "Exit Campaign", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        saveProgress();
        log("--- Campaign saved. Returning to menu. ---");
        cl.show(container, "Menu");
    }

    // inn mechanics

    private void visitInn() {
        inBattle = false;

        // revive and restore all heroes
        StringBuilder innMsg = new StringBuilder("<html><b>Inn visit — party restored:</b><br>");
        for (Hero h : party) {
            double missingHp   = h.getMaxHp()  - h.getHp();
            int    missingMana = h.getMaxMana() - h.getMana();
            boolean wasDead    = !h.isAlive();
            h.fullRestore();
            if (wasDead) {
                innMsg.append(h.getName()).append(" was <b>revived</b> and fully restored.<br>");
            } else {
                innMsg.append(h.getName()).append(": +").append((int)missingHp)
                        .append(" HP, +").append(missingMana).append(" Mana<br>");
            }
        }
        innMsg.append("</html>");
        JOptionPane.showMessageDialog(this, innMsg.toString(), "Inn", JOptionPane.INFORMATION_MESSAGE);
        log("--- Inn: all party members restored. ---");
        refreshStats();

        // recruitment only in first 10 rooms and if party < 5
        if (currentRoom <= 10 && party.size() < 5) offerRecruitment();
        showInnShop();
        roomContext.setState(new InnState());
    }

    private static final String[] HERO_CLASSES = {"Warrior", "Mage", "Order", "Chaos"};

    private void offerRecruitment() {
        String recruitClass = HERO_CLASSES[random.nextInt(HERO_CLASSES.length)];
        int recruitLevel    = 1 + random.nextInt(4); // 1-4
        int cost            = recruitLevel == 1 ? 0 : 200 * recruitLevel;
        String heroName     = recruitClass + "-" + (party.size() + 1);

        String msg = "<html>A wandering <b>" + recruitClass + "</b> (Lv" + recruitLevel + ") is looking for work!<br>"
                + (cost == 0 ? "They will join for FREE!" : "Hiring cost: <b>" + cost + "g</b>") + "</html>";

        if (JOptionPane.showConfirmDialog(this, msg, "Recruit Hero?", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (gold < cost) { JOptionPane.showMessageDialog(this, "Not enough gold!", "Recruit", JOptionPane.WARNING_MESSAGE); return; }

        gold -= cost;
        gameSubject.notifyGoldChanged(gold);
        Hero recruit = HeroFactory.getFactory(recruitClass).createHero(heroName);
        for (int i = 1; i < recruitLevel; i++) recruit.gainExperience(recruit.expNeededForLevel(i + 1));
        party.add(recruit);
        log("--- " + heroName + " the " + recruitClass + " (Lv" + recruit.getLevel() + ") joined! ---");
        refreshStats();
    }

    private static final Object[][] ITEM_DEFS = {
            {"Bread",  200,  20,   0,   false},
            {"Cheese", 500,  50,   0,   false},
            {"Steak",  1000, 200,  0,   false},
            {"Water",  150,  0,    10,  false},
            {"Juice",  400,  0,    30,  false},
            {"Wine",   750,  0,    100, false},
            {"Elixir", 2000, 0,    0,   true },
    };

    private void showInnShop() {
        while (true) {
            String[] options = new String[ITEM_DEFS.length + 1];
            for (int i = 0; i < ITEM_DEFS.length; i++) {
                String name  = (String)  ITEM_DEFS[i][0];
                int cost     = (int)     ITEM_DEFS[i][1];
                int hp       = (int)     ITEM_DEFS[i][2];
                int mana     = (int)     ITEM_DEFS[i][3];
                boolean full = (boolean) ITEM_DEFS[i][4];
                int qty      = inventory.getOrDefault(name, 0);
                String effect = full ? "Full restore" : (hp > 0 ? "+" + hp + " HP" : "") + (mana > 0 ? " +" + mana + " MP" : "");
                options[i] = name + " - " + cost + "g (" + effect + ") [owned: " + qty + "]";
            }
            options[ITEM_DEFS.length] = "Leave Shop";

            String pick = (String) JOptionPane.showInputDialog(this,
                    "Gold: " + gold + "  |  Inventory: " + inventorySummary(),
                    "Inn Shop", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (pick == null || pick.startsWith("Leave")) break;

            for (Object[] def : ITEM_DEFS) {
                if (pick.startsWith((String) def[0])) {
                    int cost = (int) def[1];
                    if (gold < cost) {
                        JOptionPane.showMessageDialog(this, "Not enough gold!", "Shop", JOptionPane.WARNING_MESSAGE);
                    } else {
                        gold -= cost;
                        gameSubject.notifyGoldChanged(gold);
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

    public void useItems() {
        if (inBattle) { JOptionPane.showMessageDialog(this, "Can't use items during battle!", "Items", JOptionPane.WARNING_MESSAGE); return; }
        if (inventory.isEmpty() || inventory.values().stream().allMatch(q -> q == 0)) {
            JOptionPane.showMessageDialog(this, "Your inventory is empty!", "Items", JOptionPane.INFORMATION_MESSAGE); return;
        }
        while (true) {
            List<String> available = new ArrayList<>();
            for (Map.Entry<String, Integer> e : inventory.entrySet())
                if (e.getValue() > 0) available.add(e.getKey() + " x" + e.getValue());
            if (available.isEmpty()) break;
            available.add("Close");

            String pick = (String) JOptionPane.showInputDialog(this, "Select item to use:", "Inventory",
                    JOptionPane.PLAIN_MESSAGE, null, available.toArray(), available.get(0));
            if (pick == null || pick.equals("Close")) break;

            String itemName = pick.split(" x")[0];
            String[] heroNames = party.stream().map(Hero::getName).toArray(String[]::new);
            String targetName = (String) JOptionPane.showInputDialog(this,
                    "Use " + itemName + " on which hero?", "Use Item",
                    JOptionPane.PLAIN_MESSAGE, null, heroNames, heroNames[0]);
            if (targetName == null) continue;

            Hero target = party.stream().filter(h -> h.getName().equals(targetName)).findFirst().orElse(party.get(0));
            for (Object[] def : ITEM_DEFS) {
                if (def[0].equals(itemName)) {
                    int hp = (int) def[2]; int mana = (int) def[3]; boolean full = (boolean) def[4];
                    if (full) target.fullRestore();
                    else { if (hp > 0) target.heal(hp); if (mana > 0) target.restoreMana(mana); }
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

    // end the campaign

    private void endCampaign() {
        // score: 100 per hero level + 10 per gold + half item price * 10
        int totalLevels = party.stream().mapToInt(Hero::getLevel).sum();
        int itemScore = 0;
        for (Map.Entry<String, Integer> e : inventory.entrySet()) {
            for (Object[] def : ITEM_DEFS) {
                if (def[0].equals(e.getKey())) {
                    int price = (int) def[1];
                    itemScore += (int)(price * 0.5 * 10) * e.getValue();
                    break;
                }
            }
        }
        int score = totalLevels * 100 + gold * 10 + itemScore;
        log("=== Campaign Complete! Final Score: " + score + " ===");
        roomContext.setState(new ResolvingState());
        DatabaseManager.getInstance().saveScore(currentUser[0], score);

        // offer to save party for PvP (up to 5 slots)
        int existingSlots = DatabaseManager.getInstance().countPvPParties(currentUser[0]);
        if (existingSlots >= 5) {
            int replace = JOptionPane.showConfirmDialog(this,
                    "You already have 5 saved parties. Replace one to save this party for PvP?",
                    "Party Full", JOptionPane.YES_NO_OPTION);
            if (replace == JOptionPane.YES_OPTION) {
                java.util.List<String> slots = DatabaseManager.getInstance().getPvPPartySlotSummaries(currentUser[0]);
                String[] slotArr = slots.toArray(new String[0]);
                String pick = (String) JOptionPane.showInputDialog(this,
                        "Choose a party slot to replace:", "Replace Party",
                        JOptionPane.PLAIN_MESSAGE, null, slotArr, slotArr[0]);
                if (pick != null) {
                    int slotId = Integer.parseInt(pick.split(":")[0].replace("Slot ", "").trim()) - 1;
                    DatabaseManager.getInstance().deletePvPParty(currentUser[0], slotId);
                    DatabaseManager.getInstance().savePvPParty(currentUser[0], slotId, party);
                    JOptionPane.showMessageDialog(this, "Party saved to PvP slot " + (slotId + 1) + "!",
                            "Saved", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } else {
            int savePvp = JOptionPane.showConfirmDialog(this,
                    "Campaign complete! Score: " + score + "\n\nSave your party for PvP battles?",
                    "Campaign Over", JOptionPane.YES_NO_OPTION);
            if (savePvp == JOptionPane.YES_OPTION) {
                DatabaseManager.getInstance().savePvPParty(currentUser[0], existingSlots, party);
                JOptionPane.showMessageDialog(this, "Party saved to PvP slot " + (existingSlots + 1) + "!",
                        "Saved", JOptionPane.INFORMATION_MESSAGE);
            }
        }

        JOptionPane.showMessageDialog(this,
                "Final Score: " + score + "\nGold: " + gold
                        + "\nParty size: " + party.size() + "\nTotal levels: " + totalLevels
                        + "\nItem bonus: " + itemScore,
                "Campaign Complete", JOptionPane.INFORMATION_MESSAGE);
        cl.show(container, "Menu");
    }

    // helper methods

    private void saveProgress() {
        DatabaseManager.getInstance().saveParty(currentUser[0], party, gold, currentRoom);
        DatabaseManager.getInstance().saveInventory(currentUser[0], inventory);
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
        for (int i = 0; i < currentMobs.size(); i++) {
            Mob mob = currentMobs.get(i);
            String label, details;
            if (mob instanceof PvPMob) {
                Hero h = ((PvPMob) mob).getHero();
                label   = h.getName() + " [" + h.getClassName() + " Lv" + h.getLevel() + "]";
                details = "HP:" + (int)mob.getHp() + " ATK:" + h.getAttack() + " DEF:" + h.getDefense();
            } else {
                label   = "Enemy " + (i + 1) + " (Lv" + mob.getLevel() + ")";
                details = "HP:" + (int)mob.getHp() + " ATK:" + (int)mob.getPower() + " DEF:" + mob.getDefense();
            }
            String txt = mob.isAlive()
                    ? "<html><center>" + label + "<br>" + details + "</center></html>"
                    : "<html><center><strike>" + label + "</strike><br>Defeated</center></html>";
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createLineBorder(mob.isAlive() ? Color.RED : Color.GRAY));
            lbl.setPreferredSize(new Dimension(130, 65));
            mobPanel.add(lbl);
        }
        mobPanel.revalidate();
        mobPanel.repaint();
    }
}