import java.io.Serializable;

public class Message implements Serializable {
    static final long serialVersionUID = 42L;

    String type;
    String sender;
    String target;
    String content;
    String gameId;
    String currentPlayer;
    String winner;
    String playerColor;
    String password;
    String friendName;
    boolean gameOver;
    boolean draw;
    boolean valid;
    boolean computerGame;
    boolean mustJump;
    int fromRow;
    int fromCol;
    int toRow;
    int toCol;
    int forcedRow;
    int forcedCol;
    int lastFromRow;
    int lastFromCol;
    int lastToRow;
    int lastToCol;
    java.util.ArrayList<String> users;
    java.util.ArrayList<String> boardData;
    java.util.ArrayList<String> friends;
    java.util.ArrayList<String> history;
    int wins;
    int losses;
    int draws;

    public Message() {
        type = "";
        sender = "";
        target = "";
        content = "";
        gameId = "";
        currentPlayer = "";
        winner = "";
        playerColor = "";
        password = "";
        friendName = "";
        users = new java.util.ArrayList<String>();
        boardData = new java.util.ArrayList<String>();
        friends = new java.util.ArrayList<String>();
        history = new java.util.ArrayList<String>();
        valid = true;
        computerGame = false;
        mustJump = false;
        forcedRow = -1;
        forcedCol = -1;
        lastFromRow = -1;
        lastFromCol = -1;
        lastToRow = -1;
        lastToCol = -1;
    }

    public Message(String type, String sender, String target, String content) {
        this();
        this.type = type;
        this.sender = sender;
        this.target = target;
        this.content = content;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(String currentPlayer) { this.currentPlayer = currentPlayer; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public String getPlayerColor() { return playerColor; }
    public void setPlayerColor(String playerColor) { this.playerColor = playerColor; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFriendName() { return friendName; }
    public void setFriendName(String friendName) { this.friendName = friendName; }
    public boolean isGameOver() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver = gameOver; }
    public boolean isDraw() { return draw; }
    public void setDraw(boolean draw) { this.draw = draw; }
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }
    public boolean isComputerGame() { return computerGame; }
    public void setComputerGame(boolean computerGame) { this.computerGame = computerGame; }
    public boolean isMustJump() { return mustJump; }
    public void setMustJump(boolean mustJump) { this.mustJump = mustJump; }
    public int getFromRow() { return fromRow; }
    public void setFromRow(int fromRow) { this.fromRow = fromRow; }
    public int getFromCol() { return fromCol; }
    public void setFromCol(int fromCol) { this.fromCol = fromCol; }
    public int getToRow() { return toRow; }
    public void setToRow(int toRow) { this.toRow = toRow; }
    public int getToCol() { return toCol; }
    public void setToCol(int toCol) { this.toCol = toCol; }
    public int getForcedRow() { return forcedRow; }
    public void setForcedRow(int forcedRow) { this.forcedRow = forcedRow; }
    public int getForcedCol() { return forcedCol; }
    public void setForcedCol(int forcedCol) { this.forcedCol = forcedCol; }
    public int getLastFromRow() { return lastFromRow; }
    public void setLastFromRow(int lastFromRow) { this.lastFromRow = lastFromRow; }
    public int getLastFromCol() { return lastFromCol; }
    public void setLastFromCol(int lastFromCol) { this.lastFromCol = lastFromCol; }
    public int getLastToRow() { return lastToRow; }
    public void setLastToRow(int lastToRow) { this.lastToRow = lastToRow; }
    public int getLastToCol() { return lastToCol; }
    public void setLastToCol(int lastToCol) { this.lastToCol = lastToCol; }
    public java.util.ArrayList<String> getUsers() { return users; }
    public void setUsers(java.util.ArrayList<String> users) { this.users = users; }
    public java.util.ArrayList<String> getBoardData() { return boardData; }
    public void setBoardData(java.util.ArrayList<String> boardData) { this.boardData = boardData; }
    public java.util.ArrayList<String> getFriends() { return friends; }
    public void setFriends(java.util.ArrayList<String> friends) { this.friends = friends; }
    public java.util.ArrayList<String> getHistory() { return history; }
    public void setHistory(java.util.ArrayList<String> history) { this.history = history; }
    public int getWins() { return wins; }
    public void setWins(int wins) { this.wins = wins; }
    public int getLosses() { return losses; }
    public void setLosses(int losses) { this.losses = losses; }
    public int getDraws() { return draws; }
    public void setDraws(int draws) { this.draws = draws; }

    public String toString() {
        if(type.equals("chat")) {
            return sender + ": " + content;
        }
        if(type.equals("gameOver")) {
            return content;
        }
        if(type.equals("error") || type.equals("server") || type.equals("info") || type.equals("authRejected") || type.equals("authAccepted")) {
            return content;
        }
        if(type.equals("stats")) {
            return sender + " stats - Wins: " + wins + " Losses: " + losses + " Draws: " + draws;
        }
        return type + " " + content;
    }
}
