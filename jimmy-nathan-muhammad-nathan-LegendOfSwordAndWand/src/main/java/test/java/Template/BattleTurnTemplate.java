package Template;

import Hero.Hero;
import Mob.Mob;
import java.util.List;

// Template Method pattern - defines the skeleton of a battle turn
// Subclasses can override specific steps while keeping the overall flow
public abstract class BattleTurnTemplate {

    // Template method - defines the fixed steps of a battle turn
    public final void executeTurn(List<Hero> party, List<Mob> mobs) {
        determineTurnOrder(party);
        executeHeroActions(party, mobs);
        executeMobActions(party, mobs);
        applyEndOfTurnEffects(party, mobs);
        checkBattleResult(party, mobs);
    }

    // Step 1: determine who goes first
    protected abstract void determineTurnOrder(List<Hero> party);

    // Step 2: heroes perform their queued actions
    protected abstract void executeHeroActions(List<Hero> party, List<Mob> mobs);

    // Step 3: mobs perform their actions
    protected abstract void executeMobActions(List<Hero> party, List<Mob> mobs);

    // Step 4: apply effects at end of turn (shields, stun recovery, etc.)
    protected void applyEndOfTurnEffects(List<Hero> party, List<Mob> mobs) {
        // default: clear stun on heroes that were stunned this round
        for (Hero h : party) {
            if (h.isStunned()) {
                h.setStunned(false);
            }
        }
    }

    // Step 5: check if battle is over
    protected abstract void checkBattleResult(List<Hero> party, List<Mob> mobs);
}
