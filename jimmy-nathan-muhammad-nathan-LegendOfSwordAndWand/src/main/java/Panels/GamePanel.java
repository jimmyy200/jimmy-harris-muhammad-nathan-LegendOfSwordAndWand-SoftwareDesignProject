package Panels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import Hero.Hero;
import Mob.Mob;
import Mob.MobSpawner;
import Mob.PvPMob;
import Observer.GameObserver;
import Observer.GameSubject;
import Singleton.DatabaseManager;
import State.BattleState;
import State.DefeatedState;
import State.InnState;
import State.ResolvingState;
import State.RoomContext;
import State.VictoryState;

/**
 * Refactored GamePanel — God Class Refactoring (#1):
 * Previously WMC=228, CBO=63, ATFD=17. Battle logic moved to BattleManager,
 * inn logic moved to InnManager. GamePanel now only handles:
 * - UI construction and display
 * - Room navigation and encounter rolls
 * - Delegating to BattleManager and InnManager
 * - Saving/loading
 */
public class GamePanel extends JPanel implements GameObserver {

    private static final int TOTAL_ROOMS = 30;
    private static final Random random = new Random();

    // game state
    private List<Hero>           party;
    private int                  gold;
    private int                  currentRoom;
    private Map<String, Integer> inventory = new LinkedHashMap<>();

    // delegated managers
    private BattleManager battleManager;
    private InnManager    innManager;

    // observer
    private GameSubject gameSubject;

    // pvp state (held here so startPvPBattle can pass to BattleManager)
    private boolean isPvP = false;
    private String  pvpPlayer1Name;
    private String  pvpPlayer2Name;

    // ui elements
    private JLabel    lblRoom, lblGold, lblHeroStats, lblTurn;
    private JTextArea logArea;
    private JButton   btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems, btnParty, btnExit;
    private JPanel    mobPanel;
    private Hero      activeHero; // tracked only for UI highlight
    private RoomContext roomContext;

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
        initManagers();
    }

    // Observer methods

    @Override public void onHeroLevelUp(String n, int lv, String cls) { log(n + " levelled up to Lv" + lv + "! [" + cls + "]"); }
    @Override public void onHeroDefeated(String n)                    { log(n + " has been defeated!"); }
    @Override public void onGoldChanged(int g)                        { lblGold.setText("Gold: " + g); }
    @Override public void onExperienceGained(String n, int xp)       { log(n + " gained " + xp + " XP."); }

    // UI

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
        mobPanel.setPreferredSize(new Dimension(0, 110));
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

        btnAttack.addActionListener(e   -> handleAttack());
        btnDefend.addActionListener(e   -> handleDefend());
        btnWait.addActionListener(e     -> handleWait());
        btnCast.addActionListener(e     -> handleCast());
        btnNextRoom.addActionListener(e -> enterNextRoom());
        btnUseItems.addActionListener(e -> handleUseItems());
        btnParty.addActionListener(e    -> showPartyView());
        btnExit.addActionListener(e     -> exitCampaign());

        roomContext = new RoomContext(btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems, btnParty, btnExit);
        roomContext.setState(new InnState());
    }

    // initialize battlemanager and innmanager
    private void initManagers() {
        battleManager = new BattleManager(
                gameSubject,
                this::log,
                this::refreshStats,
                this::refreshMobPanel,
                h -> { this.activeHero = h; },
                lblTurn::setText,
                roomContext::setState,
                this::onPvEVictory,
                this::onDefeat
        );
        innManager = new InnManager(this, gameSubject, this::log, this::refreshStats);
    }

    // initialise game variables

    public void startPvPBattle(List<Hero> p1Party, String p1Name,
                               List<Hero> p2Party, String p2Name,
                               java.util.function.BiConsumer<String, String> onResult) {
        isPvP          = true;
        pvpPlayer1Name = p1Name;
        pvpPlayer2Name = p2Name;
        party          = new ArrayList<>(p1Party);
        gold           = 0;
        currentRoom    = 0;
        logArea.setText("");
        log("=== PvP Battle: " + p1Name + " vs " + p2Name + " ===");

        battleManager.initPvP(party, p1Name, p2Party, p2Name, onResult);
        lblRoom.setText("PvP Battle");
        refreshStats();
        refreshMobPanel();
        battleManager.startNewRound();
        roomContext.setState(new BattleState());
    }

    public void startNewGame(List<Hero> startingParty) {
        isPvP       = false;
        party       = new ArrayList<>(startingParty);
        gold        = 0;
        currentRoom = 0;
        inventory   = new LinkedHashMap<>();
        logArea.setText("");
        log("=== Game Started! Party: " + partyNames() + " ===");
        enterNextRoom();
    }

    public void loadGame(List<Hero> savedParty, int savedGold, int savedRoom) {
        isPvP       = false;
        party       = new ArrayList<>(savedParty);
        gold        = savedGold;
        currentRoom = savedRoom;
        inventory   = DatabaseManager.getInstance().save.loadInventory(currentUser[0]);
        logArea.setText("");
        log("=== Save Loaded! Party: " + partyNames() + " ===");
        enterNextRoom();
    }

    private String partyNames() {
        return party.stream().map(h -> h.getName() + " (" + h.getClassName() + ")")
                .collect(Collectors.joining(", "));
    }

    // room creation

    private void enterNextRoom() {
        currentRoom++;
        if (currentRoom > TOTAL_ROOMS) { endCampaign(); return; }

        activeHero = null;
        lblRoom.setText("Room " + currentRoom + " / " + TOTAL_ROOMS);
        refreshStats();

        int cumLevel = party.stream().mapToInt(Hero::getLevel).sum();
        int encounterChance = Math.min(90, 60 + (cumLevel / 10) * 3);

        if (random.nextInt(100) < encounterChance) {
            List<Mob> mobs = MobSpawner.spawnEnemies(cumLevel);
            log("--- " + mobs.size() + " enemy mob(s) appeared! ---");
            for (Mob m : mobs)
                log("  Enemy Lv" + m.getLevel() + " | HP:" + (int)m.getHp()
                        + " ATK:" + (int)m.getPower() + " DEF:" + m.getDefense());
            battleManager.initPvE(party, mobs, gold);
            refreshMobPanel();
            roomContext.setState(new BattleState());
            battleManager.startNewRound();
        } else {
            visitInn();
        }
    }

    // battle handling

    /**
     * Refactoring #2 — Long Method (queueAttack was LOC=68, CC=23):
     * Target selection stays in GamePanel (UI concern).
     * The actual damage calculation and queuing is handled by BattleManager.queueAttack().
     */
    private void handleAttack() {
        Hero active = battleManager.getActiveHero();
        if (active == null) return;

        boolean actAsP2 = battleManager.isPvP() && battleManager.isPvP2Turn();
        List<String> names   = new ArrayList<>();
        List<Object> targets = new ArrayList<>();

        if (actAsP2) {
            for (Hero h : party) if (h.isAlive()) { names.add(h.getName()); targets.add(h); }
        } else {
            List<Mob> mobs = battleManager.getCurrentMobs();
            for (int i = 0; i < mobs.size(); i++) {
                Mob m = mobs.get(i);
                if (m.isAlive()) {
                    names.add(m instanceof PvPMob
                            ? ((PvPMob) m).getHero().getName()
                            : "Enemy " + (i + 1) + " (Lv" + m.getLevel() + ")");
                    targets.add(m);
                }
            }
        }
        if (names.isEmpty()) return;

        String chosen = names.size() == 1 ? names.get(0)
                : (String) JOptionPane.showInputDialog(this,
                "Choose target for " + active.getName() + ":", "Attack Target",
                JOptionPane.PLAIN_MESSAGE, null, names.toArray(), names.get(0));
        if (chosen == null) return;

        battleManager.queueAttack(targets.get(names.indexOf(chosen)));
    }

    private void handleDefend() { battleManager.queueDefend(); }
    private void handleWait()   { battleManager.queueWait(); }

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

    private void handleCast() {
        Hero active = battleManager.getActiveHero();
        if (active == null) return;

        String[] spells = getSpellOptions(active);
        if (spells.length == 0) { log("No spells available!"); return; }

        String chosen = (String) JOptionPane.showInputDialog(this,
                "Choose a spell for " + active.getName() + ":", "Cast Spell",
                JOptionPane.PLAIN_MESSAGE, null, spells, spells[0]);
        if (chosen == null) return;

        int cost = getSpellCost(chosen, active);
        if (active.getMana() < cost) {
            JOptionPane.showMessageDialog(this, "Not enough mana! Need " + cost + ", have " + active.getMana(),
                    "Cast Spell", JOptionPane.WARNING_MESSAGE);
            return;
        }
        battleManager.queueCast(chosen);
    }

    // battle result methods

    // refactoring checkBattleEnd()
    // refactor #5
    private void onPvEVictory() {
        battleManager.distributeXP(this::offerLevelUpChoice);
        refreshStats();
        refreshMobPanel();
        roomContext.setState(new VictoryState());
        gold = battleManager.getGold();
        saveProgress();
    }

    private void onDefeat() {
        int lostGold = (int)(gold * 0.10);
        gold -= lostGold;
        gameSubject.notifyGoldChanged(gold);

        for (Hero h : party) {
            int xpForLevel = h.getExperience() - h.expNeededForLevel(h.getLevel());
            int xpLost = (int)(xpForLevel * 0.30);
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

    // inn methods

    private void visitInn() {
        String msg = innManager.restoreParty(party);
        JOptionPane.showMessageDialog(this, msg, "Inn", JOptionPane.INFORMATION_MESSAGE);
        log("--- Inn: all party members restored. ---");
        refreshStats();

        if (currentRoom <= 10 && party.size() < 5) {
            int[] goldRef = {gold};
            Hero recruit = innManager.offerRecruitment(party, goldRef);
            if (recruit != null) { party.add(recruit); refreshStats(); }
            gold = goldRef[0];
        }

        int[] goldRef = {gold};
        innManager.showInnShop(inventory, goldRef);
        gold = goldRef[0];
        refreshStats();
        roomContext.setState(new InnState());
    }

    private void handleUseItems() {
        if (battleManager.isInBattle()) {
            JOptionPane.showMessageDialog(this, "Can't use items during battle!", "Items", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (inventory.isEmpty() || inventory.values().stream().allMatch(q -> q == 0)) {
            JOptionPane.showMessageDialog(this, "Your inventory is empty!", "Items", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        innManager.useItems(party, inventory);
    }

    // leveling up

    // Refactor 9 - Long Method (offerLevelUpChoice)
    // Split into three smaller methods and replaced instanceof chain with polymorphic call
    private void offerLevelUpChoice(Hero h) {
        if (h.getSecondaryClassName() != null) {
            levelUpExistingClass(h);
        } else {
            chooseNewSecondary(h);
        }
    }

    private void levelUpExistingClass(Hero h) {
        String primaryClass = h.getClass().getSimpleName();
        String[] options = {primaryClass, h.getSecondaryClassName()};
        String pick = (String) JOptionPane.showInputDialog(this,
                h.getName() + " levelled up! Choose which class to increase:",
                "Level Up", JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (pick == null) return;
        if (pick.equalsIgnoreCase(primaryClass)) {
            h.levelUpPrimaryClass();
            log(h.getName() + " increased " + primaryClass + " class level to " + h.getPrimaryClassLevel());
        } else {
            h.levelUpSecondaryClass();
            log(h.getName() + " increased " + h.getSecondaryClassName() + " class level to " + h.getSecondaryClassLevel());
        }
    }

    private void chooseNewSecondary(Hero h) {
        String primaryClass = h.getClass().getSimpleName();
        String[] allClasses = {"Warrior", "Mage", "Order", "Chaos"};
        List<String> options = new ArrayList<>();
        options.add(primaryClass + " (current)");
        for (String c : allClasses) if (!c.equalsIgnoreCase(primaryClass)) options.add(c + " (new secondary)");

        String pick = (String) JOptionPane.showInputDialog(this,
                h.getName() + " levelled up! Level up current class or pick a secondary:",
                "Level Up", JOptionPane.QUESTION_MESSAGE, null, options.toArray(), options.get(0));
        if (pick == null) return;
        if (pick.contains("current")) {
            h.levelUpPrimaryClass();
            log(h.getName() + " increased " + primaryClass + " class level to " + h.getPrimaryClassLevel());
        } else {
            String newClass = pick.split(" \\(")[0];
            h.setSecondaryClassName(newClass);
            h.setSecondaryClassLevel(1);
            h.triggerHybridWith(newClass.toUpperCase());
            log(h.getName() + " started learning " + newClass + " as secondary class!");
        }
    }

    // party methods

    private void showPartyView() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PARTY ===\n");
        for (Hero h : party) sb.append(buildHeroStatLine(h));
        sb.append("=== INVENTORY ===\n");
        boolean hasItems = false;
        for (Map.Entry<String, Integer> e : inventory.entrySet())
            if (e.getValue() > 0) { sb.append("  ").append(e.getKey()).append(" x").append(e.getValue()).append("\n"); hasItems = true; }
        if (!hasItems) sb.append("  (empty)\n");
        sb.append("\nGold: ").append(gold);

        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(450, 400));
        JOptionPane.showMessageDialog(this, scroll, "Party & Inventory", JOptionPane.PLAIN_MESSAGE);
    }

    private String buildHeroStatLine(Hero h) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-20s [%s Lv%d]\n", h.getName(), h.getClassName(), h.getLevel()));
        sb.append(String.format("  HP: %d/%d  MP: %d/%d  ATK: %d  DEF: %d\n",
                (int)h.getHp(), (int)h.getMaxHp(), h.getMana(), h.getMaxMana(), h.getAttack(), h.getDefense()));
        sb.append(String.format("  Class Levels: %s Lv%d", h.getClass().getSimpleName(), h.getPrimaryClassLevel()));
        if (h.getSecondaryClassName() != null)
            sb.append(String.format(" | %s Lv%d", h.getSecondaryClassName(), h.getSecondaryClassLevel()));
        if (h.isHybrid()) sb.append(" | Hybrid: ").append(h.getHybridClass());
        sb.append("\n");
        sb.append(String.format("  XP: %d | To next level: %s\n", h.getExperience(),
                h.getLevel() < 20 ? String.valueOf(h.expNeededForLevel(h.getLevel() + 1) - h.getExperience()) : "MAX"));
        if (!h.isAlive()) sb.append("  *** DEAD ***\n");
        sb.append("\n");
        return sb.toString();
    }

    // saving and exiting

    private void exitCampaign() {
        if (battleManager.isInBattle()) {
            JOptionPane.showMessageDialog(this, "You cannot exit during a battle!", "Exit", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Exit the campaign? Your progress will be saved.",
                "Exit Campaign", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        saveProgress();
        log("--- Campaign saved. Returning to menu. ---");
        cl.show(container, "Menu");
    }

    private void saveProgress() {
        DatabaseManager.getInstance().save.saveParty(currentUser[0], party, gold, currentRoom);
        DatabaseManager.getInstance().save.saveInventory(currentUser[0], inventory);
    }

    // ending

    private void endCampaign() {
        int totalLevels = party.stream().mapToInt(Hero::getLevel).sum();
        int itemScore   = calculateItemScore();
        int score       = totalLevels * 100 + gold * 10 + itemScore;

        log("=== Campaign Complete! Final Score: " + score + " ===");
        roomContext.setState(new ResolvingState());
        DatabaseManager.getInstance().save.saveScore(currentUser[0], score);

        offerPvPPartySave(score);

        JOptionPane.showMessageDialog(this,
                "Final Score: " + score + "\nGold: " + gold
                        + "\nParty size: " + party.size() + "\nTotal levels: " + totalLevels
                        + "\nItem bonus: " + itemScore,
                "Campaign Complete", JOptionPane.INFORMATION_MESSAGE);
        cl.show(container, "Menu");
    }

    private int calculateItemScore() {
        int itemScore = 0;
        for (Map.Entry<String, Integer> e : inventory.entrySet())
            for (Object[] def : InnManager.ITEM_DEFS)
                if (def[0].equals(e.getKey())) { itemScore += (int)((int)def[1] * 0.5 * 10) * e.getValue(); break; }
        return itemScore;
    }

    private void offerPvPPartySave(int score) {
        int existingSlots = DatabaseManager.getInstance().pvp.countPvPParties(currentUser[0]);
        if (existingSlots >= 5) {
            if (JOptionPane.showConfirmDialog(this,
                    "You already have 5 saved parties. Replace one?",
                    "Party Full", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            List<String> slots = DatabaseManager.getInstance().pvp.getPvPPartySlotSummaries(currentUser[0]);
            String pick = (String) JOptionPane.showInputDialog(this,
                    "Choose a party slot to replace:", "Replace Party",
                    JOptionPane.PLAIN_MESSAGE, null, slots.toArray(), slots.get(0));
            if (pick == null) return;
            int slotId = Integer.parseInt(pick.split(":")[0].replace("Slot ", "").trim()) - 1;
            DatabaseManager.getInstance().pvp.deletePvPParty(currentUser[0], slotId);
            DatabaseManager.getInstance().pvp.savePvPParty(currentUser[0], slotId, party);
            JOptionPane.showMessageDialog(this, "Party saved to PvP slot " + (slotId + 1) + "!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } else {
            if (JOptionPane.showConfirmDialog(this,
                    "Campaign complete! Score: " + score + "\n\nSave your party for PvP battles?",
                    "Campaign Over", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            DatabaseManager.getInstance().pvp.savePvPParty(currentUser[0], existingSlots, party);
            JOptionPane.showMessageDialog(this, "Party saved to PvP slot " + (existingSlots + 1) + "!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // UI

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
        List<Mob> mobs = battleManager.getCurrentMobs();
        if (mobs == null) { mobPanel.revalidate(); mobPanel.repaint(); return; }
        for (int i = 0; i < mobs.size(); i++) {
            Mob mob = mobs.get(i);
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