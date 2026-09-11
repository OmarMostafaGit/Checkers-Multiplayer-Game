import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;

public class Client extends Thread {

    Socket socketClient;
    String username;
    ObjectOutputStream out;
    ObjectInputStream in;
    private Consumer<Serializable> callback;

    Client(Consumer<Serializable> call) {
        callback = call;
        username = "";
    }

    public void run() {
        try {
            socketClient = new Socket("127.0.0.1", 5555);
            out = new ObjectOutputStream(socketClient.getOutputStream());
            in = new ObjectInputStream(socketClient.getInputStream());
            socketClient.setTcpNoDelay(true);
        }
        catch(Exception e) {
            callback.accept("Could not connect to server.");
            return;
        }
        while(true) {
            try {
                Message message = (Message) in.readObject();
                callback.accept(message);
            }
            catch(Exception e) {
                callback.accept("Disconnected from server.");
                break;
            }
        }
    }

    public void sendMessage(Message message) {
        try {
            while(out == null) {}
            out.writeObject(message);
            out.flush();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    public void createAccount(String name, String password) {
        Message message = new Message("createAccount", "", "server", name);
        message.setPassword(password);
        sendMessage(message);
    }

    public void loginAccount(String name, String password) {
        Message message = new Message("login", "", "server", name);
        message.setPassword(password);
        sendMessage(message);
    }

    public void requestGame() {
        sendMessage(new Message("findGame", username, "server", "findGame"));
    }

    public void requestComputerGame() {
        sendMessage(new Message("findComputerGame", username, "server", "findComputerGame"));
    }

    public void cancelWaiting() {
        sendMessage(new Message("cancelWaiting", username, "server", "cancelWaiting"));
    }

    public void sendChat(String gameId, String data) {
        Message message = new Message("chat", username, "game", data);
        message.setGameId(gameId);
        sendMessage(message);
    }

    public void sendMove(String gameId, int fromRow, int fromCol, int toRow, int toCol) {
        Message message = new Message("move", username, "game", "move");
        message.setGameId(gameId);
        message.setFromRow(fromRow);
        message.setFromCol(fromCol);
        message.setToRow(toRow);
        message.setToCol(toCol);
        sendMessage(message);
    }

    public void sendReplay(String gameId) {
        Message message = new Message("playAgain", username, "game", "playAgain");
        message.setGameId(gameId);
        sendMessage(message);
    }

    public void quitGame(String gameId) {
        Message message = new Message("quitGame", username, "game", "quitGame");
        message.setGameId(gameId);
        sendMessage(message);
    }

    public void requestStats() {
        sendMessage(new Message("getStats", username, "server", "getStats"));
    }

    public void leaveFinishedGame(String gameId) {
        Message message = new Message("leaveFinishedGame", username, "game", "leaveFinishedGame");
        message.setGameId(gameId);
        sendMessage(message);
    }

    public void requestFriends() {
        sendMessage(new Message("getFriends", username, "server", "getFriends"));
    }

    public void addFriend(String friendName) {
        Message message = new Message("addFriend", username, "server", "addFriend");
        message.setFriendName(friendName);
        sendMessage(message);
    }

    public void inviteFriend(String friendName) {
        Message message = new Message("inviteFriend", username, "server", "inviteFriend");
        message.setFriendName(friendName);
        sendMessage(message);
    }

    public void respondInvite(String friendName, boolean accepted) {
        Message message = new Message(accepted ? "acceptInvite" : "declineInvite", username, "server", "inviteResponse");
        message.setFriendName(friendName);
        sendMessage(message);
    }

    public void requestHistory() {
        sendMessage(new Message("getHistory", username, "server", "getHistory"));
    }

    public void logout() {
        sendMessage(new Message("logout", username, "server", "logout"));
    }

    public void setUserName(String name) {
        username = name;
    }
}
