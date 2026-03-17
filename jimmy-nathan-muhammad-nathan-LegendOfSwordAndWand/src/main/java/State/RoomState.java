package State;

import javax.swing.JButton;

/**
 * State pattern — each room state controls which buttons are active
 * and defines what happens when the player enters that state.
 */
public interface RoomState {
    void enter(RoomContext context);
    void onAttack(RoomContext context);
    void onDefend(RoomContext context);
    void onWait(RoomContext context);
    void onCast(RoomContext context);
    void onNextRoom(RoomContext context);
    void onUseItems(RoomContext context);
    void configureButtons(JButton btnAttack, JButton btnDefend, JButton btnWait,
                          JButton btnCast, JButton btnNextRoom, JButton btnUseItems);
}