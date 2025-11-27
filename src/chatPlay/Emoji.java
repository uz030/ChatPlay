package chatPlay;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Emoji extends JDialog {

    private ChatClientMain client;
    private ChatRoomData roomData;

    public Emoji(Window owner, ChatClientMain client, ChatRoomData roomData) {
    	super(owner, Dialog.ModalityType.MODELESS);
        this.client = client;
        this.roomData = roomData;

        setUndecorated(true);
        setSize(350, 120);
        setLayout(new BorderLayout());

        JPanel emojiPanel = new JPanel();
        emojiPanel.setLayout(new BoxLayout(emojiPanel, BoxLayout.X_AXIS));
        emojiPanel.setBackground(new Color(255, 255, 255, 240));

        JScrollPane scrollPane = new JScrollPane(emojiPanel);
        ScrollUtil.applyCustomScrollBar(scrollPane);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        loadIcons(emojiPanel);

        // 기본 위치 (혹시 따로 안 잡아줄 때 대비)
        setLocationRelativeTo(owner);
    }

    private void loadIcons(JPanel emojiPanel) {
        File folder = new File("src/images/icon");
        if (!folder.exists()) {
            System.out.println("⚠️ 폴더 없음: src/images/icon");
            return;
        }

        File[] files = folder.listFiles((d, n) -> n.endsWith(".png"));
        if (files == null) return;

        for (File f : files) {

            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            Image scaled = icon.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);

            JButton btn = new JButton(new ImageIcon(scaled));
            btn.setContentAreaFilled(false);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            btn.addActionListener(e -> {
                sendEmoji(f.getName());
                setVisible(false);
            });

            emojiPanel.add(btn);
            emojiPanel.add(Box.createHorizontalStrut(10));
        }
    }

    private void sendEmoji(String fileName) {
        try {
            client.getDos().writeUTF(
                "/roommsg " + roomData.getRoomId() +
                " @images " + fileName
            );
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
