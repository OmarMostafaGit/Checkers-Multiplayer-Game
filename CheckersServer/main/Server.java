import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

public class Server {

    int count = 1;
    int nextGameNumber = 1;
    ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
    HashMap<String, ClientThread> userNames = new HashMap<String, ClientThread>();
    ArrayList<ClientThread> waitingPlayers = new ArrayList<ClientThread>();
    ArrayList<GameSession> activeGames = new ArrayList<GameSession>();
    HashMap<String, int[]> stats = new HashMap<String, int[]>();
    HashMap<String, String> accounts = new HashMap<String, String>();
    HashMap<String, HashSet<String>> friends = new HashMap<String, HashSet<String>>();
    HashMap<String, ArrayList<String>> history = new HashMap<String, ArrayList<String>>();
    File statsFile;
    File accountsFile;
    File friendsFile;
    File historyFile;
    TheServer server;
    private Consumer<Serializable> callback;

    Server(Consumer<Serializable> call) {
        callback = call;
        statsFile = new File("checkers_stats.txt");
        accountsFile = new File("checkers_accounts.txt");
        friendsFile = new File("checkers_friends.txt");
        historyFile = new File("checkers_history.txt");
        loadStats();
        loadAccounts();
        loadFriends();
        loadHistory();
        server = new TheServer();
        server.start();
    }

    public synchronized void loadStats() {
        stats.clear();
        try {
            if(!statsFile.exists()) {
                statsFile.createNewFile();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(statsFile));
            String line = reader.readLine();
            while(line != null) {
                String[] pieces = line.split(",");
                if(pieces.length == 4) {
                    int[] values = new int[3];
                    values[0] = Integer.parseInt(pieces[1]);
                    values[1] = Integer.parseInt(pieces[2]);
                    values[2] = Integer.parseInt(pieces[3]);
                    stats.put(pieces[0], values);
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch(Exception e) {}
    }

    public synchronized void saveStats() {
        try {
            FileWriter writer = new FileWriter(statsFile, false);
            for(String name : stats.keySet()) {
                int[] values = stats.get(name);
                writer.write(name + "," + values[0] + "," + values[1] + "," + values[2] + "\n");
            }
            writer.close();
        }
        catch(Exception e) {}
    }

    public synchronized void loadAccounts() {
        accounts.clear();
        try {
            if(!accountsFile.exists()) {
                accountsFile.createNewFile();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(accountsFile));
            String line = reader.readLine();
            while(line != null) {
                String[] pieces = line.split(",", 2);
                if(pieces.length == 2) {
                    accounts.put(pieces[0], pieces[1]);
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch(Exception e) {}
    }

    public synchronized void saveAccounts() {
        try {
            FileWriter writer = new FileWriter(accountsFile, false);
            for(String name : accounts.keySet()) {
                writer.write(name + "," + accounts.get(name) + "\n");
            }
            writer.close();
        }
        catch(Exception e) {}
    }

    public synchronized void loadFriends() {
        friends.clear();
        try {
            if(!friendsFile.exists()) {
                friendsFile.createNewFile();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(friendsFile));
            String line = reader.readLine();
            while(line != null) {
                String[] pieces = line.split(",", 2);
                HashSet<String> set = new HashSet<String>();
                if(pieces.length == 2 && !pieces[1].trim().equals("")) {
                    String[] names = pieces[1].split(";");
                    for(int i = 0; i < names.length; i++) {
                        if(!names[i].trim().equals("")) {
                            set.add(names[i].trim());
                        }
                    }
                }
                if(pieces.length >= 1 && !pieces[0].trim().equals("")) {
                    friends.put(pieces[0].trim(), set);
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch(Exception e) {}
    }

    public synchronized void saveFriends() {
        try {
            FileWriter writer = new FileWriter(friendsFile, false);
            ArrayList<String> names = new ArrayList<String>(friends.keySet());
            Collections.sort(names);
            for(int i = 0; i < names.size(); i++) {
                ArrayList<String> friendList = new ArrayList<String>(friends.get(names.get(i)));
                Collections.sort(friendList);
                String line = names.get(i) + ",";
                for(int j = 0; j < friendList.size(); j++) {
                    line += friendList.get(j);
                    if(j < friendList.size() - 1) {
                        line += ";";
                    }
                }
                writer.write(line + "\n");
            }
            writer.close();
        }
        catch(Exception e) {}
    }


    public synchronized void loadHistory() {
        history.clear();
        try {
            if(!historyFile.exists()) {
                historyFile.createNewFile();
                return;
            }
            BufferedReader reader = new BufferedReader(new FileReader(historyFile));
            String line = reader.readLine();
            while(line != null) {
                String[] pieces = line.split(",", 2);
                ArrayList<String> list = new ArrayList<String>();
                if(pieces.length == 2 && !pieces[1].trim().equals("")) {
                    String[] values = pieces[1].split(";");
                    for(int i = 0; i < values.length; i++) {
                        if(!values[i].trim().equals("")) {
                            list.add(values[i].replace("<comma>", ",").trim());
                        }
                    }
                }
                if(pieces.length >= 1 && !pieces[0].trim().equals("")) {
                    history.put(pieces[0].trim(), list);
                }
                line = reader.readLine();
            }
            reader.close();
        }
        catch(Exception e) {}
    }

    public synchronized void saveHistory() {
        try {
            FileWriter writer = new FileWriter(historyFile, false);
            ArrayList<String> names = new ArrayList<String>(history.keySet());
            Collections.sort(names);
            for(int i = 0; i < names.size(); i++) {
                ArrayList<String> records = new ArrayList<String>(history.get(names.get(i)));
                String line = names.get(i) + ",";
                for(int j = 0; j < records.size(); j++) {
                    line += records.get(j).replace(",", "<comma>");
                    if(j < records.size() - 1) {
                        line += ";";
                    }
                }
                writer.write(line + "\n");
            }
            writer.close();
        }
        catch(Exception e) {}
    }

    public synchronized ArrayList<String> getHistoryFor(String userName) {
        if(!history.containsKey(userName)) {
            history.put(userName, new ArrayList<String>());
        }
        return history.get(userName);
    }

    public synchronized void addHistory(String userName, String entry) {
        ArrayList<String> list = getHistoryFor(userName);
        list.add(0, timeStamp() + " - " + entry);
        while(list.size() > 25) {
            list.remove(list.size() - 1);
        }
        saveHistory();
    }

    public String timeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM/dd HH:mm"));
    }

    public synchronized int[] getStatsFor(String userName) {
        if(!stats.containsKey(userName)) {
            stats.put(userName, new int[]{0, 0, 0});
        }
        return stats.get(userName);
    }

    public synchronized HashSet<String> getFriendSet(String userName) {
        if(!friends.containsKey(userName)) {
            friends.put(userName, new HashSet<String>());
        }
        return friends.get(userName);
    }

    public synchronized void addWin(String userName) {
        int[] values = getStatsFor(userName);
        values[0]++;
        saveStats();
    }

    public synchronized void addLoss(String userName) {
        int[] values = getStatsFor(userName);
        values[1]++;
        saveStats();
    }

    public synchronized void addDraw(String userName) {
        int[] values = getStatsFor(userName);
        values[2]++;
        saveStats();
    }

    public synchronized void sendStatsTo(ClientThread client) {
        int[] values = getStatsFor(client.userName);
        Message message = new Message("stats", client.userName, client.userName, "stats");
        message.setWins(values[0]);
        message.setLosses(values[1]);
        message.setDraws(values[2]);
        client.updateOneClient(message);
    }

    public synchronized void sendFriendsTo(ClientThread client) {
        Message message = new Message("friends", "server", client.userName, "friends");
        ArrayList<String> entries = new ArrayList<String>();
        ArrayList<String> names = new ArrayList<String>(getFriendSet(client.userName));
        Collections.sort(names);
        for(int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if(userNames.containsKey(name)) {
                entries.add(name + " (online)");
            }
            else {
                entries.add(name + " (offline)");
            }
        }
        message.setFriends(entries);
        client.updateOneClient(message);
    }

    public synchronized void sendHistoryTo(ClientThread client) {
        Message message = new Message("history", "server", client.userName, "history");
        message.setHistory(new ArrayList<String>(getHistoryFor(client.userName)));
        client.updateOneClient(message);
    }

    public synchronized void pushFriendsToUsers(String a, String b) {
        if(userNames.containsKey(a)) {
            sendFriendsTo(userNames.get(a));
        }
        if(userNames.containsKey(b)) {
            sendFriendsTo(userNames.get(b));
        }
    }


    public synchronized void sendUserListToAll() {
        Message message = new Message("userList", "server", "all", "users");
        ArrayList<String> users = new ArrayList<String>();
        for(int i = 0; i < clients.size(); i++) {
            if(!clients.get(i).userName.equals("")) {
                users.add(clients.get(i).userName);
            }
        }
        message.setUsers(users);
        for(int i = 0; i < clients.size(); i++) {
            clients.get(i).updateOneClient(message);
        }
    }

    public synchronized void tryMatchPlayers() {
        while(waitingPlayers.size() >= 2) {
            ClientThread playerOne = waitingPlayers.remove(0);
            ClientThread playerTwo = waitingPlayers.remove(0);
            GameSession session = new GameSession(UUID.randomUUID().toString(), nextGameNumber++, playerOne, playerTwo, false);
            activeGames.add(session);
            playerOne.gameSession = session;
            playerTwo.gameSession = session;
            session.startGame();
        }
    }

    public synchronized void createComputerGame(ClientThread player) {
        GameSession session = new GameSession(UUID.randomUUID().toString(), nextGameNumber++, player, null, true);
        activeGames.add(session);
        player.gameSession = session;
        session.startGame();
    }

    public synchronized void removeGame(GameSession session) {
        activeGames.remove(session);
    }

    public synchronized boolean createAccount(String userName, String password) {
        if(accounts.containsKey(userName)) {
            return false;
        }
        accounts.put(userName, password);
        getStatsFor(userName);
        getFriendSet(userName);
        saveAccounts();
        saveStats();
        getHistoryFor(userName);
        saveFriends();
        saveHistory();
        return true;
    }

    public synchronized boolean accountExists(String userName) {
        return accounts.containsKey(userName);
    }

    public synchronized boolean passwordMatches(String userName, String password) {
        return accounts.containsKey(userName) && accounts.get(userName).equals(password);
    }

    public synchronized boolean addFriend(String userName, String friendName) {
        if(!accounts.containsKey(friendName)) {
            return false;
        }
        if(userName.equals(friendName)) {
            return false;
        }
        getFriendSet(userName).add(friendName);
        getFriendSet(friendName).add(userName);
        getHistoryFor(userName);
        saveFriends();
        saveHistory();
        return true;
    }

    public synchronized void removeActiveClient(ClientThread client) {
        waitingPlayers.remove(client);
        clients.remove(client);
        if(!client.userName.equals("")) {
            userNames.remove(client.userName);
            sendUserListToAll();
            pushFriendsToAll();
        }
    }

    public synchronized void pushFriendsToAll() {
        for(int i = 0; i < clients.size(); i++) {
            if(!clients.get(i).userName.equals("")) {
                sendFriendsTo(clients.get(i));
            }
        }
    }

    public class TheServer extends Thread {
        public void run() {
            try(ServerSocket mysocket = new ServerSocket(5555)) {
                callback.accept("Server is waiting for a client!");
                while(true) {
                    ClientThread c = new ClientThread(mysocket.accept(), count);
                    callback.accept("client has connected to server: client #" + count);
                    clients.add(c);
                    c.start();
                    count++;
                }
            }
            catch(Exception e) {
                callback.accept("Server socket did not launch");
            }
        }
    }

    class ClientThread extends Thread {

        Socket connection;
        int count;
        ObjectInputStream in;
        ObjectOutputStream out;
        String userName;
        GameSession gameSession;
        boolean replayRequested;
        String pendingInviteFrom;

        ClientThread(Socket s, int count) {
            connection = s;
            this.count = count;
            userName = "";
            gameSession = null;
            replayRequested = false;
            pendingInviteFrom = "";
            pendingInviteFrom = "";
        }

        public void updateOneClient(Message message) {
            try {
                out.writeObject(message);
                out.flush();
            }
            catch(Exception e) {}
        }

        public void sendUserList() {
            Message message = new Message("userList", "server", "all", "users");
            ArrayList<String> users = new ArrayList<String>();
            for(int i = 0; i < clients.size(); i++) {
                if(!clients.get(i).userName.equals("")) {
                    users.add(clients.get(i).userName);
                }
            }
            message.setUsers(users);
            for(int i = 0; i < clients.size(); i++) {
                clients.get(i).updateOneClient(message);
            }
        }

        public void completeLogin(String name, String text) {
            userName = name;
            userNames.put(userName, this);
            getStatsFor(userName);
            getFriendSet(userName);
            updateOneClient(new Message("authAccepted", "server", userName, userName));
            callback.accept(text + userName);
            sendUserListToAll();
            sendStatsTo(this);
            sendFriendsTo(this);
            sendHistoryTo(this);
            pushFriendsToAll();
        }

        public void rejectAuth(String text) {
            updateOneClient(new Message("authRejected", "server", "client", text));
        }

        public void processLogout() {
            if(userName.equals("")) {
                updateOneClient(new Message("logoutDone", "server", "client", "Logged out."));
                return;
            }
            if(gameSession != null) {
                gameSession.handleQuit(this);
            }
            waitingPlayers.remove(this);
            userNames.remove(userName);
            callback.accept(userName + " logged out.");
            userName = "";
            gameSession = null;
            replayRequested = false;
            pendingInviteFrom = "";
            updateOneClient(new Message("logoutDone", "server", "client", "Logged out."));
            sendUserListToAll();
            pushFriendsToAll();
        }

        public void handleMessage(Message message) {
            if(message.getType().equals("createAccount")) {
                String name = message.getContent().trim();
                String password = message.getPassword().trim();
                if(name.equals("") || password.equals("")) {
                    rejectAuth("Enter a username and password.");
                }
                else if(name.contains(",") || password.contains(",")) {
                    rejectAuth("Usernames and passwords cannot contain commas.");
                }
                else if(accountExists(name)) {
                    rejectAuth("That username already exists. Log in instead or choose another one.");
                }
                else if(!createAccount(name, password)) {
                    rejectAuth("Could not create that account.");
                }
                else if(userNames.containsKey(name)) {
                    rejectAuth("That username is already active right now.");
                }
                else {
                    completeLogin(name, "account created for ");
                }
            }
            else if(message.getType().equals("login")) {
                String name = message.getContent().trim();
                String password = message.getPassword().trim();
                if(name.equals("") || password.equals("")) {
                    rejectAuth("Enter a username and password.");
                }
                else if(!accountExists(name)) {
                    rejectAuth("That account does not exist yet.");
                }
                else if(!passwordMatches(name, password)) {
                    rejectAuth("Incorrect password.");
                }
                else if(userNames.containsKey(name)) {
                    rejectAuth("That username is already logged in on another client.");
                }
                else {
                    completeLogin(name, "login accepted for ");
                }
            }
            else if(message.getType().equals("logout")) {
                processLogout();
            }
            else if(message.getType().equals("getStats")) {
                if(!userName.equals("")) {
                    sendStatsTo(this);
                }
            }
            else if(message.getType().equals("getFriends")) {
                if(!userName.equals("")) {
                    sendFriendsTo(this);
                }
            }
            else if(message.getType().equals("getHistory")) {
                if(!userName.equals("")) {
                    sendHistoryTo(this);
                }
            }
            else if(message.getType().equals("addFriend")) {
                if(userName.equals("")) {
                    updateOneClient(new Message("server", "server", "client", "Log in first."));
                }
                else if(addFriend(userName, message.getFriendName().trim())) {
                    updateOneClient(new Message("server", "server", userName, "Friend added: " + message.getFriendName().trim()));
                    pushFriendsToUsers(userName, message.getFriendName().trim());
                }
                else {
                    updateOneClient(new Message("server", "server", userName, "Could not add that friend."));
                }
            }
            else if(message.getType().equals("inviteFriend")) {
                if(userName.equals("")) {
                    updateOneClient(new Message("server", "server", "client", "Log in first."));
                }
                else {
                    String friendName = message.getFriendName().trim();
                    if(friendName.equals("") || !getFriendSet(userName).contains(friendName)) {
                        updateOneClient(new Message("server", "server", userName, "Invite one of your friends first."));
                    }
                    else if(!userNames.containsKey(friendName)) {
                        updateOneClient(new Message("server", "server", userName, friendName + " is not online."));
                    }
                    else {
                        ClientThread other = userNames.get(friendName);
                        if(other == this) {
                            updateOneClient(new Message("server", "server", userName, "You cannot invite yourself."));
                        }
                        else if(gameSession != null || waitingPlayers.contains(this)) {
                            updateOneClient(new Message("server", "server", userName, "Finish your current game or queue first."));
                        }
                        else if(other.gameSession != null || waitingPlayers.contains(other)) {
                            updateOneClient(new Message("server", "server", userName, friendName + " is busy right now."));
                        }
                        else {
                            other.pendingInviteFrom = userName;
                            Message invite = new Message("gameInvite", userName, friendName, userName + " invited you to play.");
                            invite.setFriendName(userName);
                            other.updateOneClient(invite);
                            updateOneClient(new Message("server", "server", userName, "Invite sent to " + friendName + "."));
                            callback.accept(userName + " invited " + friendName + " to a match.");
                        }
                    }
                }
            }
            else if(message.getType().equals("acceptInvite")) {
                String friendName = message.getFriendName().trim();
                if(!pendingInviteFrom.equals("") && pendingInviteFrom.equals(friendName) && userNames.containsKey(friendName)) {
                    ClientThread other = userNames.get(friendName);
                    if(other.gameSession == null && gameSession == null && !waitingPlayers.contains(other) && !waitingPlayers.contains(this)) {
                        pendingInviteFrom = "";
                        other.pendingInviteFrom = "";
                        updateOneClient(new Message("inviteCleared", "server", userName, "Invite accepted."));
                        other.updateOneClient(new Message("inviteCleared", "server", other.userName, userName + " accepted your invite."));
                        GameSession session = new GameSession(UUID.randomUUID().toString(), nextGameNumber++, other, this, false);
                        activeGames.add(session);
                        other.gameSession = session;
                        this.gameSession = session;
                        session.startGame();
                    }
                    else {
                        pendingInviteFrom = "";
                        updateOneClient(new Message("server", "server", userName, "That invitation is no longer available."));
                    }
                }
            }
            else if(message.getType().equals("declineInvite")) {
                String friendName = message.getFriendName().trim();
                if(!pendingInviteFrom.equals("") && pendingInviteFrom.equals(friendName)) {
                    pendingInviteFrom = "";
                    updateOneClient(new Message("inviteCleared", "server", userName, "Invite declined."));
                    if(userNames.containsKey(friendName)) {
                        ClientThread other = userNames.get(friendName);
                        other.pendingInviteFrom = "";
                        other.updateOneClient(new Message("server", "server", other.userName, userName + " declined your invite."));
                    }
                    callback.accept(userName + " declined a game invite from " + friendName + ".");
                }
            }
            else if(message.getType().equals("findGame")) {
                if(!userName.equals("") && gameSession == null && !waitingPlayers.contains(this)) {
                    waitingPlayers.add(this);
                    updateOneClient(new Message("waiting", "server", userName, "Searching for another player..."));
                    callback.accept(userName + " entered the waiting room.");
                    tryMatchPlayers();
                }
            }
            else if(message.getType().equals("findComputerGame")) {
                if(!userName.equals("") && gameSession == null) {
                    createComputerGame(this);
                }
            }
            else if(message.getType().equals("cancelWaiting")) {
                if(waitingPlayers.contains(this)) {
                    waitingPlayers.remove(this);
                    updateOneClient(new Message("server", "server", userName, "You left the waiting room."));
                    callback.accept(userName + " left the waiting room.");
                }
            }
            else if(message.getType().equals("move")) {
                if(gameSession != null) {
                    gameSession.handleMove(this, new Move(message.getFromRow(), message.getFromCol(), message.getToRow(), message.getToCol()));
                }
            }
            else if(message.getType().equals("chat")) {
                if(gameSession != null) {
                    gameSession.handleChat(this, message.getContent());
                }
            }
            else if(message.getType().equals("playAgain")) {
                if(gameSession != null) {
                    gameSession.handleReplayRequest(this);
                }
            }
            else if(message.getType().equals("quitGame")) {
                if(gameSession != null) {
                    gameSession.handleQuit(this);
                }
            }
            else if(message.getType().equals("leaveFinishedGame")) {
                if(gameSession != null) {
                    gameSession.handleLeaveAfterGame(this);
                }
            }
        }

        public void run() {
            try {
                out = new ObjectOutputStream(connection.getOutputStream());
                in = new ObjectInputStream(connection.getInputStream());
                connection.setTcpNoDelay(true);
            }
            catch(Exception e) {
                callback.accept("Streams not open");
            }
            while(true) {
                try {
                    Message message = (Message) in.readObject();
                    handleMessage(message);
                }
                catch(Exception e) {
                    callback.accept("OOOOPPs...Something wrong with the socket from client: " + count + "....closing down!");
                    waitingPlayers.remove(this);
                    if(gameSession != null) {
                        gameSession.handleDisconnect(this);
                    }
                    removeActiveClient(this);
                    break;
                }
            }
        }
    }

    class GameSession {

        String gameId;
        int gameNumber;
        ClientThread playerOne;
        ClientThread playerTwo;
        GameState gameState;
        boolean computerGame;
        String aiName;
        String aiColor;
        int lastFromRow;
        int lastFromCol;
        int lastToRow;
        int lastToCol;

        GameSession(String gameId, int gameNumber, ClientThread playerOne, ClientThread playerTwo, boolean computerGame) {
            this.gameId = gameId;
            this.gameNumber = gameNumber;
            this.playerOne = playerOne;
            this.playerTwo = playerTwo;
            this.computerGame = computerGame;
            gameState = new GameState();
            aiName = "Computer";
            aiColor = "red";
            clearLastMove();
        }

        public String gameLabel() {
            return "Game " + gameNumber;
        }

        public void clearLastMove() {
            lastFromRow = -1;
            lastFromCol = -1;
            lastToRow = -1;
            lastToCol = -1;
        }

        public void rememberMove(Move move) {
            lastFromRow = move.getFromRow();
            lastFromCol = move.getFromCol();
            lastToRow = move.getToRow();
            lastToCol = move.getToCol();
        }

        public void startGame() {
            if(computerGame) {
                clearLastMove();
                playerOne.replayRequested = false;
                Message m1 = baseBoardMessage("startGame", "Game started against the computer. Black moves first.");
                m1.setTarget(playerOne.userName);
                m1.setPlayerColor("black");
                m1.setComputerGame(true);
                playerOne.updateOneClient(m1);
                callback.accept(gameLabel() + " started for " + playerOne.userName + " vs Computer.");
                return;
            }
            clearLastMove();
            playerOne.replayRequested = false;
            playerTwo.replayRequested = false;
            Message m1 = baseBoardMessage("startGame", "Game started against " + playerTwo.userName + ". Black moves first.");
            m1.setTarget(playerOne.userName);
            m1.setPlayerColor("black");
            playerOne.updateOneClient(m1);
            Message m2 = baseBoardMessage("startGame", "Game started against " + playerOne.userName + ". Black moves first.");
            m2.setTarget(playerTwo.userName);
            m2.setPlayerColor("red");
            playerTwo.updateOneClient(m2);
            callback.accept(gameLabel() + " started between " + playerOne.userName + " and " + playerTwo.userName + ".");
        }

        public Message baseBoardMessage(String type, String content) {
            Message message = new Message(type, "server", "game", content);
            message.setGameId(gameId);
            message.setBoardData(gameState.getBoard().toRows());
            message.setCurrentPlayer(gameState.getCurrentPlayer());
            message.setWinner(gameState.getWinner());
            message.setDraw(gameState.isDraw());
            message.setGameOver(gameState.isGameOver());
            message.setComputerGame(computerGame);
            message.setForcedRow(gameState.getForcedRow());
            message.setForcedCol(gameState.getForcedCol());
            message.setMustJump(gameState.getForcedRow() != -1 && gameState.getForcedCol() != -1);
            message.setLastFromRow(lastFromRow);
            message.setLastFromCol(lastFromCol);
            message.setLastToRow(lastToRow);
            message.setLastToCol(lastToCol);
            return message;
        }

        public String getColorFor(ClientThread player) {
            if(player == playerOne) {
                return "black";
            }
            return "red";
        }

        public ClientThread getOther(ClientThread player) {
            if(player == playerOne) {
                return playerTwo;
            }
            return playerOne;
        }

        public void broadcast(Message message) {
            playerOne.updateOneClient(message);
            if(!computerGame && playerTwo != null) {
                playerTwo.updateOneClient(message);
            }
        }

        public void handleMove(ClientThread player, Move move) {
            if(computerGame && player != playerOne) {
                return;
            }
            String result = gameState.makeMove(getColorFor(player), move);
            if(!result.equals("ok")) {
                Message error = new Message("error", "server", player.userName, result);
                error.setGameId(gameId);
                player.updateOneClient(error);
                return;
            }
            rememberMove(move);
            Message update = baseBoardMessage("boardUpdate", player.userName + " made a move.");
            broadcast(update);
            callback.accept(player.userName + " moved " + move.getFromRow() + "," + move.getFromCol() + " to " + move.getToRow() + "," + move.getToCol() + " in " + gameLabel() + ".");
            if(gameState.isGameOver()) {
                finishGame();
                return;
            }
            if(computerGame && gameState.getCurrentPlayer().equals(aiColor)) {
                sendComputerThinking();
                makeComputerTurn();
            }
        }

        public void sendComputerThinking() {
            Message info = new Message("info", "server", playerOne.userName, "Computer is thinking...");
            info.setGameId(gameId);
            playerOne.updateOneClient(info);
        }

        public void pauseComputerMove() {
            try {
                Thread.sleep(650);
            }
            catch(Exception e) {}
        }

        public void makeComputerTurn() {
            pauseComputerMove();
            while(!gameState.isGameOver() && gameState.getCurrentPlayer().equals(aiColor)) {
                Move bestMove = chooseBestMove();
                if(bestMove == null) {
                    gameState.checkGameOver();
                    break;
                }
                gameState.makeMove(aiColor, bestMove);
                rememberMove(bestMove);
                Message update = baseBoardMessage("boardUpdate", aiName + " moved from " + bestMove.getFromRow() + "," + bestMove.getFromCol() + " to " + bestMove.getToRow() + "," + bestMove.getToCol() + ".");
                playerOne.updateOneClient(update);
                callback.accept(aiName + " moved " + bestMove.getFromRow() + "," + bestMove.getFromCol() + " to " + bestMove.getToRow() + "," + bestMove.getToCol() + " in " + gameLabel() + ".");
                if(!gameState.isGameOver() && gameState.getCurrentPlayer().equals(aiColor)) {
                    pauseComputerMove();
                }
            }
            if(gameState.isGameOver()) {
                finishGame();
            }
        }

        public Move chooseBestMove() {
            ArrayList<Move> moves = gameState.getValidMoves(aiColor);
            if(moves.size() == 0) {
                return null;
            }
            Move bestMove = moves.get(0);
            int bestScore = Integer.MIN_VALUE;
            for(int i = 0; i < moves.size(); i++) {
                GameState copy = gameState.copy();
                copy.makeMove(aiColor, moves.get(i));
                int score = minimax(copy, 4, aiColor, Integer.MIN_VALUE, Integer.MAX_VALUE);
                if(score > bestScore) {
                    bestScore = score;
                    bestMove = moves.get(i);
                }
            }
            return bestMove;
        }

        public int minimax(GameState state, int depth, String color, int alpha, int beta) {
            if(depth == 0 || state.isGameOver()) {
                return state.evaluateBoard(color);
            }
            ArrayList<Move> moves = state.getValidMoves(state.getCurrentPlayer());
            if(moves.size() == 0) {
                state.checkGameOver();
                return state.evaluateBoard(color);
            }
            if(state.getCurrentPlayer().equals(color)) {
                int best = Integer.MIN_VALUE;
                for(int i = 0; i < moves.size(); i++) {
                    GameState copy = state.copy();
                    copy.makeMove(copy.getCurrentPlayer(), moves.get(i));
                    int score = minimax(copy, depth - 1, color, alpha, beta);
                    if(score > best) {
                        best = score;
                    }
                    if(score > alpha) {
                        alpha = score;
                    }
                    if(beta <= alpha) {
                        break;
                    }
                }
                return best;
            }
            int best = Integer.MAX_VALUE;
            for(int i = 0; i < moves.size(); i++) {
                GameState copy = state.copy();
                copy.makeMove(copy.getCurrentPlayer(), moves.get(i));
                int score = minimax(copy, depth - 1, color, alpha, beta);
                if(score < best) {
                    best = score;
                }
                if(score < beta) {
                    beta = score;
                }
                if(beta <= alpha) {
                    break;
                }
            }
            return best;
        }

        public void finishGame() {
            if(computerGame) {
                if(gameState.isDraw()) {
                    Message drawMessage = baseBoardMessage("gameOver", "Draw game. Computer matches do not affect stats.");
                    playerOne.updateOneClient(drawMessage);
                }
                else if(gameState.getWinner().equals("black")) {
                    Message winMessage = baseBoardMessage("gameOver", "You win. Computer matches do not affect stats.");
                    winMessage.setWinner(playerOne.userName);
                    playerOne.updateOneClient(winMessage);
                }
                else {
                    Message loseMessage = baseBoardMessage("gameOver", "You lose. Computer matches do not affect stats.");
                    loseMessage.setWinner(aiName);
                    playerOne.updateOneClient(loseMessage);
                }
                if(gameState.isDraw()) {
                    addHistory(playerOne.userName, "Draw vs Computer (not counted in stats)");
                }
                else if(gameState.getWinner().equals("black")) {
                    addHistory(playerOne.userName, "Win vs Computer (not counted in stats)");
                }
                else {
                    addHistory(playerOne.userName, "Loss vs Computer (not counted in stats)");
                }
                sendHistoryTo(playerOne);
                callback.accept(gameLabel() + " ended for " + playerOne.userName + " vs Computer.");
                return;
            }
            if(gameState.isDraw()) {
                addDraw(playerOne.userName);
                addDraw(playerTwo.userName);
                Message gameOverMessage = baseBoardMessage("gameOver", "Draw game.");
                playerOne.updateOneClient(gameOverMessage);
                playerTwo.updateOneClient(gameOverMessage);
                addHistory(playerOne.userName, "Draw vs " + playerTwo.userName + " in " + gameLabel());
                addHistory(playerTwo.userName, "Draw vs " + playerOne.userName + " in " + gameLabel());
                sendHistoryTo(playerOne);
                sendHistoryTo(playerTwo);
                callback.accept(gameLabel() + " ended in a draw between " + playerOne.userName + " and " + playerTwo.userName + ".");
            }
            else {
                ClientThread winnerPlayer = gameState.getWinner().equals("black") ? playerOne : playerTwo;
                ClientThread loserPlayer = getOther(winnerPlayer);
                addWin(winnerPlayer.userName);
                addLoss(loserPlayer.userName);
                Message winMessage = baseBoardMessage("gameOver", "You win!");
                winMessage.setWinner(winnerPlayer.userName);
                winnerPlayer.updateOneClient(winMessage);
                Message loseMessage = baseBoardMessage("gameOver", "You lose.");
                loseMessage.setWinner(winnerPlayer.userName);
                loserPlayer.updateOneClient(loseMessage);
                addHistory(winnerPlayer.userName, "Win vs " + loserPlayer.userName + " in " + gameLabel());
                addHistory(loserPlayer.userName, "Loss vs " + winnerPlayer.userName + " in " + gameLabel());
                sendHistoryTo(winnerPlayer);
                sendHistoryTo(loserPlayer);
                callback.accept(winnerPlayer.userName + " won " + gameLabel() + ".");
            }
        }

        public void handleChat(ClientThread player, String text) {
            if(computerGame) {
                Message chat = new Message("chat", player.userName, "game", text);
                chat.setGameId(gameId);
                playerOne.updateOneClient(chat);
                Message reply = new Message("chat", aiName, "game", "I only focus on the board. Good luck.");
                reply.setGameId(gameId);
                playerOne.updateOneClient(reply);
                callback.accept(player.userName + " sent a chat message in " + gameLabel() + ".");
                return;
            }
            Message chat = new Message("chat", player.userName, "game", text);
            chat.setGameId(gameId);
            playerOne.updateOneClient(chat);
            playerTwo.updateOneClient(chat);
            callback.accept(player.userName + " sent a chat message in " + gameLabel() + ".");
        }

        public void handleReplayRequest(ClientThread player) {
            if(computerGame) {
                gameState.resetGame();
                startGame();
                return;
            }
            player.replayRequested = true;
            ClientThread other = getOther(player);
            other.updateOneClient(new Message("info", "server", other.userName, player.userName + " wants to play again."));
            if(playerOne.replayRequested && playerTwo.replayRequested) {
                gameState.resetGame();
                startGame();
            }
        }

        public void handleQuit(ClientThread player) {
            if(computerGame) {
                player.gameSession = null;
                player.replayRequested = false;
                player.updateOneClient(new Message("leftGame", "server", player.userName, "You left the computer game."));
                removeGame(this);
                callback.accept(player.userName + " left " + gameLabel() + ".");
                return;
            }
            ClientThread other = getOther(player);
            addLoss(player.userName);
            addWin(other.userName);
            player.replayRequested = false;
            other.replayRequested = false;
            gameState.gameOver = true;
            gameState.draw = false;
            if(player == playerOne) {
                gameState.winner = "red";
            }
            else {
                gameState.winner = "black";
            }
            Message otherMessage = baseBoardMessage("gameOver", player.userName + " quit. You win.");
            otherMessage.setWinner(other.userName);
            other.updateOneClient(otherMessage);
            Message playerMessage = baseBoardMessage("gameOver", "You quit the game. This counts as a loss.");
            playerMessage.setWinner(other.userName);
            player.updateOneClient(playerMessage);
            sendStatsTo(player);
            sendStatsTo(other);
            addHistory(player.userName, "Loss by quit vs " + other.userName + " in " + gameLabel());
            addHistory(other.userName, "Win by opponent quit vs " + player.userName + " in " + gameLabel());
            sendHistoryTo(player);
            sendHistoryTo(other);
            callback.accept(player.userName + " quit " + gameLabel() + " against " + other.userName + ".");
        }


        public void handleLeaveAfterGame(ClientThread player) {
            if(computerGame) {
                if(player == playerOne) {
                    player.gameSession = null;
                    removeGame(this);
                }
                return;
            }
            if(!gameState.isGameOver()) {
                return;
            }
            if(player == playerOne) {
                playerOne.gameSession = null;
            }
            if(player == playerTwo) {
                playerTwo.gameSession = null;
            }
            if((playerOne == null || playerOne.gameSession == null) && (playerTwo == null || playerTwo.gameSession == null)) {
                removeGame(this);
            }
        }

        public void handleDisconnect(ClientThread player) {
            if(computerGame) {
                if(player == playerOne) {
                    player.gameSession = null;
                    removeGame(this);
                }
                return;
            }
            if(gameState.isGameOver()) {
                handleLeaveAfterGame(player);
                return;
            }
            ClientThread other = getOther(player);
            addLoss(player.userName);
            addWin(other.userName);
            other.gameSession = null;
            other.replayRequested = false;
            other.updateOneClient(new Message("gameOver", "server", other.userName, player.userName + " disconnected. You win."));
            sendStatsTo(other);
            removeGame(this);
            addHistory(player.userName, "Loss by disconnect vs " + other.userName + " in " + gameLabel());
            addHistory(other.userName, "Win by opponent disconnect vs " + player.userName + " in " + gameLabel());
            sendHistoryTo(other);
            callback.accept(player.userName + " disconnected during " + gameLabel() + ".");
        }
    }
}
