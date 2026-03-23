package Template;

import Hero.Hero;
import Mob.Mob;
import java.util.List;

// battle turn template
// other classes can change this
public abstract class BattleTurnTemplate {

    // steps of a turn
    public final void executeTurn(List<Hero> party, List<Mob> mobs) {
        determineTurnOrder(party);
        executeHeroActions(party, mobs);
        executeMobActions(party, mobs);
        applyEndOfTurnEffects(party, mobs);
        checkBattleResult(party, mobs);
    }

    // who goes first
    protected abstract void determineTurnOrder(List<Hero> party);

    // step 2 heroes attack
    protected abstract void executeHeroActions(List<Hero> party, List<Mob> mobs);

    // step 3 enemies attack
    protected abstract void executeMobActions(List<Hero> party, List<Mob> mobs);

    // step 4 end of turn stuff
    protected void applyEndOfTurnEffects(List<Hero> party, List<Mob> mobs) {
        // remove stun
        for (Hero h : party) {
            if (h.isStunned()) {
                h.setStunned(false);
            }
        }
    }

    // step 5 is battle over
    protected abstract void checkBattleResult(List<Hero> party, List<Mob> mobs);
}
