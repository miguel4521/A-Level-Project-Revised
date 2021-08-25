import java.util.HashMap;

public class Piece {
    public int none = 0, pawn = 1, knight = 2, bishop = 3, rook = 4, queen = 5, king = 6, white = 1, black = -1;

    public final HashMap<Integer, String> pieceIDToString = new HashMap<>() {{
        put(1, "pawn");
        put(2, "knight");
        put(3, "bishop");
        put(4, "rook");
        put(5, "queen");
        put(6, "king");
    }};

    public final HashMap<Integer, String> pieceIDToValue = new HashMap<>() {{
        put(1, "1");
        put(2, "3");
        put(3, "3");
        put(4, "5");
        put(6, "6");
    }};

    public int plusOrMinus(int piece) {
        return piece * (piece < 0 ? -1 : 1);
    }

    public boolean isSlidingPiece(int piece) {
        return isPiece(piece, 3) || isPiece(piece, 4) || isPiece(piece, 5);
    }

    // Checks whether the two inputted pieces are the same
    public boolean isPiece(int piece1, int piece2) {
        return piece1 * (piece1 > 0 ? 1 : -1) == piece2;
    }

    private boolean isSameColour(int piece, int colour) {
        return piece > 0 && colour == 1 || piece < 0 && colour == -1;
    }

    public int pieceType(int piece) {
        return piece * (piece > 0 ? 1 : -1);
    }

}
