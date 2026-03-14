package Room;

public abstract class Room {
    protected String name;
    protected String description;

    public Room(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public abstract void onEnter();
}