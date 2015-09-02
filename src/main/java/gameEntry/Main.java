package gameEntry;

import ai.SimpleAiPlayerHandler;
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
        SimpleAiPlayerHandler ai1 = new SimpleAiPlayerHandler(chessGame);
        ai1.maxDepth = 1;
        setPlayer(chessGame, chessGui, ai1);
        new Thread(chessGame).start();
    }

    public static void setPlayer(ChessGame chessGame, ChessBoardGUI chessBoardGUI) {
        chessGame.setPlayer(Piece.COLOR_WHITE, chessBoardGUI);
        chessGame.setPlayer(Piece.COLOR_BLACK, chessBoardGUI);
    }

    public static void setPlayer(ChessGame chessGame, ChessBoardGUI chessBoardGUI,
                                 SimpleAiPlayerHandler aiPlayerHandler) {
        chessGame.setPlayer(Piece.COLOR_WHITE, chessBoardGUI);
        aiPlayerHandler.chessBoardGUI = chessBoardGUI;
        chessGame.setPlayer(Piece.COLOR_BLACK, aiPlayerHandler);
    }

    public static void setPlayer(ChessGame chessGame, SimpleAiPlayerHandler aiPlayerHandler,
                                 SimpleAiPlayerHandler aiPlayerHandler1) {
        chessGame.setPlayer(Piece.COLOR_WHITE, aiPlayerHandler);
        chessGame.setPlayer(Piece.COLOR_BLACK, aiPlayerHandler1);
    }
}
