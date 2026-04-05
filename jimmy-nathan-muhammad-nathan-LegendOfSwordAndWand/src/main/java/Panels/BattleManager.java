package Panels;

import Hero.*;
import Mob.*;
import Observer.GameSubject;
import State.*;

import javax.swing.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;

/**
 * Extracted Class — God Class Refactoring (#1):
 * GamePanel had WMC=228, CBO=63, ATFD=17 handling UI, battle logic,
 * inn logic, saving, and PvP all in one class.
 *
 * BattleManager is responsible for all turn-based battle logic:
 * round management, action queuing, damage resolution, and battle end detection.
 * GamePanel delegates to this class for everything combat-related.
 */
public class BattleManager {

    private static final Random random = new Random();

    // battle state
    private List<Hero>     party;
    private List<Mob>      currentMobs;
    private boolean        inBattle;
    private int            gold;

    // turn tracking
    private Queue<Hero>    turnQueue;
    private Hero           activeHero;
    private List<Runnable> pendingActions;
    private List<Hero>     waitQueue;

    // pvp tracking
    private boolean    isPvP;
    private String     pvpPlayer1Name;
    private String     pvpPlayer2Name;
    private List<Hero> pvpPlayer2Party;
    private boolean    pvpPlayer2Turn;
    private List<Runnable> pvpPlayer2Actions;
    private Queue<Hero>    pvpPlayer2TurnQueue;
    private List<Hero>     pvpPlayer2WaitQueue;
    private java.util.function.BiConsumer<String, String> pvpResultCallback;

    // callbacks back to GamePanel for UI updates and game flow
    private final Runnable         onVictory;        // called when PvE battle won
    private final Runnable         onDefeat;         // called when party wiped
    private final GameSubject      gameSubject;
    private final java.util.function.Consumer<String>     logFn;
    private final Runnable         refreshStatsFn;
    private final Runnable         refreshMobsFn;
    private final java.util.function.Consumer<Hero>       setActiveHeroFn;
    private final java.util.function.Consumer<String>     setTurnLabelFn;
    private final java.util.function.Consumer<RoomState>  setStateFn;

    public BattleManager(
            GameSubject gameSubject,
            java.util.function.Consumer<String> logFn,
            Runnable refreshStatsFn,
            Runnable refreshMobsFn,
            java.util.function.Consumer<Hero> setActiveHeroFn,
            java.util.function.Consumer<String> setTurnLabelFn,
            java.util.function.Consumer<RoomState> setStateFn,
            Runnable onVictory,
            Runnable onDefeat) {
        this.gameSubject     = gameSubject;
        this.logFn           = logFn;
        this.refreshStatsFn  = refreshStatsFn;
        this.refreshMobsFn   = refreshMobsFn;
        this.setActiveHeroFn = setActiveHeroFn;
        this.setTurnLabelFn  = setTurnLabelFn;
        this.setStateFn      = setStateFn;
        this.onVictory       = onVictory;
        this.onDefeat        = onDefeat;
    }

    // start game

    public void initPvE(List<Hero> party, List<Mob> mobs, int gold) {
        this.party       = party;
        this.currentMobs = mobs;
        this.gold        = gold;
        this.inBattle    = true;
        this.isPvP       = false;
    }

    public void initPvP(List<Hero> p1Party, String p1Name,
                        List<Hero> p2Party, String p2Name,
                        java.util.function.BiConsumer<String, String> callback) {
        this.party            = p1Party;
        this.pvpPlayer1Name   = p1Name;
        this.pvpPlayer2Party  = new ArrayList<>(p2Party);
        this.pvpPlayer2Name   = p2Name;
        this.pvpResultCallback = callback;
        this.isPvP            = true;
        this.inBattle         = true;

        this.currentMobs = new ArrayList<>();
        for (Hero opponent : pvpPlayer2Party) currentMobs.add(new PvPMob(opponent));
    }

    public boolean isInBattle() { return inBattle; }
    public int getGold()        { return gold; }
    public void setGold(int g)  { this.gold = g; }
    public Hero getActiveHero() { return activeHero; }
    public List<Mob> getCurrentMobs() { return currentMobs; }
    public boolean isPvP()      { return isPvP; }
    public boolean isPvP2Turn() { return pvpPlayer2Turn; }
    public List<Hero> getPvP2Party() { return pvpPlayer2Party; }

    // managing rounds

    public void startNewRound() {
        pendingActions  = new ArrayList<>();
        waitQueue       = new ArrayList<>();
        pvpPlayer2Turn  = false;

        if (isPvP) {
            pvpPlayer2Actions   = new ArrayList<>();
            pvpPlayer2WaitQueue = new ArrayList<>();
        }

        clearStunEffects(party);
        turnQueue = new LinkedList<>(buildTurnOrder(party));

        if (isPvP) {
            clearStunEffects(pvpPlayer2Party);
            pvpPlayer2TurnQueue = new LinkedList<>(buildTurnOrder(pvpPlayer2Party));
        }

        log("--- New round. ---");
        promptNextHero();
    }

    private List<Hero> buildTurnOrder(List<Hero> heroes) {
        return heroes.stream()
                .filter(h -> h.isAlive() && !h.isStunned())
                .sorted((a, b) -> {
                    if (b.getLevel() != a.getLevel()) return b.getLevel() - a.getLevel();
                    return b.getAttack() - a.getAttack();
                })
                .collect(Collectors.toList());
    }

    private void clearStunEffects(List<Hero> heroes) {
        for (Hero h : heroes) {
            if (h.isStunned()) {
                h.setStunned(false);
                log(h.getName() + " recovers from stun.");
            }
        }
    }

    public void promptNextHero() {
        if (!pvpPlayer2Turn) {
            if (turnQueue.isEmpty()) {
                if (!waitQueue.isEmpty()) {
                    activeHero = waitQueue.remove(0);
                    setActiveHeroFn.accept(activeHero);
                    setTurnLabelFn.accept(activeHero.getName() + "'s turn (waited)");
                    setStateFn.accept(new BattleState());
                    refreshStatsFn.run();
                    return;
                }
                if (isPvP) {
                    pvpPlayer2Turn = true;
                    log("--- " + pvpPlayer2Name + "'s Turn ---");
                    promptNextHero();
                    return;
                }
                resolveRound();
                return;
            }
            activeHero = turnQueue.poll();
            setActiveHeroFn.accept(activeHero);
            setTurnLabelFn.accept(activeHero.getName() + "'s turn (" + activeHero.getClassName() + ")");
        } else {
            if (pvpPlayer2TurnQueue.isEmpty()) {
                if (!pvpPlayer2WaitQueue.isEmpty()) {
                    activeHero = pvpPlayer2WaitQueue.remove(0);
                    setActiveHeroFn.accept(activeHero);
                    setTurnLabelFn.accept(activeHero.getName() + "'s turn (waited)");
                    setStateFn.accept(new BattleState());
                    refreshStatsFn.run();
                    return;
                }
                resolveRound();
                return;
            }
            activeHero = pvpPlayer2TurnQueue.poll();
            setActiveHeroFn.accept(activeHero);
            setTurnLabelFn.accept(activeHero.getName() + "'s turn (" + activeHero.getClassName() + ")");
        }
        setStateFn.accept(new BattleState());
        refreshStatsFn.run();
    }

    // queue for attack/wait/defend

    public void queueAttack(Object targetRef) {
        if (activeHero == null) return;
        final Hero attacker    = activeHero;
        final boolean actAsP2  = isPvP && pvpPlayer2Turn;

        Runnable action = () -> {
            if (targetRef instanceof Hero) {
                Hero t = (Hero) targetRef;
                if (!t.isAlive()) return;
                int dmg = Math.max(0, attacker.getAttack() - t.getDefense());
                t.takeDamage(dmg);
                log(attacker.getName() + " attacks " + t.getName() + " for " + dmg + " damage!");
            } else if (targetRef instanceof Mob) {
                Mob t = (Mob) targetRef;
                if (!t.isAlive()) return;
                int dmg = Math.max(0, attacker.getAttack() - t.getDefense());
                t.takeDamage(dmg);
                log(attacker.getName() + " attacks for " + dmg + " damage!");
                applyHybridAttackEffects(attacker, t);
            }
        };

        if (actAsP2) pvpPlayer2Actions.add(action);
        else         pendingActions.add(action);

        log(activeHero.getName() + " chose: Attack");
        activeHero = null;
        promptNextHero();
    }

    public void queueDefend() {
        if (activeHero == null) return;
        final Hero defender   = activeHero;
        final boolean actAsP2 = isPvP && pvpPlayer2Turn;

        Runnable action = () -> {
            defender.defend();
            log(defender.getName() + " defends. +10 HP, +5 Mana.");
        };

        if (actAsP2) pvpPlayer2Actions.add(action);
        else         pendingActions.add(action);

        log(activeHero.getName() + " chose: Defend");
        activeHero = null;
        promptNextHero();
    }

    public void queueWait() {
        if (activeHero == null) return;
        log(activeHero.getName() + " chose: Wait");
        if (isPvP && pvpPlayer2Turn) pvpPlayer2WaitQueue.add(activeHero);
        else waitQueue.add(activeHero);
        activeHero = null;
        promptNextHero();
    }

    public void queueCast(String spell) {
        if (activeHero == null) return;
        final Hero caster     = activeHero;
        final boolean actAsP2 = isPvP && pvpPlayer2Turn;

        final List<Hero> friends = actAsP2 ? pvpPlayer2Party : party;
        final List<Hero> enemies = actAsP2 ? party : pvpPlayer2Party;

        Runnable action = () -> {
            Hero[] friendlyArr = friends.stream().filter(Hero::isAlive).toArray(n -> new Hero[n]);
            Mob[]  enemyMobs   = isPvP
                    ? enemies.stream().map(PvPMob::new).toArray(n -> new Mob[n])
                    : currentMobs.toArray(new Mob[0]);
            castSpell(caster, spell, friendlyArr, enemyMobs);
        };

        if (actAsP2) pvpPlayer2Actions.add(action);
        else         pendingActions.add(action);

        log(activeHero.getName() + " chose: " + spell);
        activeHero = null;
        promptNextHero();
    }

    // resolving round method

    private void resolveRound() {
        setStateFn.accept(new ResolvingState());
        setTurnLabelFn.accept("Resolving round...");

        log("=== Turn 1 Actions ===");
        for (Runnable a : pendingActions) a.run();
        refreshStatsFn.run();
        refreshMobsFn.run();
        if (checkBattleEnd()) return;

        if (isPvP) {
            log("=== Turn 2 Actions ===");
            for (Runnable a : pvpPlayer2Actions) a.run();
            refreshStatsFn.run();
            refreshMobsFn.run();
            if (checkBattleEnd()) return;
            startNewRound();
        } else {
            log("=== Enemies act ===");
            mobsTurn();
        }
    }

    private void mobsTurn() {
        List<Hero> living = party.stream().filter(Hero::isAlive).collect(Collectors.toList());
        if (living.isEmpty()) { handleDefeat(); return; }

        for (Mob mob : currentMobs) {
            if (!mob.isAlive()) continue;
            int action = random.nextInt(3);
            if (action == 0) {
                Hero target = living.get(random.nextInt(living.size()));
                mob.attack(target);
                log("Enemy attacks " + target.getName() + "!");
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
        refreshStatsFn.run();
        refreshMobsFn.run();
        if (party.stream().noneMatch(Hero::isAlive)) { handleDefeat(); return; }
        if (inBattle) startNewRound();
    }

    // end battle method

    private boolean checkBattleEnd() {
        if (isPvP) {
            boolean p1Dead = party.stream().noneMatch(Hero::isAlive);
            boolean p2Dead = pvpPlayer2Party.stream().noneMatch(Hero::isAlive);
            if (!p1Dead && !p2Dead) return false;

            resetBattleState();
            String winner = p2Dead ? pvpPlayer1Name : pvpPlayer2Name;
            log("=== " + winner + " wins the PvP battle! ===");
            setStateFn.accept(new VictoryState());
            if (pvpResultCallback != null)
                pvpResultCallback.accept(winner, p2Dead ? pvpPlayer2Name : pvpPlayer1Name);
            return true;
        }

        if (currentMobs.stream().anyMatch(Mob::isAlive)) return false;

        resetBattleState();
        onVictory.run();
        return true;
    }

    private void handleDefeat() {
        resetBattleState();
        onDefeat.run();
    }

    private void resetBattleState() {
        inBattle       = false;
        turnQueue      = null;
        activeHero     = null;
        pendingActions = null;
        waitQueue      = null;
        setActiveHeroFn.accept(null);
        setTurnLabelFn.accept(" ");
    }

    // spell methods

    private void castSpell(Hero caster, String spell, Hero[] partyArr, Mob[] mobs) {
        switch (spell) {
            case "Protect":          ((Order)   caster).protect(partyArr);                    break;
            case "Fire Shield":      ((Order)   caster).fireShield(partyArr);                 break;
            case "Heal":             ((Order)   caster).heal(partyArr);                       break;
            case "Fireball":         ((Chaos)   caster).fireball(getMobsAsHeroArray(mobs));   break;
            case "Chain Lightning":  ((Chaos)   caster).chainLightning(getMobsAsHeroArray(mobs)); break;
            case "Berserker Attack": ((Warrior) caster).berserkerAttack(getMobsAsHeroArray(mobs)); break;
            case "Replenish":        ((Mage)    caster).replenish(partyArr);                  break;
        }
        refreshMobsFn.run();
    }

    private Hero[] getMobsAsHeroArray(Mob[] mobs) {
        List<Hero> targets = new ArrayList<>();
        for (Mob m : mobs)
            if (m instanceof PvPMob && m.isAlive()) targets.add(((PvPMob) m).getHero());
        return targets.toArray(new Hero[0]);
    }

    private void applyHybridAttackEffects(Hero attacker, Mob target) {
        if (attacker instanceof Warrior && attacker.isHybrid()
                && "ROGUE".equals(attacker.getHybridClass()) && random.nextBoolean()) {
            List<Mob> alive = currentMobs.stream().filter(Mob::isAlive).collect(Collectors.toList());
            if (!alive.isEmpty()) {
                Mob bonus = alive.get(random.nextInt(alive.size()));
                int bonusDmg = (int)(Math.max(0, attacker.getAttack() - bonus.getDefense()) * 0.5);
                bonus.takeDamage(bonusDmg);
                log("Sneak Attack! Bonus hit for " + bonusDmg + "!");
            }
        }
        if (attacker instanceof Mage && attacker.isHybrid()
                && "WARLOCK".equals(attacker.getHybridClass()) && target instanceof PvPMob) {
            ((Mage) attacker).manaBurn(((PvPMob) target).getHero());
        }
    }

    // helper methods

    /** Distribute XP among survivors and trigger level-ups via callback. */
    public void distributeXP(java.util.function.Consumer<Hero> onLevelUp) {
        int totalXp   = currentMobs.stream().mapToInt(Mob::getXpReward).sum();
        int totalGold = currentMobs.stream().mapToInt(Mob::getGoldReward).sum();
        gold += totalGold;
        gameSubject.notifyGoldChanged(gold);

        List<Hero> survivors = party.stream().filter(Hero::isAlive).collect(Collectors.toList());
        int xpEach = survivors.isEmpty() ? 0 : totalXp / survivors.size();
        log("--- Victory! +" + totalXp + " XP (each gets " + xpEach + "), +" + totalGold + " Gold ---");

        for (Hero h : survivors) {
            int before = h.getLevel();
            gameSubject.notifyExperienceGained(h.getName(), xpEach);
            h.gainExperience(xpEach);
            if (h.getLevel() > before) {
                gameSubject.notifyLevelUp(h.getName(), h.getLevel(), h.getClassName());
                onLevelUp.accept(h);
            }
            if (h.getLevel() < 20)
                log(h.getName() + ": " + h.getExperience() + " XP | "
                        + (h.expNeededForLevel(h.getLevel() + 1) - h.getExperience()) + " to next level");
        }
    }

    public int getTotalXpReward()   { return currentMobs.stream().mapToInt(Mob::getXpReward).sum(); }
    public int getTotalGoldReward() { return currentMobs.stream().mapToInt(Mob::getGoldReward).sum(); }

    private void log(String msg) { logFn.accept(msg); }
}