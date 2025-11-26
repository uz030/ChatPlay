package chatPlay;

import javax.swing.*;
import java.awt.*;

public class UserSelectButton extends JButton {

    private static final long serialVersionUID = 1L;

    private boolean selected = false;
    private String username;

    public UserSelectButton(String username) {
        this.username = username;

        setText(username);
        setHorizontalAlignment(SwingConstants.CENTER);
        setFocusPainted(false);

        setOpaque(false);               
        setContentAreaFilled(false);
        
        setForeground(Color.BLACK);
        setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        setBorder(BorderFactory.createLineBorder(new Color(161, 161, 161), 1));

        // 크기 설정 유지
        setPreferredSize(new Dimension(0, 50));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        setMinimumSize(new Dimension(0, 50));

        // 선택 기능 유지
        addActionListener(e -> {
            selected = !selected;
            updateStyle();
        });
    }

    public boolean isSelectedUser() {
        return selected;
    }

    public String getUsername() {
        return username;
    }

    private void updateStyle() {
        if (selected) {
            // 선택될 때만 파란 배경 등장
            setOpaque(true);
            setBackground(new Color(109, 135, 255)); 
            setForeground(Color.WHITE);
        } else {
            // 선택 해제 시 완전 투명
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            setForeground(Color.BLACK);
        }
    }
}
