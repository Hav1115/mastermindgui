/* ECE422C Mastermind Multiplayer Lab
 * ConnectionPanel
 * 
 * This panel handles the initial server connection.
 * 
 * LEARNING OBJECTIVES:
 * - Form-based GUI design
 * - Input validation
 * - Error handling in GUI
 * 
 * ESTIMATED TIME: 1 hour
 */

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ConnectionPanel extends JPanel {
    private MastermindClient client;
    private JTextField playerNameField;
    private JTextField serverAddressField;
    private JTextField portField;
    private JButton connectBtn;

    public ConnectionPanel(MastermindClient client) {
        this.client = client;
        setLayout(new BorderLayout());
        setBackground(new Color(52, 73, 94));

        initComponents();
    }

    /**
     * GUI components are already set up for you.
     * Study this method to understand Swing layout management.
     */
    private void initComponents() {
        JLabel titleLabel = new JLabel("Connect to Server", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 30, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setForeground(Color.WHITE);
        formPanel.add(nameLabel, gbc);

        gbc.gridy = 1;
        playerNameField = new JTextField(20);
        formPanel.add(playerNameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel serverLabel = new JLabel("Server Address:");
        serverLabel.setForeground(Color.WHITE);
        formPanel.add(serverLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(5, 5, 5, 5);
        serverAddressField = new JTextField("localhost", 20);
        formPanel.add(serverAddressField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(15, 5, 5, 5);
        JLabel portLabel = new JLabel("Port:");
        portLabel.setForeground(Color.WHITE);
        formPanel.add(portLabel, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(5, 5, 5, 5);
        portField = new JTextField("8080", 20);
        formPanel.add(portField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setOpaque(false);

        connectBtn = new JButton("Connect");
        connectBtn.setPreferredSize(new Dimension(120, 35));
        connectBtn.addActionListener(e -> handleConnect());

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setPreferredSize(new Dimension(120, 35));
        cancelBtn.addActionListener(e -> MastermindApp.showMainMenu());

        buttonPanel.add(connectBtn);
        buttonPanel.add(cancelBtn);

        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * TODO 1: Handle Connect Button (30 minutes)
     * 
     * Validates input and attempts to connect to the server.
     * 
     * Steps:
     * 1. Get and trim text from all three fields (playerName, serverAddress, port)
     * 2. Validate that none are empty:
     * - If any are empty, call showError("Please fill in all fields") and return
     * 3. Parse the port as an integer (wrap in try-catch for
     * NumberFormatException):
     * - If parsing fails, call showError("Invalid port number") and return
     * 4. Disable the connect button: connectBtn.setEnabled(false)
     * 5. Change button text: connectBtn.setText("Connecting...")
     * 6. Store player name in client: client.setPlayerName(playerName)
     * 7. Try to connect: client.connect(serverAddress, port,
     * this::handleServerMessage)
     * 8. Send connect message: client.send("CONNECT:" + playerName)
     * 9. Catch IOException:
     * - Call showError("Connection failed: " + e.getMessage())
     * - Re-enable button and reset text
     */
    private void handleConnect() {
        // TODO: Get and validate input fields

        // TODO: Parse port number

        // TODO: Disable button and show connecting status

        // TODO: Store player name in client

        // TODO: Attempt connection

        // TODO: Handle connection errors
        String playerName = playerNameField.getText().trim(); //Trim to cut our blank space
        String serverAddress = serverAddressField.getText().trim();
        String portText = portField.getText().trim();

        if (playerName.isEmpty() || serverAddress.isEmpty() || portText.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        try { //Try and Catch to avoid common exceptions
            int port = Integer.parseInt(portText);
            connectBtn.setEnabled(false);
            connectBtn.setText("Connecting...");

            client.setPlayerName(playerName);
            client.connect(serverAddress, port, this::handleServerMessage);
            client.send("CONNECT:" + playerName);
        } catch (NumberFormatException e) {
            showError("Invalid port number");
            return;
        } catch (IOException e) {
            showError("Connection failed: " + e.getMessage());
            connectBtn.setEnabled(true);
            connectBtn.setText("Connect");
        }
    }

    /**
     * TODO 2: Handle Server Messages (20 minutes)
     * 
     * Processes messages received from the server during connection.
     * 
     * Message formats to handle:
     * - "CONNECTED:playerId" - Connection successful
     * - "ERROR:message" - Connection failed
     * 
     * Steps:
     * 1. Find the first ':' in the message
     * 2. If not found, return early
     * 3. Split message into command (before colon) and data (after colon)
     * 4. Use a switch statement on command:
     * 
     * Case "CONNECTED":
     * a. Check if data is not empty
     * b. Store player ID: client.setPlayerId(data)
     * c. Navigate to lobby: MastermindApp.showLobby()
     * 
     * Case "ERROR":
     * a. Call showError("Connection error: " + data)
     * b. Re-enable connect button
     * c. Reset button text to "Connect"
     * 
     * @param message The message from the server
     */
    private void handleServerMessage(String message) {
        // TODO: Parse message into command and data

        // TODO: Handle CONNECTED message

        // TODO: Handle ERROR message

        if (message == null) { // Quick null check
            return;
        }

        int index = message.indexOf(':');
        if (index == -1) { //null check again
            return;
        }

        String command = message.substring(0, index).trim();
        String data = message.substring(index + 1);

        switch (command) {
            case "CONNECTED":
                if (data.isEmpty() == false) {
                    client.setPlayerId(data);
                    MastermindApp.showLobby();
                }
                break;
            case "ERROR":
                showError("Connection error: " + data);
                connectBtn.setEnabled(true);
                connectBtn.setText("Connect");
                break;
            default:
                break;
        }
    }

    /**
     * Shows an error dialog (already implemented)
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Connection Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
