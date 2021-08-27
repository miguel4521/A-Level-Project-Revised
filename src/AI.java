import javafx.application.Platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class AI implements Runnable {
    public static boolean thinking = false;
    static ArrayList<String> chessNotationMoveLog = new ArrayList<>();
    private final Move[] openingMoves = {new Move(118, 85), new Move(100, 68), new Move(98, 66)};
    //<editor-fold desc="HashMaps">
    HashMap<Integer, Character> pieceTypeToSymbol = new HashMap<>() {{
        put(1, 'P');
        put(2, 'N');
        put(3, 'B');
        put(4, 'R');
        put(5, 'Q');
        put(6, 'K');
    }};
    HashMap<Character, Integer> symbolToPieceType = new HashMap<>() {{
        put('P', 1);
        put('N', 2);
        put('B', 3);
        put('R', 4);
        put('Q', 5);
        put('K', 6);
    }};
    HashMap<Integer, String> colsToFiles = new HashMap<>() {{
        put(0, "a");
        put(1, "b");
        put(2, "c");
        put(3, "d");
        put(4, "e");
        put(5, "f");
        put(6, "g");
        put(7, "h");
    }};
    HashMap<Integer, Integer> rowsToRanks = new HashMap<>() {{
        put(7, 1);
        put(6, 2);
        put(5, 3);
        put(4, 4);
        put(3, 5);
        put(2, 6);
        put(1, 7);
        put(0, 8);
    }};
    HashMap<Character, Integer> ranksToRows = new HashMap<>() {{
        put('1', 7);
        put('2', 6);
        put('3', 5);
        put('4', 4);
        put('5', 3);
        put('6', 2);
        put('7', 1);
        put('8', 0);
    }};
    HashMap<Character, Integer> filesToCols = new HashMap<>() {{
        put('a', 0);
        put('b', 1);
        put('c', 2);
        put('d', 3);
        put('e', 4);
        put('f', 5);
        put('g', 6);
        put('h', 7);
    }};
    //</editor-fold>
    MoveGenerator moveGenerator = new MoveGenerator();
    MoveSearch ms = new MoveSearch();
    GUI gui = new GUI();
    Board b = new Board();
    MakeMove makeMove = new MakeMove();

    @Override
    public void run() {
        thinking = true;
        ArrayList<Move> legalMoves = moveGenerator.generateLegalMoves();
        if (legalMoves.isEmpty()) {
            if (moveGenerator.inCheck())
                System.out.println("Checkmate");
            else
                System.out.println("Stalemate");
            thinking = false;
            return;
        }
        Move move = generateMove(legalMoves);
        addToChessNotationMoveLog(move, legalMoves);
        makeMove.makeMove(move);
        Platform.runLater(() -> {
            gui.moveImages(move);
            GUI.hintButton.setText("Hint?");
        });
        Board.fenHistory.add(b.loadFenFromBoard());
        MouseHandler.moves = moveGenerator.generateLegalMoves();
        if (MouseHandler.moves.isEmpty()) {
            if (moveGenerator.inCheck())
                System.out.println("Checkmate");
            else
                System.out.println("Stalemate");
        }
        gui.generateHint();
        thinking = false;
    }

    public Move generateMove(ArrayList<Move> legalMoves) {
        String input = chessNotationMoveLog.toString().
                replace("[", "").replace("]", "").replace(",", "");
        Move move = notationToMove(findMove(input), legalMoves);
        if (MakeMove.moveLog.isEmpty())
            move = randomOpeningMove().setBookMove();
        if (move != null && !MakeMove.moveLog.isEmpty()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (move != null)
            gui.updateEvaluation(-1);
        if (move == null)
            move = ms.startSearch();
        if (move == null)
            move = legalMoves.get(getRandomNumber(legalMoves.size() - 1));
        return move;
    }

    private Move randomOpeningMove() {
        return openingMoves[getRandomNumber(openingMoves.length)];
    }

    public void addToChessNotationMoveLog(Move move, ArrayList<Move> legalMoves) {
        // Make piece positive all the time
        int piece = move.getPieceMoved() > 0 ? move.getPieceMoved() : move.getPieceMoved() * -1;
        String notation = "";
        if (move.isCastle()) {
            // Notation for castling
            if (b.getFile(move.getEndSq()) == 2)
                // Queen side castling
                notation = "O-O-O";
            else
                // King side castling
                notation = "O-O";
        } else {
            // Not castling
            // If isn't pawn - pawns don't need a letter
            if (piece != 1)
                notation = notation.concat(pieceTypeToSymbol.get(piece) + "");
            // If there is a capture - engine thinks that an en passant move isn't a capture
            if (move.getPieceCaptured() != 0 || move.isEnPassantMove()) {
                if (piece == 1)
                    notation = notation.concat(colsToFiles.get(b.getFile(move.getStartSq())) + "");
                notation = notation.concat("x");
            }
            // If there is ambiguity
            if (moveGenerator.squareAttackedBySamePieceFile(move, legalMoves) && move.getPieceMoved() != 0)
                notation = notation.concat(colsToFiles.get(b.getFile(move.getEndSq())));
            else if (moveGenerator.squareAttackedBySamePieceRank(move, legalMoves) && move.getPieceMoved() != 0)
                notation = notation.concat(rowsToRanks.get(b.getRank(move.getEndSq())) + "");
            // This is always at the end
            notation = notation.concat(squareToChessNotation(b.getRank(move.getEndSq()), b.getFile(move.getEndSq())));
        }
        if (moveGenerator.inCheck())
            notation = notation.concat("+");
        chessNotationMoveLog.add(notation);
        System.out.println(chessNotationMoveLog);
    }

    private String squareToChessNotation(int row, int col) {
        String square = "";
        square = square.concat(colsToFiles.get(col));
        square = square.concat(rowsToRanks.get(row) + "");
        return square;
    }

    private int getRandomNumber(int max) {
        return (int) ((Math.random() * (max)));
    }

    private String findMove(String chessNotationLog) {
        ArrayList<String> linesWithMoves = new ArrayList<>();
        // Split every move into an array
        String[] pMove = chessNotationLog.split(" ");
        Scanner sc2 = new Scanner(Objects.requireNonNull(getClass().getResourceAsStream("Book")));
        // Go through every line in the file until the end.
        while (sc2.hasNextLine()) {
            String line = sc2.nextLine();
            // The grandmaster's move
            String[] gmMove = line.split(" ");
            for (int i = 0; i < pMove.length; i++) {
                // If every move so far has been equal, add the line to the array list
                if (pMove[i].equals(gmMove[i]) && i == pMove.length - 1)
                    linesWithMoves.add(line);
                    // If the moves so far aren't equal go to the next line
                else if (!pMove[i].equals(gmMove[i]))
                    break;
            }
        }
        // Once every valid line is found, it will choose a random one and return the next move
        if (linesWithMoves.size() > 1) {
            String[] chooseLine = linesWithMoves
                    .get(getRandomNumber(linesWithMoves.size())).split(" ");
            return chooseLine[pMove.length];
        }
        // Return null if the moves so far can't be found in any lines
        return null;
    }

    private Move notationToMove(String chessNotationNextMove, ArrayList<Move> allMoves) {
        if (chessNotationNextMove == null)
            return null;
        // All of these symbols are unnecessary
        chessNotationNextMove = chessNotationNextMove.replace("+", "").replace("x", "")
                .replace("-", "").replace("#", "");
        boolean whiteToMove = true;
        // Test if each possible move is the inputted move
        for (Move moveToTest : allMoves) {
            int startRank = b.getRank(moveToTest.getStartSq());
            int startFile = b.getFile(moveToTest.getStartSq());
            int endRank = b.getRank(moveToTest.getEndSq());
            int endFile = b.getFile(moveToTest.getEndSq());

            char targetFile = chessNotationNextMove.charAt(chessNotationNextMove.length() - 2);
            char targetRank = chessNotationNextMove.charAt(chessNotationNextMove.length() - 1);
            int pieceMoved = moveToTest.getPieceMoved();
            if (pieceMoved < 0)
                whiteToMove = false;
            // Just means it's a castle
            if (chessNotationNextMove.equals("OO")) {
                if (moveToTest.isCastle())
                    // King side castle
                    return new Move(b.getSquare(startRank, 4), b.getSquare(endRank, 6)).setCastle().setBookMove();
            } else if (chessNotationNextMove.equals("OOO")) {
                if (moveToTest.isCastle())
                    // Queen side castle
                    // startRow, 4, endRow, 2
                    return new Move(b.getSquare(startRank, 4), b.getSquare(endRank, 2)).setCastle().setBookMove();
                // This means it's a pawn because their moves don't start with their piece name
            } else if (filesToCols.containsKey(chessNotationNextMove.charAt(0))) {
                // If the tested move isn't a pawn, continue
                if ((whiteToMove ? 1 : -1) != pieceMoved)
                    continue;
                // Check if it's the correct file
                if (String.valueOf(chessNotationNextMove.charAt(0)).equals(colsToFiles.get(startFile))) {
                    // Check for correct ending file and rank
                    if (String.valueOf(targetFile).equals(colsToFiles.get(endFile)) &&
                            Character.getNumericValue(targetRank) == rowsToRanks.get(endRank)) {
                        // If it is, return the move
                        return new Move(b.getSquare(startRank, startFile), b.getSquare(endRank, endFile)).setBookMove();
                    }
                }
            } else { // Regular piece move
                int movePiece = symbolToPieceType.get(chessNotationNextMove.charAt(0));
                if (!whiteToMove)
                    movePiece *= -1;
                // If it's the correct file, rank and piece type continue.
                if (movePiece == pieceMoved && String.valueOf(targetFile).equals(colsToFiles.get(endFile))
                        && Character.getNumericValue(targetRank) == rowsToRanks.get(endRank)) {
                    if (chessNotationNextMove.length() == 4) { // Extra letter for disambiguation
                        String disambiguationChar = Character.toString(chessNotationNextMove.charAt(1));
                        if (chessNotationNextMove.contains(disambiguationChar)) { // File disambiguation
                            if (disambiguationChar.equals(colsToFiles.get(endFile)))
                                return new Move(b.getSquare(startRank, startFile), b.getSquare(endRank, endFile)).setBookMove();
                        } else { // Rank disambiguation
                            if (disambiguationChar.equals(rowsToRanks.get(startRank).toString()))
                                return new Move(b.getSquare(startRank, startFile), b.getSquare(endRank, endFile)).setBookMove();
                        }
                    }
                    // Not a disambiguation move
                    return new Move(b.getSquare(startRank, startFile), b.getSquare(endRank, endFile)).setBookMove();
                }
            }
        }
        // Return null if the move can't be found
        return null;
    }
}
