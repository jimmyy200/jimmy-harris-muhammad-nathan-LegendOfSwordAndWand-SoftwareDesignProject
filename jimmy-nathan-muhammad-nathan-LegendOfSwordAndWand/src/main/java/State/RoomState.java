package State;

import javax.swing.JButton;

public interface RoomState {
    void enter(RoomContext context);
    void onAttack(RoomContext context);
    void onDefend(RoomContext context);
    void onWait(RoomContext context);
    void onCast(RoomContext context);
    void onNextRoom(RoomContext context);
    void onUseItems(RoomContext context);
    void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                          JButton btnCast, JButton btnNextRoom, JButton btnUseItems,
                          JButton btnParty, JButton btnExit);
}