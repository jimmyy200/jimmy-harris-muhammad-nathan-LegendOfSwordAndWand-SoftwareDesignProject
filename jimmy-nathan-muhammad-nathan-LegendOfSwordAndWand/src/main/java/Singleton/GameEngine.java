package Singleton;

public class GameEngine {
    private static GameEngine instance;

    // Private constructor - prevents direct instantiation
    private GameEngine() {}

    // The one and only way to get the instance
    public static GameEngine getInstance() {
        if (instance == null) {
            instance = new GameEngine();
        }
        return instance;
    }

    public void startGame() {
        System.out.println("GameEngine: Starting game...");
        // TODO: initialise game state, load the first room, spawn mobs, etc.
    }

    public void runTurn() {
        System.out.println("GameEngine: Running turn...");
        // TODO: process player action → mob reaction → update state
    }
}