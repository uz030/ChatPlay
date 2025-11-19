package chatPlay;

import java.util.Vector;
import chatPlay.ChatServer.UserService;

public class Room {

    private int roomId;
    private String roomName;
    private Vector<UserService> participants;

    public Room(int roomId, String roomName) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.participants = new Vector<>();
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public Vector<UserService> getParticipants() {
        return participants;
    }

    public void addParticipant(UserService user) {
        participants.add(user);
    }

    public void removeParticipant(UserService user) {
        participants.remove(user);
    }

    public void broadcast(String msg) {
        for (UserService us : participants) {
            us.WriteOne("[Room " + roomId + "] " + msg);
        }
    }
}
