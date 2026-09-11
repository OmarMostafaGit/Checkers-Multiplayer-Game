import java.io.Serializable;

public class Piece implements Serializable {
    static final long serialVersionUID = 42L;

    String color;
    int row;
    int col;
    boolean king;

    public Piece(String color, int row, int col) {
        this.color = color;
        this.row = row;
        this.col = col;
        king = false;
    }

    public String getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isKing() {
        return king;
    }

    public void moveTo(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void promoteToKing() {
        king = true;
    }

    public Piece copy() {
        Piece piece = new Piece(color, row, col);
        if(king) {
            piece.promoteToKing();
        }
        return piece;
    }

    public String toCell() {
        if(color.equals("black")) {
            return king ? "B" : "b";
        }
        return king ? "R" : "r";
    }
}
