# ECE422C : Mastermind Multiplayer Lab

**Due Date**: 11/10/2025 @ 11:59pm  
**Estimated Time**: 20-25 hours (start early!)  

---

## Table of Contents

1. [Setup & Verification](#setup--verification)
2. [Part 1: Understanding Provided Code](#part-1-understanding-provided-code-2-hours)
3. [Part 2: Server Implementation](#part-2-server-implementation-8-10-hours)
4. [Part 3: Client Implementation](#part-3-client-implementation-6-8-hours)
5. [Part 4: Testing](#part-4-testing-2-3-hours)
6. [Communication Protocol](#communication-protocol)
7. [Development Tips](#development-tips--best-practices)
8. [Troubleshooting](#common-issues--solutions)
9. [Submission](#submission)
10. [Grading Rubric](#grading-rubric-100-points)

---

## Setup & Verification

### Prerequisites

- Java 17 or higher (JDK)
- Terminal/command prompt
- Text editor or IDE (IntelliJ IDEA, Eclipse, VS Code recommended)

### Step 1: Verify Java Installation

```bash
java -version    # Should show 11 or higher
javac -version   # Should show 11 or higher
```

If not installed: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)

### Step 2: Extract and Build

**Linux/Mac:**
```ƒ
```

**Expected output:**
```
=========================================
   Mastermind Multiplayer - Build
=========================================
Creating bin directory...
Compiling source files...
√ Build successful!
```

**If compilation fails**: Check Java version, verify all .java files are in `src/` directory.

### Step 3: Test Single Player Mode (Must Work!)

```bash
./run-client.sh    
```

Click "🎮 Single Player" and play a complete game.

**Verify:**
- ✓ Colors select properly
- ✓ Feedback shows correct black/white pegs
- ✓ Game ends with win/lose message

**If single player doesn't work**: Re-download starter code or check Java GUI support.

### Step 4: Test Server Skeleton

**Terminal 1:**
```bash
cd scripts 
./run-server.sh
```

Server should start and wait for connections (appears to hang - this is correct).

**Terminal 2:**
```bash
cd scripts 
./run-client.sh
```

Click "🌐 Multiplayer" → Try to connect → Should fail or freeze.

**This is expected!** You haven't implemented the handlers yet.

Press Ctrl+C to stop the server.

### Step 5: Verify Test Structure

```bash
cd scripts 
./run-tests.sh
```

Should compile and show "[Not yet implemented]" for all tests. This is correct!

**✅ If all 5 steps work, you're ready to code!**

---

## Part 1: Understanding Provided Code (2 hours)

**Before writing any code**, study what's provided. This will save you hours later!

### 1.1 Play Single Player (30 min)

Play 5-10 games to fully understand:
- How colors are selected
- How feedback works (black vs white pegs)
- Win and lose conditions
- UI flow

### 1.2 Study Core Files (90 min)

Read in this order:

**GameConfiguration.java** (5 min)
- Constants that define game rules
- `guessNumber = 12`, `pegNumber = 4`, `colors = ["B","G","O","P","R","Y"]`

**SecretCodeGenerator.java** (10 min)
- Singleton pattern example
- Random code generation

**GameState.java** (20 min) ⭐ **IMPORTANT**
- Core evaluation algorithm
- Two-pass approach: black pegs, then white pegs
- You'll test this extensively!

**SinglePlayerPanel.java** (30 min) ⭐ **MOST IMPORTANT**
- Complete working GUI implementation
- Study these patterns:
  - Layout managers (BorderLayout, BoxLayout, FlowLayout)
  - Event handling (button clicks)
  - Custom painting (colored pegs)
  - Game state updates
- **This is your template for multiplayer panels!**

**MastermindApp.java** (15 min)
- Application framework
- Screen navigation: `showMainMenu()`, `showLobby()`, `showGameBoard()`

**MainMenuPanel.java** (10 min)
- Simple menu example
- Button styling

### Key Takeaway

SinglePlayerPanel shows you **exactly** how to build the multiplayer GUI. When you implement GamePanel, you'll follow the same patterns.

---

## Part 2: Server Implementation (8-10 hours)

Implement in this order. Each file has detailed TODO comments with steps!

### 2.1 ClientHandler.java (3-4 hours)

**Purpose**: Handles one client connection in its own thread.

**10 TODOs to implement:**

1. **Setup I/O Streams** (15 min)
   ```java
   out = new PrintWriter(socket.getOutputStream(), true);
   in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   ```

2. **Message Processing Loop** (30 min)
   ```java
   while (running) {
       String line = in.readLine();
       if (line == null) break;
       // Parse and route message
   }
   ```

3. **Connection Protocol** (20 min)
   - Generate unique player ID: `"p" + System.currentTimeMillis()`
   - Register with lobby
   - Send `CONNECTED:playerId`

4. **Game List Handler** (15 min)
   - Get JSON from lobby
   - Send `GAME_LIST:jsonData`

5. **Create Game Handler** (25 min)
   - Parse `gameName:requiredPlayers`
   - Call `lobby.createGame()`
   - Send `GAME_CREATED:gameId`

6. **Join Game Handler** (30 min)
   - Call `lobby.joinGame()`
   - Send `GAME_JOINED:gameId:players`
   - Broadcast `PLAYER_JOINED` to others
   - Check `session.canStart()` → `session.startGame()`

7. **Leave Game Handler** (20 min)
   - Remove player from session
   - Broadcast `PLAYER_LEFT`

8. **Guess Handler** (15 min)
   - Forward to `session.processGuess()`

9. **Chat Handler** (20 min)
   - Broadcast `CHAT_MESSAGE:gameId:playerName:message`

10. **Resource Cleanup** (20 min)
    ```java
    running = false;
    lobby.removePlayer(playerId);
    in.close();
    out.close();
    socket.close();
    ```

**Testing Checkpoints:**
- After TODO 1-3: Client connects, gets player ID
- After TODO 4-5: Can create games
- After TODO 6: Two clients join same game
- After TODO 7-9: Complete game with chat

**Common Pitfalls:**
- Forgetting to handle null values
- Splitting on ALL colons instead of first colon
- Not closing resources

### 2.2 GameSession.java (2-3 hours)

**Purpose**: Manages one game instance with turn-based logic.

**8 TODOs to implement:**

1. **Add Player** (20 min)
   ```java
   lock.lock();
   try {
       if (players.size() >= requiredPlayers || started) return false;
       players.put(playerId, handler);
       // ... more setup
       return true;
   } finally {
       lock.unlock();
   }
   ```

2. **Remove Player** (15 min)
   - Remove from all maps
   - If empty, remove session from lobby

3. **Check Start Condition** (10 min)
   ```java
   return players.size() == requiredPlayers && !started;
   ```

4. **Start Game** (20 min)
   - Generate secret code
   - Create GameState
   - Broadcast `GAME_STARTED` and `TURN_UPDATE`

5. **Process Guess** (45 min) ⭐ **MOST COMPLEX**
   - Verify game started and player's turn
   - Validate guess format
   - Evaluate with `gameState.evaluateGuess()`
   - Broadcast `GUESS_RESULT`
   - Check win condition (black == pegNumber)
   - Check out of guesses
   - Advance turn

6. **Advance Turn** (15 min)
   ```java
   currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size();
   broadcast("TURN_UPDATE:gameId:" + turnOrder.get(currentTurnIndex));
   ```

7. **Validate Guess** (20 min)
   - Check length
   - Verify each color is valid

8. **Broadcast Message** (10 min)
   ```java
   for (Map.Entry<String, ClientHandler> entry : players.entrySet()) {
       if (!entry.getKey().equals(excludePlayerId)) {
           entry.getValue().sendMessage(message);
       }
   }
   ```

**Testing Checkpoints:**
- After TODO 1-4: Game starts with correct player count
- After TODO 5-8: Complete game with proper turn rotation

**Critical Pattern:**
```java
lock.lock();
try {
    // All game state changes here
} finally {
    lock.unlock();  // ALWAYS in finally!
}
```

### 2.3 GameLobbyManager.java (2-3 hours)

**Purpose**: Central coordinator for all games and players.

**8 TODOs to implement:**

1. **Add Player** (10 min)
   ```java
   players.put(playerId, handler);
   ```

2. **Remove Player** (25 min)
   - Remove from players
   - If in a game, remove from game and broadcast

3. **Create Game** (20 min)
   ```java
   String gameId = "g" + UUID.randomUUID().toString().substring(0, 8);
   GameSession session = new GameSession(gameId, gameName, requiredPlayers, this);
   sessions.put(gameId, session);
   return gameId;
   ```

4. **Join Game** (25 min)
   - Get session and handler
   - Call `session.addPlayer()`
   - Update `playerToGame` map

5. **Get Session** (5 min)
   ```java
   return sessions.get(gameId);
   ```

6. **Remove Session** (20 min)
   - Remove from sessions
   - Clean up player mappings
   - Broadcast updated game list

7. **Generate JSON** (45 min) ⭐ **MOST TIME-CONSUMING**
   ```java
   StringBuilder json = new StringBuilder("[");
   boolean first = true;
   for (GameSession session : sessions.values()) {
       if (!first) json.append(",");
       json.append("{");
       json.append("\"id\":\"").append(session.getGameId()).append("\",");
       json.append("\"name\":\"").append(session.getGameName()).append("\",");
       json.append("\"players\":").append(session.getPlayerCount()).append(",");
       json.append("\"maxPlayers\":").append(session.getMaxPlayers()).append(",");
       json.append("\"status\":\"").append(session.getStatus()).append("\"");
       json.append("}");
       first = false;
   }
   json.append("]");
   return json.toString();
   ```

8. **Broadcast to Lobby** (15 min)
   ```java
   for (ClientHandler handler : players.values()) {
       handler.sendMessage(message);
   }
   ```

**Testing Checkpoints:**
- After TODO 1-4: Can create and join games
- After TODO 7: Game list displays correctly in client
- After TODO 8: All clients see game updates

**JSON Tips:**
- Test with 1 game first
- Use StringBuilder for efficiency
- Don't forget commas between objects!

---

## Part 3: Client Implementation (6-8 hours)

### 3.1 MastermindClient.java (2 hours)

**Purpose**: Manages server connection and async messaging.

**5 TODOs to implement:**

1. **Connect to Server** (30 min)
   ```java
   socket = new Socket(host, port);
   out = new PrintWriter(socket.getOutputStream(), true);
   in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
   this.onMessage = messageCallback;
   connected = true;
   
   listenerThread = new Thread(this::listenLoop, "NetworkListener");
   listenerThread.setDaemon(true);
   listenerThread.start();
   
   send("HELLO:1");
   ```

2. **Message Listener Loop** (45 min) ⭐ **CRITICAL**
   ```java
   try {
       String line;
       while (connected && (line = in.readLine()) != null) {
           final String message = line;
           if (!shuttingDown) {
               SwingUtilities.invokeLater(() -> {
                   if (onMessage != null && !shuttingDown) {
                       onMessage.accept(message);
                   }
               });
           }
       }
   } catch (IOException e) {
       // Handle error
   } finally {
       disconnect();
   }
   ```

3. **Send Message** (10 min)
   ```java
   if (out != null && connected) {
       out.println(message);
   }
   ```

4. **Disconnect** (25 min)
   ```java
   if (connected) {
       connected = false;
       shuttingDown = true;
       send("DISCONNECT");
       Thread.sleep(100);
       in.close();
       out.close();
       socket.close();
   }
   ```

5. **Check Connection** (5 min)
   ```java
   return connected && socket != null && !socket.isClosed();
   ```

**Why SwingUtilities.invokeLater()?**

GUI updates MUST happen on the Event Dispatch Thread. Network thread ≠ GUI thread!

```java
// WRONG - will cause freezing/crashes
onMessage.accept(message);

// RIGHT - schedules on GUI thread
SwingUtilities.invokeLater(() -> onMessage.accept(message));
```

### 3.2 ConnectionPanel.java (1 hour)

**Purpose**: Server connection GUI.

**2 TODOs to implement:**

1. **Handle Connect Button** (30 min)
   ```java
   String playerName = playerNameField.getText().trim();
   String serverAddress = serverAddressField.getText().trim();
   String portText = portField.getText().trim();
   
   if (playerName.isEmpty() || serverAddress.isEmpty() || portText.isEmpty()) {
       showError("Please fill in all fields");
       return;
   }
   
   try {
       int port = Integer.parseInt(portText);
       connectBtn.setEnabled(false);
       connectBtn.setText("Connecting...");
       
       client.setPlayerName(playerName);
       client.connect(serverAddress, port, this::handleServerMessage);
       client.send("CONNECT:" + playerName);
   } catch (NumberFormatException e) {
       showError("Invalid port number");
   } catch (IOException e) {
       showError("Connection failed: " + e.getMessage());
   }
   ```

2. **Handle Server Messages** (20 min)
   - Parse `CONNECTED:playerId` → navigate to lobby
   - Parse `ERROR:message` → show error dialog

### 3.3 LobbyPanel.java (2-3 hours)

**Purpose**: Game lobby with game list.

**4 TODOs to implement:**

1. **Handle Create Game** (20 min)
   ```java
   String gameName = JOptionPane.showInputDialog(...);
   if (gameName != null && !gameName.trim().isEmpty()) {
       String[] options = {"2", "3", "4", "5", "6", "7", "8"};
       String requiredPlayers = JOptionPane.showInputDialog(..., options, "2");
       if (requiredPlayers != null) {
           client.send("CREATE_GAME:" + gameName.trim() + ":" + requiredPlayers);
       }
   }
   ```

2. **Handle Join Game** (20 min)
   ```java
   int selectedRow = gamesTable.getSelectedRow();
   if (selectedRow >= 0 && selectedRow < games.size()) {
       GameInfo game = games.get(selectedRow);
       if (game.status.equals("Waiting")) {
           client.send("JOIN_GAME:" + game.id);
       } else {
           JOptionPane.showMessageDialog(this, "Cannot join game in progress");
       }
   }
   ```

3. **Handle Server Messages** (45 min)
   - `GAME_LIST` → parse and update table
   - `GAME_CREATED` → refresh list
   - `GAME_JOINED` → navigate to game board
   - `GAME_STARTED` → navigate to game board
   - `ERROR` → show error dialog

4. **Parse JSON Game List** (60 min) ⭐ **CHALLENGING**
   ```java
   games.clear();
   tableModel.setRowCount(0);
   
   json = json.trim();
   if (json.equals("[]")) return;
   
   json = json.substring(1, json.length() - 1);
   String[] gameObjects = json.split("\\},\\{");
   
   for (String gameObj : gameObjects) {
       gameObj = gameObj.replace("{", "").replace("}", "");
       String id = extractJsonValue(gameObj, "id");
       String name = extractJsonValue(gameObj, "name");
       String playerCount = extractJsonValue(gameObj, "players");
       String maxPlayers = extractJsonValue(gameObj, "maxPlayers");
       String status = extractJsonValue(gameObj, "status");
       
       GameInfo game = new GameInfo(id, name, playerCount + "/" + maxPlayers, status);
       games.add(game);
       tableModel.addRow(new Object[]{name, playerCount + "/" + maxPlayers, status});
   }
   ```

**Note**: `extractJsonValue()` helper is provided - use it!

### 3.4 GamePanel.java (2-3 hours)

**Purpose**: Multiplayer game board.

**8 TODOs to implement:**

1. **Add Color to Guess** (15 min)
   ```java
   if (!isMyTurn) {
       JOptionPane.showMessageDialog(this, "It's not your turn!");
       return;
   }
   if (currentGuess.size() < GameConfiguration.pegNumber) {
       currentGuess.add(color);
       updateCurrentGuessDisplay();
       if (currentGuess.size() == GameConfiguration.pegNumber) {
           submitBtn.setEnabled(true);
       }
   }
   ```

2. **Update Guess Display** (20 min)
   ```java
   currentGuessPanel.removeAll();
   for (int i = 0; i < GameConfiguration.pegNumber; i++) {
       Color color = i < currentGuess.size() ? 
           colorMap.get(currentGuess.get(i)) : Color.LIGHT_GRAY;
       currentGuessPanel.add(createPeg(color, 30));
   }
   currentGuessPanel.revalidate();
   currentGuessPanel.repaint();
   ```

3. **Submit Guess** (20 min)
   ```java
   if (currentGuess.size() == GameConfiguration.pegNumber && isMyTurn) {
       String guess = String.join("", currentGuess);
       client.send("GUESS:" + gameId + ":" + guess);
       currentGuess.clear();
       updateCurrentGuessDisplay();
       submitBtn.setEnabled(false);
   }
   ```

4. **Clear Guess** (10 min)
5. **Send Chat** (15 min)
6. **Back to Lobby** (10 min)

7. **Handle Server Messages** (60 min) ⭐ **MOST COMPLEX**
   - `GAME_JOINED` → show players
   - `PLAYER_JOINED` → add chat message
   - `GAME_STARTED` → show game started
   - `TURN_UPDATE` → update turn display
     ```java
     isMyTurn = activePlayerId.equals(myPlayerId);
     if (isMyTurn) {
         turnLabel.setText("Your Turn!");
         turnLabel.setForeground(new Color(46, 204, 113));
     } else {
         turnLabel.setText("Waiting for other player...");
         turnLabel.setForeground(new Color(243, 156, 18));
     }
     ```
   - `GUESS_RESULT` → add to history
   - `GAME_WON` → show winner dialog
   - `GAME_OVER` → reveal code and show dialog
   - `CHAT_MESSAGE` → add to chat
   - `PLAYER_LEFT` → add chat message

8. **Reveal Secret Code** (15 min)
   ```java
   secretCodePanel.removeAll();
   for (int i = 0; i < code.length(); i++) {
       String colorCode = String.valueOf(code.charAt(i));
       secretCodePanel.add(createPeg(colorMap.get(colorCode), 25));
   }
   secretCodePanel.revalidate();
   secretCodePanel.repaint();
   ```

---

## Part 4: Testing (2-3 hours)

### GameStateTest.java

Write 10 comprehensive test cases for the evaluation algorithm.

**Test Strategy:**
1. Work out expected answer BY HAND first
2. Write the test
3. Run it
4. Debug if needed

**Test Cases:**

1. **Perfect Match** (15 min)
   - Code: "BGRP", Guess: "BGRP"
   - Expected: 4 black, 0 white

2. **No Matches** (15 min)
   - Code: "BGRP", Guess: "YYOO"
   - Expected: 0 black, 0 white

3. **Partial Correct Positions** (20 min)
   - Code: "BGRP", Guess: "BYRY"
   - Expected: 2 black, 0 white

4. **Correct Colors, Wrong Positions** (20 min)
   - Code: "BGRP", Guess: "PRBG"
   - Expected: 0 black, 4 white

5. **Mixed Results** (25 min)
   - Code: "BGRP", Guess: "BGYO"
   - Expected: 2 black, 0 white

6. **Duplicate Colors in Guess** (30 min)
   - Code: "BGRP", Guess: "BBBB"
   - Expected: 1 black, 0 white (only first B matches)

7. **Duplicate Colors in Code** (30 min)
   - Code: "BBGR", Guess: "BOPY"
   - Expected: 1 black, 0 white

8. **Complex Duplicates** (30 min)
   - Code: "BBRR", Guess: "RBBR"
   - Work this out carefully by hand!

9. **All Same Color** (15 min)
   - Code: "BBBB", Guess: "BBBB"
   - Expected: 4 black, 0 white

10. **Additional Edge Cases** (20 min)
    - Add 2+ creative test cases

**Running Tests:**
```bash
./run-tests.sh
```

---

## Communication Protocol

### Message Format
All messages: `COMMAND:data`

### Complete Protocol Table

C = client, S = server

| Direction | Message       | Format                               | Example                               |
|-----------|---------------|--------------------------------------|---------------------------------------|
| C→S       | HELLO         | `HELLO:version`                      | `HELLO:1`                             |
| C→S       | CONNECT       | `CONNECT:name`                       | `CONNECT:Alice`                       |
| S→C       | CONNECTED     | `CONNECTED:playerId`                 | `CONNECTED:p1234567890`               |
| C→S       | GET_GAMES     | `GET_GAMES`                          | `GET_GAMES`                           |
| S→C       | GAME_LIST     | `GAME_LIST:[{...}]`                  | See JSON below                        |
| C→S       | CREATE_GAME   | `CREATE_GAME:name:players`           | `CREATE_GAME:MyGame:2`                |
| S→C       | GAME_CREATED  | `GAME_CREATED:gameId`                | `GAME_CREATED:g12345678`              |
| C→S       | JOIN_GAME     | `JOIN_GAME:gameId`                   | `JOIN_GAME:g12345678`                 |
| S→C       | GAME_JOINED   | `GAME_JOINED:gameId:players`         | `GAME_JOINED:g12345678:Alice,Bob`     |
| S→C       | PLAYER_JOINED | `PLAYER_JOINED:gameId:name`          | `PLAYER_JOINED:g12345678:Carol`       |
| S→C       | GAME_STARTED  | `GAME_STARTED:gameId:firstPlayer`    | `GAME_STARTED:g12345678:p1234567890`  |
| S→C       | TURN_UPDATE   | `TURN_UPDATE:gameId:playerId`        | `TURN_UPDATE:g12345678:p1234567890`   |
| C→S       | GUESS         | `GUESS:gameId:guess`                 | `GUESS:g12345678:BGRP`                |
| S→C       | GUESS_RESULT  | `GUESS_RESULT:gameId:name:num:B:W`   | `GUESS_RESULT:g12345678:Alice:3:2:1`  |
| S→C       | GAME_WON      | `GAME_WON:gameId:winner:guesses`     | `GAME_WON:g12345678:Alice:5`          |
| S→C       | GAME_OVER     | `GAME_OVER:gameId:code`              | `GAME_OVER:g12345678:BGRP`            |
| C→S       | CHAT          | `CHAT:gameId:message`                | `CHAT:g12345678:Hello!`               |
| S→C       | CHAT_MESSAGE  | `CHAT_MESSAGE:gameId:name:msg`       | `CHAT_MESSAGE:g12345678:Alice:Hello!` |
| C→S       | LEAVE_GAME    | `LEAVE_GAME:gameId`                  | `LEAVE_GAME:g12345678`                |
| S→C       | PLAYER_LEFT   | `PLAYER_LEFT:gameId:name`            | `PLAYER_LEFT:g12345678:Alice`         |
| C→S       | DISCONNECT    | `DISCONNECT`                         | `DISCONNECT`                          |
| S→C       | ERROR         | `ERROR:message`                      | `ERROR:Game full`                     |



### Game List JSON Format
```json
[
  {
    "id": "g12345678",
    "name": "Alice's Game",
    "players": 2,
    "maxPlayers": 4,
    "status": "Waiting"
  }
]
```

---

## Development Tips & Best Practices

### Message Parsing

```java
// Parse only FIRST colon
int colonIndex = message.indexOf(':');
String command = message.substring(0, colonIndex);
String data = message.substring(colonIndex + 1);

// NOT: String[] parts = message.split(":"); // WRONG if data contains colons!
```

### Thread Safety

**Always use locks for shared data:**
```java
lock.lock();
try {
    // Modify game state
} finally {
    lock.unlock();  // ALWAYS in finally!
}
```

**Always use SwingUtilities for GUI updates:**
```java
SwingUtilities.invokeLater(() -> {
    // Update GUI components here
});
```

### Debugging

**Add strategic logging:**
```java
System.out.println("Received: " + message);
System.out.println("Current turn: " + turnOrder.get(currentTurnIndex));
System.out.println("Sending: " + message);
```

**Test incrementally:**
1. Write one TODO
2. Compile
3. Test
4. Fix issues
5. Move to next TODO

### Code Quality

**Good variable names:**
```java
// Bad
String[] p = data.split(":");
String x = p[0];

// Good
String[] parts = data.split(":");
String gameId = parts[0];
```

**Handle errors:**
```java
try {
    int port = Integer.parseInt(portText);
    // ...
} catch (NumberFormatException e) {
    showError("Invalid port number");
} catch (IOException e) {
    showError("Connection failed: " + e.getMessage());
}
```

---

## Common Issues & Solutions

| Problem             | Cause                              | Solution                                    |
|---------------------|------------------------------------|---------------------------------------------|
| Port already in use | Previous server running            | `lsof -i :8080` then `kill -9 <PID>`        |
| GUI freezes         | Updating GUI from network thread   | Use `SwingUtilities.invokeLater()`          |
| Connection refused  | Server not running                 | Start server before client                  |
| Turn not advancing  | Missing TURN_UPDATE                | Check `advanceTurn()` broadcasts message    |
| IndexOutOfBounds    | Splitting on all colons            | Parse only FIRST colon                      |
| Race conditions     | Missing locks                      | Use `lock.lock()` in try-finally            |
| Wrong peg counts    | Algorithm bug                      | Write unit tests to debug                   |

---

## Submission

### Before Submitting

- [ ] `./build.sh` compiles successfully
- [ ] Single player mode still works
- [ ] Can connect 2+ clients to server
- [ ] Can create and join games
- [ ] Turn rotation works correctly
- [ ] Guess feedback is accurate
- [ ] Chat messages work
- [ ] Game ends properly (win/lose)
- [ ] `./run-tests.sh` passes all tests
- [ ] Code is well-commented
- [ ] No debug print statements
- [ ] Clean disconnect works

### What to Submit

Create a ZIP file: `lastname_firstname_eid.zip` with files in this structure:

```
MastermindGui
├── src/
│   ├── ClientHandler.java
│   ├── GameSession.java
│   ├── GameLobbyManager.java
│   ├── MastermindClient.java
│   ├── ConnectionPanel.java
│   ├── LobbyPanel.java
│   ├── GamePanel.java
│   └── [all other .java files]
├── test/
│   └── GameStateTest.java
├── scripts/
│   ├── build.sh
│   ├── run-server.sh
│   ├── run-client.sh
│   └── run-tests.sh
└── Documents/
    ├── SUBMISSION_README.txt
    ├── JavaDocs/
    │   └── [javadoc HTML files]
    └── UML_Diagrams/
        └── [UML diagram files]
```

### SUBMISSION_README.txt Template

```
Name: [Your Full Name]
EID: [Your EID]
Date: [Submission Date]

HOURS SPENT:
- Server Implementation: X hours
- Client Implementation: X hours  
- Testing: X hours
- Debugging: X hours
Total: X hours

KNOWN ISSUES:
[List any bugs or incomplete features, or write "None"]

EXTRA FEATURES:
[List any bonus features, or write "None"]

COMPILATION:
./build.sh

RUNNING:
1. Start server: ./run-server.sh
2. Start client(s): ./run-client.sh
3. Run tests: ./run-tests.sh

TESTING NOTES:
[Describe how you tested - e.g., "Tested with 3 clients playing simultaneously"]

AI USAGE:
[If you used AI, describe what and how. Otherwise write "None"]
```

---

## Grading Rubric (100 points)

### Functionality (60 points)

**Server Implementation (25 points)**
- ClientHandler (10 pts)
  - Message parsing: 3 pts
  - Request routing: 3 pts
  - Resource cleanup: 2 pts
  - Error handling: 2 pts
- GameSession (10 pts)
  - Player management: 3 pts
  - Turn logic: 3 pts
  - Guess evaluation: 3 pts
  - Win/lose detection: 1 pt
- GameLobbyManager (5 pts)
  - Game creation/joining: 2 pts
  - JSON generation: 2 pts
  - Broadcasting: 1 pt

**Client Implementation (25 points)**
- MastermindClient (8 pts)
  - Connection handling: 3 pts
  - Async message reception: 3 pts
  - Clean disconnect: 2 pts
- GUI Panels (17 pts)
  - ConnectionPanel: 3 pts
  - LobbyPanel (game list, create/join): 7 pts
  - GamePanel (gameplay, turns, chat): 7 pts

**Integration (10 points)**
- Complete game playable: 5 pts
- Multiple clients supported: 3 pts
- No crashes/freezes: 2 pts

### Code Quality (20 points)

- **Documentation** (5 pts): Clear comments, meaningful variable names
- **Error Handling** (5 pts): Try-catch blocks, input validation
- **Organization** (5 pts): Well-structured methods, no code duplication
- **Best Practices** (5 pts): Proper locks, SwingUtilities, clean code

### Testing (20 points)

- **Unit Tests Implemented** (10 pts)
  - All 10 test cases: 7 pts
  - Tests are meaningful: 3 pts
- **Test Coverage** (5 pts): Edge cases covered
- **Tests Pass** (5 pts): All tests run successfully

---

## Academic Integrity

### ✅ Allowed
- Discuss concepts and approaches with classmates
- Use course materials and Java documentation
- Search for general Java programming help (syntax, APIs)
- Ask questions on Ed Discussion (no code sharing)

### ❌ Not Allowed
- Share your code with other students
- Copy code from the internet or other sources
- Use AI to generate complete solutions or large code blocks
- Submit code you don't understand

### AI Usage Policy

If you use AI assistants (ChatGPT, GitHub Copilot, etc.):
1. You MUST document usage in `AI_USAGE.md`
2. Include: What was asked, what was generated, how you modified it
3. You must understand every line of submitted code
4. Excessive AI-generated code will be considered plagiarism

**All submitted code must be primarily your own work.**

Violations will result in academic misconduct charges.

---

## Getting Help

1. **Read this document thoroughly** - Most questions are answered here
2. **Study SinglePlayerPanel.java** - Complete working example
3. **Start early** - Don't wait until the last week
4. **Test incrementally** - Compile and test after each TODO
5. **Use print statements** - Debug message flow
6. **Office hours** - Bring specific questions and code
7. **Ed Discussion** - Post questions (no code sharing)
8. **Course resources** - [Java tutorials](https://docs.oracle.com/javase/tutorial/)
9. **JSON help** - [JSON.org](https://www.json.org/json-en.html)
---

**Good luck!** 🎮

This project teaches real-world skills you'll use in industry: network programming, GUI development, concurrent programming, and testing. Take your time, test thoroughly, and don't hesitate to ask for help.

**Remember**: The goal is to learn, not just to finish. Understanding the concepts will help you in future courses and your career.
