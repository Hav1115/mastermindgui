# ECE422C Mastermind Multiplayer

In this lab you will code a multiplayer Mastermind game that builds on the 
single-player Mastermind game you already developed.  Players can play in 
single player mode in the GUI or compete in games against each other over 
a network.

This project will help you practice and develope your understanding of:

- 🌐 Fundamentals of network programming
- 🏗️ Client-server architecture
- 🎨 GUI development with Swing
- 🧵 Multithreading
- 🧪 Software testing

START EARLY! This is a complex project that requires careful planning and
incremental development. Follow the instructions closely, and don't hesitate to
ask for help during office hours or on Ed Discussion. You will be required to
demonstrate your working multiplayer game to the instructor or TAs during 
recitation or office hours. 

Take time to read through this README.md and the provided ASSIGNMENT.md file
thoroughly and carefully before starting your implementation as it contains
everything you should need to get going.

**Due Date**: 11/10/2025 @ 11:59pm  
**Estimated Time**: 20-25 hours

---

## Quick Start (5 minutes)

### 1. Verify Setup
```bash
# Check Java version (need 11+)
java -version

cd scripts 

# Make scripts executable (Linux/Mac)
chmod +x *.sh

# Build project
./build.sh        
```

### 2. Test Single Player (Fully Working!)
```bash
./run-client.sh   
# Click "Single Player" - study this before coding!
```

### 3. Start Development
```bash
# Terminal 1: Start server
./run-server.sh   

# Terminal 2: Start client
./run-client.sh   

# Run tests
./run-tests.sh    
```

---

## What You'll Build

**Provided (Study These!):**
- ✅ Complete single-player game
- ✅ Core game logic (GameState, SecretCodeGenerator)
- ✅ Application framework (MastermindApp, GameConfiguration)
- ✅ GUI reference implementation (SinglePlayerPanel)

**You'll Implement (45 TODOs):**
- 🔧 **Server** (8-10 hours): ClientHandler, GameSession, GameLobbyManager
- 🔧 **Client** (6-8 hours): MastermindClient, ConnectionPanel, LobbyPanel, GamePanel
- 🔧 **Tests** (2-3 hours): GameStateTest with 10 test cases

---

## Conceptual Project Structure

```
src/
├── PROVIDED (No changes needed)
│   ├── GameConfiguration.java       # Constants
│   ├── SecretCodeGenerator.java     # Code generation
│   ├── GameState.java               # Evaluation logic
│   ├── MastermindApp.java           # App framework
│   ├── MainMenuPanel.java           # Main menu
│   └── SinglePlayerPanel.java       # STUDY THIS!
│
└── TODO (You implement)
    ├── Server (26 TODOs)
    │   ├── MastermindServer.java    # Provided framework
    │   ├── ClientHandler.java       # 10 TODOs, 3-4 hours
    │   ├── GameSession.java         # 8 TODOs, 2-3 hours
    │   └── GameLobbyManager.java    # 8 TODOs, 2-3 hours
    │
    └── Client (19 TODOs)
        ├── MastermindClient.java    # 5 TODOs, 2 hours
        ├── ConnectionPanel.java     # 2 TODOs, 1 hour
        ├── LobbyPanel.java          # 4 TODOs, 2-3 hours
        └── GamePanel.java           # 8 TODOs, 2-3 hours

test/
└── GameStateTest.java               # 10 test cases, 2-3 hours
```

---

## Game Rules

**Mastermind** is a code-breaking game:
- Secret code: 4 colored pegs (Blue, Green, Orange, Purple, Red, Yellow)
- Each player has 12 guesses
- **Feedback after each guess:**
  - ○ Black peg = Correct color, correct position
  - ◉ White peg = Correct color, wrong position
- **Multiplayer**: Players take turns until someone wins or all run out of guesses

---

## Implementation Order

### Week 1: Server (8-10 hours)
1. **ClientHandler.java** (3-4 hours) - Handle client connections
   - Setup I/O, message parsing, protocol handlers
2. **GameSession.java** (2-3 hours) - Manage individual games
   - Player management, turn logic, guess evaluation
3. **GameLobbyManager.java** (2-3 hours) - Coordinate all games
   - Game creation, JSON generation, broadcasting

### Week 2: Client (6-8 hours)
1. **MastermindClient.java** (2 hours) - Network communication
   - Async message handling, SwingUtilities.invokeLater()
2. **ConnectionPanel.java** (1 hour) - Server connection GUI
3. **LobbyPanel.java** (2-3 hours) - Game lobby with JSON parsing
4. **GamePanel.java** (2-3 hours) - Multiplayer game board

### Week 3: Testing & Polish (2-3 hours)
1. **GameStateTest.java** - Write 10 comprehensive test cases
2. Integration testing with multiple clients
3. Bug fixes and documentation

---

## Communication Protocol

All messages follow this format: `COMMAND:data`

**Key Messages:**

C = client, S = server

| Direction |   Message    |                Example               |
|-----------|--------------|--------------------------------------|
|   C→S     | CONNECT      | `CONNECT:Alice`                      |
|   S→C     | CONNECTED    | `CONNECTED:p1234567890`              |
|   C→S     | CREATE_GAME  | `CREATE_GAME:MyGame:2`               |
|   C→S     | JOIN_GAME    | `JOIN_GAME:g12345678`                |
|   S→C     | GAME_STARTED | `GAME_STARTED:g12345678:p1234567890` |
|   C→S     | GUESS        | `GUESS:g12345678:BGRP`               |
|   S→C     | GUESS_RESULT | `GUESS_RESULT:g12345678:Alice:3:2:1` |
|   S→C     | GAME_WON     | `GAME_WON:g12345678:Alice:5`         |
|   S→C     | GAME_OVER    | `GAME_OVER:g12345678:BGRP`           |

*See ASSIGNMENT.md for complete protocol table*

---

## Grading (100 points)

- **Functionality (60%)**: Server (25), Client (25), Integration (10)
- **Code Quality (20%)**:  Documentation, error handling, organization, best practices
- **Testing (10%)**:       Unit tests, coverage, passing tests
- **Documentation (10%)**: Javadoc pages, README.md, comments
---

## Critical Concepts

### SwingUtilities.invokeLater()
```java
// WRONG - network thread updates GUI
onMessage.accept(message);

// RIGHT - schedule on GUI thread
SwingUtilities.invokeLater(() -> onMessage.accept(message));
```

### Lock Management
```java
lock.lock();
try {
    // Critical section
} finally {
    lock.unlock();  // ALWAYS in finally!
}
```

---

## Common Issues

|       Problem       |          Solution                    |
|---------------------|--------------------------------------|
| Port already in use | `lsof -i :8080` then `kill -9 <PID>` |
| GUI freezes         | Use SwingUtilities.invokeLater()     |
| Turn not advancing  | Check TURN_UPDATE message handling   |
| Connection refused  | Start server before client           |

---

## Submission Checklist

Before submitting:
- [ ] Code compiles: `./build.sh`
- [ ] Single player still works
- [ ] 2+ clients can play multiplayer game
- [ ] All tests pass: `./run-tests.sh`
- [ ] No debug print statements
- [ ] Code is well-commented
- [ ] SUBMISSION_README.txt included with name, hours, known issues

**Submit**: ZIP with all `src/` files, `test/` files, JavaDoc output, UML diagrams, scripts, and SUBMISSION_README.txt

---

## Getting Help

1. **Read ASSIGNMENT.md** - Complete detailed instructions
2. **Study SinglePlayerPanel.java** - Working GUI reference
3. **Start early** - 20-25 hours over 3 weeks
4. **Test incrementally** - Don't write everything before testing
5. **Office hours** - Bring specific questions
6. **Ed Discussion** - Post questions (no code sharing)

---

## Academic Integrity

✅ **Allowed**: Discuss concepts, use documentation, search general Java help  
❌ **Not Allowed**: Share code, copy from internet, use AI for complete solutions

Document any AI assistance in `AI_USAGE.md`.

**All code must be your own work.**

---

## Resources

- Complete instructions: **ASSIGNMENT.md**
- [Java Socket Programming](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [Swing Tutorial](https://docs.oracle.com/javase/tutorial/uiswing/)
- [Java Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/)

**Good luck!** 🎮
