public class Piece {
    public int none = 0, pawn = 1, knight = 2, bishop = 3, rook = 4, queen = 5, king = 6, white = 1, black = -1;

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

    public int pieceCount(int piece, boolean countWhite) {
        int count = 0;
        for (int i = 0; i < 120; i++) {
            if (Board.board[i] == piece && isSameColour(piece, countWhite ? 1 : -1))
                count++;
        }
        return count;
    }

}
