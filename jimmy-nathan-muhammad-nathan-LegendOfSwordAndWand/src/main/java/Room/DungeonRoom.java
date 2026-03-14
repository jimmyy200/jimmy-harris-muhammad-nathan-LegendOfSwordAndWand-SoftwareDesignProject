package Room;

public class DungeonRoom extends Room {

    public DungeonRoom() {
        super("Dungeon Room", "A dark and dangerous dungeon filled with monsters.");
    }

    @Override
    public void onEnter() {
        System.out.println("You enter the " + name + ". " + description);
        System.out.println("You sense danger lurking in the shadows...");
    }
}