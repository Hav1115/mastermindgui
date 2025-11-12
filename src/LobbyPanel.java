/* ECE422C Mastermind Multiplayer Lab
 * LobbyPanel
 * 
 * This panel displays available games and allows players to create or join them.
 * 
 * LEARNING OBJECTIVES:
 * - JTable for displaying data
 * - JSON parsing (manual)
 * - Dynamic UI updates from network events
 * 
 * ESTIMATED TIME: 2-3 hours
 */

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class LobbyPanel extends JPanel {
    private MastermindClient client;
    private JTable gamesTable;
    private DefaultTableModel tableModel;
    private List<GameInfo> games = new ArrayList<>();

    public LobbyPanel(MastermindClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        setBackground(new Color(44, 62, 80));
        
        initComponents();
        
        // Set this panel as the message handler
        client.setMessageCallback(this::handleServerMessage);
        
        // Request game list when lobby opens
        client.send("GET_GAMES");
    }

    /**
     * GUI setup is provided - study to learn Swing components
     */
    private void initComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Game Lobby");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        String[] columnNames = {"Game Name", "Players", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        gamesTable = new JTable(tableModel);
        gamesTable.setFont(new Font("Arial", Font.PLAIN, 14));
        gamesTable.setRowHeight(30);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(gamesTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        buttonPanel.setOpaque(false);
        
        JButton createGameBtn = new JButton("Create Game");
        JButton joinGameBtn = new JButton("Join Game");
        JButton refreshBtn = new JButton("Refresh");
        JButton backBtn = new JButton("Back to Menu");
        
        joinGameBtn.setEnabled(false);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            joinGameBtn.setEnabled(gamesTable.getSelectedRow() >= 0);
        });
        
        createGameBtn.addActionListener(e -> handleCreateGame());
        joinGameBtn.addActionListener(e -> handleJoinGame());
        refreshBtn.addActionListener(e -> client.send("GET_GAMES"));
        backBtn.addActionListener(e -> MastermindApp.showMainMenu());
        
        buttonPanel.add(createGameBtn);
        buttonPanel.add(joinGameBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(backBtn);
        
        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * TODO 1: Handle Create Game (20 minutes)
     * 
     * Prompts user for game details and creates a new game.
     * 
     * Steps:
     * 1. Show input dialog: JOptionPane.showInputDialog(this, "Enter game name:", "Create Game", JOptionPane.PLAIN_MESSAGE)
     * 2. Check if gameName is not null and not empty (after trim)
     * 3. If valid, create options array: {"2", "3", "4", "5", "6", "7", "8"}
     * 4. Show selection dialog: JOptionPane.showInputDialog(this, "Select required number of players:", 
     *    "Create Game", JOptionPane.PLAIN_MESSAGE, null, options, "2")
     * 5. If requiredPlayers is not null:
     *    - Send message: "CREATE_GAME:" + gameName.trim() + ":" + requiredPlayers
     */
    private void handleCreateGame() {
        // TODO: Prompt for game name
        
        // TODO: Validate game name
        
        // TODO: Prompt for number of players
        
        // TODO: Send CREATE_GAME message
        
        String name = (String)JOptionPane.showInputDialog(this, "Enter game name:", "Create Game", JOptionPane.PLAIN_MESSAGE);
        if(name == null || name.trim().isEmpty()) {
            return;
        }
        name = name.trim();
        String[] options = {"2", "3", "4", "5", "6", "7", "8"};
        String requiredPlayers = (String)JOptionPane.showInputDialog(this,"Select required number of players:", "Create Game", JOptionPane.PLAIN_MESSAGE, null, options, "2");
         if (requiredPlayers != null) {
           client.send("CREATE_GAME:" + name.trim() + ":" + requiredPlayers);
       }
    }

    /**
     * TODO 2: Handle Join Game (20 minutes)
     * 
     * Joins the currently selected game.
     * 
     * Steps:
     * 1. Get selected row: gamesTable.getSelectedRow()
     * 2. Check if row is valid (>= 0 and < games.size())
     * 3. Get the GameInfo from the games list at the selected index
     * 4. Check if game status is "Waiting":
     *    - If yes, send message: "JOIN_GAME:" + game.id
     *    - If no, show error dialog: "Cannot join game in progress"
     */
    private void handleJoinGame() {
        // TODO: Get selected row
        
        // TODO: Validate selection
        
        // TODO: Check game status
        
        // TODO: Send JOIN_GAME message
        int selectedRow = gamesTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < games.size()) {
            GameInfo game = games.get(selectedRow);
            if (game.status.equals("Waiting")) {
                client.send("JOIN_GAME:" + game.id);
            } 
            else {
                    JOptionPane.showMessageDialog(this, "Cannot join game in progress");
                }
        }
     }

    /**
     * TODO 3: Handle Server Messages (45 minutes)
     * 
     * Processes all messages from the server while in the lobby.
     * 
     * Important: Don't process messages if client is shutting down!
     * 
     * Steps:
     * 1. Check if client is not null and is connected, if not return early
     * 2. Find first ':' in message and split into command and data
     * 3. Use switch statement to handle each message type:
     * 
     * Case "GAME_LIST":
     *   - If data is not empty, call parseGameList(data)
     * 
     * Case "GAME_CREATED":
     *   - If data is not empty, send "GET_GAMES" to refresh the list
     * 
     * Case "GAME_JOINED":
     *   - Split data by ":" to get gameId (first part)
     *   - Navigate to game board: MastermindApp.showGameBoard(gameId)
     * 
     * Case "GAME_STARTED":
     *   - Split data by ":" to get gameId (first part)
     *   - Navigate to game board: MastermindApp.showGameBoard(gameId)
     * 
     * Case "ERROR":
     *   - Print error to System.err
     *   - Show error dialog with the data
     * 
     * @param message The message from the server
     */
    private void handleServerMessage(String message) {
        // TODO: Check if client is still connected
        
        // TODO: Parse message into command and data
        
        // TODO: Handle each message type with switch statement
        if(client == null || client.isConnected() == false || message == null) {
            return;
        }

        int index = message.indexOf(':');
        if(index == -1) {
            return;
        }
        String command = message.substring(0, index).trim();
        String data = message.substring(index + 1);

        switch(command) {
            case "GAME_LIST": {
                if (!data.isEmpty()) {
                parseGameList(data);
                }
                break;
            }
            case "GAME_CREATED": {
                if (!data.isEmpty()) {
                client.send("GET_GAMES");
                }
                break;
            }

            case "GAME_JOINED": {
                int index2 = data.indexOf(':');
                String gameId = data.substring(0, index2);
                MastermindApp.showGameBoard(gameId);
                break;
            }

            case "GAME_STARTED": {
                int index2 = data.indexOf(':');
                String gameId = data.substring(0, index2);
                MastermindApp.showGameBoard(gameId);
                break;
            }

            case "ERROR": {
                System.err.println("Lobby error: " + data);
                JOptionPane.showMessageDialog(this, data,"Server Error",JOptionPane.ERROR_MESSAGE);
                break;
            }
            default:
                break;
        }
    }

    /**
     * TODO 4: Parse Game List JSON (60 minutes)
     * 
     * Parses a JSON array of games and updates the table.
     * 
     * This is manual JSON parsing - a great learning exercise!
     * 
     * JSON format: [{"id":"g123","name":"Game1","players":2,"maxPlayers":4,"status":"Waiting"},...]
     * 
     * Steps:
     * 1. Clear the games list and table: games.clear() and tableModel.setRowCount(0)
     * 2. Trim the JSON string
     * 3. Check if it's an empty array "[]", if so return early
     * 4. Remove outer brackets: json = json.substring(1, json.length() - 1)
     * 5. Split by "},{"  to separate game objects
     * 6. For each gameObj string:
     *    a. Remove any remaining braces: replace("{", "").replace("}", "")
     *    b. Extract values using extractJsonValue() helper:
     *       - id = extractJsonValue(gameObj, "id")
     *       - name = extractJsonValue(gameObj, "name")
     *       - playerCount = extractJsonValue(gameObj, "players")
     *       - maxPlayers = extractJsonValue(gameObj, "maxPlayers")
     *       - status = extractJsonValue(gameObj, "status")
     *    c. Create players string: playerCount + "/" + maxPlayers
     *    d. Create GameInfo object and add to games list
     *    e. Add row to table: tableModel.addRow(new Object[]{name, players, status})
     * 
     * @param json The JSON string to parse
     */
    private void parseGameList(String json) {
        // TODO: Clear existing data
        
        // TODO: Handle empty array case
        
        // TODO: Remove outer brackets
        
        // TODO: Split into game objects
        
        // TODO: Parse each game object
        
        // TODO: Add to table
        games.clear();
        tableModel.setRowCount(0);
        if(json == null) {
            return;
        }
        json = json.trim();
        if (json.equals("[]")) return;
        
        json = json.substring(1, json.length() - 1);
        String[] gameObjects = json.split("\\},\\{");
        
        for (String gameObj : gameObjects) {
            gameObj = gameObj.trim();
            gameObj = gameObj.replace("{", "").replace("}", "");
            String id = extractJsonValue(gameObj, "id");
            String name = extractJsonValue(gameObj, "name");
            String playerCount = extractJsonValue(gameObj, "players");
            String maxPlayers = extractJsonValue(gameObj, "maxPlayers");
            String status = extractJsonValue(gameObj, "status");
            if (id.isEmpty() || name.isEmpty() || playerCount.isEmpty() || maxPlayers.isEmpty() || status.isEmpty()) {
                continue; 
            }
            String playersstr = playerCount + "/" + maxPlayers;
            GameInfo game = new GameInfo(id, name, playersstr, status);
            games.add(game);
            tableModel.addRow(new Object[]{name, playersstr, status});
            }
    }
    
    /**
     * Helper method to extract JSON values (PROVIDED)
     * 
     * Example: extractJsonValue("\"id\":\"g123\",\"name\":\"Game1\"", "name") returns "Game1"
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return "";
        
        startIndex += searchKey.length();
        
        // Skip whitespace
        while (startIndex < json.length() && Character.isWhitespace(json.charAt(startIndex))) {
            startIndex++;
        }
        
        // Check if value is quoted
        boolean isQuoted = startIndex < json.length() && json.charAt(startIndex) == '"';
        if (isQuoted) {
            startIndex++;
            int endIndex = json.indexOf('"', startIndex);
            if (endIndex == -1) return "";
            return json.substring(startIndex, endIndex);
        } else {
            // Numeric or boolean value
            int endIndex = json.indexOf(',', startIndex);
            if (endIndex == -1) {
                return json.substring(startIndex).trim();
            }
            return json.substring(startIndex, endIndex).trim();
        }
    }

    /**
     * Inner class to store game information (PROVIDED)
     */
    static class GameInfo {
        String id, name, players, status;
        
        GameInfo(String id, String name, String players, String status) {
            this.id = id;
            this.name = name;
            this.players = players;
            this.status = status;
        }
    }
}
