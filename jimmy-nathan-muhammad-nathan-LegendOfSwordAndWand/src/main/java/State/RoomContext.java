package State;

import javax.swing.JButton;

/**
 * Context held by GamePanel — delegates button behaviour to the current RoomState.
 */
public class RoomContext {

    private RoomState currentState;

    private final JButton btnAttack;
    private final JButton btnDefend;
    private final JButton btnWait;
    private final JButton btnCast;
    private final JButton btnNextRoom;
    private final JButton btnUseItems;

    public RoomContext(JButton btnAttack, JButton btnDefend, JButton btnWait,
                       JButton btnCast, JButton btnNextRoom, JButton btnUseItems) {
        this.btnAttack   = btnAttack;
        this.btnDefend   = btnDefend;
        this.btnWait     = btnWait;
        this.btnCast     = btnCast;
        this.btnNextRoom = btnNextRoom;
        this.btnUseItems = btnUseItems;
    }

    public void setState(RoomState state) {
        this.currentState = state;
        state.enter(this);
        state.configureButtons(btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems);
    }

    public RoomState getState() { return currentState; }

    // Delegate button actions to current state
    public void onAttack()   { if (currentState != null) currentState.onAttack(this); }
    public void onDefend()   { if (currentState != null) currentState.onDefend(this); }
    public void onWait()     { if (currentState != null) currentState.onWait(this); }
    public void onCast()     { if (currentState != null) currentState.onCast(this); }
    public void onNextRoom() { if (currentState != null) currentState.onNextRoom(this); }
    public void onUseItems() { if (currentState != null) currentState.onUseItems(this); }
}