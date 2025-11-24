package chatPlay;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UserSelectDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private List<JCheckBox> boxes = new ArrayList<>();
    private List<String> selectedUsers = new ArrayList<>();
    private boolean isOk = false;

    public UserSelectDialog(ChatClientMain parent) {
        super(parent, "대화상대 초대", true);
        setSize(300, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        add(new JLabel("초대할 친구 선택", SwingConstants.CENTER), BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        DefaultListModel<UserProfile> model = parent.getUserListModel();
        for(int i=0; i<model.getSize(); i++) {
            JCheckBox box = new JCheckBox(model.getElementAt(i).getUsername());
            boxes.add(box);
            listPanel.add(box);
        }
        add(new JScrollPane(listPanel), BorderLayout.CENTER);

        JButton btnOk = new JButton("확인");
        btnOk.addActionListener(e -> {
            for(JCheckBox box : boxes) if(box.isSelected()) selectedUsers.add(box.getText());
            if(!selectedUsers.isEmpty()) { isOk = true; dispose(); }
        });
        add(btnOk, BorderLayout.SOUTH);
    }

    public boolean isOk() { return isOk; }
    public List<String> getSelectedUsers() { return selectedUsers; }
}