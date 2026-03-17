package State;

import javax.swing.JButton;

/**
 * Mid-round resolution — all buttons disabled while actions play out.
 */
public class ResolvingState implements RoomState {

    @Override
    public void enter(RoomContext context) {
        System.out.println("State: Resolving round...");
    }

    @Override
    public void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                                 JButton btnCast, JButton btnNextRoom, JButton btnUseItems) {
        btnAttack.setEnabled(false);
        btnDefend.setEnabled(false);
        btnWait.setEnabled(false);
        btnCast.setEnabled(false);
        btnNextRoom.setEnabled(false);
        btnUseItems.setEnabled(false);
    }

    @Override public void onAttack(RoomContext context)   {}
    @Override public void onDefend(RoomContext context)   {}
    @Override public void onWait(RoomContext context)     {}
    @Override public void onCast(RoomContext context)     {}
    @Override public void onNextRoom(RoomContext context) {}
    @Override public void onUseItems(RoomContext context) {}
}