# Checkers-Multiplayer-Game

## Overview

Royal Checkers is a desktop checkers game built in Java. The goal was to let players play a full match against a friend or practice against a computer opponent. Players can create an account, invite friends, chat during a multiplayer match, and review their match history afterward.

## Demo

[Watch the Royal Checkers walkthrough (6:19)](https://drive.google.com/file/d/1agDlNt5ZfeqYFcKbcHbivxI9BzGUYyXE/view?usp=sharing)

The walkthrough shows the game lobby, a multiplayer match, and play against the computer.

![Royal Checkers board and in-game chat during a match](docs/images/checkers-gameplay.png)

*The game screen shows the board, whose turn it is, and the match chat alongside the board.*

## How It Works

The project is split into two Java applications. The JavaFX client displays the board, lobby, and chat. The server uses sockets to receive player actions and send updates back to the clients. It handles client connections on separate threads so two players can stay connected and play in the same match.

The computer opponent uses minimax to compare possible moves and replies before choosing its move. The server saves player statistics and match history so players can review earlier games after logging back in.

## Project Outcome

The finished game supports two ways to play. In **Play vs Player**, two players can join a match and exchange messages while they play. In **Play vs Computer**, a single player can start a match without waiting for someone else. The project brought together a graphical interface, multiplayer networking, and computer-controlled gameplay in one application.
