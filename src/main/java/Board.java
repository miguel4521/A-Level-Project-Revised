import java.util.HashMap;

public class Board {
    public static int[] board = new int[128];
    public static boolean whiteToMove = true;
    public static int whiteKingLocation = 116;
    public static int blackKingLocation = 4;

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
