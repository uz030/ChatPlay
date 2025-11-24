package chatPlay;

import javax.swing.DefaultListModel;

public class ChatRoomData {
    private int roomId;
    private String roomName;
    private DefaultListModel<ChatMessage> messageLog;

    public ChatRoomData(int roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.messageLog = new DefaultListModel<>();
    }

    public int getRoomId() { return roomId; }
    public String getRoomName() { return roomName; }
    public DefaultListModel<ChatMessage> getMessageLog() { return messageLog; }
    
    @Override
    public String toString() { return roomName; }
}