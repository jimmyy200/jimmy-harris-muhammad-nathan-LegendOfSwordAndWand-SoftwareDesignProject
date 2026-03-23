package Observer;

// Subject interface for Observer pattern - defines how observers register and get notified
public interface Subject {
    void attach(GameObserver observer);
    void detach(GameObserver observer);
    void notifyObservers();
}
