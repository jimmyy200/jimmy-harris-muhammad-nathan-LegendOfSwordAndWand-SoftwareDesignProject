package Observer;

// gets game events
public interface GameObserver {
    void onHeroLevelUp(String heroName, int newLevel, String className);
    void onHeroDefeated(String heroName);
    void onGoldChanged(int newGold);
    void onExperienceGained(String heroName, int xpGained);
}
