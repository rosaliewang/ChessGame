package logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Yuchen Wang on 8/15/15.
 */
public class Move {
    public int sourceRow;
    public int sourceColumn;
    public int targetRow;
    public int targetColumn;
    private boolean isValid;

    public int score;
    public Piece capturedPiece;

    public Move rookCastlingMove; // [King, Rook]: castling; also reset Piece.hasNotMoved

    public boolean pawnPromotion; // just performed Pawn Promotion
    public boolean pawnTwoSteps; // Pawn just moved two steps
    public boolean enPassant; // Pawn just performed En Passant

    public Move(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
        this.sourceRow = sourceRow;
        this.sourceColumn = sourceColumn;
        this.targetRow = targetRow;
        this.targetColumn = targetColumn;
//        this.isValid = false;
    }

    @Override
    public String toString() {
        String str = "";
        str += Piece.getRowString(sourceRow);
        str += Piece.getColumnString(sourceColumn);
        str += "->";
        str += Piece.getRowString(targetRow);
        str += Piece.getColumnString(targetColumn);
        return str;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    public Move clone(){
        return new Move(sourceRow, sourceColumn, targetRow, targetColumn);
    }

    /**
     * move to (x, y), capture -> on drawing: modify move to (x - 1, y)
     * when undo: release captured piece and return to source position
     */
    public void enPassantPosition(int enPassantIncrement) {
        targetRow += enPassantIncrement;
    }

    /**
     * for Pawn promotion
     */
    public void pawnPromotion(Piece piece, Piece pieceToCapture) {
        System.out.println(Thread.currentThread() + " pawn promotion");
        pawnPromotion = true;

        if (pieceToCapture != null) {
            if (pieceToCapture.getType() == Piece.TYPE_KING) return;
        }

        String input;
        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            // print game state and ask for user input
            //
            System.out.println("pawn promotion to one of the following");
            System.out.println("Queen/q, Bishop/b, Knight/n, Rook/r: ");

            try {
                // read user input
                input = inputReader.readLine();

                // exit, if user types 'exit'
                if (input.equalsIgnoreCase("queen") ||
                        input.equalsIgnoreCase("q")) {
                    piece.setType(Piece.TYPE_QUEEN);
                    return;
                } else if (input.equalsIgnoreCase("bishop") ||
                        input.equalsIgnoreCase("b")) {
                    piece.setType(Piece.TYPE_BISHOP);
                    return;
                } else if (input.equalsIgnoreCase("knight") ||
                        input.equalsIgnoreCase("n")) {
                    piece.setType(Piece.TYPE_KNIGHT);
                    return;
                } else if (input.equalsIgnoreCase("rook") ||
                        input.equalsIgnoreCase("r")) {
                    piece.setType(Piece.TYPE_ROOK);
                    return;
                } else {
                    System.out.println("invalid input, retry");
                }
            } catch (Exception e) {
                System.out.println(e.getClass() + ": " + e.getMessage());
            }
        }
    }

    /**
     * undo pawn promotion
     */
    public void undoPromotion(Piece piece) {
        if (pawnPromotion) {
            piece.setType(Piece.TYPE_PAWN);
            pawnPromotion = false;
        }
    }
}
