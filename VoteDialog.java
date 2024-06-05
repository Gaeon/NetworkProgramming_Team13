package LiarGame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.List;

public class VoteDialog {
    private JFrame voteFrame;
    private boolean isVoteDialogOpen = false;
    private String selectedParticipant;

    public VoteDialog(List<String> participants, ActionListener voteActionListener) {
        if (isVoteDialogOpen) return;
        isVoteDialogOpen = true;

        voteFrame = new JFrame("Vote");
        voteFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        voteFrame.setSize(300, 400);
        voteFrame.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(participants.size() + 1, 1));

        JLabel titleLabel = new JLabel("Vote for a player:");
        panel.add(titleLabel);

        ButtonGroup group = new ButtonGroup();

        for (String participant : participants) {
            JRadioButton radioButton = new JRadioButton(participant);
            group.add(radioButton);
            panel.add(radioButton);
        }

        JButton confirmButton = new JButton("Vote");
        confirmButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (Enumeration<AbstractButton> buttons = group.getElements(); buttons.hasMoreElements();) {
                    AbstractButton button = buttons.nextElement();
                    if (button.isSelected()) {
                        selectedParticipant = button.getText();
                        break;
                    }
                }
                voteActionListener.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, selectedParticipant));
                isVoteDialogOpen = false;
                voteFrame.dispose();
            }
        });
        panel.add(confirmButton);

        voteFrame.add(panel, BorderLayout.CENTER);
        voteFrame.setVisible(true);
    }

    public String getSelectedParticipant() {
        return selectedParticipant;
    }
}
