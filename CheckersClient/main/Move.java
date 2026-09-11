import java.io.Serializable;

public class Move implements Serializable {
    static final long serialVersionUID = 42L;

    int fromRow;
    int fromCol;
    int toRow;
    int toCol;
    boolean jump;

    public Move(int fromRow, int fromCol, int toRow, int toCol) {
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        jump = Math.abs(fromRow - toRow) == 2;
    }

    public int getFromRow() {
        return fromRow;
    }

    public int getFromCol() {
        return fromCol;
    }

    public int getToRow() {
        return toRow;
    }

    public int getToCol() {
        return toCol;
    }

    public boolean isJump() {
        return jump;
    }
}
