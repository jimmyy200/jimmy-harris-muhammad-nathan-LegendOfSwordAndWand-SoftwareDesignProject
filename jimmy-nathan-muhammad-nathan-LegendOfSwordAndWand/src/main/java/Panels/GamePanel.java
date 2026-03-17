package Panels;

import Hero.*;
import Mob.*;
import Singleton.DatabaseManager;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {

    private static final int TOTAL_ROOMS = 30;
    private static final Random random = new Random();

    // ── Game State ────────────────────────────────────────────
    private Hero   hero;
    private List<Hero> party;
    private int    gold;
    private int    currentRoom;
    private int    baseEncounterChance = 60; // %
    private List<Mob> currentMobs;
    private boolean inBattle;
    private boolean waitingForPlayer; // player has chosen "Wait"

    // ── UI References ─────────────────────────────────────────
    private JLabel  lblRoom, lblGold, lblHeroStats, lblStatus;
    private JTextArea logArea;
    private JButton btnAttack, btnDefend, btnWait, btnCast, btnNextRoom;
    private JPanel  mobPanel;
    private JPanel  actionPanel;
    private final String[] currentUser;
    private final JPanel   container;
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
        // ── TOP: hero stats bar ──
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 5, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        lblHeroStats = new JLabel("", SwingConstants.LEFT);
        lblRoom      = new JLabel("", SwingConstants.CENTER);
        lblGold      = new JLabel("", SwingConstants.RIGHT);
        topPanel.add(lblHeroStats);
        topPanel.add(lblRoom);
        topPanel.add(lblGold);
        add(topPanel, BorderLayout.NORTH);

        // ── CENTER: mob display + log ──
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

        // ── BOTTOM: actions + status ──
        JPanel bottomPanel = new JPanel(new BorderLayout());

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setForeground(Color.RED);
        bottomPanel.add(lblStatus, BorderLayout.NORTH);

        actionPanel = new JPanel(new FlowLayout());
        btnAttack   = new JButton("Attack");
        btnDefend   = new JButton("Defend");
        btnWait     = new JButton("Wait");
        btnCast     = new JButton("Cast Spell");
        btnNextRoom = new JButton("Next Room ▶");

        actionPanel.add(btnAttack);
        actionPanel.add(btnDefend);
        actionPanel.add(btnWait);
        actionPanel.add(btnCast);
        actionPanel.add(btnNextRoom);
        bottomPanel.add(actionPanel, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);

        // ── Wire buttons ──
        btnAttack.addActionListener(e   -> playerAttack());
        btnDefend.addActionListener(e   -> playerDefend());
        btnWait.addActionListener(e     -> playerWait());
        btnCast.addActionListener(e     -> playerCast());
        btnNextRoom.addActionListener(e -> enterNextRoom());
    }

    // ── Init / Load ───────────────────────────────────────────

    /** Called from ClassSelectPanel / LoadGamePanel to start the game */
    public void startNewGame(String heroClass, String heroName) {
        hero        = createHero(heroClass, heroName);
        party       = new ArrayList<>();
        party.add(hero);
        gold        = 0;
        currentRoom = 0;
        log("=== Game Started! You are a " + heroClass + " ===");
        enterNextRoom();
    }

    public void loadGame(String heroClass, String heroName, int level, double hp,
                         int attack, int defense, int mana, int goldAmt, int room) {
        hero = createHero(heroClass, heroName);
        party = new ArrayList<>();
        party.add(hero);
        // Restore saved state
        hero.setLevel(level);
        hero.changeHp(hp);
        hero.changeAttack(attack);
        hero.changeDefense(defense);
        hero.changeMana(mana);
        this.gold        = goldAmt;
        this.currentRoom = room;
        log("=== Save Loaded! Room " + room + " ===");
        enterNextRoom();
    }

    private Hero createHero(String heroClass, String heroName) {
        switch (heroClass.toUpperCase()) {
            case "WARRIOR": return new Warrior(heroName);
            case "MAGE":    return new Mage(heroName);
            case "ORDER":   return new Order(heroName);
            case "CHAOS":   return new Chaos(heroName);
            default:        return new Warrior(heroName);
        }
    }

    // ── Room Logic ────────────────────────────────────────────

    private void enterNextRoom() {
        currentRoom++;

        if (currentRoom > TOTAL_ROOMS) {
            endCampaign();
            return;
        }

        currentMobs = new ArrayList<>();
        inBattle    = false;

        // Encounter probability shifts 3% per 10 cumulative hero levels
        int shift = (hero.getLevel() / 10) * 3;
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
        int numMobs  = 1 + random.nextInt(3); // 1-3 mobs
        int mobLevel = Math.max(1, hero.getLevel() * 2 + random.nextInt(3) - 1);

        for (int i = 0; i < numMobs; i++) {
            double hp    = 50  + mobLevel * 10;
            double power = 3   + mobLevel * 2;
            int xp       = 50  * mobLevel;
            int g        = 75  * mobLevel;
            currentMobs.add(new NormalMob(hp, power, xp, g, 0.75));
        }
        log("--- " + numMobs + " enemy mob(s) (Lv" + mobLevel + ") appeared! ---");
    }

    private void startBattle() {
        inBattle = true;
        setActionButtons(true, false);
        refreshMobPanel();
    }

    private void visitInn() {
        inBattle = false;
        setActionButtons(false, true);
        for (Hero h : party) h.fullRestore();
        log("--- You found an Inn! All party members fully restored. ---");
        refreshStats();
        if (currentRoom <= 10 && party.size() < 5) offerRecruitment();
        showInnShop();
    }

    // ── Recruitment ───────────────────────────────────────────

    private static final String[] HERO_CLASSES = {"Warrior", "Mage", "Order", "Chaos"};

    private void offerRecruitment() {
        String recruitClass = HERO_CLASSES[random.nextInt(HERO_CLASSES.length)];
        int recruitLevel    = 1 + random.nextInt(4);
        int cost            = recruitLevel == 1 ? 0 : 200 * recruitLevel;
        String heroName     = recruitClass + "-" + (party.size() + 1);

        String msg = "<html>A wandering <b>" + recruitClass + "</b> (Lv" + recruitLevel + ") is looking for work!<br>"
                + (cost == 0 ? "They will join for FREE!" : "Hiring cost: <b>" + cost + "g</b>") + "</html>";

        int choice = JOptionPane.showConfirmDialog(this, msg, "Recruit Hero?", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) return;

        if (gold < cost) {
            JOptionPane.showMessageDialog(this, "Not enough gold!", "Recruit", JOptionPane.WARNING_MESSAGE);
            return;
        }

        gold -= cost;
        Hero recruit = createHero(recruitClass, heroName);
        for (int i = 1; i < recruitLevel; i++) recruit.gainExperience(recruit.expNeededForLevel(i + 1));
        party.add(recruit);
        log("--- " + heroName + " the " + recruitClass + " (Lv" + recruit.getLevel() + ") joined your party! ---");
        refreshStats();
    }

    // ── Combat Actions ────────────────────────────────────────

    private void playerAttack() {
        if (!inBattle || currentMobs.isEmpty()) return;
        Mob target = pickLivingMob();
        if (target == null) return;

        int damage = Math.max(0, hero.getAttack() - (int)(target.getPower() * 0.5));
        target.takeDamage(damage);
        log(hero.getName() + " attacks for " + damage + " damage!");
        refreshMobPanel();

        checkBattleEnd();
        if (inBattle) mobsTurn();
    }

    private void playerDefend() {
        if (!inBattle) return;
        hero.defend();
        log(hero.getName() + " defends. +10 HP, +5 Mana.");
        refreshStats();
        if (inBattle) mobsTurn();
    }

    private void playerWait() {
        if (!inBattle) return;
        log(hero.getName() + " waits...");
        mobsTurn();
        // After all mobs act, hero acts last
        log(hero.getName() + " acts after waiting.");
    }

    private void playerCast() {
        if (!inBattle || currentMobs.isEmpty()) return;

        String[] spells = getSpellOptions();
        if (spells.length == 0) {
            log("No spells available!");
            return;
        }

        String chosen = (String) JOptionPane.showInputDialog(
                this, "Choose a spell:", "Cast Spell",
                JOptionPane.PLAIN_MESSAGE, null, spells, spells[0]);

        if (chosen == null) return;

        Hero[] dummyParty = { hero };
        Mob[]  mobArray   = currentMobs.toArray(new Mob[0]);

        // Route spell to the right method
        castSpell(chosen, dummyParty, mobArray);
        refreshStats();
        checkBattleEnd();
        if (inBattle) mobsTurn();
    }

    private void castSpell(String spell, Hero[] party, Mob[] mobs) {
        // Build Hero[] from mobs for damage — mobs don't extend Hero so we use takeDamage directly
        switch (spell) {
            case "Protect": ((Order) hero).protect(party); break;
            case "Heal":    ((Order) hero).heal(party);    break;
            case "Fireball":
                if (!hero.spendMana(30)) return;
                log(hero.getName() + " launches Fireball!");
                for (int i = 0; i < Math.min(3, mobs.length); i++) {
                    int dmg = Math.max(0, hero.getAttack() - (int)(mobs[i].getPower() * 0.3));
                    mobs[i].takeDamage(dmg);
                }
                break;
            case "Chain Lightning":
                if (!hero.spendMana(40)) return;
                log(hero.getName() + " casts Chain Lightning!");
                double dmg = hero.getAttack();
                for (Mob m : mobs) {
                    if (m.isAlive()) { m.takeDamage(dmg); dmg *= 0.25; }
                }
                break;
            case "Berserker Attack":
                if (!hero.spendMana(60)) return;
                log(hero.getName() + " goes Berserk!");
                if (mobs.length > 0) {
                    int primary = Math.max(0, hero.getAttack() - (int)(mobs[0].getPower() * 0.3));
                    mobs[0].takeDamage(primary);
                    int splash = (int)(primary * 0.25);
                    for (int i = 1; i < Math.min(3, mobs.length); i++) mobs[i].takeDamage(splash);
                }
                break;
            case "Replenish": ((Mage) hero).replenish(party); break;
        }
        refreshMobPanel();
    }

    private String[] getSpellOptions() {
        String cls = hero.getClass().getSimpleName().toUpperCase();
        switch (cls) {
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
        boolean allDead = party.stream().noneMatch(Hero::isAlive);
        if (allDead) playerDied();
    }

    // ── Battle Resolution ─────────────────────────────────────

    private void checkBattleEnd() {
        boolean allDead = currentMobs.stream().noneMatch(Mob::isAlive);
        if (allDead) {
            inBattle = false;
            int totalXp   = currentMobs.stream().mapToInt(Mob::getXpReward).sum();
            int totalGold = currentMobs.stream().mapToInt(Mob::getGoldReward).sum();
            gold += totalGold;

            // XP split among living party members per spec
            List<Hero> survivors = new ArrayList<>();
            for (Hero h : party) { if (h.isAlive()) survivors.add(h); }
            int xpEach = survivors.isEmpty() ? 0 : totalXp / survivors.size();

            log("--- Victory! +" + totalXp + " XP (each survivor gets " + xpEach + "), +" + totalGold + " Gold ---");

            for (Hero h : survivors) {
                int before = h.getLevel();
                h.gainExperience(xpEach);
                int expToNext = h.expNeededForLevel(h.getLevel() + 1) - h.getExperience();
                if (h.getLevel() > before) {
                    log(h.getName() + " levelled up to Lv" + h.getLevel() + "!");
                }
                if (h.getLevel() < 20) {
                    log(h.getName() + ": " + h.getExperience() + " XP | " + expToNext + " to next level");
                }
            }

            refreshStats();
            refreshMobPanel();
            setActionButtons(false, true);
            saveProgress();
        }
    }

    private void playerDied() {
        inBattle = false;
        int lostGold = (int)(gold * 0.10);
        gold -= lostGold;
        log("--- You were defeated! Lost " + lostGold + " gold. Returning to last inn... ---");
        setActionButtons(false, true);
        hero.fullRestore();
        refreshStats();
        JOptionPane.showMessageDialog(this, "You were defeated! Lost " + lostGold + " gold.", "Defeated", JOptionPane.WARNING_MESSAGE);
    }

    // ── Inn ───────────────────────────────────────────────────

    private void showInnShop() {
        String[] items   = {"Bread (200g +20HP)", "Cheese (500g +50HP)", "Steak (1000g +200HP)",
                "Water (150g +10MP)", "Juice (400g +30MP)", "Wine (750g +100MP)",
                "Elixir (2000g Full)", "Leave Inn"};
        boolean shopping = true;
        while (shopping) {
            String pick = (String) JOptionPane.showInputDialog(
                    this, "Gold: " + gold + " | HP: " + (int)hero.getHp() + "/" + (int)hero.getMaxHp()
                            + " | Mana: " + hero.getMana() + "/" + hero.getMaxMana(),
                    "Inn Shop", JOptionPane.PLAIN_MESSAGE, null, items, items[0]);

            if (pick == null || pick.equals("Leave Inn")) { shopping = false; break; }

            if      (pick.startsWith("Bread"))  buyItem(200,  () -> hero.heal(20));
            else if (pick.startsWith("Cheese")) buyItem(500,  () -> hero.heal(50));
            else if (pick.startsWith("Steak"))  buyItem(1000, () -> hero.heal(200));
            else if (pick.startsWith("Water"))  buyItem(150,  () -> hero.restoreMana(10));
            else if (pick.startsWith("Juice"))  buyItem(400,  () -> hero.restoreMana(30));
            else if (pick.startsWith("Wine"))   buyItem(750,  () -> hero.restoreMana(100));
            else if (pick.startsWith("Elixir")) buyItem(2000, () -> hero.fullRestore());
        }
        refreshStats();
    }

    private void buyItem(int cost, Runnable effect) {
        if (gold >= cost) {
            gold -= cost;
            effect.run();
            log("Purchased item for " + cost + "g.");
            refreshStats();
        } else {
            JOptionPane.showMessageDialog(this, "Not enough gold!", "Shop", JOptionPane.WARNING_MESSAGE);
        }
    }

    // ── End Campaign ──────────────────────────────────────────

    private void endCampaign() {
        int totalLevels = party.stream().mapToInt(Hero::getLevel).sum();
        int score = totalLevels * 100 + gold * 10;
        log("=== Campaign Complete! Final Score: " + score + " ===");
        setActionButtons(false, false);
        DatabaseManager.getInstance().saveScore(currentUser[0], score);
        JOptionPane.showMessageDialog(this,
                "Campaign complete!\nFinal Score: " + score + "\nGold: " + gold
                        + "\nParty size: " + party.size() + "\nTotal levels: " + totalLevels,
                "Game Over", JOptionPane.INFORMATION_MESSAGE);
        cl.show(container, "Menu");
    }

    // ── Helpers ───────────────────────────────────────────────

    private void saveProgress() {
        DatabaseManager.getInstance().updateSave(
                currentUser[0], hero.getLevel(), hero.getHp(),
                hero.getAttack(), hero.getDefense(), hero.getMana(), gold, currentRoom);
    }

    private Mob pickLivingMob() {
        return currentMobs.stream().filter(Mob::isAlive).findFirst().orElse(null);
    }

    private void log(String msg) {
        logArea.append(msg + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private void refreshStats() {
        if (hero == null) return;
        StringBuilder sb = new StringBuilder("<html>");
        for (Hero h : party) {
            sb.append(h.getName())
                    .append(" [").append(h.getClassName()).append(" Lv").append(h.getLevel()).append("] ")
                    .append("HP:").append((int)h.getHp()).append("/").append((int)h.getMaxHp())
                    .append(" MP:").append(h.getMana()).append("/").append(h.getMaxMana());
            if (!h.isAlive()) sb.append(" <font color='red'>DEAD</font>");
            sb.append("<br>");
        }
        sb.append("</html>");
        lblHeroStats.setText(sb.toString());
        lblGold.setText("Gold: " + gold);
    }

    private void refreshMobPanel() {
        mobPanel.removeAll();
        for (Mob mob : currentMobs) {
            String txt = mob.isAlive()
                    ? "<html>Enemy<br>HP: " + (int)mob.getHp() + "</html>"
                    : "<html><strike>Enemy</strike><br>Defeated</html>";
            JLabel lbl = new JLabel(txt, SwingConstants.CENTER);
            lbl.setBorder(BorderFactory.createLineBorder(mob.isAlive() ? Color.RED : Color.GRAY));
            lbl.setPreferredSize(new Dimension(80, 60));
            mobPanel.add(lbl);
        }
        mobPanel.revalidate();
        mobPanel.repaint();
    }

    private void setActionButtons(boolean battleMode, boolean showNext) {
        btnAttack.setEnabled(battleMode);
        btnDefend.setEnabled(battleMode);
        btnWait.setEnabled(battleMode);
        btnCast.setEnabled(battleMode);
        btnNextRoom.setEnabled(showNext);
    }
}