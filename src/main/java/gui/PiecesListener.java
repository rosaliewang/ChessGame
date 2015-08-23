package gui;

import logic.ChessGame;
import logic.Piece;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

/**
 * Created by Yuchen Wang on 8/13/15.
 */
public class PiecesListener implements MouseListener, MouseMotionListener {
    private List<GUIPiece> guiPieces;
    private ChessBoardGUI chessBoardGUI;

//    private GUIPiece pieceToMove;
    private int dragOffsetX;
    private int dragOffsetY;

    public PiecesListener(List<GUIPiece> guiPieces, ChessBoardGUI chessBoardGUI) {
        this.guiPieces = guiPieces;
        this.chessBoardGUI = chessBoardGUI;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!this.chessBoardGUI.isDraggingGamePiecesEnabled()) {
            return;
        }

        int x = e.getPoint().x;
        int y = e.getPoint().y;

        // find out which piece to move.
        // we check the list from top to button
        // (therefore we iterate in reverse order)
        //
        for (int i = this.guiPieces.size() - 1; i >= 0; i--) {
            GUIPiece guiPiece = this.guiPieces.get(i);
            if (guiPiece.isCaptured()) continue;

            if (mouseOverPiece(guiPiece, x, y)) {
                if ((this.chessBoardGUI.getGameState() == ChessGame.GAME_STATE_WHITE
                        && guiPiece.getColor() == Piece.COLOR_WHITE) ||
                        (this.chessBoardGUI.getGameState() == ChessGame.GAME_STATE_BLACK
                                && guiPiece.getColor() == Piece.COLOR_BLACK)) {
                    // calculate offset, because we do not want the drag piece
                    // to jump with it's upper left corner to the current mouse
                    // position
                    //
                    this.dragOffsetX = x - guiPiece.getX();
                    this.dragOffsetY = y - guiPiece.getY();
                    this.chessBoardGUI.setDragPiece(guiPiece);
                    this.chessBoardGUI.repaint();
                    break;
                }
            }
        }

        // move drag piece to the top of the list
        if(this.chessBoardGUI.getDragPiece() != null){
            this.guiPieces.remove( this.chessBoardGUI.getDragPiece() );
            this.guiPieces.add(this.chessBoardGUI.getDragPiece());
        }
    }

    /**
     * check if mouse is currently over one piece
     * @param piece piece to check
     * @param x     x coordinate of mouse
     * @param y     y coordinate of mouse
     * @return      true if mouse is over this piece
     */
    private boolean mouseOverPiece(GUIPiece piece, int x, int y) {
        return piece.getX() <= x && piece.getX() + piece.getWidth() >= x &&
                piece.getY() <= y && piece.getY() + piece.getHeight() >= y;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if( this.chessBoardGUI.getDragPiece() != null){
            int x = e.getPoint().x - this.dragOffsetX;
            int y = e.getPoint().y - this.dragOffsetY;

            // set game piece to the new location if possible
            //
            chessBoardGUI.setNewPieceLocation(this.chessBoardGUI.getDragPiece(), x, y);
            this.chessBoardGUI.repaint();
            this.chessBoardGUI.setDragPiece(null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(this.chessBoardGUI.getDragPiece() != null){

            int x = e.getPoint().x - this.dragOffsetX;
            int y = e.getPoint().y - this.dragOffsetY;

//            System.out.println(
//                    "row:"+chessBoardGUI.convertYToRow(y)
//                            +" column:"+chessBoardGUI.convertXToColumn(x));

            GUIPiece dragPiece = this.chessBoardGUI.getDragPiece();
            dragPiece.setX(x);
            dragPiece.setY(y);

            this.chessBoardGUI.repaint();
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {}
}
