package Observer;

// observer pattern subject
public interface Subject {
    void attach(GameObserver observer);
    void detach(GameObserver observer);
    void notifyObservers();
}
