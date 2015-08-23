package ch07;
import ch07.console.ChessConsole;
import ch07.gui.ChessGui;
import ch07.logic.ChessGame;
import ch07.logic.Piece;


public class Main {

	public static void main(String[] args) {
		// first we create the game
		ChessGame chessGame = new ChessGame();
		
		// then we create the clients/players
		ChessGui chessGui = new ChessGui(chessGame);
		ChessConsole chessConsole = new ChessConsole(chessGame);
		
		// then we attach the clients/players to the game
		chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
		chessGame.setPlayer(Piece.COLOR_BLACK, chessConsole);
		
		// in the end we start the game
		new Thread(chessGame).start();
	}
	
}
