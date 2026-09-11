import java.io.Serializable;
import java.util.ArrayList;

public class GameState implements Serializable {
    static final long serialVersionUID = 42L;

    Board board;
    String currentPlayer;
    String winner;
    boolean gameOver;
    boolean draw;
    int forcedRow;
    int forcedCol;

    public GameState() {
        board = new Board();
        currentPlayer = "black";
        winner = "";
        gameOver = false;
        draw = false;
        forcedRow = -1;
        forcedCol = -1;
    }

    public GameState(GameState other) {
        board = new Board();
        board.grid = new Piece[8][8];
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = other.board.getPiece(row, col);
                if(piece != null) {
                    board.grid[row][col] = piece.copy();
                }
            }
        }
        currentPlayer = other.currentPlayer;
        winner = other.winner;
        gameOver = other.gameOver;
        draw = other.draw;
        forcedRow = other.forcedRow;
        forcedCol = other.forcedCol;
    }

    public GameState copy() {
        return new GameState(this);
    }

    public void resetGame() {
        board.initializeBoard();
        currentPlayer = "black";
        winner = "";
        gameOver = false;
        draw = false;
        forcedRow = -1;
        forcedCol = -1;
    }

    public Board getBoard() { return board; }
    public String getCurrentPlayer() { return currentPlayer; }
    public String getWinner() { return winner; }
    public boolean isGameOver() { return gameOver; }
    public boolean isDraw() { return draw; }
    public int getForcedRow() { return forcedRow; }
    public int getForcedCol() { return forcedCol; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
    public void setForced(int forcedRow, int forcedCol) { this.forcedRow = forcedRow; this.forcedCol = forcedCol; }

    public void loadBoardData(ArrayList<String> rows) {
        board.grid = new Piece[8][8];
        for(int row = 0; row < 8 && row < rows.size(); row++) {
            String line = rows.get(row);
            for(int col = 0; col < 8 && col < line.length(); col++) {
                char ch = line.charAt(col);
                if(ch == 'b' || ch == 'B') {
                    board.grid[row][col] = new Piece("black", row, col);
                    if(ch == 'B') {
                        board.grid[row][col].promoteToKing();
                    }
                }
                else if(ch == 'r' || ch == 'R') {
                    board.grid[row][col] = new Piece("red", row, col);
                    if(ch == 'R') {
                        board.grid[row][col].promoteToKing();
                    }
                }
            }
        }
    }

    public String makeMove(String playerColor, Move move) {
        if(gameOver) {
            return "Game is already over.";
        }
        if(!playerColor.equals(currentPlayer)) {
            return "It is not your turn.";
        }
        if(!board.isInside(move.getFromRow(), move.getFromCol()) || !board.isInside(move.getToRow(), move.getToCol())) {
            return "That move is out of bounds.";
        }
        Piece piece = board.getPiece(move.getFromRow(), move.getFromCol());
        if(piece == null) {
            return "There is no piece there.";
        }
        if(!piece.getColor().equals(playerColor)) {
            return "You must move your own piece.";
        }
        if(forcedRow != -1 && (piece.getRow() != forcedRow || piece.getCol() != forcedCol)) {
            return "You must continue the jump with the same piece.";
        }
        if(board.getPiece(move.getToRow(), move.getToCol()) != null) {
            return "That square is already occupied.";
        }
        if((move.getToRow() + move.getToCol()) % 2 == 0) {
            return "You can only move on dark squares.";
        }
        int rowDiff = move.getToRow() - move.getFromRow();
        int colDiff = move.getToCol() - move.getFromCol();
        if(Math.abs(rowDiff) != Math.abs(colDiff)) {
            return "Pieces move diagonally.";
        }
        if(Math.abs(rowDiff) != 1 && Math.abs(rowDiff) != 2) {
            return "That move is too far.";
        }
        if(!piece.isKing()) {
            if(piece.getColor().equals("black") && rowDiff <= 0) {
                return "Black pieces move down unless they are kings.";
            }
            if(piece.getColor().equals("red") && rowDiff >= 0) {
                return "Red pieces move up unless they are kings.";
            }
        }
        boolean mustJump = playerHasJump(playerColor);
        if(mustJump && Math.abs(rowDiff) != 2) {
            return "You must take a jump when one is available.";
        }
        if(Math.abs(rowDiff) == 2) {
            int middleRow = (move.getFromRow() + move.getToRow()) / 2;
            int middleCol = (move.getFromCol() + move.getToCol()) / 2;
            Piece middlePiece = board.getPiece(middleRow, middleCol);
            if(middlePiece == null || middlePiece.getColor().equals(piece.getColor())) {
                return "There is no opponent piece to capture.";
            }
            board.removePiece(middleRow, middleCol);
        }
        board.removePiece(move.getFromRow(), move.getFromCol());
        board.setPiece(move.getToRow(), move.getToCol(), piece);
        if(piece.getColor().equals("black") && move.getToRow() == 7) {
            piece.promoteToKing();
        }
        if(piece.getColor().equals("red") && move.getToRow() == 0) {
            piece.promoteToKing();
        }
        if(Math.abs(rowDiff) == 2 && pieceHasJump(piece)) {
            forcedRow = piece.getRow();
            forcedCol = piece.getCol();
        }
        else {
            forcedRow = -1;
            forcedCol = -1;
            switchTurn();
        }
        checkGameOver();
        return "ok";
    }

    public void switchTurn() {
        if(currentPlayer.equals("black")) {
            currentPlayer = "red";
        }
        else {
            currentPlayer = "black";
        }
    }

    public boolean playerHasJump(String color) {
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if(piece != null && piece.getColor().equals(color) && pieceHasJump(piece)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean pieceHasJump(Piece piece) {
        int[] rowSteps;
        if(piece.isKing()) {
            rowSteps = new int[]{-2, 2};
        }
        else if(piece.getColor().equals("black")) {
            rowSteps = new int[]{2};
        }
        else {
            rowSteps = new int[]{-2};
        }
        int[] colSteps = new int[]{-2, 2};
        for(int i = 0; i < rowSteps.length; i++) {
            for(int j = 0; j < colSteps.length; j++) {
                int newRow = piece.getRow() + rowSteps[i];
                int newCol = piece.getCol() + colSteps[j];
                int middleRow = piece.getRow() + (rowSteps[i] / 2);
                int middleCol = piece.getCol() + (colSteps[j] / 2);
                if(board.isInside(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                    Piece middle = board.getPiece(middleRow, middleCol);
                    if(middle != null && !middle.getColor().equals(piece.getColor())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean playerHasMove(String color) {
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if(piece != null && piece.getColor().equals(color)) {
                    if(pieceHasJump(piece) || pieceHasSimpleMove(piece)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean pieceHasSimpleMove(Piece piece) {
        int[] rowSteps;
        if(piece.isKing()) {
            rowSteps = new int[]{-1, 1};
        }
        else if(piece.getColor().equals("black")) {
            rowSteps = new int[]{1};
        }
        else {
            rowSteps = new int[]{-1};
        }
        int[] colSteps = new int[]{-1, 1};
        for(int i = 0; i < rowSteps.length; i++) {
            for(int j = 0; j < colSteps.length; j++) {
                int newRow = piece.getRow() + rowSteps[i];
                int newCol = piece.getCol() + colSteps[j];
                if(board.isInside(newRow, newCol) && board.getPiece(newRow, newCol) == null) {
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Move> getValidMoves(String color) {
        ArrayList<Move> moves = new ArrayList<Move>();
        boolean mustJump = playerHasJump(color);
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if(piece != null && piece.getColor().equals(color)) {
                    if(forcedRow != -1 && (row != forcedRow || col != forcedCol)) {
                        continue;
                    }
                    addMovesForPiece(moves, piece, mustJump);
                }
            }
        }
        return moves;
    }

    public ArrayList<Move> getValidMovesForPiece(int row, int col, String color) {
        ArrayList<Move> moves = new ArrayList<Move>();
        Piece piece = board.getPiece(row, col);
        if(piece == null || !piece.getColor().equals(color)) {
            return moves;
        }
        if(forcedRow != -1 && (row != forcedRow || col != forcedCol)) {
            return moves;
        }
        addMovesForPiece(moves, piece, playerHasJump(color));
        return moves;
    }

    public void addMovesForPiece(ArrayList<Move> moves, Piece piece, boolean mustJump) {
        int[] rowSteps;
        if(piece.isKing()) {
            rowSteps = mustJump ? new int[]{-2, 2} : new int[]{-2, -1, 1, 2};
        }
        else if(piece.getColor().equals("black")) {
            rowSteps = mustJump ? new int[]{2} : new int[]{1, 2};
        }
        else {
            rowSteps = mustJump ? new int[]{-2} : new int[]{-2, -1};
        }
        int[] colSteps = mustJump ? new int[]{-2, 2} : new int[]{-2, -1, 1, 2};
        for(int i = 0; i < rowSteps.length; i++) {
            for(int j = 0; j < colSteps.length; j++) {
                if(Math.abs(rowSteps[i]) != Math.abs(colSteps[j])) {
                    continue;
                }
                Move move = new Move(piece.getRow(), piece.getCol(), piece.getRow() + rowSteps[i], piece.getCol() + colSteps[j]);
                GameState copy = copy();
                if(copy.makeMove(piece.getColor(), move).equals("ok")) {
                    moves.add(move);
                }
            }
        }
    }

    public int evaluateBoard(String color) {
        int blackScore = 0;
        int redScore = 0;
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if(piece != null) {
                    int value = piece.isKing() ? 5 : 3;
                    if(piece.getColor().equals("black")) {
                        blackScore += value;
                    }
                    else {
                        redScore += value;
                    }
                }
            }
        }
        int mobility = getValidMoves("black").size() - getValidMoves("red").size();
        int score = (blackScore - redScore) + mobility;
        if(color.equals("black")) {
            return score;
        }
        return -score;
    }

    public void checkGameOver() {
        int blackCount = 0;
        int redCount = 0;
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if(piece != null) {
                    if(piece.getColor().equals("black")) {
                        blackCount++;
                    }
                    else {
                        redCount++;
                    }
                }
            }
        }
        if(blackCount == 0) {
            winner = "red";
            gameOver = true;
            draw = false;
            return;
        }
        if(redCount == 0) {
            winner = "black";
            gameOver = true;
            draw = false;
            return;
        }
        boolean blackMoves = playerHasMove("black");
        boolean redMoves = playerHasMove("red");
        if(!blackMoves && !redMoves) {
            winner = "";
            gameOver = true;
            draw = true;
            return;
        }
        if(!blackMoves) {
            winner = "red";
            gameOver = true;
            draw = false;
            return;
        }
        if(!redMoves) {
            winner = "black";
            gameOver = true;
            draw = false;
        }
    }
}
