import java.util.ArrayList;
import java.util.HashMap;

public class Board {
    public static int[] board = new int[128];
    public static boolean whiteToMove = true;
    public static int whiteKingLocation = 116;
    public static int blackKingLocation = 4;

    public static ArrayList<String> fenHistory = new ArrayList<>();
    Piece p = new Piece();

    public int[] loadFromFen(String fen) {
        HashMap<Character, Integer> pieceTypeFromSymbol = new HashMap<>() {{
            put('p', p.pawn);
            put('n', p.knight);
            put('b', p.bishop);
            put('r', p.rook);
            put('q', p.queen);
            put('k', p.king);
        }};
        String fenBoard = fen.split(" ")[0];
        int file = 0, rank = 7;
        for (char symbol : fenBoard.toCharArray()) {
            if (symbol == '/') {
                file = 0;
                rank--;
            } else {
                if (Character.isDigit(symbol))
                    file += Character.getNumericValue(symbol);
                else {
                    int pieceColour = (Character.isUpperCase(symbol) ? p.black : p.white);
                    int pieceType = pieceTypeFromSymbol.get(Character.toLowerCase(symbol));
                    board[getSquare(rank, file)] = pieceType * pieceColour;
                    file++;
                }
            }
        }
        return board;
    }

    public String loadFenFromBoard() {
        StringBuilder fen = new StringBuilder();
        HashMap<Integer, Character> pieceTypeToSymbol = new HashMap<>() {{
            put(1, 'p');
            put(2, 'n');
            put(3, 'b');
            put(4, 'r');
            put(5, 'q');
            put(6, 'k');
        }};
        for (int rank = 7; rank >= 0; rank--) {
            int numEmptyFiles = 0;
            for (int file = 0; file < 8; file++) {
                int i = getSquare(rank, file);
                int piece = board[i];
                if (piece != 0) {
                    if (numEmptyFiles != 0) {
                        fen.append(numEmptyFiles);
                        numEmptyFiles = 0;
                    }
                    boolean isBlack = piece < 0;
                    char symbol = pieceTypeToSymbol.get(p.plusOrMinus(piece));
                    fen.append(isBlack ? Character.toUpperCase(symbol) : symbol);
                } else
                    numEmptyFiles++;
            }
            if (numEmptyFiles != 0)
                fen.append(numEmptyFiles);
            if (rank != 0)
                fen.append('/');
        }
        fen.append(" ").append(Board.whiteToMove ? 'w' : 'b');
        if (MakeMove.castlingRights[2])
            fen.append("K");
        if (MakeMove.castlingRights[3])
            fen.append("Q");
        if (MakeMove.castlingRights[0])
            fen.append("k");
        if (MakeMove.castlingRights[1])
            fen.append("q");

        return fen.toString();
    }

    public int getSquare(int rank, int file) {
        return rank * 16 + file;
    }

    public int getFile(int square) {
        return square & 7;
    }

    public int getRank(int square) {
        return square >> 4;
    }

    public boolean isSq(int square) {
        return (square & 0x88) == 0;
    }
}
