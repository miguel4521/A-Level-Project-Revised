public class Move {
    private final int startSq, endSq, pieceMoved, pieceCaptured;
    private final boolean isDoubleSqMove;
    private final boolean isCaptureMove;
    private final int[] moveID;
    Piece p = new Piece();
    Board b = new Board();
    private boolean isCastle;
    private boolean enPassantMove;
    private boolean isPromotion = false;

    public Move(int startSq, int endSq) {
        this.startSq = startSq;
        this.endSq = endSq;
        this.pieceMoved = Board.board[startSq];
        this.pieceCaptured = Board.board[endSq];
        this.moveID = new int[]{startSq, endSq};
        isDoubleSqMove = p.isPiece(pieceMoved, p.pawn) && endSq - 32 == startSq || endSq + 32 == startSq;
        if (p.isPiece(getPieceMoved(), p.pawn) && (b.getRank(endSq) == 0 || b.getRank(endSq) == 7))
            isPromotion = true;
        isCaptureMove = pieceCaptured != p.none;
    }

    public int getStartSq() {
        return startSq;
    }

    public int getEndSq() {
        return endSq;
    }

    public int getPieceMoved() {
        return pieceMoved;
    }

    public int getPieceCaptured() {
        return pieceCaptured;
    }

    public int[] getMoveID() {
        return moveID;
    }

    public Move setCastle() {
        isCastle = true;
        return this;
    }

    public Move setEnPassantMove() {
        enPassantMove = true;
        return this;
    }

    public boolean isCastle() {
        return isCastle;
    }

    public boolean isEnPassantMove() {
        return enPassantMove;
    }

    public boolean isDoubleSqMove() {
        return isDoubleSqMove;
    }

    public boolean isPromotion() {
        return isPromotion;
    }

    public boolean isCaptureMove() {
        return isCaptureMove;
    }
}
