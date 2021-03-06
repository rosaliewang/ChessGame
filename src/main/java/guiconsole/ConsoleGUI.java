//package guiconsole;
//
//import logic.ChessGame;
//import logic.Move;
//import logic.Piece;
//import logic.PlayerHandler;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//
///**
// * Created by Yuchen Wang on 8/14/15.
// */
//public class ConsoleGUI {
//    private ChessGame chessGame;
//
//    public ConsoleGUI() {
//        chessGame = new ChessGame();
//    }
//
//    public static void main(String[] args) {
//        new ConsoleGUI().run();
//    }
//
//    /**
//     * Contains the main loop of the application. The application will print the
//     * current game state and ask the user for his move. If the user enters
//     * "exit", the application ends. Otherwise the user input is interpreted as
//     * a move and the application tries to execute that move.
//     */
//    public void run() {
//
//        // prepare for reading input
//        //
//        String input;
//        BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
//
//        while (true) {
//            // print game state and ask for user input
//            //
//            printCurrentGameState();
//            System.out.println("your move (e.g. e2-e4): ");
//            try {
//                // read user input
//                input = inputReader.readLine();
//
//                // exit, if user types 'exit'
//                if (input.equalsIgnoreCase("exit")){
//                    return;
//                } else {
//                    handleMove(input);
//                    chessGame.changeGameState();
//                }
//            } catch (Exception e) {
//                System.out.println(e.getClass() + ": " + e.getMessage());
//            }
//        }
//    }
//
//    /**
//     * Move piece to the specified location.
//     *
//     * @param input - a valid move-string (e.g. "e7-e5")
//     */
//    private void handleMove(String input) {
//        String strSourceColumn = input.substring(0, 1);
//        String strSourceRow = input.substring(1, 2);
//        String strTargetColumn = input.substring(3, 4);
//        String strTargetRow = input.substring(4, 5);
//
//        int sourceColumn = convertColumnStrToColumnInt(strSourceColumn);
//        int sourceRow = convertRowStrToRowInt(strSourceRow);
//        int targetColumn = convertColumnStrToColumnInt(strTargetColumn);
//        int targetRow = convertRowStrToRowInt(strTargetRow);
//
//        chessGame.movePiece(new Move(sourceRow, sourceColumn, targetRow, targetColumn));
//    }
//
//    /**
//     * Converts a column string (e.g. 'a') into its internal representation.
//     *
//     * @param strColumn a valid column string (e.g. 'a')
//     * @return internal integer representation of the column
//     */
//    private int convertColumnStrToColumnInt(String strColumn) {
//        if (strColumn.equalsIgnoreCase("a")) {
//            return Piece.COLUMN_A;
//        } else if (strColumn.equalsIgnoreCase("b")) {
//            return Piece.COLUMN_B;
//        } else if (strColumn.equalsIgnoreCase("c")) {
//            return Piece.COLUMN_C;
//        } else if (strColumn.equalsIgnoreCase("d")) {
//            return Piece.COLUMN_D;
//        } else if (strColumn.equalsIgnoreCase("e")) {
//            return Piece.COLUMN_E;
//        } else if (strColumn.equalsIgnoreCase("f")) {
//            return Piece.COLUMN_F;
//        } else if (strColumn.equalsIgnoreCase("g")) {
//            return Piece.COLUMN_G;
//        } else if (strColumn.equalsIgnoreCase("h")) {
//            return Piece.COLUMN_H;
//        } else
//            throw new IllegalArgumentException("invalid column: " + strColumn);
//    }
//
//    /**
//     * Converts a row string (e.g. '1') into its internal representation.
//     *
//     * @param strRow a valid row string (e.g. '1')
//     * @return internal integer representation of the row
//     */
//    private int convertRowStrToRowInt(String strRow) {
//        if (strRow.equalsIgnoreCase("1")) {
//            return Piece.ROW_1;
//        } else if (strRow.equalsIgnoreCase("2")) {
//            return Piece.ROW_2;
//        } else if (strRow.equalsIgnoreCase("3")) {
//            return Piece.ROW_3;
//        } else if (strRow.equalsIgnoreCase("4")) {
//            return Piece.ROW_4;
//        } else if (strRow.equalsIgnoreCase("5")) {
//            return Piece.ROW_5;
//        } else if (strRow.equalsIgnoreCase("6")) {
//            return Piece.ROW_6;
//        } else if (strRow.equalsIgnoreCase("7")) {
//            return Piece.ROW_7;
//        } else if (strRow.equalsIgnoreCase("8")) {
//            return Piece.ROW_8;
//        } else
//            throw new IllegalArgumentException("invalid column: " + strRow);
//    }
//
//    /**
//     * Print current game board and game state information.
//     */
//    private void printCurrentGameState() {
//
//        System.out.println("  a  b  c  d  e  f  g  h  ");
//        for (int row = Piece.ROW_8; row >= Piece.ROW_1; row--) {
//
//            System.out.println(" +--+--+--+--+--+--+--+--+");
//            String strRow = (row + 1) + "|";
//            for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
//                Piece piece = this.chessGame.getNonCapturedPieceAtLocation(row, column);
//                String pieceStr = getNameOfPiece(piece);
//                strRow += pieceStr + "|";
//            }
//            System.out.println(strRow + (row + 1));
//        }
//        System.out.println(" +--+--+--+--+--+--+--+--+");
//        System.out.println("  a  b  c  d  e  f  g  h  ");
//
//        String turnColor =
//                (chessGame.getGameState() == ChessGame.GAME_STATE_BLACK ? "black" : "white");
//        System.out.println("turn: " + turnColor);
//
//    }
//
//    /**
//     * Returns the name of the specified piece. The name is based on color and
//     * type.
//     *
//     * The first letter represents the color: B=black, W=white, ?=unknown
//     *
//     * The second letter represents the type: B=Bishop, K=King, N=Knight,
//     * P=Pawn, Q=Queen, R=Rook, ?=unknown
//     *
//     * A two letter empty string is returned in case the specified piece is null
//     *
//     * @param piece a chess piece
//     * @return string representation of the piece or a two letter empty string
//     *         if the specified piece is null
//     */
//    private String getNameOfPiece(Piece piece) {
//        if (piece == null)
//            return "  ";
//
//        String strColor;
//        switch (piece.getColor()) {
//            case Piece.COLOR_BLACK:
//                strColor = "B";
//                break;
//            case Piece.COLOR_WHITE:
//                strColor = "W";
//                break;
//            default:
//                strColor = "?";
//                break;
//        }
//
//        String strType;
//        switch (piece.getType()) {
//            case Piece.TYPE_BISHOP:
//                strType = "B";
//                break;
//            case Piece.TYPE_KING:
//                strType = "K";
//                break;
//            case Piece.TYPE_KNIGHT:
//                strType = "N";
//                break;
//            case Piece.TYPE_PAWN:
//                strType = "P";
//                break;
//            case Piece.TYPE_QUEEN:
//                strType = "Q";
//                break;
//            case Piece.TYPE_ROOK:
//                strType = "R";
//                break;
//            default:
//                strType = "?";
//                break;
//        }
//
//        return strColor + strType;
//    }
//
//}
