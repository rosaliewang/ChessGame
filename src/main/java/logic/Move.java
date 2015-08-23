package logic;

/**
 * Created by Yuchen Wang on 8/15/15.
 */
public class Move {
    public int sourceRow;
    public int sourceColumn;
    public int targetRow;
    public int targetColumn;
    private boolean isValid;

    public Move(int sourceRow, int sourceColumn, int targetRow, int targetColumn) {
        this.sourceRow = sourceRow;
        this.sourceColumn = sourceColumn;
        this.targetRow = targetRow;
        this.targetColumn = targetColumn;
        this.isValid = false;
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
}
