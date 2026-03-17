package State;

import javax.swing.JButton;

/**
 * Inn visit — items and navigation enabled, combat disabled.
 */
public class InnState implements RoomState {

    @Override
    public void enter(RoomContext context) {
        System.out.println("State: Entering Inn");
    }

    @Override
    public void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                                 JButton btnCast, JButton btnNextRoom, JButton btnUseItems) {
        btnAttack.setEnabled(false);
        btnDefend.setEnabled(false);
        btnWait.setEnabled(false);
        btnCast.setEnabled(false);
        btnNextRoom.setEnabled(true);
        btnUseItems.setEnabled(true);
    }

    @Override public void onAttack(RoomContext context)   {}
    @Override public void onDefend(RoomContext context)   {}
    @Override public void onWait(RoomContext context)     {}
    @Override public void onCast(RoomContext context)     {}
    @Override public void onNextRoom(RoomContext context) {}
    @Override public void onUseItems(RoomContext context) {}
}