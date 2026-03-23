package Observer;

import java.util.ArrayList;
import java.util.List;

// Subject that observers register to for game event notifications
public class GameSubject {
    private final List<GameObserver> observers = new ArrayList<>();

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void notifyLevelUp(String heroName, int newLevel, String className) {
        for (GameObserver o : observers) {
            o.onHeroLevelUp(heroName, newLevel, className);
        }
    }

    public void notifyHeroDefeated(String heroName) {
        for (GameObserver o : observers) {
            o.onHeroDefeated(heroName);
        }
    }

    public void notifyGoldChanged(int newGold) {
        for (GameObserver o : observers) {
            o.onGoldChanged(newGold);
        }
    }

    public void notifyExperienceGained(String heroName, int xpGained) {
        for (GameObserver o : observers) {
            o.onExperienceGained(heroName, xpGained);
        }
    }
}
