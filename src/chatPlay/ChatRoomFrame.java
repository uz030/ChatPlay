package chatPlay;

import javax.swing.*;

/**
 * 채팅방 프레임
 * 채팅방 창을 관리하는 독립 윈도우
 */
public class ChatRoomFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private ChatRoomPanel panel;

    /**
     * 채팅방 프레임 생성자
     * @param parent 부모 클라이언트 프레임
     * @param roomData 채팅방 데이터
     */
    public ChatRoomFrame(ChatClientMain parent, ChatRoomData roomData) {
        setTitle(roomData.getRoomName());
        setSize(392, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        panel = new ChatRoomPanel(parent, roomData);
        setContentPane(panel);
    }

    /**
     * 메시지 추가
     * @param msg 채팅 메시지
     */
    public void appendMessage(ChatMessage msg) {
        panel.addBubble(msg);
    }
    
    /**
     * 채팅방 패널 조회
     */
    public ChatRoomPanel getChatRoomPanel() {
        return panel;
    }
    
    /**
     * 패널 전환 (날씨, 뉴스 등 다른 화면으로 전환)
     * @param panel 전환할 패널
     */
    public void switchToPanel(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

}
