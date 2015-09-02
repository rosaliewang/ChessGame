package logic;

/**
 * Created by Yuchen Wang on 8/14/15.
 */

import console.ChessConsole;

import java.util.ArrayList;
import java.util.List;

/**
 * reference
 *   a  b  c  d  e  f  g  h
 *  +--+--+--+--+--+--+--+--+
 * 8|BR|BN|BB|BQ|BK|BB|BN|BR|8
 *  +--+--+--+--+--+--+--+--+
 * 7|BP|BP|BP|BP|BP|BP|BP|BP|7
 *  +--+--+--+--+--+--+--+--+
 * ..
 * 2|WP|WP|WP|WP|WP|WP|WP|WP|2
 *  +--+--+--+--+--+--+--+--+
 * 1|WR|WN|WB|WQ|WK|WB|WN|WR|1
 *  +--+--+--+--+--+--+--+--+
 *   a  b  c  d  e  f  g  h
 *
 */
public class ChessRule {
    private ChessGame chessGame;
    private boolean debug;

    public ChessRule(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    /**
     * Checks if the specified move is valid
     * If valid, execute the move and modify the corresponding pieces
     * Else, modify nothing
     * @return true if move is valid, false if move is invalid
     */
    public boolean isValidMove(Move move, boolean debug) {
        this.debug = debug;
        int fromRow = move.sourceRow;
        int fromColumn = move.sourceColumn;
        int toRow = move.targetRow;
        int toColumn = move.targetColumn;

        Piece sourcePiece = chessGame.getNonCapturedPieceAtLocation(fromRow, fromColumn);
        Piece targetPiece = chessGame.getNonCapturedPieceAtLocation(toRow, toColumn);

        // source piece does not exist
        if (sourcePiece == null) {
            log("no source piece", false);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), false);

            return false;
        }

        // source piece has right color?
        if (sourcePiece.getColor() == Piece.COLOR_WHITE
                && chessGame.getGameState() == ChessGame.GAME_STATE_WHITE) {
            // ok
        } else if (sourcePiece.getColor() == Piece.COLOR_BLACK
                && chessGame.getGameState() == ChessGame.GAME_STATE_BLACK) {
            // ok
        } else {
            log("it's not your turn: "
                    +"pieceColor="+Piece.getColorString(sourcePiece.getColor())
                    +"gameState="+this.chessGame.getGameState(), true);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), true);

//            ChessConsole.printCurrentGameState(this.chessGame);
            // it's not your turn
            return false;
        }

        // check if target location within boundaries
        if (toRow < Piece.ROW_1 || toRow > Piece.ROW_8
                || toColumn < Piece.COLUMN_A || toColumn > Piece.COLUMN_H) {
            log("target row or column out of scope", true);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), true);

            return false;
        }

        return isValidPieceMovementRules(move, sourcePiece, targetPiece, toRow, toColumn, false, true);
    }

    /**
     * helper method of isValidMove
     * checks if sourcePiece can move to (toRow, toColumn) and if there exists
     * a targetPiece to be capture
     * @param move 1. simulation: null; 2. forPossibleMoves: move
     * @param sourcePiece piece to move
     * @param targetPiece piece on position (toRow, toColumn)
     * @param isSimulating if true, then is only simulating valid moves and modify nothing;
     *                     else, false in {@link this.isValidMove()} and modify if valid
     * @param forPossibleMove for {@link this.isValidKingMove()} only. if false, called in
     *                        simulation and checks if castling is possible; else, called
     *                        in {@link this.isValidMove()} to make valid moves
     */
    public boolean isValidPieceMovementRules(Move move, Piece sourcePiece, Piece targetPiece,
                                              int toRow, int toColumn,
                                             boolean isSimulating, boolean forPossibleMove) {
        // validate piece movement rules
        boolean validPieceMove = false;
        switch (sourcePiece.getType()) {
            case Piece.TYPE_BISHOP:
                validPieceMove = isValidBishopMove(sourcePiece, targetPiece,
                        toRow, toColumn, isSimulating); break;
            case Piece.TYPE_KING:
                validPieceMove = isValidKingMove(move, sourcePiece, targetPiece,
                        toRow, toColumn, isSimulating, forPossibleMove); break;
            case Piece.TYPE_KNIGHT:
                validPieceMove = isValidKnightMove(sourcePiece, targetPiece,
                        toRow, toColumn, isSimulating); break;
            case Piece.TYPE_PAWN:
                validPieceMove = isValidPawnMove(move, sourcePiece, targetPiece,
                        toRow, toColumn, isSimulating); break;
            case Piece.TYPE_QUEEN:
                validPieceMove = isValidQueenMove(sourcePiece, targetPiece,
                        toRow, toColumn); break;
            case Piece.TYPE_ROOK:
                validPieceMove = isValidRookMove(sourcePiece, targetPiece,
                        toRow, toColumn, isSimulating); break;
            default: break;
        }
        return validPieceMove;
    }

    /**
     * check if King in current turn is in check
     * @return threatening pieces after iterating all pieces
     */
    public List<Piece> isKingInCheck() {
        Piece King = null;
        for (Piece piece : chessGame.getPieces()) {
            if (piece.getType() == Piece.TYPE_KING) {
                if ((piece.getColor() == Piece.COLOR_WHITE &&
                        chessGame.getGameState() == ChessGame.GAME_STATE_WHITE) ||
                        (piece.getColor() == Piece.COLOR_BLACK &&
                                chessGame.getGameState() == ChessGame.GAME_STATE_BLACK)) {
                    King = piece;
                    break;
                }
            }
        }

        return underAttack(King, true);
    }

    /**
     * remember to parse in targetPiece
     * @param targetPiece target piece to attack
     * @param fullCheck if true, check all opponents
     * @return list of threatening pieces if targetPiece is under attack, null if not
     */
    private List<Piece> underAttack(Piece targetPiece, boolean fullCheck) {
        if (targetPiece != null) {
            boolean isInCheck = false;
            int opponentColor = chessGame.getGameState() == ChessGame.GAME_STATE_WHITE ?
                    Piece.COLOR_BLACK : Piece.COLOR_WHITE;
            List<Piece> list = new ArrayList<Piece>();
            list.add(targetPiece);

            for (Piece piece : chessGame.getPieces()) {
                // TODO: could move !piece.isCaptured() check here
                if (!piece.isCaptured() && piece.getColor() == opponentColor) {
                    switch (piece.getType()) {
                        case Piece.TYPE_BISHOP:
                            isInCheck = inBishopAttack(piece, targetPiece,
                                    targetPiece.getRow(), targetPiece.getColumn());
                            break;
                        case Piece.TYPE_KING:
                            isInCheck = inKingAttack(piece, targetPiece,
                                    targetPiece.getRow(), targetPiece.getColumn());
                            break;
                        case Piece.TYPE_KNIGHT:
                            isInCheck = inKnightAttack(piece, targetPiece,
                                    targetPiece.getRow(), targetPiece.getColumn());
                            break;
                        case Piece.TYPE_PAWN:
                            isInCheck = inPawnAttack(piece, targetPiece,
                                    targetPiece.getRow(), targetPiece.getColumn());
                            break;
                        case Piece.TYPE_QUEEN:
                            isInCheck = inQueenAttack(piece, targetPiece,
                                    targetPiece.getRow(), targetPiece.getColumn());
                            break;
                        case Piece.TYPE_ROOK:
                            isInCheck = inRookAttack(piece, targetPiece,
                                    targetPiece.getRow(), targetPiece.getColumn());
                            break;
                    }

                    if (isInCheck) {
                        list.add(piece);
                        if (!fullCheck) return list;
                    }
                }
            }

            if (list.size() == 1) return null;
            else return list;
        } else return null;
    }

    /**
     * check if game for current game state(white or black) is stalemate
     * Stalemate is a situation in the game of chess where the player whose
     * turn it is to move is not in check but has no legal move.
     * ends as a draw
     * For the most part, a draw occurs when it appears that neither side
     * will win. Draws are codified by various rules of chess including
     * stalemate (when the player to move has no legal move and is not in
     * check), threefold repetition (when the same position occurs three
     * times with the same player to move), and the fifty-move rule (when the
     * last fifty successive moves made by both players contain no capture or
     * pawn move). A draw also occurs when neither player has sufficient
     * material to checkmate the opponent or when no sequence of legal moves can
     * lead to checkmate.
     * https://en.wikipedia.org/wiki/Draw_(chess)
     * @return true if stalemate and set chessGame.stalemate to true
     */
    public boolean isStalemate() {
        int color = chessGame.getGameState() == ChessGame.GAME_STATE_WHITE ?
                Piece.COLOR_WHITE : Piece.COLOR_BLACK;
        int opponentColor = color == Piece.COLOR_WHITE ? Piece.COLOR_BLACK : Piece.COLOR_WHITE;
        if ((color == Piece.COLOR_WHITE && !chessGame.isWhiteKingInCheck()) ||
                (color == Piece.COLOR_BLACK && !chessGame.isBlackKingInCheck())) {
            return !hasValidMoves(color, opponentColor);
        }

        // TODO: fifty-move
        return false;
    }

    /**
     * checks if current game state is still able to move some piece
     * to valid positions
     * first iterate through opponent's pieces and calculate the positions under
     * attack; then iterate through player's pieces and try all the safe positions
     * for {@link this.isStalemate}: King not in check & have no valid moves
     * for {@link this.isCheckmate}: King in check & have no valid moves
     * @return true if has valid moves
     */
    private boolean hasValidMoves(int color, int opponentColor) {
        boolean[][] attackPosition = new
                boolean[Piece.ROW_8 - Piece.ROW_1 + 1][Piece.COLUMN_H - Piece.COLUMN_A + 1];

        // for every color piece, set it to isCaptured, compute enemy attack range
        // find safe position, set it to !isCaptured
        for (Piece sourcePiece : chessGame.getPieces()) {
            if (!sourcePiece.isCaptured() && sourcePiece.getColor() == color) {
                // TODO: set to isCaptured
                sourcePiece.setIsCaptured(true);

                // all position that are under attack
                for (Piece piece: chessGame.getPieces()) {
                    // for all alive opponent pieces, compute attack positions
                    // TODO: could remove isCaptured check here
                    if (!piece.isCaptured() && piece.getColor() == opponentColor) {
                        for (int i = 0; i < attackPosition.length; i++) {
                            for (int j = 0; j < attackPosition[0].length; j++) {
                                if (!attackPosition[i][j]) {
                                    Piece targetPiece = chessGame.getNonCapturedPieceAtLocation(i, j);
                                    if (isValidPieceMovementRules(null, piece, targetPiece, i, j, true, false))
                                        attackPosition[i][j] = true;
                                }
                            }
                        }
                    }
                }

//                printAttackPosition(attackPosition);

                for (int i = 0; i < attackPosition.length; i++) {
                    for (int j = 0; j < attackPosition[0].length; j++) {
                        if (!attackPosition[i][j]) {
                            Piece targetPiece = chessGame.getNonCapturedPieceAtLocation(i, j);
                            if (isValidPieceMovementRules(null, sourcePiece, targetPiece, i, j, true, false)) {
                                sourcePiece.setIsCaptured(false);
                                return true;
                            }
                        }
                    }
                }

                sourcePiece.setIsCaptured(false);
            }
        }

        return false;
    }

    /**
     * print the attack positions
     */
    private void printAttackPosition(boolean[][] attackPosition) {
        System.out.println("  a b c d e f g h  ");
        for (int row = Piece.ROW_8; row >= Piece.ROW_1; row--) {

            System.out.println(" +-+-+-+-+-+-+-+-+");
            String strRow = (row + 1) + "|";
            for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
                Piece piece = this.chessGame.getNonCapturedPieceAtLocation(row, column);
                String pieceStr = " ";
                if (piece != null) pieceStr = "P" ;
                if (attackPosition[row][column]) pieceStr = "*";

                strRow += pieceStr + "|";
            }
            System.out.println(strRow + (row + 1));
        }
        System.out.println(" +-+-+-+-+-+-+-+-+");
        System.out.println("  a b c d e f g h  ");

        String turnColor =
                (chessGame.getGameState() == ChessGame.GAME_STATE_BLACK ? "black" : "white");
        System.out.println("turn: " + turnColor);
    }


    /**
     * check if current game is checkmate: when color is going to lose
     * Checkmate (often shortened to mate) is a game position in chess (and in other
     * board games of the chaturanga family) in which a player's king is in check
     * (threatened with capture) and there is no way to remove the threat.
     *
     * King for current player is in check and there's no valid moves
     * @return true if checkmate and set chessGame.checkmate to true
     */
    public boolean isCheckmate() {
        int color = chessGame.getGameState() == ChessGame.GAME_STATE_WHITE ?
                Piece.COLOR_WHITE : Piece.COLOR_BLACK;
        int opponentColor = color == Piece.COLOR_WHITE ? Piece.COLOR_BLACK : Piece.COLOR_WHITE;
        if ((color == Piece.COLOR_WHITE && chessGame.isWhiteKingInCheck()) ||
                (color == Piece.COLOR_BLACK && chessGame.isBlackKingInCheck())) {
            return !hasValidMoves(color, opponentColor);
        }
        return false;
    }

    /**
     * checks if sourcePiece is able to attack targetPiece
     * @param sourcePiece Queen
     * @param targetPiece piece to attack
     * @param toRow targetPiece row
     * @param toColumn targetPiece column
     * @return true if Queen is able to attack targetPiece
     */
    private boolean inQueenAttack(Piece sourcePiece, Piece targetPiece,
                                  int toRow, int toColumn) {
        return isValidQueenMove(sourcePiece, targetPiece, toRow, toColumn);
    }

    /**
     * checks if sourcePiece is able to attack targetPiece
     * @param sourcePiece Rook
     * @param targetPiece piece to attack
     * @param toRow targetPiece row
     * @param toColumn targetPiece column
     * @return true if Rook is able to attack targetPiece
     */
    private boolean inRookAttack(Piece sourcePiece, Piece targetPiece,
                                 int toRow, int toColumn) {
        return isValidRookMove(sourcePiece, targetPiece, toRow, toColumn, true);
    }

    /**
     * checks if sourcePiece is able to attack targetPiece
     * @param sourcePiece Pawn
     * @param targetPiece piece to attack
     * @param toRow targetPiece row
     * @param toColumn targetPiece column
     * @return true if Pawn is able to attack targetPiece
     */
    private boolean inPawnAttack(Piece sourcePiece, Piece targetPiece,
                                 int toRow, int toColumn) {
        return isValidPawnMove(null, sourcePiece, targetPiece, toRow, toColumn, true);
    }

    /**
     * checks if sourcePiece is able to attack targetPiece
     * @param sourcePiece Knight
     * @param targetPiece piece to attack
     * @param toRow targetPiece row
     * @param toColumn targetPiece column
     * @return true if Knight is able to attack targetPiece
     */
    private boolean inKnightAttack(Piece sourcePiece, Piece targetPiece,
                                   int toRow, int toColumn) {
        return isValidKnightMove(sourcePiece, targetPiece, toRow, toColumn, true);
    }

    /**
     * checks if sourcePiece is able to attack targetPiece
     * @param sourcePiece Bishop
     * @param targetPiece piece to attack
     * @param toRow targetPiece row
     * @param toColumn targetPiece column
     * @return true if Bishop is able to attack targetPiece
     */
    private boolean inBishopAttack(Piece sourcePiece, Piece targetPiece,
                                   int toRow, int toColumn) {
        return isValidBishopMove(sourcePiece, targetPiece, toRow, toColumn, true);
    }

    /**
     * checks if sourcePiece is able to attack targetPiece
     * @param sourcePiece King
     * @param targetPiece piece to attack
     * @param toRow targetPiece row
     * @param toColumn targetPiece column
     * @return true if King is able to attack targetPiece
     */
    private boolean inKingAttack(Piece sourcePiece, Piece targetPiece,
                                 int toRow, int toColumn) {
        // no hint, is simulating and not prepare for castling
        return isValidKingMove(null, sourcePiece, targetPiece, toRow, toColumn, true, false);
    }

    /**
     * check if target location has opponent's piece
     * @return true if it has
     */
    private boolean isTargetLocationCapturable(Piece sourcePiece, Piece targetPiece) {
        return targetPiece != null && targetPiece.getColor() != sourcePiece.getColor();
    }

    /**
     * checks if target location is free
     * @param targetPiece provides target location
     * @return true if is free
     */
    private boolean isTargetLocationFree(Piece targetPiece) {
        return targetPiece == null;
    }

    /**
     * checks if Rook as sourcePiece is able to move to (toRow, toColumn) position
     * @param sourcePiece Rook
     * @param targetPiece piece on (toRow, toColumn) position
     * @param isSimulating if it's simulating, don't print out information
     * @return true if it is valid to move Rook
     */
    private boolean isValidRookMove(Piece sourcePiece, Piece targetPiece,
                                    int toRow, int toColumn,
                                    boolean isSimulating) {
        // The rook can move any number of squares along any rank or file, but
        // may not leap over other pieces. Along with the king, the rook is also
        // involved during the king's castling move.
        if (!isTargetLocationFree(targetPiece) && !isTargetLocationCapturable(sourcePiece, targetPiece)) {
            log("target location not free and not capturable", isSimulating);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

            return false;
        }

        boolean isValid;
        int fromRow = sourcePiece.getRow(), fromColumn = sourcePiece.getColumn();

        // first lets check if the path to the target is straight at all
        int diffRow = Math.abs(toRow - fromRow);
        int diffColumn = Math.abs(toColumn - fromColumn);

        if ((diffRow == 0 && diffColumn != 0) ||
                (diffColumn == 0 && diffRow != 0)) {
            isValid = !arePiecesBetweenSourceAndTarget(fromRow, fromColumn, toRow, toColumn, isSimulating);
        } else {
            log(sourcePiece.toString() + " not moving straight", isSimulating);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

            isValid = false;
        }

        return isValid;
    }

    /**
     * checks if Queen as sourcePiece is able to move to (toRow, toColumn) position
     * @param sourcePiece Queen
     * @param targetPiece piece on (toRow, toColumn) position
     * @return true if it is valid to move Queen
     */
    private boolean isValidQueenMove(Piece sourcePiece, Piece targetPiece,
                                     int toRow, int toColumn) {
        // The queen combines the power of the rook and bishop and can move any number
        // of squares along rank, file, or diagonal, but it may not leap over other pieces.
        //
        boolean result = isValidBishopMove(sourcePiece, targetPiece, toRow, toColumn, true);
        result |= isValidRookMove(sourcePiece, targetPiece, toRow, toColumn, true);
        return result;
    }

    /**
     * It's permissible to move Pawn two steps only at the first time.
     * checks if Pawn as sourcePiece is able to move to (toRow, toColumn) position
     * @param sourcePiece Pawn
     * @param targetPiece piece on (toRow, toColumn) position
     * @param isSimulating just simulating, don't set any value
     * @return true if it is valid to move Pawn
     */
    private boolean isValidPawnMove(Move move, Piece sourcePiece, Piece targetPiece,
                                    int toRow, int toColumn,
                                    boolean isSimulating) {
        boolean isValid = false;
        int fromRow = sourcePiece.getRow(), fromColumn = sourcePiece.getColumn();
        // The pawn may move forward to the unoccupied square immediately in front
        // of it on the same file, or on its first move it may advance two squares
        // along the same file provided both squares are unoccupied

        int color = sourcePiece.getColor();
        int increment = color == Piece.COLOR_WHITE ? 1 : -1;
        int initialRow = color == Piece.COLOR_WHITE ? Piece.ROW_2 : Piece.ROW_7;
        int promotionRow = color == Piece.COLOR_WHITE ? Piece.ROW_8 : Piece.ROW_1;
        if (isTargetLocationFree(targetPiece)) {
            // Pawn push
            if (fromColumn == toColumn) {
                // same column
                if (fromRow + increment == toRow) {
                    // move one up
                    if (!isSimulating) {
                        sourcePiece.setLastMovedTwoSteps(false);
                        // TODO: Pawn promotion
                        if (toRow == promotionRow) {
//                            System.out.println("------promotion by pawn push------");
//                            System.out.println(move.toString());
//                            System.out.println("------------------------------------");
//                            move.pawnPromotion = true;
                            move.pawnPromotion(sourcePiece, null);
//                            sourcePiece.pawnPromotion();
                        }
                    }
                    isValid = true;
                    // on its first move it may advance two squares
                } else if (fromRow == initialRow &&
                        fromRow + increment * 2 == toRow &&
                        !arePiecesBetweenSourceAndTarget(fromRow, fromColumn, toRow, toColumn, isSimulating)) {
                    if (!isSimulating) {
                        sourcePiece.setLastMovedTwoSteps(true);
                        move.pawnTwoSteps = true;
                    }
                    isValid = true;
                } else {
                    if (fromRow != initialRow)
                        log(sourcePiece.toString() + " not moving one up and target location is free", isSimulating);
                    else log(sourcePiece.toString() + " not moving two up and target location is free", isSimulating);

                    //
                    //
                    log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                    isValid = false;
                }
            } else {
                // not the same column
                log(sourcePiece.toString() + " not staying in same column and target location is free", isSimulating);

                //
                //
                log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                isValid = false;
            }
            // or it may move
            // to a square occupied by an opponents piece, which is diagonally in front
            // of it on an adjacent file, capturing that piece.
        } else if (isTargetLocationCapturable(sourcePiece, targetPiece)) {
            if (fromColumn - increment == toColumn || fromColumn + increment == toColumn) {
                // one column to the right or left
                    if (fromRow + increment == toRow) {
                        // move one up
                        if (!isSimulating) {
                            sourcePiece.setLastMovedTwoSteps(false);
                            // TODO: Pawn promotion
                            if (toRow == promotionRow) {
//                                System.out.println("--------promotion by capture--------");
//                                sourcePiece.pawnPromotion();
//                                move.pawnPromotion = true;
//                                System.out.println(move.toString());
//                                System.out.println("------------------------------------");
                                move.pawnPromotion(sourcePiece, targetPiece);
                            }
                        }
                        isValid = true;
                        // TODO: pawn capture en passant
                    } else if (fromRow == toRow && targetPiece.isLastMovedTwoSteps()) {
                        if (!isSimulating) {
//                            sourcePiece.setPerformedEnPassant(true);
                            move.enPassant = true;
                        }
                        isValid = true;
                    } else {
                        log(sourcePiece.toString() + " not moving one up and target location is capturable", isSimulating);

                        //
                        //
                        log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                        isValid = false;
                    }
            } else {
                // note one column to the left or right
                log(sourcePiece.toString() + " not moving one column to left or right + from ("
                        + fromRow + ", " + fromColumn + ") to (" + toRow + ", " + toColumn + ")", isSimulating);

                //
                //
                log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                isValid = false;
            }
        }

        // TODO:
        // The pawn has two special
        // moves, the en passant capture, and pawn promotion.
        return isValid;
    }

    /**
     * checks if Knight as sourcePiece is able to move to (toRow, toColumn) position
     * @param sourcePiece Knight
     * @param targetPiece piece on (toRow, toColumn) position
     * @return true if it is valid to move Knight
     */
    private boolean isValidKnightMove(Piece sourcePiece, Piece targetPiece,
                                      int toRow, int toColumn,
                                      boolean isSimulating) {
        // The knight moves to any of the closest squares which are not on the same rank,
        // file or diagonal, thus the move forms an "L"-shape two squares long and one
        // square wide. The knight is the only piece which can leap over other pieces.

        // target location possible?
        if (!isTargetLocationFree(targetPiece) && !isTargetLocationCapturable(sourcePiece, targetPiece)) {
            log("target location not free and not capturable", isSimulating);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

            return false;
        }

        int fromRow = sourcePiece.getRow(), fromColumn = sourcePiece.getColumn();
        int diffRow = Math.abs(toRow - fromRow);
        int diffColumn = Math.abs(toColumn - fromColumn);

        return (diffRow == 2 && diffColumn == 1) ||
                (diffRow == 1 && diffColumn == 2);
    }

    /**
     * checks if King as sourcePiece is able to move to (toRow, toColumn) position
     * @param sourcePiece King
     * @param targetPiece piece on (toRow, toColumn) position
     * @param isSimulating if true, don't count castling
     * @param forPossibleMove if true, modify Rook related with castling
     * @return true if it is valid to move King
     */
    private boolean isValidKingMove(Move move, Piece sourcePiece, Piece targetPiece,
                                    int toRow, int toColumn,
                                    boolean isSimulating, boolean forPossibleMove) {
        // target location possible?
        if (!isTargetLocationFree(targetPiece) && !isTargetLocationCapturable(sourcePiece, targetPiece)) {
            log("target location not free and not capturable", isSimulating);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

            return false;
        }

        int fromRow = sourcePiece.getRow(), fromColumn = sourcePiece.getColumn();
        // The king moves one square in any direction, the king has also a special move which is
        // called castling and also involves a rook.
        int diffRow = Math.abs(toRow - fromRow);
        int diffColumn = Math.abs(toColumn - fromColumn);

        if ((diffRow == 1 && diffColumn == 0) ||
                (diffColumn == 1 && diffRow == 0) ||
                (diffRow == 1 && diffColumn == 1)) {
            return true;
        } else {
            // TODO: castling
            // ..
            if (isSimulating && !forPossibleMove) return false;
//            if (isSimulating) return false;

            boolean isKingInCheck = chessGame.getGameState() == ChessGame.GAME_STATE_BLACK ?
                    chessGame.isBlackKingInCheck() :
                    chessGame.isWhiteKingInCheck();
            if (sourcePiece.hasNotMoved() &&
                    diffRow == 0 && diffColumn == 2 &&
                    !isKingInCheck) {
                Move castlingMove = castlingValid(move, sourcePiece, toRow, toColumn, isSimulating);
                if (castlingMove != null) {
                    // TODO: preform castling
                    if (!isSimulating) {
                        move.rookCastlingMove = castlingMove;
//                        sourcePiece.setCastling(true);
//                        sourcePiece.setRookForCastling(rookForCastling);
//                        sourcePiece.setCastlingMove(new Move(fromRow, fromColumn, toRow, toColumn));
//                        System.out.println("successful castling for " + sourcePiece.toString());
                    }
                    return true;
                }
                else {
                    log("invalid castling for " + sourcePiece.toString(), isSimulating);

                    //
                    //
                    log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                    return false;
                }
            } else {
                log(sourcePiece.toString() + " moving too far", isSimulating);

                //
                //
                log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                return false;
            }
        }
    }

    /**
     * checks if it is valid to perform castling
     * @param isSimulating if true, don't set position and modify related Rook
     * @return null if not valid, move for rook if valid
     */
    private Move castlingValid(Move move, Piece sourcePiece, int toRow, int toColumn,
                                boolean isSimulating) {
        int fromRow = sourcePiece.getRow();
        int fromColumn = sourcePiece.getColumn();
        Piece rookForCastling;
        Move castlingMove = new Move(0, 0, 0, 0);

        if (toColumn == Piece.COLUMN_C) {
            rookForCastling = chessGame.getNonCapturedPieceAtLocation(fromRow, Piece.COLUMN_A);
        } else { // Piece.COLUMN_G
            rookForCastling = chessGame.getNonCapturedPieceAtLocation(fromRow, Piece.COLUMN_H);
        }

        if (rookForCastling!= null &&
                rookForCastling.getType() == Piece.TYPE_ROOK &&
                rookForCastling.getColor() == sourcePiece.getColor() &&
                rookForCastling.hasNotMoved() &&
                !arePiecesBetweenSourceAndTarget(fromRow, fromColumn,
                        toRow, rookForCastling.getColumn(), isSimulating)) {
            castlingMove.sourceColumn = rookForCastling.getColumn();
            castlingMove.sourceRow = fromRow;
            castlingMove.targetRow = fromRow;
            if (move != null) castlingMove.isAi = move.isAi;
            if (!move.isAi) {
                if (toColumn == Piece.COLUMN_C) {
                    if (!isSimulating) {
                        castlingMove.targetColumn = Piece.COLUMN_D;
                        rookForCastling.setColumn(Piece.COLUMN_D);
                    }
                } else {
                    if (!isSimulating) {
                        castlingMove.targetColumn = Piece.COLUMN_F;
                        rookForCastling.setColumn(Piece.COLUMN_F);
                    }
                }
            }
            return castlingMove;
        }
        else return null;
    }

    /**
     * checks if Bishop as sourcePiece is able to move to (toRow, toColumn) position
     * @param sourcePiece Bishop
     * @param targetPiece piece on (toRow, toColumn) position
     * @return true if it is valid to move Bishop
     */
    private boolean isValidBishopMove(Piece sourcePiece, Piece targetPiece,
                                      int toRow, int toColumn,
                                      boolean isSimulating) {
        //The bishop can move any number of squares diagonally, but may not leap
        //over other pieces.
        // target location possible?
        if (!isTargetLocationFree(targetPiece) && !isTargetLocationCapturable(sourcePiece, targetPiece)) {
            log("target location not free and not capturable", isSimulating);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

            return false;
        }

        boolean isValid;
        int fromRow = sourcePiece.getRow(), fromColumn = sourcePiece.getColumn();
        // first lets check if the path to the target is diagonally at all
        int diffRow = toRow - fromRow;
        int diffColumn = toColumn - fromColumn;

        if (Math.abs(diffRow) == Math.abs(diffColumn)) {
            isValid = !arePiecesBetweenSourceAndTarget(fromRow, fromColumn, toRow, toColumn, isSimulating);
        } else {
            // not moving diagonally
            log(sourcePiece.toString() + " not moving diagonally", isSimulating);

            //
            //
            log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

            isValid = false;
        }

        return isValid;
    }

    /**
     * checks if there are pieces between sourcePiece and targetPiece
     * @return true if there are pieces between source and target
     */
    private boolean arePiecesBetweenSourceAndTarget(int fromRow, int fromColumn,
                                                    int toRow, int toColumn,
                                                    boolean isSimulating) {
        int rowIncrementPerStep, columnIncrementPerStep;

        if (fromRow > toRow) rowIncrementPerStep = -1;
        else if (fromRow < toRow) rowIncrementPerStep = 1;
        else rowIncrementPerStep = 0;

        if (fromColumn > toColumn) columnIncrementPerStep = -1;
        else if (fromColumn < toColumn) columnIncrementPerStep = 1;
        else columnIncrementPerStep = 0;

        int currentRow = fromRow + rowIncrementPerStep;
        int currentColumn = fromColumn + columnIncrementPerStep;
        while (true) {
            if(currentRow == toRow && currentColumn == toColumn) {
                break;
            }
            if (currentRow < Piece.ROW_1 || currentRow > Piece.ROW_8
                    || currentColumn < Piece.COLUMN_A || currentColumn > Piece.COLUMN_H) {
                break;
            }

            if (chessGame.hasNonCapturedPieceAtLocation(currentRow, currentColumn)) {
                log("pieces in between source and target", isSimulating);

                //
                //
                log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                return true;
            }

            currentRow += rowIncrementPerStep;
            currentColumn += columnIncrementPerStep;
        }
        return false;
    }

    @SuppressWarnings("unused")
    private boolean arePiecesBetweenSourceAndTarget(int fromRow, int fromColumn,
                                                    int toRow, int toColumn,
                                                    int rowIncrementPerStep, int columnIncrementPerStep,
                                                    boolean isSimulating) {

        int currentRow = fromRow + rowIncrementPerStep;
        int currentColumn = fromColumn + columnIncrementPerStep;
        while(true) {
            if(currentRow == toRow && currentColumn == toColumn) {
                break;
            }
            if (currentRow < Piece.ROW_1 || currentRow > Piece.ROW_8
                    || currentColumn < Piece.COLUMN_A || currentColumn > Piece.COLUMN_H) {
                break;
            }

            if (chessGame.hasNonCapturedPieceAtLocation(currentRow, currentColumn)) {
                log("pieces in between source and target", isSimulating);

                //
                //
                log("Line at: ChessRule.java " + new Exception().getStackTrace()[0].getLineNumber(), isSimulating);

                return true;
            }

            currentRow += rowIncrementPerStep;
            currentColumn += columnIncrementPerStep;
        }
        return false;
    }

    /**
     * print out information when not in simulation and in debug
     */
    private void log(String message, boolean isSimulating) {
        if (!isSimulating && debug) System.out.println(message);
    }

    public static void main(String[] args) {
        ChessGame ch = new ChessGame();
        ChessRule mo = new ChessRule(ch);
        Move move;
        boolean isValid;

        int sourceRow; int sourceColumn; int targetRow; int targetColumn;
        int testCounter = 1;

        // ok
        sourceRow = Piece.ROW_2; sourceColumn = Piece.COLUMN_D;
        targetRow = Piece.ROW_3; targetColumn = Piece.COLUMN_D;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        ch.movePiece(move);
        System.out.println(testCounter+". test result: "+(isValid));
        testCounter++;

        // it's not white's turn
        sourceRow = Piece.ROW_2; sourceColumn = Piece.COLUMN_B;
        targetRow = Piece.ROW_3; targetColumn = Piece.COLUMN_B;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        System.out.println(testCounter+". test result: "+(!isValid));
        testCounter++;

        // ok
        sourceRow = Piece.ROW_7; sourceColumn = Piece.COLUMN_E;
        targetRow = Piece.ROW_6; targetColumn = Piece.COLUMN_E;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        ch.movePiece(move);
        System.out.println(testCounter+". test result: "+(isValid));
        testCounter++;

        // pieces in the way
        sourceRow = Piece.ROW_1; sourceColumn = Piece.COLUMN_F;
        targetRow = Piece.ROW_4; targetColumn = Piece.COLUMN_C;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        System.out.println(testCounter+". test result: "+(!isValid));
        testCounter++;

        // ok
        sourceRow = Piece.ROW_1; sourceColumn = Piece.COLUMN_C;
        targetRow = Piece.ROW_4; targetColumn = Piece.COLUMN_F;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        ch.movePiece(move);
        System.out.println(testCounter+". test result: "+(isValid));
        testCounter++;

        // ok
        sourceRow = Piece.ROW_8; sourceColumn = Piece.COLUMN_B;
        targetRow = Piece.ROW_6; targetColumn = Piece.COLUMN_C;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        ch.movePiece(move);
        System.out.println(testCounter+". test result: "+(isValid));
        testCounter++;

        // invalid knight move
        sourceRow = Piece.ROW_1; sourceColumn = Piece.COLUMN_G;
        targetRow = Piece.ROW_3; targetColumn = Piece.COLUMN_G;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        System.out.println(testCounter+". test result: "+(!isValid));
        testCounter++;

        // invalid knight move
        sourceRow = Piece.ROW_1; sourceColumn = Piece.COLUMN_G;
        targetRow = Piece.ROW_2; targetColumn = Piece.COLUMN_E;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        System.out.println(testCounter+". test result: "+(!isValid));
        testCounter++;

        // ok
        sourceRow = Piece.ROW_1; sourceColumn = Piece.COLUMN_G;
        targetRow = Piece.ROW_3; targetColumn = Piece.COLUMN_H;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        ch.movePiece(move);
        System.out.println(testCounter+". test result: "+(isValid));
        testCounter++;

        // pieces in between
        sourceRow = Piece.ROW_8; sourceColumn = Piece.COLUMN_A;
        targetRow = Piece.ROW_5; targetColumn = Piece.COLUMN_A;
        move = new Move(sourceRow, sourceColumn, targetRow, targetColumn);
        isValid = mo.isValidMove(move, true);
        ch.movePiece(move);
        System.out.println(testCounter+". test result: "+(!isValid));
//        testCounter++;

        //ConsoleGui.printCurrentGameState(ch);
    }
}
