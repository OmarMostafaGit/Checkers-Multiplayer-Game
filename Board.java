import java.io.Serializable;

public class Board implements Serializable {
    static final long serialVersionUID = 42L;

    Piece[][] grid;

    public Board() {
        grid = new Piece[8][8];
        initializeBoard();
    }

    public void initializeBoard() {
        grid = new Piece[8][8];
        for(int row = 0; row < 3; row++) {
            for(int col = 0; col < 8; col++) {
                if((row + col) % 2 == 1) {
                    grid[row][col] = new Piece("black", row, col);
                }
            }
        }
        for(int row = 5; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                if((row + col) % 2 == 1) {
                    grid[row][col] = new Piece("red", row, col);
                }
            }
        }
    }

    public Piece getPiece(int row, int col) {
        if(!isInside(row, col)) {
            return null;
        }
        return grid[row][col];
    }

    public void setPiece(int row, int col, Piece piece) {
        if(isInside(row, col)) {
            grid[row][col] = piece;
            if(piece != null) {
                piece.moveTo(row, col);
            }
        }
    }

    public void removePiece(int row, int col) {
        if(isInside(row, col)) {
            grid[row][col] = null;
        }
    }

    public boolean isInside(int row, int col) {
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    public java.util.ArrayList<String> toRows() {
        java.util.ArrayList<String> rows = new java.util.ArrayList<String>();
        for(int row = 0; row < 8; row++) {
            String line = "";
            for(int col = 0; col < 8; col++) {
                if(grid[row][col] == null) {
                    line += ".";
                }
                else {
                    line += grid[row][col].toCell();
                }
            }
            rows.add(line);
        }
        return rows;
    }
}
