package chatPlay;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends JPanel {
    public ChatPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 255, 245));

        JLabel lbl = new JLabel("채팅 화면입니다.", SwingConstants.CENTER);

        add(lbl, BorderLayout.CENTER);
    }
}
