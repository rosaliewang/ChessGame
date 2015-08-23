package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Yuchen Wang on 8/13/15.
 */
public class RestartGameListener implements ActionListener {
    private ChessBoardGUI chessBoardGUI;

    public RestartGameListener(ChessBoardGUI chessBoardGUI) {
        this.chessBoardGUI = chessBoardGUI;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        chessBoardGUI.reset();
    }
}
