package State;

import javax.swing.JButton;

/**
 * Party defeated — only Next Room enabled (to return to inn).
 */
public class DefeatedState implements RoomState {

    @Override
    public void enter(RoomContext context) {
        System.out.println("State: Defeated.");
    }

    @Override
    public void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                                 JButton btnCast, JButton btnNextRoom, JButton btnUseItems) {
        btnAttack.setEnabled(false);
        btnDefend.setEnabled(false);
        btnWait.setEnabled(false);
        btnCast.setEnabled(false);
        btnNextRoom.setEnabled(true);
        btnUseItems.setEnabled(false);
    }

    @Override public void onAttack(RoomContext context)   {}
    @Override public void onDefend(RoomContext context)   {}
    @Override public void onWait(RoomContext context)     {}
    @Override public void onCast(RoomContext context)     {}
    @Override public void onNextRoom(RoomContext context) {}
    @Override public void onUseItems(RoomContext context) {}
}