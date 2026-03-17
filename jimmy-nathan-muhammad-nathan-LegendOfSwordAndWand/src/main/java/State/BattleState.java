package State;

import javax.swing.JButton;

/**
 * Active battle — combat buttons enabled, navigation disabled.
 */
public class BattleState implements RoomState {

    @Override
    public void enter(RoomContext context) {
        System.out.println("State: Entering Battle");
    }

    @Override
    public void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                                 JButton btnCast, JButton btnNextRoom, JButton btnUseItems) {
        btnAttack.setEnabled(true);
        btnDefend.setEnabled(true);
        btnWait.setEnabled(true);
        btnCast.setEnabled(true);
        btnNextRoom.setEnabled(false);
        btnUseItems.setEnabled(false);
    }

    // In BattleState these are handled by GamePanel's queue system
    @Override public void onAttack(RoomContext context)   {}
    @Override public void onDefend(RoomContext context)   {}
    @Override public void onWait(RoomContext context)     {}
    @Override public void onCast(RoomContext context)     {}
    @Override public void onNextRoom(RoomContext context) {}
    @Override public void onUseItems(RoomContext context) {}
}