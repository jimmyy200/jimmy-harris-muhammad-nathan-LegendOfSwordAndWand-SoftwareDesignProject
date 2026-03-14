package Room;

public class Inn extends Room {

    public Inn() {
        super("Inn", "A cozy inn where weary travelers can rest and recover.");
    }

    @Override
    public void onEnter() {
        System.out.println("You enter the " + name + ". " + description);
        System.out.println("The innkeeper greets you warmly. Your HP has been fully restored!");
    }
}