import java.util.ArrayList;
import java.util.HashMap;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GuiClient extends Application {

    HashMap<String, Scene> sceneMap;
    Client clientConnection;
    Stage primaryStage;
    String currentUser;
    String currentGameId;
    String myColor;
    String currentTurn;
    boolean gameOver;
    boolean computerGame;
    int selectedRow;
    int selectedCol;
    ArrayList<String> boardData;
    ArrayList<String> legalMoves;
    int forcedRow;
    int forcedCol;
    int lastFromRow;
    int lastFromCol;
    int lastToRow;
    int lastToCol;

    TextField nameField;
    PasswordField passwordField;
    Button createAccountButton;
    Button loginButton;
    Button exitAuthButton;
    Label loginStatus;

    Label homeLabel;
    Label statsLabel;
    Button playButton;
    Button computerButton;
    Button statsButton;
    Button friendsButton;
    Button logoutButton;
    Button exitHomeButton;
    TextField addFriendField;
    Button addFriendButton;
    Label friendStatusLabel;
    ListView<String> friendsList;
    Button inviteFriendButton;
    ListView<String> historyList;
    Button historyButton;
    Label inviteLabel;
    Button acceptInviteButton;
    Button declineInviteButton;
    String pendingInviteFrom;

    Label waitingLabel;
    Button waitingCancelButton;

    GridPane boardPane;
    Label gameStatusLabel;
    Label colorLabel;
    Label turnLabel;
    Label modeLabel;
    ListView<String> chatList;
    TextField chatField;
    Button chatButton;
    Button quitButton;

    Label gameOverLabel;
    Button playAgainButton;
    Button homeButton;
    Button exitButton;

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage mainStage) {
        primaryStage = mainStage;
        sceneMap = new HashMap<String, Scene>();
        boardData = new ArrayList<String>();
        legalMoves = new ArrayList<String>();
        forcedRow = -1;
        forcedCol = -1;
        lastFromRow = -1;
        lastFromCol = -1;
        lastToRow = -1;
        lastToCol = -1;
        selectedRow = -1;
        selectedCol = -1;
        currentUser = "";
        pendingInviteFrom = "";
        currentGameId = "";
        myColor = "";
        currentTurn = "";
        gameOver = false;
        computerGame = false;
        clientConnection = new Client(data -> {
            Platform.runLater(() -> {
                if(data instanceof Message) {
                    handleMessage((Message) data);
                }
                else {
                    loginStatus.setText(data.toString());
                    showLogin();
                }
            });
        });
        clientConnection.start();

        sceneMap.put("login", createLoginScene());
        sceneMap.put("home", createHomeScene());
        sceneMap.put("waiting", createWaitingScene());
        sceneMap.put("game", createGameScene());
        sceneMap.put("gameOver", createGameOverScene());

        primaryStage.setOnCloseRequest((WindowEvent t) -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.setScene(sceneMap.get("login"));
        primaryStage.setTitle("Checkers Client");
        primaryStage.show();
    }

    public void handleMessage(Message message) {
        if(message.getType().equals("authAccepted")) {
            currentUser = message.getContent();
            clientConnection.setUserName(currentUser);
            homeLabel.setText("Welcome, " + currentUser);
            loginStatus.setText("Success.");
            nameField.clear();
            passwordField.clear();
            showHome();
            clientConnection.requestStats();
            clientConnection.requestFriends();
            clientConnection.requestHistory();
        }
        else if(message.getType().equals("authRejected")) {
            loginStatus.setText(message.getContent());
        }
        else if(message.getType().equals("logoutDone")) {
            currentUser = "";
        pendingInviteFrom = "";
            currentGameId = "";
            myColor = "";
            currentTurn = "";
            computerGame = false;
            boardData.clear();
            friendsList.getItems().clear();
            historyList.getItems().clear();
            pendingInviteFrom = "";
            inviteLabel.setText("No pending invites.");
            friendStatusLabel.setText("Add friends and check who is online.");
            statsLabel.setText("Wins: 0   Losses: 0   Draws: 0");
            loginStatus.setText(message.getContent());
            showLogin();
        }
        else if(message.getType().equals("stats")) {
            statsLabel.setText("Wins: " + message.getWins() + "   Losses: " + message.getLosses() + "   Draws: " + message.getDraws());
        }
        else if(message.getType().equals("friends")) {
            friendsList.getItems().setAll(message.getFriends());
        }
        else if(message.getType().equals("history")) {
            historyList.getItems().setAll(message.getHistory());
        }
        else if(message.getType().equals("gameInvite")) {
            pendingInviteFrom = message.getFriendName();
            inviteLabel.setText(message.getFriendName() + " invited you to play.");
            showHome();
        }
        else if(message.getType().equals("inviteCleared")) {
            pendingInviteFrom = "";
            inviteLabel.setText(message.getContent());
        }
        else if(message.getType().equals("waiting")) {
            waitingLabel.setText(message.getContent());
            showWaiting();
        }
        else if(message.getType().equals("startGame")) {
            currentGameId = message.getGameId();
            myColor = message.getPlayerColor();
            currentTurn = message.getCurrentPlayer();
            boardData = message.getBoardData();
            computerGame = message.isComputerGame();
            forcedRow = message.getForcedRow();
            forcedCol = message.getForcedCol();
            lastFromRow = message.getLastFromRow();
            lastFromCol = message.getLastFromCol();
            lastToRow = message.getLastToRow();
            lastToCol = message.getLastToCol();
            legalMoves.clear();
            selectedRow = -1;
            selectedCol = -1;
            gameOver = false;
            chatList.getItems().clear();
            colorLabel.setText("Side: " + capitalize(myColor));
            modeLabel.setText(computerGame ? "Mode: vs Computer" : "Mode: vs Player");
            gameStatusLabel.setText(message.getContent());
            updateTurnLabel();
            drawBoard();
            showGame();
        }
        else if(message.getType().equals("boardUpdate")) {
            currentTurn = message.getCurrentPlayer();
            boardData = message.getBoardData();
            computerGame = message.isComputerGame();
            forcedRow = message.getForcedRow();
            forcedCol = message.getForcedCol();
            lastFromRow = message.getLastFromRow();
            lastFromCol = message.getLastFromCol();
            lastToRow = message.getLastToRow();
            lastToCol = message.getLastToCol();
            legalMoves.clear();
            selectedRow = -1;
            selectedCol = -1;
            gameStatusLabel.setText(message.getContent());
            updateTurnLabel();
            drawBoard();
        }
        else if(message.getType().equals("chat")) {
            chatList.getItems().add(message.toString());
        }
        else if(message.getType().equals("error")) {
            gameStatusLabel.setText(message.getContent());
        }
        else if(message.getType().equals("gameOver")) {
            if(message.getBoardData().size() == 8) {
                currentTurn = message.getCurrentPlayer();
                boardData = message.getBoardData();
                forcedRow = message.getForcedRow();
                forcedCol = message.getForcedCol();
                lastFromRow = message.getLastFromRow();
                lastFromCol = message.getLastFromCol();
                lastToRow = message.getLastToRow();
                lastToCol = message.getLastToCol();
                legalMoves.clear();
                selectedRow = -1;
                selectedCol = -1;
                drawBoard();
            }
            gameOver = true;
            gameOverLabel.setText(message.getContent());
            playAgainButton.setDisable(false);
            if(computerGame) {
                playAgainButton.setText("Play Again vs Computer");
            }
            else {
                playAgainButton.setText("Play Again");
            }
            showGameOver();
            clientConnection.requestStats();
            clientConnection.requestFriends();
            clientConnection.requestHistory();
        }
        else if(message.getType().equals("leftGame")) {
            gameOver = true;
            selectedRow = -1;
            selectedCol = -1;
            legalMoves.clear();
            currentGameId = "";
            gameOverLabel.setText(message.getContent());
            playAgainButton.setDisable(true);
            showGameOver();
            clientConnection.requestStats();
            clientConnection.requestHistory();
        }
        else if(message.getType().equals("server") || message.getType().equals("info")) {
            if(getCurrentScene() == sceneMap.get("game")) {
                gameStatusLabel.setText(message.getContent());
            }
            else if(getCurrentScene() == sceneMap.get("waiting")) {
                waitingLabel.setText(message.getContent());
            }
            else if(getCurrentScene() == sceneMap.get("home")) {
                friendStatusLabel.setText(message.getContent());
            }
            else {
                loginStatus.setText(message.getContent());
            }
        }
    }

    public Scene getCurrentScene() {
        return primaryStage.getScene();
    }

    public void showLogin() {
        primaryStage.setScene(sceneMap.get("login"));
    }

    public void showHome() {
        primaryStage.setScene(sceneMap.get("home"));
    }

    public void showWaiting() {
        primaryStage.setScene(sceneMap.get("waiting"));
    }

    public void showGame() {
        primaryStage.setScene(sceneMap.get("game"));
    }

    public void showGameOver() {
        primaryStage.setScene(sceneMap.get("gameOver"));
    }

    public Scene createLoginScene() {
        Label title = new Label("Royal Checkers");
        title.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");

        Label subtitle = new Label("Sign in or create a new account to enter the lobby.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #ead5ae;");

        nameField = new TextField();
        nameField.setPromptText("Username");
        nameField.setMaxWidth(320);
        nameField.setStyle(fieldStyle());

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setMaxWidth(320);
        passwordField.setStyle(fieldStyle());

        createAccountButton = new Button("Create Account");
        loginButton = new Button("Login");
        exitAuthButton = new Button("Exit");
        createAccountButton.setPrefWidth(150);
        loginButton.setPrefWidth(150);
        exitAuthButton.setPrefWidth(150);
        createAccountButton.setStyle(buttonStyle("#8e5a34"));
        loginButton.setStyle(buttonStyle("#244e74"));
        exitAuthButton.setStyle(buttonStyle("#4d4d4d"));

        createAccountButton.setOnAction(e -> {
            sendAuth("create");
        });
        loginButton.setOnAction(e -> {
            sendAuth("login");
        });
        exitAuthButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        loginStatus = new Label("Every account uses a unique username and password.");
        loginStatus.setWrapText(true);
        loginStatus.setStyle("-fx-font-size: 14px; -fx-text-fill: #f3e7d0;");
        loginStatus.setMaxWidth(380);

        HBox buttons = new HBox(12, createAccountButton, loginButton, exitAuthButton);
        buttons.setAlignment(Pos.CENTER);

        VBox card = new VBox(16, title, subtitle, nameField, passwordField, buttons, loginStatus);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(440);
        card.setStyle(cardStyle());

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        root.setStyle(rootStyle());

        return new Scene(root, 980, 650);
    }

    public Scene createHomeScene() {
        homeLabel = new Label("Welcome");
        homeLabel.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        statsLabel = new Label("Wins: 0   Losses: 0   Draws: 0");
        statsLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ead8b7;");

        playButton = new Button("Play vs Player");
        computerButton = new Button("Play vs Computer");
        statsButton = new Button("Refresh Stats");
        friendsButton = new Button("Refresh Friends");
        logoutButton = new Button("Logout");
        exitHomeButton = new Button("Exit");
        historyButton = new Button("Refresh History");

        playButton.setStyle(buttonStyle("#8e5a34"));
        computerButton.setStyle(buttonStyle("#1f5f4a"));
        statsButton.setStyle(buttonStyle("#244e74"));
        friendsButton.setStyle(buttonStyle("#6a4e90"));
        logoutButton.setStyle(buttonStyle("#8a3030"));
        exitHomeButton.setStyle(buttonStyle("#4d4d4d"));
        historyButton.setStyle(buttonStyle("#7a5c28"));

        playButton.setOnAction(e -> {
            clientConnection.requestGame();
        });
        computerButton.setOnAction(e -> {
            clientConnection.requestComputerGame();
        });
        statsButton.setOnAction(e -> {
            clientConnection.requestStats();
        });
        friendsButton.setOnAction(e -> {
            clientConnection.requestFriends();
        });
        logoutButton.setOnAction(e -> {
            clientConnection.logout();
        });
        exitHomeButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
        historyButton.setOnAction(e -> {
            clientConnection.requestHistory();
        });

        Label aiNote = new Label("Computer matches do not affect your statistics.");
        aiNote.setWrapText(true);
        aiNote.setStyle("-fx-font-size: 13px; -fx-text-fill: #d8c9ae;");
        aiNote.setMaxWidth(250);

        VBox leftBox = new VBox(14, homeLabel, statsLabel, playButton, computerButton, aiNote, statsButton, historyButton, logoutButton, exitHomeButton);
        leftBox.setPadding(new Insets(24));
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPrefWidth(300);
        leftBox.setStyle(cardStyle());

        Label friendTitle = new Label("Friends");
        friendTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        friendsList = new ListView<String>();
        friendsList.setPrefHeight(210);
        addFriendField = new TextField();
        addFriendField.setPromptText("Enter username to add");
        addFriendField.setStyle(fieldStyle());
        addFriendButton = new Button("Add Friend");
        inviteFriendButton = new Button("Invite Friend");
        addFriendButton.setStyle(buttonStyle("#8e5a34"));
        inviteFriendButton.setStyle(buttonStyle("#1f5f4a"));
        addFriendButton.setOnAction(e -> {
            String text = addFriendField.getText().trim();
            if(text.equals("")) {
                friendStatusLabel.setText("Enter a username to add.");
            }
            else {
                clientConnection.addFriend(text);
                addFriendField.clear();
            }
        });
        inviteFriendButton.setOnAction(e -> {
            String selected = friendsList.getSelectionModel().getSelectedItem();
            if(selected == null || selected.equals("")) {
                friendStatusLabel.setText("Select a friend first.");
                return;
            }
            if(!selected.contains("(online)")) {
                friendStatusLabel.setText("That friend is not online.");
                return;
            }
            clientConnection.inviteFriend(extractName(selected));
        });
        friendStatusLabel = new Label("Add friends and check who is online.");
        friendStatusLabel.setWrapText(true);
        friendStatusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ead8b7;");
        inviteLabel = new Label("No pending invites.");
        inviteLabel.setWrapText(true);
        inviteLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #f8e8c8;");
        acceptInviteButton = new Button("Accept Invite");
        declineInviteButton = new Button("Decline Invite");
        acceptInviteButton.setStyle(buttonStyle("#244e74"));
        declineInviteButton.setStyle(buttonStyle("#8a3030"));
        acceptInviteButton.setOnAction(e -> {
            if(!pendingInviteFrom.equals("")) {
                clientConnection.respondInvite(pendingInviteFrom, true);
            }
        });
        declineInviteButton.setOnAction(e -> {
            if(!pendingInviteFrom.equals("")) {
                clientConnection.respondInvite(pendingInviteFrom, false);
            }
        });
        HBox friendRow = new HBox(10, addFriendField, addFriendButton);
        HBox.setHgrow(addFriendField, Priority.ALWAYS);
        HBox inviteRow = new HBox(10, inviteFriendButton, friendsButton);
        HBox inviteAnswerRow = new HBox(10, acceptInviteButton, declineInviteButton);
        VBox rightBox = new VBox(12, friendTitle, friendsList, friendRow, inviteRow, inviteLabel, inviteAnswerRow, friendStatusLabel);
        rightBox.setPadding(new Insets(24));
        rightBox.setPrefWidth(360);
        rightBox.setStyle(cardStyle());

        Label historyTitle = new Label("Match History");
        historyTitle.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        historyList = new ListView<String>();
        historyList.setPrefHeight(420);
        VBox historyBox = new VBox(12, historyTitle, historyList);
        historyBox.setPadding(new Insets(24));
        historyBox.setPrefWidth(380);
        historyBox.setStyle(cardStyle());

        HBox center = new HBox(24, leftBox, rightBox, historyBox);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(28));

        StackPane root = new StackPane(center);
        root.setStyle(rootStyle());
        return new Scene(root, 1320, 760);
    }

    public Scene createWaitingScene() {
        waitingLabel = new Label("Waiting for another player...");
        waitingLabel.setWrapText(true);
        waitingLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #f8e8c8;");
        waitingCancelButton = new Button("Cancel");
        waitingCancelButton.setStyle(buttonStyle("#8a3030"));
        waitingCancelButton.setOnAction(e -> {
            clientConnection.cancelWaiting();
            showHome();
        });
        VBox card = new VBox(22, waitingLabel, waitingCancelButton);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(440);
        card.setStyle(cardStyle());
        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        root.setStyle(rootStyle());
        return new Scene(root, 980, 650);
    }

    public Scene createGameScene() {
        boardPane = new GridPane();
        boardPane.setAlignment(Pos.CENTER);
        gameStatusLabel = new Label("Game status");
        colorLabel = new Label("Side");
        turnLabel = new Label("Turn");
        modeLabel = new Label("Mode");
        gameStatusLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #f8e8c8;");
        colorLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        turnLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ead8b7;");
        modeLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ead8b7;");
        chatList = new ListView<String>();
        chatList.setPrefWidth(300);
        chatList.setPrefHeight(420);
        chatField = new TextField();
        chatField.setPromptText("Send a message");
        chatField.setStyle(fieldStyle());
        chatButton = new Button("Send");
        quitButton = new Button("Quit Match");
        chatButton.setStyle(buttonStyle("#1f5f4a"));
        quitButton.setStyle(buttonStyle("#8a3030"));
        chatButton.setOnAction(e -> {
            if(!chatField.getText().trim().equals("")) {
                clientConnection.sendChat(currentGameId, chatField.getText().trim());
                chatField.clear();
            }
        });
        quitButton.setOnAction(e -> {
            if(!currentGameId.equals("")) {
                clientConnection.quitGame(currentGameId);
            }
        });

        Label boardTitle = new Label("Checkers Board");
        boardTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        VBox leftBox = new VBox(12, boardTitle, modeLabel, colorLabel, turnLabel, gameStatusLabel, boardPane);
        leftBox.setAlignment(Pos.TOP_CENTER);
        leftBox.setPadding(new Insets(20));
        leftBox.setStyle(cardStyle());

        Label chatTitle = new Label("Match Chat");
        chatTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        HBox sendRow = new HBox(10, chatField, chatButton);
        HBox.setHgrow(chatField, Priority.ALWAYS);
        VBox rightBox = new VBox(14, chatTitle, chatList, sendRow, quitButton);
        rightBox.setPadding(new Insets(20));
        rightBox.setPrefWidth(340);
        rightBox.setStyle(cardStyle());

        HBox center = new HBox(24, leftBox, rightBox);
        center.setAlignment(Pos.CENTER);
        center.setPadding(new Insets(28));

        BorderPane pane = new BorderPane();
        pane.setCenter(center);
        pane.setStyle(rootStyle());
        return new Scene(pane, 1140, 720);
    }

    public Scene createGameOverScene() {
        Label title = new Label("Game Over");
        title.setStyle("-fx-font-size: 34px; -fx-font-weight: bold; -fx-text-fill: #f8e8c8;");
        gameOverLabel = new Label("Game Over");
        gameOverLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #ead8b7;");
        gameOverLabel.setWrapText(true);
        gameOverLabel.setMaxWidth(420);
        playAgainButton = new Button("Play Again");
        homeButton = new Button("Home");
        exitButton = new Button("Exit");
        playAgainButton.setStyle(buttonStyle("#8e5a34"));
        homeButton.setStyle(buttonStyle("#244e74"));
        exitButton.setStyle(buttonStyle("#4d4d4d"));
        playAgainButton.setOnAction(e -> {
            if(currentGameId.equals("")) {
                return;
            }
            clientConnection.sendReplay(currentGameId);
            playAgainButton.setDisable(true);
            if(computerGame) {
                gameOverLabel.setText("Starting a new computer match...");
            }
            else {
                gameOverLabel.setText("Waiting for the other player to accept the rematch.");
            }
        });
        homeButton.setOnAction(e -> {
            if(!currentGameId.equals("")) {
                clientConnection.leaveFinishedGame(currentGameId);
            }
            currentGameId = "";
            gameOver = false;
            selectedRow = -1;
            selectedCol = -1;
            legalMoves.clear();
            boardData.clear();
            showHome();
            clientConnection.requestStats();
            clientConnection.requestFriends();
        });
        exitButton.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
        VBox card = new VBox(18, title, gameOverLabel, playAgainButton, homeButton, exitButton);
        card.setPadding(new Insets(36));
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(460);
        card.setStyle(cardStyle());
        StackPane root = new StackPane(card);
        root.setPadding(new Insets(30));
        root.setStyle(rootStyle());
        return new Scene(root, 980, 650);
    }

    public void sendAuth(String mode) {
        String user = nameField.getText().trim();
        String pass = passwordField.getText().trim();
        if(user.equals("") || pass.equals("")) {
            loginStatus.setText("Enter both a username and password.");
            return;
        }
        if(user.contains(",") || pass.contains(",")) {
            loginStatus.setText("Usernames and passwords cannot contain commas.");
            return;
        }
        if(mode.equals("create")) {
            clientConnection.createAccount(user, pass);
        }
        else {
            clientConnection.loginAccount(user, pass);
        }
    }

    public String buttonStyle(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: #f8f2e6; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 18 10 18;";
    }

    public String fieldStyle() {
        return "-fx-font-size: 15px; -fx-background-color: #f7f1e6; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 10 12 10 12;";
    }

    public String cardStyle() {
        return "-fx-background-color: rgba(28, 22, 18, 0.94); -fx-background-radius: 18; -fx-border-color: #c8a76d; -fx-border-width: 2; -fx-border-radius: 18;";
    }

    public String rootStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #23140e, #4d2d1d, #1f120d);";
    }

    public void updateTurnLabel() {
        String text = "Turn: " + capitalize(currentTurn);
        if(currentTurn.equals(myColor)) {
            text += " (your turn)";
        }
        turnLabel.setText(text);
    }

    public String capitalize(String text) {
        if(text == null || text.equals("")) {
            return "";
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public void drawBoard() {
        boardPane.getChildren().clear();
        for(int row = 0; row < 8; row++) {
            for(int col = 0; col < 8; col++) {
                StackPane cell = createCell(row, col);
                boardPane.add(cell, col, row);
            }
        }
    }

    public StackPane createCell(int row, int col) {
        StackPane cell = new StackPane();
        Rectangle square = new Rectangle(68, 68);
        if((row + col) % 2 == 0) {
            square.setFill(Color.web("#ecd8b2"));
        }
        else {
            square.setFill(Color.web("#6f4327"));
        }
        cell.getChildren().add(square);
        if(row == lastFromRow && col == lastFromCol) {
            Rectangle mark = new Rectangle(62, 62);
            mark.setFill(Color.TRANSPARENT);
            mark.setStroke(Color.web("#8fd3ff"));
            mark.setStrokeWidth(3);
            cell.getChildren().add(mark);
        }
        if(row == lastToRow && col == lastToCol) {
            Rectangle mark = new Rectangle(62, 62);
            mark.setFill(Color.TRANSPARENT);
            mark.setStroke(Color.web("#ffcf5a"));
            mark.setStrokeWidth(3);
            cell.getChildren().add(mark);
        }
        if(isLegalMoveSquare(row, col)) {
            Rectangle mark = new Rectangle(58, 58);
            mark.setFill(Color.TRANSPARENT);
            mark.setStroke(Color.web("#58d68d"));
            mark.setStrokeWidth(3);
            cell.getChildren().add(mark);
        }
        if(row == selectedRow && col == selectedCol) {
            Rectangle mark = new Rectangle(64, 64);
            mark.setFill(Color.TRANSPARENT);
            mark.setStroke(Color.web("#ffd36b"));
            mark.setStrokeWidth(4);
            cell.getChildren().add(mark);
        }
        if(boardData.size() == 8) {
            char ch = boardData.get(row).charAt(col);
            if(ch != '.') {
                Circle pieceCircle = new Circle(22);
                if(ch == 'b' || ch == 'B') {
                    pieceCircle.setFill(Color.web("#121212"));
                }
                else {
                    pieceCircle.setFill(Color.web("#b8382c"));
                }
                pieceCircle.setStroke(Color.web("#f7f1e6"));
                pieceCircle.setStrokeWidth(3);
                cell.getChildren().add(pieceCircle);
                if(ch == 'B' || ch == 'R') {
                    Label kingLabel = new Label("K");
                    kingLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffd36b;");
                    cell.getChildren().add(kingLabel);
                }
            }
        }
        cell.setOnMouseClicked(e -> {
            handleBoardClick(row, col);
        });
        return cell;
    }

    public void handleBoardClick(int row, int col) {
        if(gameOver) {
            return;
        }
        if(!currentTurn.equals(myColor)) {
            gameStatusLabel.setText("Wait for your turn.");
            return;
        }
        if(boardData.size() != 8) {
            return;
        }
        char ch = boardData.get(row).charAt(col);
        if(selectedRow == -1) {
            if(isMyPiece(ch)) {
                selectedRow = row;
                selectedCol = col;
                refreshLegalMoves();
                if(forcedRow != -1 && forcedCol != -1 && (selectedRow != forcedRow || selectedCol != forcedCol)) {
                    gameStatusLabel.setText("You must continue the jump with the same piece.");
                }
                else if(legalMoves.size() == 0) {
                    gameStatusLabel.setText("That piece has no legal moves.");
                }
                else if(forcedRow != -1 || forcedCol != -1) {
                    gameStatusLabel.setText("You must make a jump.");
                }
                drawBoard();
            }
            return;
        }
        if(isMyPiece(ch)) {
            selectedRow = row;
            selectedCol = col;
            refreshLegalMoves();
            if(forcedRow != -1 && forcedCol != -1 && (selectedRow != forcedRow || selectedCol != forcedCol)) {
                gameStatusLabel.setText("You must continue the jump with the same piece.");
            }
            else if(legalMoves.size() == 0) {
                gameStatusLabel.setText("That piece has no legal moves.");
            }
            else if(forcedRow != -1 || forcedCol != -1) {
                gameStatusLabel.setText("You must make a jump.");
            }
            drawBoard();
            return;
        }
        if(isLegalMoveSquare(row, col)) {
            clientConnection.sendMove(currentGameId, selectedRow, selectedCol, row, col);
        }
        else {
            gameStatusLabel.setText("That is not a legal move.");
        }
    }

    public void refreshLegalMoves() {
        legalMoves.clear();
        GameState state = new GameState();
        state.loadBoardData(boardData);
        state.setCurrentPlayer(currentTurn);
        state.setForced(forcedRow, forcedCol);
        ArrayList<Move> moves = state.getValidMovesForPiece(selectedRow, selectedCol, myColor);
        for(int i = 0; i < moves.size(); i++) {
            legalMoves.add(moves.get(i).getToRow() + "," + moves.get(i).getToCol());
        }
    }

    public boolean isLegalMoveSquare(int row, int col) {
        return legalMoves.contains(row + "," + col);
    }

    public String extractName(String text) {
        int spot = text.indexOf(" (");
        if(spot == -1) {
            return text.trim();
        }
        return text.substring(0, spot).trim();
    }

    public boolean isMyPiece(char ch) {
        if(myColor.equals("black")) {
            return ch == 'b' || ch == 'B';
        }
        return ch == 'r' || ch == 'R';
    }
}
