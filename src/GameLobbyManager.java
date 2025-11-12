/* ECE422C Mastermind Multiplayer Lab
 * GameLobbyManager
 * 
 * This class manages all active games and connected players.
 * It serves as the central coordination point for the server.
 * 
 * LEARNING OBJECTIVES:
 * - Thread-safe data structures (ConcurrentHashMap)
 * - Central coordination logic
 * - JSON generation
 * 
 * ESTIMATED TIME: 2-3 hours
 */

import java.util.*;
import java.util.concurrent.*;

public class GameLobbyManager {
    private final ConcurrentHashMap<String, ClientHandler> players = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> playerToGame = new ConcurrentHashMap<>();

    /**
     * TODO 1: Add Player to Lobby (10 minutes)
     * 
     * Registers a new player in the lobby.
     * 
     * Steps:
     * 1. Add the player to the players map with playerId as key
     * 
     * @param playerId The unique player ID
     * @param playerName The player's display name
     * @param handler The ClientHandler for this player
     */
    public void addPlayer(String playerId, String playerName, ClientHandler handler) {
        players.put(playerId, handler);
        
    }

    /**
     * TODO 2: Remove Player from Lobby (25 minutes)
     * 
     * Removes a player and handles cleanup.
     * 
     * Steps:
     * 1. Remove player from players map and store the handler
     * 2. If handler was found:
     *    a. Check if player was in a game (playerToGame map)
     *    b. If yes, remove from playerToGame map
     *    c. Get the GameSession they were in
     *    d. If session exists:
     *       - Call session.removePlayer(playerId)
     *       - Broadcast "PLAYER_LEFT:gameId:playerName" to remaining players
     * 
     * @param playerId The ID of the player to remove
     */
    public void removePlayer(String playerId) {
        //Remove fromt players list
        if(playerId == null) {
            return;
        }
        ClientHandler handle = players.get(playerId);
        players.remove(playerId);
        //Check if they play

        String gID = playerToGame.remove(playerId);
        GameSession sesh;
        if(gID != null) {
            sesh = sessions.get(gID);
            if(sesh == null) {
                return;
            }
        }
        else {
            return;
        }
        String name;
        if(handle != null) {
            name = handle.getPlayerName();
        }
        else {
            name = playerId;
        }

        //Remove from session
        sesh.removePlayer(playerId);

        //Broadcast
        sesh.broadcast("PLAYER_LEFT:" + gID + ":" + name, playerId);
    }

    /**
     * TODO 3: Create New Game (20 minutes)
     * 
     * Creates a new game session.
     * 
     * Steps:
     * 1. Generate a unique game ID: "g" + UUID.randomUUID().toString().substring(0, 8)
     * 2. Create a new GameSession with the gameId, gameName, requiredPlayers, and this manager
     * 3. Add the session to the sessions map
     * 4. Return the gameId
     * 
     * Note: The creator is NOT automatically added to the game - they must join manually
     * 
     * @param gameName The name for the game
     * @param requiredPlayers Number of players needed to start
     * @param creatorId The ID of the player creating the game
     * @return The generated game ID
     */
    public String createGame(String gameName, int requiredPlayers, String creatorId) {
        String id = "g" + UUID.randomUUID().toString().substring(0, 8);

        GameSession sesh = new GameSession(id, gameName, requiredPlayers, this);

        sessions.put(id, sesh);

        return id;
    }

    /**
     * TODO 4: Join Game (25 minutes)
     * 
     * Adds a player to an existing game.
     * 
     * Steps:
     * 1. Get the GameSession from sessions map
     * 2. Get the ClientHandler from players map
     * 3. If both exist:
     *    a. Try to add player to session: session.addPlayer(playerId, handler)
     *    b. If successful, add to playerToGame map
     *    c. Return true
     * 4. Otherwise return false
     * 
     * @param gameId The ID of the game to join
     * @param playerId The ID of the player joining
     * @return true if successfully joined, false otherwise
     */
    public boolean joinGame(String gameId, String playerId) {
        GameSession sesh = sessions.get(gameId);
        ClientHandler handle = players.get(playerId);
        
        if(sesh == null) {
            return false;
        }
        if(handle == null) {
            return false;
        }

        boolean add = sesh.addPlayer(playerId, handle);
        if (add == true) {
            playerToGame.put(playerId, gameId);
        }

        return add;
    }
//New Method leaveGame Just leaves session

    public void leaveGame(String playerId, String gameId) {
        if(gameId == null || playerId == null) {
            return;
        }
        GameSession sesh = sessions.get(gameId);
        if(sesh == null) {
            return;
        }
        ClientHandler handle = players.get(playerId);
        String name = "";
        if(handle != null && handle.getPlayerName() != null) {
            name = handle.getPlayerName();
        }
        sesh.removePlayer(playerId);
        playerToGame.remove(playerId);
        sesh.broadcast("PLAYER_LEFT:" + gameId + ":" + name, playerId);
        if(sesh.isSeshEmpty() || sesh.getFinished()) {
            removeSession(gameId);
        }
    }
    /**
     * TODO 5: Get Game Session (5 minutes)
     * 
     * Retrieves a game session by ID.
     * 
     * Steps:
     * 1. Return the session from the sessions map
     * 
     * @param gameId The game ID to look up
     * @return The GameSession, or null if not found
     */
    public GameSession getSession(String gameId) {
       
        return sessions.get(gameId);
    }

    /**
     * TODO 6: Remove Game Session (20 minutes)
     * 
     * Removes a completed or empty game.
     * 
     * Steps:
     * 1. Remove the session from sessions map
     * 2. If session was found:
     *    a. For each player ID in the session, remove from playerToGame map
     *    b. Broadcast updated game list to all players in lobby
     * 
     * @param gameId The ID of the game to remove
     */
    public void removeSession(String gameId) {
        GameSession sesh = sessions.remove(gameId);

        if (sesh == null) {
            return;
        }

        Iterator<Map.Entry<String, String>> it = playerToGame.entrySet().iterator();
        while (it.hasNext()) {
            if (gameId.equals(it.next().getValue())) {
                it.remove();
            }
        }

         broadcastToLobby("GAME_LIST:" + getGameListJson());
        
    }

    /**
     * TODO 7: Generate Game List JSON (45 minutes)
     * 
     * Creates a JSON array of all active games.
     * 
     * JSON format:
     * [
     *   {"id":"g12345","name":"Game1","players":2,"maxPlayers":4,"status":"Waiting"},
     *   {"id":"g67890","name":"Game2","players":4,"maxPlayers":4,"status":"In Progress"}
     * ]
     * 
     * Steps:
     * 1. Create a StringBuilder starting with "["
     * 2. Use a boolean flag to track if this is the first game (for comma placement)
     * 3. For each GameSession in sessions.values():
     *    a. If not first, append a comma
     *    b. Append "{"
     *    c. Append: "\"id\":\"" + session.getGameId() + "\","
     *    d. Append: "\"name\":\"" + session.getGameName() + "\","
     *    e. Append: "\"players\":" + session.getPlayerCount() + ","
     *    f. Append: "\"maxPlayers\":" + session.getMaxPlayers() + ","
     *    g. Append: "\"status\":\"" + session.getStatus() + "\""
     *    h. Append "}"
     *    i. Set first flag to false
     * 4. Append "]" and return the string
     * 
     * @return JSON string representing all games
     */
    public String getGameListJson() {
        // TODO: Build JSON string manually
        // Hint: Use StringBuilder for efficiency
        
        StringBuilder st = new StringBuilder();

        st.append("[");
        
        boolean f = true;

        for(GameSession sesh : sessions.values()) {
            if(!f) {
                st.append(",");
            }
            f = false;
            st.append("{");
            st.append("\"id\":\"").append(sesh.getGameId()).append("\",");
            st.append("\"name\":\"").append(sesh.getGameName()).append("\",");
            st.append("\"players\":").append(sesh.getPlayerCount()).append(",");
            st.append("\"maxPlayers\":").append(sesh.getMaxPlayers()).append(",");
            st.append("\"status\":\"").append(sesh.getStatus()).append("\"");
            st.append("}");
        }
        st.append("]");
        return st.toString();
    }

    /**
     * TODO 8: Broadcast to All Lobby Players (15 minutes)
     * 
     * Sends a message to all connected players.
     * 
     * Steps:
     * 1. Iterate through all ClientHandlers in the players map
     * 2. For each handler, call handler.sendMessage(message)
     * 
     * @param message The message to broadcast
     */
    public void broadcastToLobby(String message) {
        for(ClientHandler handle : players.values()) {
            handle.sendMessage(message);
        }
        
    }

    /**
     * Get a specific player's handler (already implemented)
     */
    public ClientHandler getPlayer(String playerId) {
        return players.get(playerId);
    }
}
