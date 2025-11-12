/* ECE422C Mastermind Multiplayer Lab
 * GameSession
 * 
 * This class represents a single game instance with multiple players.
 * It manages turn-taking, guess evaluation, and game state.
 * 
 * LEARNING OBJECTIVES:
 * - Concurrent access control with locks
 * - Game state management
 * - Turn-based logic implementation
 * 
 * ESTIMATED TIME: 2-3 hours
 */

import java.util.*;
import java.util.concurrent.locks.*;

public class GameSession {
    private final String gameId;
    private final String gameName;
    private final int requiredPlayers;
    private final GameLobbyManager lobby;
    private final ReentrantLock lock = new ReentrantLock();

    private final Map<String, ClientHandler> players = new LinkedHashMap<>();
    private final Map<String, Integer> guessCount = new HashMap<>();
    private final List<String> turnOrder = new ArrayList<>();
    private final Map<String, String> playerNames = new HashMap<>();

    private GameState gameState;
    private String secretCode;
    private int currentTurnIndex = 0;
    private String status = "Waiting";
    private boolean started = false;
    private boolean hasWinner = false;
    private boolean isFinished = false;

    public GameSession(String gameId, String gameName, int requiredPlayers, GameLobbyManager lobby) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.requiredPlayers = requiredPlayers;
        this.lobby = lobby;
    }

    /**
     * TODO 1: Add Player to Game (20 minutes)
     * 
     * Adds a player to this game session if there's room and game hasn't started.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Check if game is full (players.size() >= requiredPlayers) or already
     * started
     * 3. If full or started, release lock and return false
     * 4. Add player to players map: players.put(playerId, handler)
     * 5. Initialize guess count: guessCount.put(playerId, 0)
     * 6. Add to turn order: turnOrder.add(playerId)
     * 7. Store player name: playerNames.put(playerId, handler.getPlayerName())
     * 8. Broadcast updated game list to lobby
     * 9. Release lock and return true
     * 
     * @param playerId The unique ID of the player
     * @param handler  The ClientHandler for this player
     * @return true if player was added successfully, false otherwise
     */
    public boolean addPlayer(String playerId, ClientHandler handler) {
        lock.lock();
        // Try and finally to ensure Lock stays locked
        try {
            if (players.containsKey(playerId)) {
                return false;
            }
            if (started || players.size() >= requiredPlayers) {
                return false;
            }
            players.put(playerId, handler);
            guessCount.put(playerId, 0);
            turnOrder.add(playerId);
            playerNames.put(playerId, handler.getPlayerName());
            lobby.broadcastToLobby("GAME_LIST:" + lobby.getGameListJson());
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 2: Remove Player from Game (15 minutes)
     * 
     * Removes a player from the game session.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Remove from all data structures (players, guessCount, turnOrder,
     * playerNames)
     * 3. If game is now empty, call lobby.removeSession(gameId)
     * 4. Otherwise, if game has started and currentTurnIndex is out of bounds,
     * reset to 0
     * 5. Broadcast updated game list
     * 6. Release the lock
     * 
     * @param playerId The ID of the player to remove
     */
    public void removePlayer(String playerId) {
        lock.lock();
        // Try block for multithreading
        try {
            players.remove(playerId);
            guessCount.remove(playerId);
            turnOrder.remove(playerId);
            playerNames.remove(playerId);
            if (players.size() <= 0) {
                lobby.removeSession(gameId);
            } else if (started && currentTurnIndex >= turnOrder.size()) {
                currentTurnIndex = 0;
            }
            lobby.broadcastToLobby("GAME_LIST:" + lobby.getGameListJson());
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 3: Check if Game Can Start (10 minutes)
     * 
     * Determines if the game has enough players to start.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Return true if players.size() equals requiredPlayers AND game hasn't
     * started
     * 3. Release the lock
     * 
     * @return true if game can start, false otherwise
     */
    public boolean canStart() {
        lock.lock();
        // Try block for multithreading
        try {
            if (players.size() == requiredPlayers && !started) {
                return true;
            } else {
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 4: Start the Game (20 minutes)
     * 
     * Initializes the game and notifies all players.
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Check if already started, if so return early
     * 3. Set started = true and status = "In Progress"
     * 4. Generate secret code: SecretCodeGenerator.getInstance().getNewSecretCode()
     * 5. Create GameState: new GameState(secretCode)
     * 6. If turnOrder is not empty:
     * a. Get first player: turnOrder.get(0)
     * b. Broadcast "GAME_STARTED:gameId:firstPlayer" to all players
     * c. Broadcast "TURN_UPDATE:gameId:firstPlayer" to all players
     * 7. Update lobby's game list
     * 8. Release the lock
     */
    public void startGame() {
        lock.lock();
        // Try block for multithreading
        try {
            if (started) {
                return;
            }
            started = true;
            status = "In Progress";
            secretCode = SecretCodeGenerator.getInstance().getNewSecretCode();
            gameState = new GameState(secretCode);
            if (!turnOrder.isEmpty()) {
                String player1 = turnOrder.get(0);
                broadcast("GAME_STARTED:" + gameId + ":" + player1, null);
                broadcast("TURN_UPDATE:" + gameId + ":" + player1, null);
            }
            lobby.broadcastToLobby("GAME_LIST:" + lobby.getGameListJson());
        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 5: Process Player Guess (45 minutes)
     * 
     * Evaluates a player's guess and updates game state.
     * 
     * This is the most complex method - handle it carefully!
     * 
     * Steps:
     * 1. Acquire the lock
     * 2. Check if game has started, if not send "ERROR:Game not started" and return
     * 3. Get current player from turnOrder.get(currentTurnIndex)
     * 4. Verify it's this player's turn, if not send "ERROR:Not your turn" and
     * return
     * 5. Validate the guess using isValidGuess(), if invalid send error and return
     * 6. Evaluate guess: gameState.evaluateGuess(guess) returns int[]{black, white}
     * 7. Increment and store guess count for this player
     * 8. Broadcast result: "GUESS_RESULT:gameId:playerName:guessNum:black:white"
     * 9. Check if player won (black pegs == GameConfiguration.pegNumber):
     * a. Set status = "Finished"
     * b. Broadcast "GAME_WON:gameId:winnerName:guessCount"
     * c. Broadcast "GAME_OVER:gameId:secretCode"
     * d. Release lock and return
     * 10. Check if player is out of guesses (>= GameConfiguration.guessNumber):
     * a. Advance to next turn
     * b. Check if all players are out of guesses
     * c. If yes, set status = "Finished" and broadcast
     * "GAME_OVER:gameId:secretCode"
     * 11. Otherwise, just advance to next turn
     * 12. Release the lock
     * 
     * @param playerId The ID of the player making the guess
     * @param guess    The guess string (e.g., "BGRP")
     */
    public void processGuess(String playerId, String guess) {
        // TODO: Check if game has started

        // TODO: Verify it's this player's turn

        // TODO: Validate the guess

        // TODO: Evaluate the guess using GameState

        // TODO: Update guess count

        // TODO: Broadcast guess result

        // TODO: Check for win condition

        // TODO: Check if player is out of guesses

        // TODO: Advance turn or end game
        lock.lock();
        // try block for multithreading
        try {
            ClientHandler handle = players.get(playerId);
            if (started == false) {
                handle.sendMessage("ERROR:Game not started");
                return;
            }
            if (turnOrder.isEmpty() || players.containsKey(playerId) == false) {
                return;
            }

            String cPlayer = turnOrder.get(currentTurnIndex);
            if (!playerId.equals(cPlayer)) {
                if (handle != null) {
                    handle.sendMessage("ERROR:Not your turn");
                    return;
                }
            }

            if (!isValidGuess(guess)) {
                if (handle != null) {
                    handle.sendMessage("ERROR:Not valid guess");
                }
                return;
            }

            int[] evaluations = gameState.evaluateGuess(guess);
            int white = evaluations[1];
            int black = evaluations[0];
            int gCount = guessCount.get(playerId);
            gCount++;
            guessCount.put(playerId, gCount);

            String name = playerNames.get(playerId);
            if (name == null || name.isEmpty()) {
                name = "Player";
            }

            broadcast("GUESS_RESULT:" + gameId + ":" + name + ":" + gCount + ":" + black + ":" + white, null);

            //gCount and number of pegs determines the next action
            if (black == GameConfiguration.pegNumber) {
                hasWinner = true;
                isFinished = true;
                status = "Finished";
                broadcast("GAME_WON:" + gameId + ":" + name + ":" + gCount + ":" + secretCode, null);
                //broadcast("GAME_OVER:" + gameId + ":" + secretCode, null);
                return;
            }

            else if (gCount >= GameConfiguration.guessNumber) {
                advanceTurn();
                boolean checkGuess = true;
                for (String Id : turnOrder) {
                    int numGuesses = guessCount.get(Id);
                    if (numGuesses < GameConfiguration.guessNumber && players.containsKey(Id)) {
                        checkGuess = false;
                    }
                }
                if (checkGuess == true) {
                    status = "Finished";
                    isFinished = true;
                    broadcast("GAME_OVER:" + gameId + ":" + secretCode, null);
                    return;
                }
            } else {
                advanceTurn();
            }

        } finally {
            lock.unlock();
        }
    }

    /**
     * TODO 6: Advance Turn (15 minutes)
     * 
     * Moves to the next player's turn.
     * 
     * Steps:
     * 1. Increment currentTurnIndex using modulo: (currentTurnIndex + 1) %
     * turnOrder.size()
     * 2. Get the next player ID from turnOrder
     * 3. Broadcast "TURN_UPDATE:gameId:nextPlayerId" to all players
     * 
     * Note: This method should be called while holding the lock
     */
    private void advanceTurn() {
        if (turnOrder.isEmpty()) { //empty check
            return; 
        }
        for (int i = 0; i < turnOrder.size(); i++) {
            currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
            String nextPlayer = turnOrder.get(currentTurnIndex);
            int gcount = guessCount.get(nextPlayer);
            if (gcount < GameConfiguration.guessNumber && players.containsKey(nextPlayer)) {
                broadcast("TURN_UPDATE:" + gameId + ":" + nextPlayer, null);
                return;
            }
        }
    }

    /**
     * TODO 7: Validate Guess Format (20 minutes)
     * 
     * Checks if a guess string is valid.
     * 
     * A valid guess must:
     * - Not be null
     * - Have length equal to GameConfiguration.pegNumber
     * - Contain only valid color codes from GameConfiguration.colors
     * 
     * Steps:
     * 1. Check if guess is null or wrong length
     * 2. For each character in the guess:
     * a. Check if it matches any color in GameConfiguration.colors
     * b. If no match found, return false
     * 3. If all checks pass, return true
     * 
     * @param guess The guess string to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidGuess(String guess) {
        if (guess == null || guess.length() != GameConfiguration.pegNumber) { //null check
            return false;
        }
        for (int i = 0; i < guess.length(); i++) {
            char singleGuess = guess.charAt(i);
            boolean there = false;
            for (int j = 0; j < GameConfiguration.colors.length; j++) {
                if (GameConfiguration.colors[j].charAt(0) == singleGuess) {
                    there = true;
                    break;
                }
            }
            if (there == false) {
                return false;
            }
        }

        return true;
    }

    /**
     * TODO 8: Broadcast Message (10 minutes)
     * 
     * Sends a message to all players in the game, optionally excluding one.
     * 
     * Steps:
     * 1. Iterate through all entries in the players map
     * 2. For each entry, check if we should exclude this player
     * 3. If not excluded, call handler.sendMessage(message)
     * 
     * @param message         The message to broadcast
     * @param excludePlayerId Player ID to exclude, or null to send to all
     */
    // AI Helped with for loop logic
    public void broadcast(String message, String excludePlayerId) {
        for (Map.Entry<String, ClientHandler> player : players.entrySet()) {
            String id = player.getKey();
            if (excludePlayerId == null || !id.equals(excludePlayerId)) {
                player.getValue().sendMessage(message);
            }
        }
    }

    // Getters - already implemented
    /**
     * Returns how to identify the game
     * 
     * @return the gameID
     */
    public String getGameId() {
        return gameId;
    }

    /**
     * Returns the game's display name
     * 
     * @return gameName
     */
    public String getGameName() {
        return gameName;
    }

    /**
     * Give's amount of players needed to start the game
     * 
     * @return requiredPlayers
     */
    public int getMaxPlayers() {
        return requiredPlayers;
    }

    /**
     * Number of Connected Players
     * 
     * @return players list size
     */
    public int getPlayerCount() {
        return players.size();
    }

    /**
     * Returns what state the game is in
     * 
     * @return status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Gets names from the list already given
     * 
     * @return List of player names
     */
    public List<String> getPlayerNames() {
        List<String> names = new ArrayList<>();
        for (ClientHandler handler : players.values()) {
            names.add(handler.getPlayerName());
        }
        return names;
    }

    /**
     * Gets player ids
     * 
     * @return set of player ids
     */
    public Set<String> getPlayerIds() {
        return new HashSet<>(players.keySet());
    }

    /**
     * Indicated if game is finsihed or not
     * 
     * @return true if game is finshed false otherwise
     */
    public boolean getFinished() {
        return isFinished;
    }

    /**
     * Says if its a win or draw
     * 
     * @return true if theres a winner and false otherwise
     */
    public boolean getWinner() {
        return hasWinner;
    }

    /**
     * Can tell if the Game session has no players
     * 
     * @return true if players is empty and false otherwise
     */
    public boolean isSeshEmpty() {
        return players.isEmpty();
    }
}
