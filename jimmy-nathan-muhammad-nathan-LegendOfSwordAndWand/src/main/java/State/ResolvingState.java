package State;

import javax.swing.JButton;

public class ResolvingState implements RoomState {
    @Override public void enter(RoomContext context) {}
    @Override public void onAttack(RoomContext c)   {}
    @Override public void onDefend(RoomContext c)   {}
    @Override public void onWait(RoomContext c)     {}
    @Override public void onCast(RoomContext c)     {}
    @Override public void onNextRoom(RoomContext c) {}
    @Override public void onUseItems(RoomContext c) {}

    @Override
    public void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                                 JButton btnCast, JButton btnNextRoom, JButton btnUseItems,
                                 JButton btnParty, JButton btnExit) {
        btnAttack.setEnabled(false); btnDefend.setEnabled(false);
        btnWait.setEnabled(false);   btnCast.setEnabled(false);
        btnNextRoom.setEnabled(false); btnUseItems.setEnabled(false);
        btnParty.setEnabled(false);  btnExit.setEnabled(false);
    }
}