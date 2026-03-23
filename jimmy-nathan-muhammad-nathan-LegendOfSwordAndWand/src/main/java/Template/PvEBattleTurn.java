package Template;

import Hero.Hero;
import Mob.Mob;
import java.util.*;
import java.util.stream.Collectors;

// pve battle turn logic
public class PvEBattleTurn extends BattleTurnTemplate {

    private List<Hero> turnOrder;
    private List<Runnable> heroActions;
    private final Random random = new Random();

    public void setHeroActions(List<Runnable> actions) {
        this.heroActions = actions;
    }

    @Override
    protected void determineTurnOrder(List<Hero> party) {
        // sort by level then attack
        turnOrder = party.stream()
                .filter(h -> h.isAlive() && !h.isStunned())
                .sorted((a, b) -> {
                    if (b.getLevel() != a.getLevel()) return b.getLevel() - a.getLevel();
                    return b.getAttack() - a.getAttack();
                })
                .collect(Collectors.toList());
    }

    @Override
    protected void executeHeroActions(List<Hero> party, List<Mob> mobs) {
        if (heroActions != null) {
            for (Runnable action : heroActions) {
                action.run();
            }
        }
    }

    @Override
    protected void executeMobActions(List<Hero> party, List<Mob> mobs) {
        List<Hero> living = party.stream()
                .filter(Hero::isAlive)
                .collect(Collectors.toList());
        if (living.isEmpty()) return;

        for (Mob mob : mobs) {
            if (!mob.isAlive()) continue;
            int action = random.nextInt(3);
            if (action == 0) {
                Hero target = living.get(random.nextInt(living.size()));
                mob.attack(target);
            }
            // defend or wait
        }
    }

    @Override
    protected void checkBattleResult(List<Hero> party, List<Mob> mobs) {
        // checked by panel
    }

    public List<Hero> getTurnOrder() {
        return turnOrder;
    }
}
