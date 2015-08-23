package gui;

import logic.Piece;

import java.awt.*;

/**
 * Created by Yuchen Wang on 8/13/15.
 */
public class GUIPiece {
    private Image image;
    private int x;
    private int y;
    private Piece piece;

    public GUIPiece(Image image, Piece piece) {
        this.image = image;
        this.piece = piece;

        correctPiecePosition();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Image getImage() {
        return image;
    }

    public int getWidth() {
        return image.getHeight(null);
    }

    public int getHeight() {
        return image.getHeight(null);
    }

    public int getColor() {
        return piece.getColor();
    }

    public String getColorString() {
        return piece.getColorString();
    }

    public Piece getPiece() {
        return piece;
    }

    public boolean isCaptured() {
        return piece.isCaptured();
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getTypeString() {
        return Piece.getTypeString(piece.getType());
    }

    @Override
    public String toString() {
        return this.piece + " " + x + "/" + y;
    }

    public void correctPiecePosition() {
        this.x = ChessBoardGUI.convertColumnToX(piece.getColumn());
        this.y = ChessBoardGUI.convertRowToY(piece.getRow());
    }
}
