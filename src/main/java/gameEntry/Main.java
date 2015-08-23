package gameEntry;

import gui.ChessBoardGUI;
import logic.ChessGame;
import logic.Piece;

/**
 * Created by Yuchen Wang on 8/21/15.
 */
public class Main {
    public static void main(String[] args) {
        ChessGame chessGame = new ChessGame();
        ChessBoardGUI chessGui = new ChessBoardGUI(chessGame);
        setPlayer(chessGame, chessGui);
        new Thread(chessGame).start();
    }

    public static void setPlayer(ChessGame chessGame, ChessBoardGUI chessBoardGUI) {
        chessGame.setPlayer(Piece.COLOR_WHITE, chessBoardGUI);
        chessGame.setPlayer(Piece.COLOR_BLACK, chessBoardGUI);
    }
}
