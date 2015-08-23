package logic;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by Yuchen Wang on 8/13/15.
 */
public class Piece {
    private int color;
    private int type;
    private boolean hasNotMoved; // King & Rook: first move could choose castling
    private boolean lastMovedTwoSteps; // Pawn: moved two steps in last turn
    private boolean captureEnPassant; // Pawn: just performed captureEnPassant
    private boolean hasPromotion; // for Pawn reached last rank
    private boolean castling; // King

    private Piece rookForCastling; // King: rook to castling

    public static final int COLOR_WHITE = 0;
    public static final int COLOR_BLACK = 1;

    public static final int TYPE_ROOK = 1;
    public static final int TYPE_KNIGHT = 2;
    public static final int TYPE_BISHOP = 3;
    public static final int TYPE_QUEEN = 4;
    public static final int TYPE_KING = 5;
    public static final int TYPE_PAWN = 6;

    //Chess is played on a square board of
    //eight rows (called ranks and denoted with numbers 1 to 8)
    //and eight columns (called files and denoted with letters a to h) of squares.
    private int row;
    public static final int ROW_1 = 0;
    public static final int ROW_2 = 1;
    public static final int ROW_3 = 2;
    public static final int ROW_4 = 3;
    public static final int ROW_5 = 4;
    public static final int ROW_6 = 5;
    public static final int ROW_7 = 6;
    public static final int ROW_8 = 7;

    private int column;
    public static final int COLUMN_A = 0;
    public static final int COLUMN_B = 1;
    public static final int COLUMN_C = 2;
    public static final int COLUMN_D = 3;
    public static final int COLUMN_E = 4;
    public static final int COLUMN_F = 5;
    public static final int COLUMN_G = 6;
    public static final int COLUMN_H = 7;

    private boolean isCaptured = false;

    public Piece(int color, int type, int row, int column) {
        this.row = row;
        this.column = column;
        this.type = type;
        this.color = color;
        this.hasNotMoved = true;
        this.lastMovedTwoSteps = false; // for Pawn
        this.captureEnPassant = false; // for Pawn
        this.hasPromotion = false; // for Pawn
        this.rookForCastling = null;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColor() {
        return color;
    }

    public int getType() {
        return type;
    }

    public boolean hasNotMoved() {
        return hasNotMoved;
    }

    public boolean isLastMovedTwoSteps() {
        return lastMovedTwoSteps;
    }

    public boolean hasPromotion() {
        return hasPromotion;
    }

    public boolean isCastling() {
        return castling;
    }

    @Override
    public String toString() {
        String strColor = (this.color==COLOR_WHITE?"white":"black");

        String strType = "unknown";
        switch (this.type) {
            case TYPE_BISHOP: strType = "B";break;
            case TYPE_KING: strType = "K";break;
            case TYPE_KNIGHT: strType = "N";break;
            case TYPE_PAWN: strType = "P";break;
            case TYPE_QUEEN: strType = "Q";break;
            case TYPE_ROOK: strType = "R";break;
        }

        String strRow = getRowString(this.row);
        String strColumn = getColumnString(this.column);

        return strColor+" "+strType+" "+strRow+"/"+strColumn;
    }

    public String getColorString() {
        return color == COLOR_BLACK ? "black" : "white";
    }

    public static String getColorString(int color) {
        return color == COLOR_BLACK ? "black" : "white";
    }

    public boolean isCaptureEnPassant() {
        return captureEnPassant;
    }

    public static String getTypeInColor(int color, int type) {
        String strType = "unknown";
        if (color == COLOR_WHITE) {
            switch (type) {
                case TYPE_BISHOP:   strType = "♗"; break;
                case TYPE_KING:     strType = "♔"; break;
                case TYPE_KNIGHT:   strType = "♘"; break;
                case TYPE_PAWN:     strType = "♙"; break;
                case TYPE_QUEEN:    strType = "♕"; break;
                case TYPE_ROOK:     strType = "♖"; break;
            }
        } else if (color == COLOR_BLACK) {
            switch (type) {
                case TYPE_BISHOP:   strType = "♝"; break;
                case TYPE_KING:     strType = "♚"; break;
                case TYPE_KNIGHT:   strType = "♞"; break;
                case TYPE_PAWN:     strType = "♟"; break;
                case TYPE_QUEEN:    strType = "♛"; break;
                case TYPE_ROOK:     strType = "♜"; break;
            }
        }
        return strType;
    }

    public static String getTypeString(int type) {
        String strType = "unknown";
        switch (type) {
            case TYPE_BISHOP:   strType = "Bishop"; break;
            case TYPE_KING:     strType = "King"; break;
            case TYPE_KNIGHT:   strType = "Knight"; break;
            case TYPE_PAWN:     strType = "Pawn"; break;
            case TYPE_QUEEN:    strType = "Queen"; break;
            case TYPE_ROOK:     strType = "Rook"; break;
        }
        return strType;
    }


    public static String getRowString(int row){
        String strRow = "unknown";
        switch (row) {
            case ROW_1: strRow = "1";break;
            case ROW_2: strRow = "2";break;
            case ROW_3: strRow = "3";break;
            case ROW_4: strRow = "4";break;
            case ROW_5: strRow = "5";break;
            case ROW_6: strRow = "6";break;
            case ROW_7: strRow = "7";break;
            case ROW_8: strRow = "8";break;
        }
        return strRow;
    }

    public static String getColumnString(int column){
        String strColumn = "unknown";
        switch (column) {
            case COLUMN_A: strColumn = "A";break;
            case COLUMN_B: strColumn = "B";break;
            case COLUMN_C: strColumn = "C";break;
            case COLUMN_D: strColumn = "D";break;
            case COLUMN_E: strColumn = "E";break;
            case COLUMN_F: strColumn = "F";break;
            case COLUMN_G: strColumn = "G";break;
            case COLUMN_H: strColumn = "H";break;
        }
        return strColumn;
    }

    public Piece getRookForCastling() {
        return rookForCastling;
    }

    public void setRookForCastling(Piece rookForCastling) {
        this.rookForCastling = rookForCastling;
    }

    public void setHasNotMoved(boolean hasNotMoved) {
        this.hasNotMoved = hasNotMoved;
    }

    public void setLastMovedTwoSteps(boolean lastMovedTwoSteps) {
        this.lastMovedTwoSteps = lastMovedTwoSteps;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCaptureEnPassant(boolean captureEnPassant) {
        this.captureEnPassant = captureEnPassant;
    }

    public void setCastling(boolean castling) {
        this.castling = castling;
    }

    /**
     * for Pawn promotion
     */
    public void pawnPromotion() {
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
                    setType(TYPE_QUEEN);
                    hasPromotion = true;
                    return;
                } else if (input.equalsIgnoreCase("bishop") ||
                        input.equalsIgnoreCase("b")) {
                    hasPromotion = true;
                    setType(TYPE_BISHOP);
                    return;
                } else if (input.equalsIgnoreCase("knight") ||
                        input.equalsIgnoreCase("n")) {
                    hasPromotion = true;
                    setType(TYPE_KNIGHT);
                    return;
                } else if (input.equalsIgnoreCase("rook") ||
                        input.equalsIgnoreCase("r")) {
                    hasPromotion = true;
                    setType(TYPE_ROOK);
                    return;
                } else {
                    System.out.println("invalid input, retry");
                }
            } catch (Exception e) {
                System.out.println(e.getClass() + ": " + e.getMessage());
            }
        }
    }

    public void setIsCaptured(boolean isCaptured) {
        this.isCaptured = isCaptured;
    }

    public boolean isCaptured() {
        return isCaptured;
    }
}
