package LiarGame;

import javax.swing.*;

public class ResultDialog {
    public ResultDialog(String votedLiar, String actualLiar, boolean winner) {
        String resultMessage = "투표된 라이어: " + votedLiar + "\n실제 라이어: " + actualLiar + "\n";
        resultMessage += winner ? "시민의 승리!" : "라이어의 승리!";

        JOptionPane.showMessageDialog(null, resultMessage, "게임 결과", JOptionPane.INFORMATION_MESSAGE);
    }
}
