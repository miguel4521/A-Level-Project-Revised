public class Move {
    private final int pieceMoved, pieceCaptured;
    private final boolean isDoubleSqMove, isCaptureMove;
    private final int[] moveID, board;
    Piece p = new Piece();
    Board b = new Board();
    private int startSq, endSq;
    private boolean isCastle, enPassantMove, isPromotion = false, isUndoMove, isBookMove;

    public Move(int startSq, int endSq) {
        this.startSq = startSq;
        this.endSq = endSq;
        board = Board.board;
        this.pieceMoved = board[startSq];
        this.pieceCaptured = board[endSq];
        this.moveID = new int[]{startSq, endSq};
        isDoubleSqMove = p.isPiece(pieceMoved, p.pawn) && endSq - 32 == startSq || endSq + 32 == startSq;
        if (p.isPiece(getPieceMoved(), p.pawn) && (b.getRank(endSq) == 0 || b.getRank(endSq) == 7))
            isPromotion = true;
        isCaptureMove = pieceCaptured != p.none;
    }

    public int getStartSq() {
        return startSq;
    }

    public void setStartSq(int startSq) {
        this.startSq = startSq;
    }

    public int getEndSq() {
        return endSq;
    }

    public void setEndSq(int endSq) {
        this.endSq = endSq;
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

    public boolean isBookMove() {
        return isBookMove;
    }

    public Move setCastle() {
        isCastle = true;
        return this;
    }

    public Move setEnPassantMove() {
        enPassantMove = true;
        return this;
    }

    public Move setUndoMove() {
        isUndoMove = true;
        return this;
    }

    public Move setBookMove() {
        isBookMove = true;
        return this;
    }

    public boolean isUndoMove() {
        return isUndoMove;
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

    public int[] getBoard() {
        return board;
    }

}
