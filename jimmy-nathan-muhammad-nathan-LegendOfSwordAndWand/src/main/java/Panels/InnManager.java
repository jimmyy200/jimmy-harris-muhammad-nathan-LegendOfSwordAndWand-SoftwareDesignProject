package Panels;

import Factory.HeroFactory;
import Hero.*;
import Observer.GameSubject;
import Singleton.DatabaseManager;

import javax.swing.*;
import java.util.*;
import java.util.List;

/**
 * Extracted Class — God Class Refactoring (#1):
 * InnManager handles all inn-related logic extracted from GamePanel:
 * hero restoration, recruitment, item shop, and item usage.
 * GamePanel delegates to this class whenever the player visits an inn.
 */
public class InnManager {

    private static final Random random = new Random();

    private static final String[] HERO_CLASSES = {"Warrior", "Mage", "Order", "Chaos"};

    /** Item definitions: name, cost, hp, mana, fullRestore */
    public static final Object[][] ITEM_DEFS = {
            {"Bread",  200,  20,   0,   false},
            {"Cheese", 500,  50,   0,   false},
            {"Steak",  1000, 200,  0,   false},
            {"Water",  150,  0,    10,  false},
            {"Juice",  400,  0,    30,  false},
            {"Wine",   750,  0,    100, false},
            {"Elixir", 2000, 0,    0,   true },
    };

    private final JPanel       parentPanel;
    private final GameSubject  gameSubject;
    private final java.util.function.Consumer<String> logFn;
    private final Runnable     refreshStatsFn;

    public InnManager(JPanel parentPanel, GameSubject gameSubject,
                      java.util.function.Consumer<String> logFn, Runnable refreshStatsFn) {
        this.parentPanel    = parentPanel;
        this.gameSubject    = gameSubject;
        this.logFn          = logFn;
        this.refreshStatsFn = refreshStatsFn;
    }

    /**
     * Restores all party members and returns an HTML message describing what was restored.
     * Extracted from visitInn() — separates restoration logic from UI flow.
     */
    public String restoreParty(List<Hero> party) {
        StringBuilder msg = new StringBuilder("<html><b>Inn visit — party restored:</b><br>");
        for (Hero h : party) {
            double missingHp   = h.getMaxHp()  - h.getHp();
            int    missingMana = h.getMaxMana() - h.getMana();
            boolean wasDead    = !h.isAlive();
            h.fullRestore();
            if (wasDead) {
                msg.append(h.getName()).append(" was <b>revived</b> and fully restored.<br>");
            } else {
                msg.append(h.getName()).append(": +").append((int)missingHp)
                        .append(" HP, +").append(missingMana).append(" Mana<br>");
            }
        }
        msg.append("</html>");
        return msg.toString();
    }

    /**
     * Offers a random hero for recruitment. Returns the recruited hero or null if declined.
     */
    public Hero offerRecruitment(List<Hero> party, int[] goldRef) {
        String recruitClass = HERO_CLASSES[random.nextInt(HERO_CLASSES.length)];
        int recruitLevel    = 1 + random.nextInt(4);
        int cost            = recruitLevel == 1 ? 0 : 200 * recruitLevel;
        String heroName     = recruitClass + "-" + (party.size() + 1);

        String msg = "<html>A wandering <b>" + recruitClass + "</b> (Lv" + recruitLevel + ") is looking for work!<br>"
                + (cost == 0 ? "They will join for FREE!" : "Hiring cost: <b>" + cost + "g</b>") + "</html>";

        if (JOptionPane.showConfirmDialog(parentPanel, msg, "Recruit Hero?", JOptionPane.YES_NO_OPTION)
                != JOptionPane.YES_OPTION) return null;

        if (goldRef[0] < cost) {
            JOptionPane.showMessageDialog(parentPanel, "Not enough gold!", "Recruit", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        goldRef[0] -= cost;
        gameSubject.notifyGoldChanged(goldRef[0]);
        Hero recruit = HeroFactory.getFactory(recruitClass).createHero(heroName);
        for (int i = 1; i < recruitLevel; i++) recruit.gainExperience(recruit.expNeededForLevel(i + 1));
        logFn.accept("--- " + heroName + " the " + recruitClass + " (Lv" + recruit.getLevel() + ") joined! ---");
        return recruit;
    }

    /**
     * Runs the inn shop loop. Modifies inventory and goldRef in place.
     */
    public void showInnShop(Map<String, Integer> inventory, int[] goldRef) {
        while (true) {
            String[] options = new String[ITEM_DEFS.length + 1];
            for (int i = 0; i < ITEM_DEFS.length; i++) {
                String name  = (String)  ITEM_DEFS[i][0];
                int cost     = (int)     ITEM_DEFS[i][1];
                int hp       = (int)     ITEM_DEFS[i][2];
                int mana     = (int)     ITEM_DEFS[i][3];
                boolean full = (boolean) ITEM_DEFS[i][4];
                int qty      = inventory.getOrDefault(name, 0);
                String effect = full ? "Full restore"
                        : (hp > 0 ? "+" + hp + " HP" : "") + (mana > 0 ? " +" + mana + " MP" : "");
                options[i] = name + " - " + cost + "g (" + effect + ") [owned: " + qty + "]";
            }
            options[ITEM_DEFS.length] = "Leave Shop";

            String pick = (String) JOptionPane.showInputDialog(parentPanel,
                    "Gold: " + goldRef[0] + "  |  Inventory: " + inventorySummary(inventory),
                    "Inn Shop", JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
            if (pick == null || pick.startsWith("Leave")) break;

            for (Object[] def : ITEM_DEFS) {
                if (pick.startsWith((String) def[0])) {
                    int cost = (int) def[1];
                    if (goldRef[0] < cost) {
                        JOptionPane.showMessageDialog(parentPanel, "Not enough gold!", "Shop", JOptionPane.WARNING_MESSAGE);
                    } else {
                        goldRef[0] -= cost;
                        gameSubject.notifyGoldChanged(goldRef[0]);
                        String name = (String) def[0];
                        inventory.put(name, inventory.getOrDefault(name, 0) + 1);
                        logFn.accept("Bought " + name + " for " + cost + "g. [" + name + " x" + inventory.get(name) + "]");
                        refreshStatsFn.run();
                    }
                    break;
                }
            }
        }
    }

    /**
     * Handles using inventory items outside of battle.
     */
    public void useItems(List<Hero> party, Map<String, Integer> inventory) {
        while (true) {
            List<String> available = new ArrayList<>();
            for (Map.Entry<String, Integer> e : inventory.entrySet())
                if (e.getValue() > 0) available.add(e.getKey() + " x" + e.getValue());
            if (available.isEmpty()) break;
            available.add("Close");

            String pick = (String) JOptionPane.showInputDialog(parentPanel,
                    "Select item to use:", "Inventory",
                    JOptionPane.PLAIN_MESSAGE, null, available.toArray(), available.get(0));
            if (pick == null || pick.equals("Close")) break;

            String itemName  = pick.split(" x")[0];
            String[] names   = party.stream().map(Hero::getName).toArray(String[]::new);
            String targetName = (String) JOptionPane.showInputDialog(parentPanel,
                    "Use " + itemName + " on which hero?", "Use Item",
                    JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
            if (targetName == null) continue;

            Hero target = party.stream().filter(h -> h.getName().equals(targetName))
                    .findFirst().orElse(party.get(0));
            for (Object[] def : ITEM_DEFS) {
                if (def[0].equals(itemName)) {
                    int hp = (int) def[2]; int mana = (int) def[3]; boolean full = (boolean) def[4];
                    if (full) target.fullRestore();
                    else { if (hp > 0) target.heal(hp); if (mana > 0) target.restoreMana(mana); }
                    inventory.put(itemName, inventory.get(itemName) - 1);
                    logFn.accept(target.getName() + " used " + itemName + "!");
                    refreshStatsFn.run();
                    break;
                }
            }
        }
    }

    public static String inventorySummary(Map<String, Integer> inventory) {
        if (inventory.isEmpty()) return "empty";
        return inventory.entrySet().stream()
                .filter(e -> e.getValue() > 0)
                .map(e -> e.getKey() + "x" + e.getValue())
                .collect(java.util.stream.Collectors.joining(", "));
    }
}