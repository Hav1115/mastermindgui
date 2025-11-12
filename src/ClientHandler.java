
/* ECE422C Mastermind Multiplayer Lab
 * ClientHandler
 * 
 * This class handles communication with a single connected client.
 * It runs in its own thread and processes messages from the client.
 * 
 * LEARNING OBJECTIVES:
 * - Thread-based network communication
 * - Protocol design and message parsing
 * - Resource cleanup and error handling
 * 
 * ESTIMATED TIME: 3-4 hours
 */
import java.util.List;
import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameLobbyManager lobby;
    private PrintWriter out;
    private BufferedReader in;
    private String playerId;
    private String playerName;
    private volatile boolean running = true;

    public ClientHandler(Socket socket, GameLobbyManager lobby) {
        this.socket = socket;
        this.lobby = lobby;
    }

    @Override
    public void run() {
        try {
            setupStreams();
            handleClientMessages();
        } catch (IOException e) {
            System.err.println("Client handler error: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     * TODO 1: Setup Input/Output Streams (15 minutes)
     * 
     * Initialize the input and output streams for this client connection.
     * 
     * Steps:
     * 1. Create a PrintWriter from socket.getOutputStream() with auto-flush enabled
     * 2. Create a BufferedReader from socket.getInputStream() wrapped in
     * InputStreamReader
     * 
     * Hint: Use the socket object that was passed in the constructor
     */
    private void setupStreams() throws IOException {
        // Initialized 'out' as a PrintWriter with auto-flush
        out = new PrintWriter(socket.getOutputStream(), true);
        // Initialized 'in' as a BufferedReader
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    /**
     * TODO 2: Message Processing Loop (30 minutes)
     * 
     * Create the main message processing loop that:
     * 1. Continuously reads messages from the client
     * 2. Parses each message to extract the command and data
     * 3. Routes each message to the appropriate handler method
     * 
     * Message format: "COMMAND:data"
     * Examples: "CONNECT:PlayerName", "GUESS:gameId:BGRP"
     * 
     * Steps:
     * 1. Loop while 'running' is true
     * 2. Read a line from 'in' (use readLine())
     * 3. If line is null, break the loop (client disconnected)
     * 4. Split the message on the first ':' to get command and data
     * 5. Use a switch statement to route to handler methods
     * 
     * Commands to handle:
     * - HELLO: handleHello(data)
     * - CONNECT: handleConnect(data)
     * - GET_GAMES: handleGetGames()
     * - CREATE_GAME: handleCreateGame(data)
     * - JOIN_GAME: handleJoinGame(data)
     * - LEAVE_GAME: handleLeaveGame(data)
     * - GUESS: handleGuess(data)
     * - CHAT: handleChat(data)
     * - DISCONNECT: break the loop
     */
    private void handleClientMessages() throws IOException {
        while (running) {
            String str = in.readLine();
            if (str == null) {
                break;
            }
            String command;
            String data;
            if (str.indexOf(':') == -1) {
                command = str;
                data = "";
            } else {
                command = str.substring(0, str.indexOf(':'));
                data = str.substring(str.indexOf(':') + 1);
            }
            //If and Else statements so only one runs
            if ("HELLO".equals(command)) {
                handleHello(data);
            } else if ("CONNECT".equals(command)) {
                handleConnect(data);
            } else if ("GET_GAMES".equals(command)) {
                handleGetGames();
            } else if ("CREATE_GAME".equals(command)) {
                handleCreateGame(data);
            } else if ("JOIN_GAME".equals(command)) {
                handleJoinGame(data);
            } else if ("LEAVE_GAME".equals(command)) {
                handleLeaveGame(data);
            } else if ("GUESS".equals(command)) {
                handleGuess(data);
            } else if ("CHAT".equals(command)) {
                handleChat(data);
            } else if ("DISCONNECT".equals(command)) {
                running = false;
            }
        }

    }

    /**
     * TODO 3: Connection Protocol Handler (20 minutes)
     * 
     * Handle the initial connection handshake.
     * 
     * Steps:
     * 1. Generate a unique player ID (use "p" + System.currentTimeMillis())
     * 2. Store the player name from the data parameter
     * 3. Register this client with the lobby manager
     * 4. Send a "CONNECTED:playerId" message back to the client
     * 
     * @param playerName The name provided by the connecting client
     */
    private void handleConnect(String playerName) {
        // Generates unique player ID, to then add to lobby with given name
        if (playerName == null) {
            playerName = "Player";
        }
        //Use this.variable to ensure that the proper thing is being changed
        this.playerId = "p" + System.currentTimeMillis();
        this.playerName = playerName;
        lobby.addPlayer(playerId, playerName, this);
        sendMessage("CONNECTED:" + playerId);

    }

    /**
     * TODO 4: Game List Handler (15 minutes)
     * 
     * Send the current list of available games to the client.
     * 
     * Steps:
     * 1. Get the game list JSON from the lobby manager
     * 2. Send it to the client with format "GAME_LIST:jsonData"
     */
    private void handleGetGames() {
        // Getting game list to send to lobby
        String list = lobby.getGameListJson();
        sendMessage("GAME_LIST:" + list);
    }

    /**
     * TODO 5: Create Game Handler (25 minutes)
     * 
     * Handle a request to create a new game.
     * 
     * Data format: "gameName:requiredPlayers"
     * Example: "MyGame:2"
     * 
     * Steps:
     * 1. Split the data by ':' to extract gameName and requiredPlayers
     * 2. Parse requiredPlayers as an integer
     * 3. Call lobby.createGame() with the parsed parameters
     * 4. Send "GAME_CREATED:gameId" back to the client
     * 5. If any error occurs, send "ERROR:Failed to create game"
     */
    private void handleCreateGame(String data) {
        // TODO: Parse game name and required players

        // TODO: Create game through lobby manager

        // TODO: Send response to client

        //Try and Catch to ensure program doesn't crash
        try {
            int index = data.indexOf(":");
            if (index == -1) {
                sendMessage("ERROR:Failed to create game");
                return;
            }
            String name = data.substring(0, index).trim(); //Trim so there's no blank space
            String nump = data.substring(index + 1).trim(); //Trim so there's no blank space

            if (name.isEmpty() || nump.isEmpty()) {
                sendMessage("ERROR:Failed to create game");
                return;
            }

            int numPlayers = Integer.parseInt(nump);
            String gID = lobby.createGame(name, numPlayers, playerId);
            if (gID == null) {
                sendMessage("ERROR:Failed to create game");
                return;
            }

            sendMessage("GAME_CREATED:" + gID);
        } catch (Exception e) {
            sendMessage("ERROR:Failed to create game");
        }

    }

    /**
     * TODO 6: Join Game Handler (30 minutes)
     * 
     * Handle a request to join an existing game.
     * 
     * Data format: "gameId"
     * 
     * Steps:
     * 1. Call lobby.joinGame() with the gameId and this player's ID
     * 2. If successful:
     * a. Get the GameSession from the lobby
     * b. Get the list of player names in the game
     * c. Send "GAME_JOINED:gameId:playersList" to this client
     * d. Broadcast "PLAYER_JOINED:gameId:playerName" to other players
     * e. Check if the game can start (session.canStart())
     * f. If yes, call session.startGame()
     * 3. If join fails, send "ERROR:Failed to join game"
     */
    private void handleJoinGame(String gameId) {
        // TODO: Attempt to join the game

        // TODO: Send appropriate response

        // TODO: Check if game should start

      
        if (gameId == null) {   //Null check just in case
            sendMessage("ERROR:Failed to join game");
            return;
        }
        gameId = gameId.trim(); //Trim so there's no blank space
        if (gameId.isEmpty()) {
            sendMessage("ERROR:Failed to join game");
            return;
        }

        boolean check = lobby.joinGame(gameId, playerId);
        if (check == false) {
            sendMessage("ERROR:Failed to join game");
            return;
        }

        GameSession sesh = lobby.getSession(gameId);
        if (sesh == null) {
            sendMessage("ERROR:Failed to join game");
            return;
        }

        List<String> names = sesh.getPlayerNames();
        sendMessage("GAME_JOINED:" + gameId + ":" + names);
        sesh.broadcast("PLAYER_JOINED:" + gameId + ":" + playerName, null);
        if (sesh.canStart()) {
            sesh.startGame();
        }

    }

    /**
     * TODO 7: Leave Game Handler (20 minutes)
     * 
     * Handle a request to leave a game.
     * 
     * Steps:
     * 1. Get the GameSession from the lobby
     * 2. If session exists, call session.removePlayer(playerId)
     * 3. Broadcast "PLAYER_LEFT:gameId:playerName" to remaining players
     */
    private void handleLeaveGame(String gameId) {
        // TODO: Remove player from game session

        // TODO: Notify other players
        if (gameId == null) {
            return;
        }
        gameId = gameId.trim(); //Trim so there's no blank space
        if (gameId == null || gameId.isEmpty()) {
            return;
        }
        GameSession sesh = lobby.getSession(gameId);
        if (sesh == null) {
            return;
        }
        sesh.removePlayer(playerId);
        sesh.broadcast("PLAYER_LEFT:" + gameId + ":" + playerName, null);
    }

    /**
     * TODO 8: Guess Handler (15 minutes)
     * 
     * Handle a guess submission from the client.
     * 
     * Data format: "gameId:guess"
     * Example: "g12345678:BGRP"
     * 
     * Steps:
     * 1. Split the data to extract gameId and guess
     * 2. Get the GameSession from the lobby
     * 3. If session exists, call session.processGuess(playerId, guess)
     */
    private void handleGuess(String data) {
        if (data == null) {
            return;
        }
        int index = data.indexOf(':');
        if (index == -1) {
            return;
        }
        String gID = data.substring(0, index).trim(); //Trim so there's no blank space
        String guess = data.substring(index + 1).trim(); //Trim so there's no blank space
        if (gID.isEmpty() || guess.isEmpty()) {
            return;
        }

        GameSession sesh = lobby.getSession(gID);
        if (sesh == null) {
            return;
        }
        sesh.processGuess(playerId, guess);
    }

    /**
     * TODO 9: Chat Handler (20 minutes)
     * 
     * Handle a chat message from the client.
     * 
     * Data format: "gameId:message"
     * 
     * Steps:
     * 1. Split the data to extract gameId and message
     * 2. Get the GameSession from the lobby
     * 3. If session exists, broadcast "CHAT_MESSAGE:gameId:playerName:message" to
     * all players
     */
    private void handleChat(String data) {
        // TODO: Parse gameId and message

        // TODO: Broadcast chat message to all players in the game
        if (data == null) {
            return;
        }
        int index = data.indexOf(':');
        if (index == -1) {
            return;
        }
        String gID = data.substring(0, index).trim(); //Trim so there's no blank space
        String message = data.substring(index + 1).trim(); //Trim so there's no blank space
        if (gID.isEmpty() || message.isEmpty()) {
            return;
        }

        GameSession sesh = lobby.getSession(gID);
        if (sesh == null) { // Null Check Just in Case
            return;
        }
        sesh.broadcast("CHAT_MESSAGE:" + gID + ":" + playerName + ":" + message, null);
    }

    /**
     * Handles the initial HELLO message (version check)
     */
    private void handleHello(String version) {
        sendMessage("HELLO:" + version);
    }

    /**
     * TODO 10: Resource Cleanup (20 minutes)
     * 
     * Clean up resources when the client disconnects.
     * 
     * Steps:
     * 1. Set running to false
     * 2. Remove this player from the lobby manager
     * 3. Close the input stream (in)
     * 4. Close the output stream (out)
     * 5. Close the socket
     * 6. Wrap each close operation in a try-catch to handle potential IOExceptions
     */
    private void cleanup() {
        // TODO: Stop the running loop

        // TODO: Remove player from lobby

        // TODO: Close all streams and socket

        running = false;
        if (playerId != null) { 
            lobby.removePlayer(playerId);

        }

        try { //Have to try catch on IO stream just to ensure the program doesn't crash on exit
            if (in != null) {
                in.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception ignored) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        System.out.println("Client disconnected: " + playerName);
    }

    /**
     * Sends a message to this client
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

     /**
     * Gets the Player ID
     * 
     * @return playerId
     */
    public String getPlayerId() {
        return playerId;
    }

     /**
     * Gets the Player Name
     * 
     * @return playerName
     */
    public String getPlayerName() {
        return playerName;
    }
}
