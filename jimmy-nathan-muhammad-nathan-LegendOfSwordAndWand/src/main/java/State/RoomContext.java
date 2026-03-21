package State;

import javax.swing.JButton;

public class RoomContext {
    private RoomState currentState;
    private final JButton btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems, btnParty, btnExit;

    public RoomContext(JButton btnAttack, JButton btnDefend, JButton btnWait,
                       JButton btnCast, JButton btnNextRoom, JButton btnUseItems,
                       JButton btnParty, JButton btnExit) {
        this.btnAttack   = btnAttack;
        this.btnDefend   = btnDefend;
        this.btnWait     = btnWait;
        this.btnCast     = btnCast;
        this.btnNextRoom = btnNextRoom;
        this.btnUseItems = btnUseItems;
        this.btnParty    = btnParty;
        this.btnExit     = btnExit;
    }

    public void setState(RoomState state) {
        this.currentState = state;
        state.enter(this);
        state.configureButtons(btnAttack, btnDefend, btnWait, btnCast, btnNextRoom, btnUseItems, btnParty, btnExit);
    }

    public RoomState getState() { return currentState; }
    public void onAttack()   { if (currentState != null) currentState.onAttack(this); }
    public void onDefend()   { if (currentState != null) currentState.onDefend(this); }
    public void onWait()     { if (currentState != null) currentState.onWait(this); }
    public void onCast()     { if (currentState != null) currentState.onCast(this); }
    public void onNextRoom() { if (currentState != null) currentState.onNextRoom(this); }
    public void onUseItems() { if (currentState != null) currentState.onUseItems(this); }
}