package ch04.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class ChangeGameStateButtonActionListener implements ActionListener {

	private ChessGui chessGui;

	public ChangeGameStateButtonActionListener(ChessGui chessGui) {
		this.chessGui = chessGui;
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		// change game state
		this.chessGui.changeGameState();
	}
}
