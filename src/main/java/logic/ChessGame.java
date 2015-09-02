package logic;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yuchen Wang on 8/13/15.
 */
public class ChessGame implements Runnable {
    // The game state variable is used for keeping track who's turn it is
    private int gameState = GAME_STATE_WHITE;
    public static final int GAME_STATE_WHITE = 0;
    public static final int GAME_STATE_BLACK = 1;
    public static final int GAME_STATE_END_BLACK_WON = 2;
    public static final int GAME_STATE_END_WHITE_WON = 3;
    public static final int GAME_STATE_END_DRAW = 4;

    private volatile boolean isRunning = true;

    private List<Piece> pieces;
    private List<Piece> capturedPieces;

    private ChessRule chessRule;
    private PlayerHandler blackPlayerHandler;
    private PlayerHandler whitePlayerHandler;
    private PlayerHandler activePlayerHandler;

    private boolean isBlackKingInCheck, isWhiteKingInCheck;
    private boolean stalemate, checkmate;
    private List<Move> historyMoves;
    private List<Piece> threateningPieces; // containing opponent's pieces and King itself

    private final Object lock = new Object();

    public ChessGame() {
        this.chessRule = new ChessRule(this);
        addChessBoard();
        this.historyMoves = new ArrayList<Move>();
        this.threateningPieces = new ArrayList<Piece>();
        this.capturedPieces = new ArrayList<Piece>();
    }

    /**
     * set the player/client for the specified piece color
     * @param pieceColor - the color the client/player controls
     * @param playerHandler - the player/client
     */
    public void setPlayer(int pieceColor, PlayerHandler playerHandler) {
        switch (pieceColor) {
            case Piece.COLOR_BLACK: this.blackPlayerHandler = playerHandler; break;
            case Piece.COLOR_WHITE: this.whitePlayerHandler = playerHandler; break;
            default: throw new IllegalArgumentException("Invalid pieceColor: " + pieceColor);
        }
    }

    /**
     * start main game flow
     */
    public void startGame() {
        // check if all players are ready
        System.out.println(Thread.currentThread() + " 0.ChessGame: waiting for players");
        while (this.blackPlayerHandler == null || this.whitePlayerHandler == null) {
            // players are still missing
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
        }

        // set start player
        this.activePlayerHandler = this.whitePlayerHandler;

        // start game flow
        System.out.println(Thread.currentThread() + " 0.ChessGame: starting game flow");
        while (isRunning && !isGameEndConditionReached()) {
            System.out.println(Thread.currentThread() + " 1.-------------------------------------");
            System.out.println(Thread.currentThread() + " 2.ChessGame.startGame(): calling waitForMove()");
            waitForMove();
            if (isRunning) {
                System.out.println(Thread.currentThread() + " 5.ChessGame.startGame(): calling swapActivePlayer()");
                swapActivePlayer();
//            if (threateningPieces != null &&
//                    threateningPieces.size() == 0) System.out.println(Thread.currentThread() +
//                    " 9.ChessGame.startGame(): threatening pieces 0 after swapActivePlayer()");
                System.out.println(Thread.currentThread() + " 10.ChessGame.startGame(): calling gameStateAnnouncer()");
                gameStateAnnouncer();
            }
//            System.out.println("11.------------------------------------");
        }

        System.out.println(Thread.currentThread() + " ChessGame: game ended");
        isRunning = false;
    }

    /**
     * swap active player and update game state
     */
    private void swapActivePlayer() {
        if (!isRunning) return;

        if (this.activePlayerHandler == this.whitePlayerHandler) {
            this.activePlayerHandler = this.blackPlayerHandler;
        } else {
            this.activePlayerHandler = this.whitePlayerHandler;
        }

        this.changeGameState();

        if (!isGameEndState()) {
            System.out.println("swap player: game end state here");
            // TODO
            setThreateningPieces(chessRule.isKingInCheck());

            // check if king is in check
            if (threateningPieces != null) {
//            System.out.println(Thread.currentThread() + " 6.ChessGame.swapActivePlayer(): threatening pieces "
//                    + threateningPieces.size());
                if (getGameState() == GAME_STATE_WHITE) {
                    this.isWhiteKingInCheck = true;
                    this.isBlackKingInCheck = false;
                } else {
                    this.isBlackKingInCheck = true;
                    this.isWhiteKingInCheck = false;
                }

                // compute checkmate status after swap player
                if (this.chessRule.isCheckmate()) {
                    this.checkmate = true;
                    this.gameState = getGameState() == GAME_STATE_WHITE ?
                            GAME_STATE_END_BLACK_WON : GAME_STATE_END_WHITE_WON;
                } else this.checkmate = false;
            } else {
//            System.out.println(Thread.currentThread() + " 6.ChessGame.swapActivePlayer(): threatening pieces null");

                this.isBlackKingInCheck = false;
                this.isWhiteKingInCheck = false;

                if (this.chessRule.isStalemate()) {
                    this.stalemate = true;
                    this.gameState = GAME_STATE_END_DRAW;
                } else this.stalemate = false;
            }

            this.activePlayerHandler.moveSuccessfullyExecuted(null); // repaint
        }
    }

    /**
     * wait for a valid player move and execute it
     */
    private void waitForMove() {
        Move move;
        // wait for a valid move
        do {
            move = this.activePlayerHandler.getMove();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (this.isRunning && (move == null || !move.isValid()));

        if (!this.isRunning) return;

        //execute move
        boolean success = this.movePiece(move);
        if (success) {
            // TODO: undecided
//            System.out.println(Thread.currentThread() + " 3.ChessGame.waitForMove(): will call moveSuccessfullyExecuted()");
            this.activePlayerHandler.moveSuccessfullyExecuted(move);
//            this.blackPlayerHandler.moveSuccessfullyExecuted(move);
//            this.whitePlayerHandler.moveSuccessfullyExecuted(move);
        } else {
            throw new IllegalStateException("move was valid, but failed to execute it");
        }
    }

    /**
     * Announces game state: King in check, checkmate, stalemate
     */
    private void gameStateAnnouncer() {
        if (!isRunning) return;
//        System.out.println(Thread.currentThread() + " ChessGame.gameSateAnnouncer(): announcing...");

        if (getGameState() == ChessGame.GAME_STATE_WHITE && this.isWhiteKingInCheck)
            System.out.println(Thread.currentThread() + " Dangerous! White King is in check!");
        else if (getGameState() == ChessGame.GAME_STATE_BLACK && this.isBlackKingInCheck) {
            System.out.println(Thread.currentThread() + " Dangerous! Black King is in check!");
        }
        else if (getGameState() == ChessGame.GAME_STATE_END_BLACK_WON ||
                getGameState() == ChessGame.GAME_STATE_END_WHITE_WON) {
            if (this.stalemate)
                System.out.println("Stalemate.");
            else if (this.checkmate)
                System.out.println("Checkmate!!!");

            if (getGameState() == GAME_STATE_END_BLACK_WON)
                System.out.println("Game over! Black won!");
            else if (getGameState() == GAME_STATE_END_WHITE_WON)
                System.out.println("Game over! White won!");
            else if (getGameState() == GAME_STATE_END_DRAW)
                System.out.printf("Game over! Draw!");
        }

        printHistoryMoves();
    }



    /**
     * add pieces
     */
    private void addChessBoard() {
        pieces = new ArrayList<Piece>();
        /**
         * normal chess board
         */
        addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_A);
        addPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1, Piece.COLUMN_B);
        addPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1, Piece.COLUMN_C);
        addPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_1, Piece.COLUMN_D);
        addPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_BISHOP, Piece.ROW_1, Piece.COLUMN_F);
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_KNIGHT, Piece.ROW_1, Piece.COLUMN_G);
        addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);

        //pawns
//        int currentColumn = Piece.COLUMN_A;
//        for (int i = 0; i < 8; i++) {
//            addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_2, currentColumn);
//            currentColumn++;
//        }

        addPiece(Piece.COLOR_WHITE, Piece.TYPE_PAWN, Piece.ROW_7, Piece.COLUMN_D);

//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_A);
        addPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8, Piece.COLUMN_B);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8, Piece.COLUMN_C);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_QUEEN, Piece.ROW_8, Piece.COLUMN_D);
        addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_8, Piece.COLUMN_E);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_BISHOP, Piece.ROW_8, Piece.COLUMN_F);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_KNIGHT, Piece.ROW_8, Piece.COLUMN_G);
        addPiece(Piece.COLOR_BLACK, Piece.TYPE_ROOK, Piece.ROW_8, Piece.COLUMN_H);

        //pawns
//        currentColumn = Piece.COLUMN_A;
//        for (int i = 0; i < 8; i++) {
//            addPiece(Piece.COLOR_BLACK, Piece.TYPE_PAWN, Piece.ROW_7, currentColumn);
//            currentColumn++;
//        }

        /**
         * for debug: check game state
         */
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_4, Piece.COLUMN_E);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_1, Piece.COLUMN_E);

        /**
         * for debug: checkmate
         */
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_ROOK, Piece.ROW_1, Piece.COLUMN_H);
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_4, Piece.COLUMN_F);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_5, Piece.COLUMN_H);

        /**
         * for debug: stalemate
         */
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_QUEEN, Piece.ROW_6, Piece.COLUMN_G);
//        addPiece(Piece.COLOR_WHITE, Piece.TYPE_KING, Piece.ROW_6, Piece.COLUMN_F);
//        addPiece(Piece.COLOR_BLACK, Piece.TYPE_KING, Piece.ROW_8, Piece.COLUMN_H);
    }

    /**
     * add a piece to {@link this.pieces}
     * @param color color of the player
     * @param type  type of piece
     * @param row   offset of x
     * @param column offset of y
     */
    public void addPiece(int color, int type, int row, int column) {
        Piece piece = new Piece(color, type, row, column);
        synchronized (lock) {
            this.pieces.add(piece);
        }
    }

    /**
     * Move piece to the specified location. If the target location is occupied
     * by an opponent piece, that piece is marked as 'captured'. If the move
     * could not be executed successfully, 'false' is returned and the game
     * state does not change.
     *
     * @param move to execute
     * @return true, if piece was moved successfully
     */
    public boolean movePiece(Move move) {
        //set captured piece in move
        // this information is needed in the undoMove() method.
        move.capturedPiece = this.getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
//        System.out.println(pieces);
        Piece piece = getNonCapturedPieceAtLocation(move.sourceRow, move.sourceColumn);

        //check if the move is capturing an opponent piece
        assert piece != null;
        int opponentColor = (piece.getColor() == Piece.COLOR_BLACK ?
                Piece.COLOR_WHITE : Piece.COLOR_BLACK);
        // capture
        if (hasNonCapturedPieceAtLocation(opponentColor, move.targetRow, move.targetColumn)) {
            Piece opponentPiece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);
            this.pieces.remove(opponentPiece);
            this.capturedPieces.add(opponentPiece);
            opponentPiece.setIsCaptured(true); // for guiPiece
        }

        if (move.enPassant) {
            move.enPassantPosition(piece.getColor() == Piece.COLOR_BLACK ? -1 : 1);
//            move.targetRow += piece.getColor() == Piece.COLOR_BLACK ? -1 : 1;
//            piece.setPerformedEnPassant(false);
        }
//        else if (move.pawnPromotion) {
//            move.pawnPromotion(piece);
//        }

        // castling is done in ChessRule
//        else if (piece.isCastling()) {
//            piece.setCastling(false);
//        }

        piece.setRow(move.targetRow);
        piece.setColumn(move.targetColumn);

        return true;
    }

    /**
     * check if the games end condition is met:
     * One color has a captured king, stalemate, checkmate
     * @return true if the game end condition is met
     */
    private boolean isGameEndConditionReached() {
        for (Piece piece : capturedPieces) {
            if (piece.getType() == Piece.TYPE_KING) {
                return true;
            }
        }

        return isStalemate() || isCheckmate();
    }

    /**
     * used after first calling of changeGameState() in swapActivePlayer()
     */
    private boolean isGameEndState() {
        return this.gameState == ChessGame.GAME_STATE_END_DRAW ||
                this.gameState == ChessGame.GAME_STATE_END_BLACK_WON ||
                this.gameState == ChessGame.GAME_STATE_END_WHITE_WON;
    }

    public void addToHistoryMove(Move move) {
        this.historyMoves.add(move);
    }

    /**
     * returns the first piece at the specified location that is not marked
     * as 'captured'.
     * @param row one of Piece.ROW_..
     * @param column one of Piece.COLUMN_..
     * @return the first not captured piece at the specified location
     */
    public Piece getNonCapturedPieceAtLocation(int row, int column) {
        for (Piece piece : this.pieces) {
            if (piece.getRow() == row && piece.getColumn() == column) {
                return piece;
            }
        }
        return null;
    }

    /**
     * check if there exists a non-captured piece at given location
     * @param color specified color of non-captured piece
     * @param row   location coordinate of x
     * @param column    location coordinate of y
     * @return  true if there is a non-captured piece at given location
     */
    public boolean hasNonCapturedPieceAtLocation(int color, int row, int column) {
        for (Piece piece : pieces) {
            if (piece.getRow() == row && piece.getColumn() == column &&
                    piece.getColor() == color) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether there is a non-captured piece at the specified location
     *
     * @param row one of Piece.ROW_..
     * @param column on of Piece.COLUMN_..
     * @return true, if the location contains a piece
     */
    public boolean hasNonCapturedPieceAtLocation(int row, int column) {
        for (Piece piece : this.pieces) {
            if (piece.getRow() == row && piece.getColumn() == column) {
                return true;
            }
        }
        return false;
    }

    /**
     * Undo the specified move. It will also adjust the game state appropriately.
     */
    public void undoMove(Move move) {
        Piece piece = getNonCapturedPieceAtLocation(move.targetRow, move.targetColumn);

        piece.setRow(move.sourceRow);
        piece.setColumn(move.sourceColumn);

        // capture[All], en passant[Pawn]
        if (move.capturedPiece != null) {
            move.capturedPiece.setRow(move.targetRow);
            move.capturedPiece.setColumn(move.targetColumn);
            move.capturedPiece.setIsCaptured(false);
            this.capturedPieces.remove(move.capturedPiece);
            this.pieces.add(move.capturedPiece);
//            piece.setPerformedEnPassant(false);
        } else if (move.rookCastlingMove != null) { // castling[King]
            Move castlingMove = move.rookCastlingMove;
//            piece.setCastlingMove(null);
//            piece.setRookForCastling(null);
            piece.setHasNotMoved(true);
            getNonCapturedPieceAtLocation(castlingMove.targetRow, castlingMove.targetColumn).
                    setHasNotMoved(true);

            undoMove(castlingMove);
        } else if (piece.isLastMovedTwoSteps()) { // first move[Pawn]
            piece.setLastMovedTwoSteps(false);
        }

        // pawn promotion
        if (move.pawnPromotion) {
            move.undoPromotion(piece);
        }

        if (piece.getColor() == Piece.COLOR_BLACK) {
            this.gameState = ChessGame.GAME_STATE_BLACK;
        } else {
            this.gameState = ChessGame.GAME_STATE_WHITE;
        }
    }

    /**
     * @return current game state
     */
    public int getGameState() {
        return gameState;
    }

    /**
     * for debug
     * @param gameState set to gameState
     */
    public void setGameState(int gameState) {
        this.gameState = gameState;
    }

    public List<Piece> getThreateningPieces() {
        synchronized (lock) {
            return threateningPieces;
        }
    }

    public void setThreateningPieces(List<Piece> pieces) {
        synchronized (lock) {
            this.threateningPieces = pieces;
        }
    }

    public List<Move> getHistoryMoves() {
        return historyMoves;
    }

    public void printHistoryMoves() {
        System.out.println("------------History moves-------------");
        for (Move move : historyMoves) {
            System.out.println(move.toString());
        }
        System.out.println("----------History moves end-----------");
    }

    public List<Piece> getPieces() {
        synchronized (lock) {
            return pieces;
        }
    }

    public ChessRule getChessRule() {
        return chessRule;
    }

    public boolean isBlackKingInCheck() {
        return isBlackKingInCheck;
    }

    public boolean isWhiteKingInCheck() {
        return isWhiteKingInCheck;
    }

    public boolean isStalemate() {
        return stalemate;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public void printPieces() {
        System.out.println("current pieces left");
        for (Piece piece : pieces) {
            System.out.println(piece.toString());
        }
    }

    /**
     * switches between the different game states
     */
    public void changeGameState() {
        // check if game end condition has been reached
        //
        if (this.isGameEndConditionReached()) {
            System.out.println("game end reached");
            if (isStalemate()) {
                this.gameState = ChessGame.GAME_STATE_END_DRAW;
            } else if (this.gameState == ChessGame.GAME_STATE_BLACK) {
                this.gameState = ChessGame.GAME_STATE_END_BLACK_WON;
            } else if (this.gameState == ChessGame.GAME_STATE_WHITE) {
                this.gameState = ChessGame.GAME_STATE_END_WHITE_WON;
            }
            return;
        }

        switch (gameState) {
            case GAME_STATE_BLACK:
                this.gameState = GAME_STATE_WHITE;
                break;
            case GAME_STATE_WHITE:
                this.gameState = GAME_STATE_BLACK;
                break;
            case GAME_STATE_END_WHITE_WON:
            case GAME_STATE_END_BLACK_WON:
                // don't change anymore
                break;
            default:
                throw new IllegalStateException("unknown game state:" + this.gameState);
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            this.startGame();
        }
    }

    public void kill() {
        this.isRunning = false;
    }
}
