# Chess Game

A fully-featured chess engine and GUI implementation created as my first major programming project during my sixth form studies in the summer of 2021.

## About the Project

This chess game was developed as my A-Level Computer Science project, marking my first significant venture into software development. What started as an academic requirement turned into a passionate exploration of game development, artificial intelligence, and user interface design.

### Features

- Complete chess rules implementation including:
  - All standard piece movements
  - Special moves (castling, en passant, pawn promotion)
  - Check and checkmate detection
- Graphical user interface built with JavaFX
- AI opponent with:
  - Opening book database
  - Position evaluation
  - Multiple difficulty levels
- Move hints system
- Move notation in standard chess format
- Interactive piece movement with drag-and-drop
- Visual move highlighting
- Captured pieces display

## Technical Implementation

### Chess Engine
- Uses a 0x88 board representation for efficient move generation and validation
- Implements minimax algorithm with alpha-beta pruning for AI move searching
- Position evaluation based on:
  - Material counting
  - Piece-square tables for positional understanding
  - Mobility evaluation
  - King safety considerations

### GUI
- Built using JavaFX for a modern, responsive interface
- Custom animations for piece movements
- Real-time move validation and highlighting
- Intuitive drag-and-drop piece movement

### Opening Book
- Includes a database of common chess openings
- AI can play book moves in the early game
- Helps create more natural and varied gameplay

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven

### Installation
1. Clone the repository
```bash
git clone [repository-url]
```

2. Navigate to the project directory
```bash
cd chess-game
```

3. Build the project
```bash
mvn clean install
```

4. Run the game
```bash
mvn javafx:run
```

## Learning Outcomes

This project taught me invaluable lessons about:
- Software architecture and design patterns
- Algorithm implementation and optimization
- GUI development and user experience design
- Project management and documentation
- The importance of testing and debugging

## Future Improvements

While the project successfully meets its initial goals, there are several areas for potential enhancement:
- Implementing an opening book editor
- Adding network play capabilities
- Improving AI performance through more advanced algorithms
- Adding a PGN game import/export feature
- Implementing a game analysis mode

## License

This project is open source and available under the MIT License. 