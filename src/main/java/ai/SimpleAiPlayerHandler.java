package ai;

import console.ChessConsole;
import gui.ChessBoardGUI;
import gui.GUIPiece;
import logic.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuchen Wang on 9/1/15.
 */
public class SimpleAiPlayerHandler implements PlayerHandler{
    private ChessGame chessGame;
    private ChessRule chessRule;
    public ChessBoardGUI chessBoardGUI;

    /**
     * number of moves to look into the future
     */
    public int maxDepth = 2;

    public SimpleAiPlayerHandler(ChessGame chessGame) {
        this.chessGame = chessGame;
        this.chessRule = chessGame.getChessRule();
    }

    @Override
    public Move getMove() {
        return getBestMove();
    }

    /**
     * get best move for current game situation
     * @return a valid Move instance
     */
    private Move getBestMove() {
        System.out.println("getting best move");
        ChessConsole.printCurrentGameState(this.chessGame);
        System.out.println("thinking...");

        List<Move> validMoves = generateMoves(false);
        int bestResult = Integer.MIN_VALUE;
        Move bestMove = null;

        for (Move move : validMoves) {

//            GUIPiece guiPiece = chessBoardGUI.getGuiPieceAt(Piece.ROW_1, Piece.COLUMN_A);
//            if (guiPiece.toString().equals("white R 1/A 302/402")) {
//                System.out.println(move.toString());
//            }

            executeMove(move);
//            System.out.println("evaluate move: " + move + " =========================================");
            int evaluationResult = -1 * negaMax(this.maxDepth, "");
//            System.out.println("result: " + evaluationResult);
            undoMove(move);

//            guiPiece = chessBoardGUI.getGuiPieceAt(Piece.ROW_1, Piece.COLUMN_A);
//            if (!guiPiece.toString().equals("white R 1/A 302/402")) {
//                System.out.println(move.toString());
//                System.out.println();
//            }

            if (evaluationResult > bestResult) {
                bestResult = evaluationResult;
                bestMove = move;
            }
        }
        System.out.println("done thinking! best move is: " + bestMove);
        return bestMove;
    }

    @Override
    public void moveSuccessfullyExecuted(Move move) {
        // we are using the same chessGame instance, so no need to do anything here.
        if (move != null) {
            System.out.println("executed: " + move);

            chessBoardGUI.lastMove = move;
            chessGame.addToHistoryMove(move);

            if (chessBoardGUI != null) {
                List<GUIPiece> guiPieces = chessBoardGUI.getGuiPieces();

                GUIPiece dragPiece = chessBoardGUI.getGuiPieceAt(move.targetRow, move.targetColumn);
                dragPiece.correctPiecePosition();

                // if Pawn promotion, change Pawn image
                if (move.pawnPromotion) {
//                System.out.println("pawn promotion in chessBoardGUI");
                    Piece piece = dragPiece.getPiece();
                    Image img = chessBoardGUI.getPieceImage(piece.getColor(), piece.getType());
                    dragPiece.setImage(img);
                    // if castling, also modify related Rook's guiPiece
                } else if (move.rookCastlingMove != null) {
                    Move castlingMove = move.rookCastlingMove;
                    Piece piece = chessGame.getNonCapturedPieceAtLocation(
                            castlingMove.targetRow, castlingMove.targetColumn);
                    for (GUIPiece guiPiece : guiPieces) {
                        if (guiPiece.getPiece() == piece) {
                            guiPiece.correctPiecePosition(); // need to correct in order to draw
                            break;
                        }
                    }
                }
                chessBoardGUI.repaint();
            }
        }
    }


    /**
     * evaluate current game state according to nega max algorithm
     *
     * @param depth - current depth level (number of counter moves that still need to be evaluated)
     * @param indent - debug string, that is placed in front of each log message
     * @return integer score of game state after looking at "depth" counter moves
     */
    private int negaMax(int depth, String indent) {

        if (depth <= 0
                || this.chessGame.getGameState() == ChessGame.GAME_STATE_END_WHITE_WON
                || this.chessGame.getGameState() == ChessGame.GAME_STATE_END_BLACK_WON) {

            return evaluateState();
        }

        List<Move> moves = generateMoves(false);
        int currentMax = Integer.MIN_VALUE;

        for (Move currentMove : moves) {
            executeMove(currentMove);
            //ChessConsole.printCurrentGameState(this.chessGame);
            int score = -1 * negaMax(depth - 1, indent + " ");
            //System.out.println(indent+"handling move: "+currentMove+" : "+score);
            undoMove(currentMove);

            if (score > currentMax) {
                currentMax = score;
            }
        }
        //System.out.println(indent+"max: "+currentMax);
        return currentMax;
    }

    /**
     * undo specified move
     */
    private void undoMove(Move move) {
        //System.out.println("undoing move");
        this.chessGame.undoMove(move);
        //state.changeGameState();
    }

    /**
     * Execute specified move. This will also change the game state after the
     * move has been executed.
     */
    private void executeMove(Move move) {
        //System.out.println("executing move");
        this.chessGame.movePiece(move);
        this.chessGame.changeGameState();
    }

    /**
     * generate all possible/valid moves for the specified game
     * @return list of all possible/valid moves
     */
    private List<Move> generateMoves(boolean debug) {
        List<Piece> pieces = this.chessGame.getPieces();
        List<Move> validMoves = new ArrayList<Move>();

        int pieceColor = (this.chessGame.getGameState() == ChessGame.GAME_STATE_WHITE ?
                Piece.COLOR_WHITE :
                Piece.COLOR_BLACK);

        // iterate over all non-captured pieces
        for (Piece piece : pieces) {
            // only look at pieces of current players color
            if (pieceColor == piece.getColor()) {
                // start generating move
                // iterate over all board rows and columns
                for (int targetRow = Piece.ROW_1; targetRow <= Piece.ROW_8; targetRow++) {
                    for (int targetColumn = Piece.COLUMN_A; targetColumn <= Piece.COLUMN_H; targetColumn++) {
                        Move testMove = new Move(piece.getRow(), piece.getColumn(), targetRow, targetColumn);
                        testMove.isAi = true;
                        // finish generating move

                        if (debug) System.out.println("testing move: " + testMove);

//                        if (testMove.toString().equals("1E->1G")) {
//                            System.out.println("stop");
//                        }
                        // check if generated move is valid
                        if (this.chessRule.isValidMove(testMove, false)) {
                            // valid move
                            testMove.setIsValid(true);
                            validMoves.add(testMove);
                        } else {
                            // generated move is invalid, so we skip it
                        }
                    }
                }

            }
        }
        return validMoves;
    }

    /**
     * evaluate the current game state from the view of the
     * current player. High numbers indicate a better situation for
     * the current player.
     *
     * @return integer score of current game state
     */
    private int evaluateState() {

        // add up score
        //
        int scoreWhite = 0;
        int scoreBlack = 0;
        for (Piece piece : this.chessGame.getPieces()) {
            if (piece.getColor() == Piece.COLOR_BLACK) {
                scoreBlack +=
                        getScoreForPieceType(piece.getType());
                scoreBlack +=
                        getScoreForPiecePosition(piece.getRow(), piece.getColumn());
            } else if ( piece.getColor() == Piece.COLOR_WHITE) {
                scoreWhite +=
                        getScoreForPieceType(piece.getType());
                scoreWhite +=
                        getScoreForPiecePosition(piece.getRow(), piece.getColumn());
            } else {
                throw new IllegalStateException(
                        "unknown piece color found: " + piece.getColor());
            }
        }

        // return evaluation result depending on who's turn it is
        int gameState = this.chessGame.getGameState();

        if (gameState == ChessGame.GAME_STATE_BLACK) {
            return scoreBlack - scoreWhite;
        } else if (gameState == ChessGame.GAME_STATE_WHITE) {
            return scoreWhite - scoreBlack;
        } else if (gameState == ChessGame.GAME_STATE_END_WHITE_WON
                || gameState == ChessGame.GAME_STATE_END_BLACK_WON) {
            return Integer.MIN_VALUE + 1;
        } else {
            throw new IllegalStateException("unknown game state: "+gameState);
        }
    }

    /**
     * get the evaluation bonus for the specified position
     * @param row - one of Piece.ROW_..
     * @param column - one of Piece.COLUMN_..
     * @return integer score
     */
    private int getScoreForPiecePosition(int row, int column) {
        byte[][] positionWeight = {
                {1,1,1,1,1,1,1,1},
                {2,2,2,2,2,2,2,2},
                {2,2,3,3,3,3,2,2},
                {2,2,3,4,4,3,2,2},
                {2,2,3,4,4,3,2,2},
                {2,2,3,3,3,3,2,2},
                {2,2,2,2,2,2,2,2},
                {1,1,1,1,1,1,1,1}
                };
        return positionWeight[row][column];
    }

    /**
     * get the evaluation score for the specified piece type
     * @param type - one of Piece.TYPE_..
     * @return integer score
     */
    private int getScoreForPieceType(int type) {
        switch (type) {
            case Piece.TYPE_BISHOP: return 30;
            case Piece.TYPE_KING: return 99999;
            case Piece.TYPE_KNIGHT: return 30;
            case Piece.TYPE_PAWN: return 10;
            case Piece.TYPE_QUEEN: return 90;
            case Piece.TYPE_ROOK: return 50;
            default: throw new IllegalArgumentException("unknown piece type: "+type);
        }
    }

    public static void main(String[] args) {
        ChessGame ch = new ChessGame();
        SimpleAiPlayerHandler ai = new SimpleAiPlayerHandler(ch);
		/*
		ch.pieces = new ArrayList<Piece>();
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_3, Piece.COLUMN_C);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_4, Piece.COLUMN_C);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_5, Piece.COLUMN_C);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_4, Piece.COLUMN_B);
		ChessConsole.printCurrentGameState(ch);
		System.out.println("score: "+ai.evaluateState());
		System.out.println("move: "+ai.getBestMove()); //c4 b4
		*/

		/*
		  a  b  c  d  e  f  g  h
		  +--+--+--+--+--+--+--+--+
		 8|BR|  |  |  |  |  |  |BR|8
		  +--+--+--+--+--+--+--+--+
		 7|BP|  |WR|  |BK|  |BP|BP|7
		  +--+--+--+--+--+--+--+--+
		 6|  |  |  |  |BP|BP|  |  |6
		  +--+--+--+--+--+--+--+--+
		 5|  |  |  |  |  |  |  |  |5
		  +--+--+--+--+--+--+--+--+
		 4|  |  |  |  |BB|  |  |  |4
		  +--+--+--+--+--+--+--+--+
		 3|  |  |  |  |WB|WP|  |  |3
		  +--+--+--+--+--+--+--+--+
		 2|WP|  |  |WQ|  |  |  |WP|2
		  +--+--+--+--+--+--+--+--+
		 1|  |  |  |  |WK|  |  |WR|1
		  +--+--+--+--+--+--+--+--+
		   a  b  c  d  e  f  g  h
		*/

		/*
		ch.pieces = new ArrayList<Piece>();
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_A);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_7, Piece.COLUMN_E);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_G);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_H);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_7, Piece.COLUMN_C);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_6, Piece.COLUMN_E);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_6, Piece.COLUMN_F);
		ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_4, Piece.COLUMN_E);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_3, Piece.COLUMN_E);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_3, Piece.COLUMN_F);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, Piece.COLUMN_A);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_2, Piece.COLUMN_D);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, Piece.COLUMN_H);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);
		ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);
		ChessConsole.printCurrentGameState(ch);
		ai = new SimpleAiPlayerHandler(ch);
		System.out.println("score: "+ai.evaluateState());
		System.out.println("move: "+ai.getBestMove()); //c4 b4
		*/

		/*
		 *   a  b  c  d  e  f  g  h
 +--+--+--+--+--+--+--+--+
8|BR|  |  |  |  |  |  |BR|8
 +--+--+--+--+--+--+--+--+
7|BP|BB|WR|  |BK|  |BP|BP|7
 +--+--+--+--+--+--+--+--+
6|  |  |  |  |BP|BP|  |  |6
 +--+--+--+--+--+--+--+--+
5|  |  |  |  |  |  |  |  |5
 +--+--+--+--+--+--+--+--+
4|  |  |  |  |WP|  |  |  |4
 +--+--+--+--+--+--+--+--+
3|  |  |  |  |WB|WP|  |  |3
 +--+--+--+--+--+--+--+--+
2|WP|  |  |WQ|  |  |  |WP|2
 +--+--+--+--+--+--+--+--+
1|  |  |  |  |WK|  |  |WR|1
 +--+--+--+--+--+--+--+--+
  a  b  c  d  e  f  g  h
		 */

        ch.clearPieces();
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_A);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_7, Piece.COLUMN_B);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_7, Piece.COLUMN_C);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_7, Piece.COLUMN_E);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_G);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_H);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_6, Piece.COLUMN_E);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_6, Piece.COLUMN_F);
        ch.addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_4, Piece.COLUMN_E);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_3, Piece.COLUMN_E);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_3, Piece.COLUMN_F);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, Piece.COLUMN_A);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_2, Piece.COLUMN_D);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, Piece.COLUMN_H);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);
        ch.addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);
        ch.setGameState(ChessGame.GAME_STATE_BLACK);
//        ChessConsole.printCurrentGameState(ch);
        System.out.println("score: "+ai.evaluateState());
        System.out.println("move: "+ai.getBestMove()); //c4 b4
    }
}

