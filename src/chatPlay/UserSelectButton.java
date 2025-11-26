package chatPlay;

import javax.swing.*;
import java.awt.*;

public class UserSelectButton extends JButton {

    private static final long serialVersionUID = 1L;

    private boolean selected = false;
    private String username;

    public UserSelectButton(UserProfile profile) {
        this.username = profile.getUsername();

        // 1. 텍스트 설정
        setText(username);
        setFont(new Font("맑은 고딕", Font.BOLD, 14));
        setForeground(Color.BLACK);

        // 2. 아이콘 설정 (프사 + username)
        ImageIcon icon = profile.getIcon();
        if (icon != null) {
            Image img = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            setIcon(new ImageIcon(img));
        }
        setIconTextGap(15); 

        // 3. 정렬 및 스타일
        setHorizontalAlignment(SwingConstants.LEFT); 
        setFocusPainted(false);
        setOpaque(false);                
        setContentAreaFilled(false);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10) 
        ));

        // 크기 설정
        setPreferredSize(new Dimension(0, 60)); 
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        setMinimumSize(new Dimension(0, 60));

        // 선택 기능
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
            setOpaque(true);
            setBackground(new Color(109, 135, 255)); 
            setForeground(Color.WHITE);
        } else {
            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
            setForeground(Color.BLACK);
        }
    }
}