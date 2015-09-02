package gui;

import gameEntry.Main;
import logic.*;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class draws the gui of a chessboard with files(columns) and ranks(rows).
 * Created by Yuchen Wang on 8/13/15.
 */
public class ChessBoardGUI extends JPanel implements PlayerHandler {
    private static final int BOARD_START_X = 301;
    private static final int BOARD_START_Y = 51;

    private static final int SQUARE_WIDTH = 50;
    private static final int SQUARE_HEIGHT = 50;

    private static final int PIECE_WIDTH = 48;
    private static final int PIECE_HEIGHT = 48;

    private static final int PIECES_START_X = BOARD_START_X + (int)(SQUARE_WIDTH/2.0 - PIECE_WIDTH/2.0);
    private static final int PIECES_START_Y = BOARD_START_Y + (int)(SQUARE_HEIGHT/2.0 - PIECE_HEIGHT/2.0);

    private static final int DRAG_TARGET_SQUARE_START_X = BOARD_START_X - (int)(PIECE_WIDTH/2.0);
    private static final int DRAG_TARGET_SQUARE_START_Y = BOARD_START_Y - (int)(PIECE_HEIGHT/2.0);

    private Image imgBackground;
    private JLabel lblGameState;

    private ChessGame chessGame;
    // 0 = bottom, size-1 = top
    private List<GUIPiece> guiPieces = new ArrayList<GUIPiece>();

    private GUIPiece dragPiece; // currently dragged game piece

    private PiecesListener piecesListener;

    public Move lastMove; // the last executed move (used for highlighting)
    private Move currentMove;

    private boolean debug; // set debug true

    private boolean draggingGamePiecesEnabled;

    private final Object lock = new Object();

    public ChessBoardGUI(ChessGame chessGame) {
        this.setLayout(null);

        URL backgroundImg = getClass().getResource("../img/board01.png");
        this.imgBackground = new ImageIcon(backgroundImg).getImage();

        this.chessGame = chessGame;

        addAllGuiPieces();

//        PiecesListener piecesListener = new PiecesListener(guiPieces, this);
        this.piecesListener = new PiecesListener(guiPieces, this);
        this.addMouseListener(piecesListener);
        this.addMouseMotionListener(piecesListener);

        // button to start new game
        JButton btnChangeGameState = new JButton("rematch");
        btnChangeGameState
                .addActionListener(new RestartGameListener(this));
        btnChangeGameState.setBounds(0, 0, 80, 30);
        this.add(btnChangeGameState);

        // label to display game state
        String labelText = this.getGameStateAsText();
        this.lblGameState = new JLabel(labelText);
        lblGameState.setBounds(10, 30, 80, 30);
        lblGameState.setForeground(Color.WHITE);
        this.add(lblGameState);

        // create application frame and set visible
        //
        JFrame f = new JFrame();
        f.setVisible(true);
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.add(this);
        f.setResizable(false);
        f.setSize(this.imgBackground.getWidth(null), this.imgBackground.getHeight(null));

        debug = true;
    }

    /**
     * @return textual description of current game state
     */
    private String getGameStateAsText() {
        String state = "unknown";
        switch (this.chessGame.getGameState()) {
            case ChessGame.GAME_STATE_BLACK: state = "black"; break;
            case ChessGame.GAME_STATE_END_WHITE_WON: state = "white won"; break;
            case ChessGame.GAME_STATE_END_BLACK_WON: state = "black won"; break;
            case ChessGame.GAME_STATE_WHITE: state = "white"; break;
        }
        return state;
    }

    private void addAllGuiPieces() {
        this.guiPieces = new ArrayList<GUIPiece>();
        //wrap game pieces into their graphical representation
        for (Piece piece : this.chessGame.getPieces()) {
            addGuiPiece(piece);
        }
    }

    /**
     * create and add a game piece
     */
    private void addGuiPiece(Piece piece) {
        Image img = this.getPieceImage(piece.getColor(), piece.getType());
        GUIPiece guiPiece = new GUIPiece(img, piece);
        this.guiPieces.add(guiPiece);
    }

    /**
     * repaint gui and components
     */
    public void reset() {
        System.out.println("kill current thread");
        this.chessGame.kill();

        this.chessGame = new ChessGame();
        addAllGuiPieces();

        // reset listener
        // TODO: two lines commented, don't know if should remove listener first
        this.removeMouseListener(piecesListener);
        this.removeMouseMotionListener(piecesListener);
//        PiecesListener piecesListener = new PiecesListener(guiPieces, this);
        this.piecesListener = new PiecesListener(guiPieces, this);
        this.addMouseListener(piecesListener);
        this.addMouseMotionListener(piecesListener);

        this.lastMove = null;
        this.currentMove = null;
        this.lblGameState.setForeground(Color.WHITE);
        repaint();

        this.debug = true;

        System.out.println("reset players");
//        chessGame.setPlayer(Piece.COLOR_WHITE, this);
//        chessGame.setPlayer(Piece.COLOR_BLACK, this);
        Main.setPlayer(chessGame, this);

        System.out.println("start new thread");
        new Thread(this.chessGame).start();
    }

    public Image getPieceImage(int color, int type) {
        String fileName = color == Piece.COLOR_WHITE ? "w" : "b";

        switch (type) {
            case Piece.TYPE_ROOK: fileName += "r"; break;
            case Piece.TYPE_KNIGHT: fileName += "n"; break;
            case Piece.TYPE_BISHOP: fileName += "b"; break;
            case Piece.TYPE_QUEEN: fileName += "q"; break;
            case Piece.TYPE_KING: fileName += "k"; break;
            case Piece.TYPE_PAWN: fileName += "p"; break;
        }
        fileName += ".png";

        URL pieceImg = getClass().getResource("/img/" + fileName);
        return new ImageIcon(pieceImg).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(imgBackground, 0, 0, null);

        for (GUIPiece guiPiece : guiPieces) {
            if (!guiPiece.isCaptured()) {
                g.drawImage(guiPiece.getImage(), guiPiece.getX(), guiPiece.getY(), null);
            }
        }

        // draw last move, if user is not dragging game piece
        if (!isUserDraggingPiece() && this.lastMove != null) {
            int highlightSourceX = convertColumnToX(this.lastMove.sourceColumn);
            int highlightSourceY = convertRowToY(this.lastMove.sourceRow);
            int highlightTargetX = convertColumnToX(this.lastMove.targetColumn);
            int highlightTargetY = convertRowToY(this.lastMove.targetRow);

            g.setColor(new Color(0, 128, 255, 60));
            g.fillRoundRect(highlightSourceX - 2, highlightSourceY - 2,
                    SQUARE_WIDTH, SQUARE_HEIGHT, 10, 10);
            if (chessGame.isBlackKingInCheck() || chessGame.isWhiteKingInCheck()) {
                List<Piece> threateningPieces = chessGame.getThreateningPieces();
                for (Piece piece : threateningPieces) {
                    int highlightX = convertColumnToX(piece.getColumn());
                    int highlightY = convertRowToY(piece.getRow());
                    g.setColor(new Color(255, 0, 0, 70));
                    g.fillRoundRect(highlightX - 2, highlightY - 2,
                            SQUARE_WIDTH, SQUARE_HEIGHT, 10, 10);
                }
//                chessGame.getThreateningPieces().clear();
            } else {
                g.fillRoundRect(highlightTargetX - 2, highlightTargetY - 2,
                        SQUARE_WIDTH, SQUARE_HEIGHT, 10, 10);
            }
        }

        // draw valid target locations, if user is dragging a game piece
        if (isUserDraggingPiece()) {
            ChessRule moveValidator = chessGame.getChessRule();
            Piece sourcePiece = chessGame.getNonCapturedPieceAtLocation(dragPiece.getPiece().getRow(),
                    dragPiece.getPiece().getColumn());

            // iterate the complete board to check if target locations are valid
            for (int column = Piece.COLUMN_A; column <= Piece.COLUMN_H; column++) {
                for (int row = Piece.ROW_1; row <= Piece.ROW_8; row++) {
                    Piece targetPiece = chessGame.getNonCapturedPieceAtLocation(row, column);

                    // check if target location is valid
                    if (moveValidator.isValidPieceMovementRules(null, sourcePiece, targetPiece,
                            row, column, true, true)) {
                        int highlightX = convertColumnToX(column);
                        int highlightY = convertRowToY(row);

                        // draw the highlight
                        g.setColor(new Color(0, 204, 0, 60));
                        g.fillRoundRect(highlightX - 2, highlightY - 2,
                                SQUARE_WIDTH, SQUARE_HEIGHT, 10, 10);
                    }
                }
            }
        }

        lblGameState.setText(this.getGameStateAsText());
    }

    /**
     * @return true if user is currently dragging a game piece
     */
    private boolean isUserDraggingPiece() {
        return this.dragPiece != null;
    }

//    /**
//     * switches between the different game states
//     */
//    public void changeGameState() {
//        this.chessGame.changeGameState();
//        this.lblGameState.setText(this.getGameStateAsText());
//    }

    /**
     * @return current game state
     */
    public int getGameState() {
        return this.chessGame.getGameState();
    }

    /**
     * convert logical column into x coordinate
     * @return x coordinate for column
     */
    public static int convertColumnToX(int column) {
        return PIECES_START_X + SQUARE_WIDTH * column;
    }

    /**
     * convert logical row into y coordinate
     * @return y coordinate for row
     */
    public static int convertRowToY(int row) {
        return PIECES_START_Y + SQUARE_HEIGHT * (Piece.ROW_8 - row);
    }

    /**
     * convert x coordinate into logical column
     * @return logical column for x coordinate
     */
    public static int convertXToColumn(int x) {
        return (x - DRAG_TARGET_SQUARE_START_X)/SQUARE_WIDTH;
    }

    /**
     * convert y coordinate into logical row
     * @return logical row for y coordinate
     */
    public static int convertYToRow(int y) {
        return Piece.ROW_8 - (y - DRAG_TARGET_SQUARE_START_Y)/SQUARE_HEIGHT;
    }

    /**
     * change location of given piece, if the location is valid.
     * If the location is not valid, move the piece back to its original
     * position.
     */
    public void setNewPieceLocation(GUIPiece dragPiece, int x, int y) {
        int targetRow = ChessBoardGUI.convertYToRow(y);
        int targetColumn = ChessBoardGUI.convertXToColumn(x);

        Move move = new Move(dragPiece.getPiece().getRow(), dragPiece.getPiece().getColumn(),
                targetRow, targetColumn);
        if (this.chessGame.getChessRule().isValidMove(move, debug)) {
            move.setIsValid(true); // move.isValid = true
            this.currentMove = move;

            // if Pawn promotion, change Pawn image
            if (move.pawnPromotion) {
//                System.out.println("pawn promotion in chessBoardGUI");
                Piece piece = dragPiece.getPiece();
//                piece.setRow(move.targetRow);
//                piece.setColumn(move.targetColumn);
//                System.out.println(piece.toString());
                Image img = this.getPieceImage(piece.getColor(), piece.getType());
                dragPiece.setImage(img);
//                dragPiece.correctPiecePosition();
                // if castling, also modify related Rook's guiPiece
            } else if (move.rookCastlingMove != null) {
                Move castlingMove = move.rookCastlingMove;
                Piece piece = chessGame.getNonCapturedPieceAtLocation(
                        castlingMove.targetRow, castlingMove.targetColumn);
//                piece.setRow(castlingMove.targetRow);
//                piece.setColumn(castlingMove.targetColumn);
                for (GUIPiece guiPiece : this.guiPieces) {
                    if (guiPiece.getPiece() == piece) {
                        guiPiece.correctPiecePosition(); // need to correct in order to draw
//                        dragPiece.getPiece().setRookForCastling(null);
                        break;
                    }
                }
            }
        } else {
            dragPiece.correctPiecePosition();
        }
    }

    /**
     * @param guiPiece - set the gui piece that the user is current dragging
     */
    public void setDragPiece(GUIPiece guiPiece) {
        this.dragPiece = guiPiece;
    }

    /**
     * @return the gui piece that the user is currently dragging
     */
    public GUIPiece getDragPiece() {
        return this.dragPiece;
    }

    @Override
    public Move getMove() {
        this.draggingGamePiecesEnabled = true;
        Move moveForExecution = this.currentMove;
        this.currentMove = null;
        return moveForExecution;
    }

    @Override
    public void moveSuccessfullyExecuted(Move move) {
        if (move != null) {
            System.out.println(Thread.currentThread() + " 4.ChessBoardGUI.moveSuccessfullyExecuted(): " +
                    "repaint with move " + move.toString());
            // adjust GUI piece
            GUIPiece guiPiece = this.getGuiPieceAt(move.targetRow, move.targetColumn);
            if (guiPiece == null) {
                throw new IllegalStateException("no guiPiece at " + move.targetRow + "/" + move.targetColumn);
            }
            guiPiece.correctPiecePosition();

            // remember last move
            this.lastMove = move;
            this.chessGame.addToHistoryMove(lastMove);
//        chessGame.printHistoryMoves();

            // disable dragging until asked by ChessGame for the next move
            this.draggingGamePiecesEnabled = false;

//            System.out.println(Thread.currentThread() +
//                    " 4-1.ChessBoardGUI.moveSuccessfullyExecuted(): calling ===repaint()===");
//            this.repaint();
        }
//        else {
//            if (chessGame.getThreateningPieces() != null) {
//                System.out.println(Thread.currentThread() +
//                        " 7.ChessBoardGUI.moveSuccessfullyExecuted(): threatening pieces "
//                        + chessGame.getThreateningPieces().size() + " with move = null");
//            } else System.out.println(Thread.currentThread() +
//                    " 7.ChessBoardGUI.moveSuccessfullyExecuted(): threatening pieces null"
//                    + " with move = null");
//
//            System.out.println(Thread.currentThread() +
//                    " 8.ChessBoardGUI.moveSuccessfullyExecuted(): calling ===repaint()=== with move = null");
//            this.repaint();
//        }
        // repaint the new state
        //TODO
//        System.out.println(Thread.currentThread() +
//                " 8-1.ChessBoardGUI.moveSuccessfullyExecuted(): calling repaint()");
        this.repaint();
    }

    public boolean isDraggingGamePiecesEnabled() {
        return draggingGamePiecesEnabled;
    }

    /**
     * get non-captured the gui piece at the specified position
     * @return the gui piece at the specified position, null if there is no piece
     */
    public GUIPiece getGuiPieceAt(int row, int column) {
        for (GUIPiece guiPiece : this.guiPieces) {
            if (guiPiece.getPiece().getRow() == row &&
                    guiPiece.getPiece().getColumn() == column &&
                    !guiPiece.isCaptured()) {
                return guiPiece;
            }
        }
        return null;
    }

    public List<GUIPiece> getGuiPieces() {
        synchronized (lock) {
            return guiPieces;
        }
    }

    public static void main(String[] args) {
        ChessGame chessGame = new ChessGame();
        ChessBoardGUI chessGui = new ChessBoardGUI(chessGame);
        chessGame.setPlayer(Piece.COLOR_WHITE, chessGui);
        chessGame.setPlayer(Piece.COLOR_BLACK, chessGui);
        new Thread(chessGame).start();
    }
}
