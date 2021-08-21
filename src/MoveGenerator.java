import java.util.ArrayList;
import java.util.Arrays;

public class MoveGenerator extends Board {
    // 0 - North, 1 - south, 2 - west, 3 - east, 4 - nw, 5 - ne, 6 - sw, 7 - se
    private final int[] vectors = {-16, 16, 1, -1, -15, -17, 15, 17};
    private final int[] knightVectors = {-33, -31, -18, -14, 14, 18, 31, 33};
    MakeMove makeMove = new MakeMove();

    public ArrayList<Move> generateLegalMoves() {
        ArrayList<Move> moves = pseudoLegalMoveGeneration();
        castleMoves(moves);
        // Make each move
        for (int i = moves.size(); i-- > 0; ) {
            makeMove.makeMove(moves.get(i));
            whiteToMove = !whiteToMove;
            if (inCheck())
                moves.remove(moves.get(i));
            whiteToMove = !whiteToMove;
            makeMove.undoMove();
        }
        return moves;
    }

    public boolean squareAttackedBySamePieceFile(Move pMove, ArrayList<Move> moves) {
        int counter = 0;
        for (Move move : moves) {
            if (Arrays.equals(move.getMoveID(), pMove.getMoveID())
                    && (move.getPieceMoved() == pMove.getPieceMoved())) {
                counter++;
            }
            if (counter > 1)
                return true;
        }
        return false;
    }

    public boolean squareAttackedBySamePieceRank(Move pMove, ArrayList<Move> moves) {
        int counter = 0;
        for (Move move : moves) {
            if (Arrays.equals(move.getMoveID(), pMove.getMoveID())
                    && move.getPieceMoved() == pMove.getPieceMoved())
                counter++;
            if (counter > 1)
                return true;
        }
        return false;
    }

    private void castleMoves(ArrayList<Move> moves) {
        if (canCastleKing()) {
            if (whiteToMove)
                moves.add(new Move(116, 118).setCastle());
            else
                moves.add(new Move(4, 6).setCastle());
        }
        if (canCastleQueen()) {
            if (whiteToMove)
                moves.add(new Move(116, 114).setCastle());
            else
                moves.add(new Move(4, 2).setCastle());
        }
    }

    private boolean canCastleKing() {
        boolean[] castlingRights = MakeMove.castlingRights;
        if (whiteToMove)
            return castlingRights[2] && !inCheck() && canCastleThroughSquare(117) && canCastleThroughSquare(118);
        else
            return castlingRights[0] && !inCheck() && canCastleThroughSquare(5) && canCastleThroughSquare(6);
    }

    private boolean canCastleQueen() {
        boolean[] castlingRights = MakeMove.castlingRights;
        if (whiteToMove)
            return castlingRights[3] && !inCheck() && canCastleThroughSquare(113) && canCastleThroughSquare(114)
                    && canCastleThroughSquare(115);
        else
            return castlingRights[1] && !inCheck() && canCastleThroughSquare(1) && canCastleThroughSquare(2)
                    && canCastleThroughSquare(3);
    }

    private boolean canCastleThroughSquare(int square) {
        return board[square] == p.none && !isSquareAttacked(square);
    }

    public boolean inCheck() {
        if (whiteToMove)
            return isSquareAttacked(whiteKingLocation);
        else
            return isSquareAttacked(blackKingLocation);
    }

    private boolean isSquareAttacked(int square) {
        whiteToMove = !whiteToMove;
        ArrayList<Move> enemyMoves = pseudoLegalMoveGeneration();
        whiteToMove = !whiteToMove;
        for (Move move : enemyMoves) {
            if (move.getEndSq() == square)
                return true;
        }
        return false;
    }

    private ArrayList<Move> pseudoLegalMoveGeneration() {
        ArrayList<Move> moves = new ArrayList<>();
        for (int sq = 0; sq < 120; sq++) {
            // Continue if square isn't on board
            if (!isSq(sq))
                continue;
            int piece = board[sq];
            boolean white = piece > 0;
            // Skip if it's the opposition's colour
            if (piece < 0 && whiteToMove || piece > 0 && !whiteToMove)
                continue;
            // Add moves to array list depending on piece type
            if (p.isPiece(piece, p.pawn))
                generatePawnMoves(sq, white, moves);
            else if (p.isPiece(piece, p.knight))
                generateKnightMoves(sq, white, moves);
                // Sliding pieces are very similar to each other
            else if (p.isSlidingPiece(piece))
                generateSlidingMoves(sq, white, moves);
            else if (p.isPiece(piece, p.king))
                generateKingMoves(sq, white, moves);
        }
        return moves;
    }

    private void generatePawnMoves(int sq, boolean white, ArrayList<Move> moves) {
        generatePawnAdvances(sq, white, moves);
        generatePawnCaptures(sq, white, moves);
    }

    private void generatePawnAdvances(int sq, boolean white, ArrayList<Move> moves) {
        // Pawn goes north if white, south if black
        int pawnPush = white ? vectors[0] : vectors[1];
        // Used to see if piece can do double advance
        int startRank = white ? 6 : 1;
        // Single advance if place in front is empty
        if (isSq(sq + pawnPush) && (board[sq + pawnPush] == p.none)) {
            moves.add(new Move(sq, sq + pawnPush));
            // Double advance
            if (isSq(sq + pawnPush * 2) && board[sq + pawnPush * 2] == p.none &&
                    getRank(sq) == startRank)
                moves.add(new Move(sq, sq + pawnPush * 2));
        }
    }

    private void generatePawnCaptures(int sq, boolean white, ArrayList<Move> moves) {
        int NE = vectors[5], NW = vectors[4], SW = vectors[6], SE = vectors[7];
        if (white) { // Diagonal captures for white
            if (isSq(sq + NE) && (board[sq + NE] < 0))
                moves.add(new Move(sq, sq + NE));
            if (isSq(sq + NW) && (board[sq + NW] < 0))
                moves.add(new Move(sq, sq + NW));
            if (isSq(sq + vectors[3]) && board[sq + vectors[3]] == -1 &&
                    sq + vectors[3] == MakeMove.doublePawnAdvanceDestination)
                moves.add(new Move(sq, sq + NE).setEnPassantMove());
            if (isSq(sq + vectors[2]) && board[sq + vectors[2]] == -1 &&
                    sq + vectors[2] == MakeMove.doublePawnAdvanceDestination)
                moves.add(new Move(sq, sq + NW).setEnPassantMove());
        } else { // Diagonal captures for black
            if (isSq(sq + SE) && (board[sq + SE] > 0))
                moves.add(new Move(sq, sq + SE));
            if (isSq(sq + SW) && (board[sq + SW] > 0))
                moves.add(new Move(sq, sq + SW));
            if (isSq(sq + vectors[3]) && board[sq + vectors[3]] == 1 &&
                    sq + vectors[3] == MakeMove.doublePawnAdvanceDestination)
                moves.add(new Move(sq, sq + SW).setEnPassantMove());
            if (isSq(sq + vectors[2]) && board[sq + vectors[2]] == 1 &&
                    sq + vectors[2] == MakeMove.doublePawnAdvanceDestination)
                moves.add(new Move(sq, sq + SE).setEnPassantMove());
        }
    }

    private void generateKnightMoves(int sq, boolean white, ArrayList<Move> moves) {
        generateSingleMoves(sq, white, moves, knightVectors);
    }

    private void generateSlidingMoves(int sq, boolean white, ArrayList<Move> moves) {
        int piece = board[sq];
        if (p.isPiece(piece, p.bishop))
            generateSlidingPieceMoves(sq, white, moves, 4, 8);
        else if (p.isPiece(piece, p.rook))
            generateSlidingPieceMoves(sq, white, moves, 0, 4);
        else if (p.isPiece(piece, p.queen))
            generateSlidingPieceMoves(sq, white, moves, 0, 8);
    }

    private void generateSlidingPieceMoves(int sq, boolean white, ArrayList<Move> moves,
                                           int startVecIndex, int endVecIndex) {
        // Iterate through each direction
        for (int direction = startVecIndex; direction < endVecIndex; direction++) {
            // Keep iterating until it reaches the end of the board
            for (int i = 1; i < 8; i++) {
                int endSq = sq + vectors[direction] * i;
                if (!isSq(endSq))
                    break;
                int endPiece = board[endSq];
                // If it's an empty space, add it to the list and keep iterating
                if (endPiece == p.none)
                    moves.add(new Move(sq, endSq));
                    // If it's an opposite colour add it to the list and break the iteration
                else if ((endPiece > 0 && !white) || (endPiece < 0 && white)) {
                    moves.add(new Move(sq, endSq));
                    break;
                    // Otherwise, just stop
                } else
                    break;
            }
        }
    }

    private void generateKingMoves(int sq, boolean white, ArrayList<Move> moves) {
        generateSingleMoves(sq, white, moves, vectors);
    }

    private void generateSingleMoves(int sq, boolean white, ArrayList<Move> moves, int[] vectors) {
        for (int direction = 0; direction < 8; direction++) {
            int endSq = sq + vectors[direction];
            if (!isSq(endSq))
                continue;
            int endPiece = board[endSq];
            if ((endPiece > 0 && !white) || (endPiece < 0 && white) || endPiece == 0) {
                moves.add(new Move(sq, endSq));
            }
        }
    }
}
