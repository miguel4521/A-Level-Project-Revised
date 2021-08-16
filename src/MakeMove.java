import java.util.ArrayList;
import java.util.Collections;

public class MakeMove {
    public static ArrayList<Move> moveLog = new ArrayList<>();
//     black king castle, black queen castle, white king castle, black king castle
    public static boolean[] castlingRights = {true, true, true, true};
    public static ArrayList<boolean[]> castlingRightsLog = new ArrayList<>(
            Collections.singletonList(new boolean[]{true, true, true, true}));
    public static int doublePawnAdvanceDestination;
    Piece p = new Piece();
    Board b = new Board();

    public void makeMove(Move move) {
        int startSq = move.getStartSq();
        int endSq = move.getEndSq();
        Board.board[startSq] = p.none;
        Board.board[endSq] = move.getPieceMoved();
        if (p.isPiece(move.getPieceMoved(), p.king)) {
            if (move.getPieceMoved() > 0)
                Board.whiteKingLocation = endSq;
            else
                Board.blackKingLocation = endSq;
        }
        if (move.isEnPassantMove())
            doEnpassantMove(move);
        if (move.isPromotion())
            Board.board[move.getEndSq()] = move.getPieceMoved() > 0 ? 5 : -5;
        castlingRightsLog.add(new boolean[]
                {castlingRights[0], castlingRights[1], castlingRights[2], castlingRights[3]});
        updateCastlingRights(move);
        if (move.isCastle())
            doCastleMove(move);
        if (move.isDoubleSqMove())
            doublePawnAdvanceDestination = move.getEndSq();
        moveLog.add(move);
        Board.whiteToMove = !Board.whiteToMove;
    }

    public void undoMove() {
        if (!moveLog.isEmpty()) {
            Move move = moveLog.get(moveLog.size() - 1);
            moveLog.remove(moveLog.size() - 1);
            Board.board[move.getStartSq()] = move.getPieceMoved();
            Board.board[move.getEndSq()] = move.getPieceCaptured();
            if (move.getPieceMoved() == p.king)
                Board.whiteKingLocation = move.getStartSq();
            else if (move.getPieceMoved() == -p.king)
                Board.blackKingLocation = move.getStartSq();
            if (move.isEnPassantMove())
                undoEnpassantMove(move);
            if (!castlingRightsLog.isEmpty()) {
                castlingRights = castlingRightsLog.get(castlingRightsLog.size() - 1);
                castlingRightsLog.remove(castlingRightsLog.size() - 1);
            }
            if (move.isCastle())
                undoCastleMove(move);
            Board.whiteToMove = !Board.whiteToMove;
        }
    }

    private void doEnpassantMove(Move move) {
        if (Board.whiteToMove)
            Board.board[move.getEndSq() + 16] = 0;
        else
            Board.board[move.getEndSq() - 16] = 0;
    }

    private void undoEnpassantMove(Move move) {
        if (Board.whiteToMove)
            Board.board[move.getEndSq() - 16] = 1;
        else
            Board.board[move.getEndSq() + 16] = -1;
    }

    private void undoCastleMove(Move move) {
        if (move.getEndSq() - move.getStartSq() == 2) {
            if (move.getStartSq() == 116) {
                Board.board[119] = p.rook;
                Board.board[117] = p.none;
            } else {
                Board.board[7] = -p.rook;
                Board.board[5] = p.none;
            }
        } else {
            if (move.getStartSq() == 116) {
                Board.board[112] = p.rook;
                Board.board[115] = p.none;
            } else {
                Board.board[0] = -p.rook;
                Board.board[3] = p.none;
            }
        }
    }

    private void doCastleMove(Move move) {
        if (move.getEndSq() - move.getStartSq() == 2) {
            if (move.getStartSq() == 116) {
                Board.board[119] = p.none;
                Board.board[117] = p.rook;
            } else {
                Board.board[7] = p.none;
                Board.board[5] = -p.rook;
            }
        } else {
            if (move.getStartSq() == 116) {
                Board.board[112] = p.none;
                Board.board[115] = p.rook;
            } else {
                Board.board[0] = p.none;
                Board.board[3] = -p.rook;
            }
        }
    }

    private void updateCastlingRights(Move move) {
        int file = b.getFile(move.getStartSq());
        if (move.getPieceMoved() == -p.rook) { // If black
            if (file == 7) // Change rights for left or right
                castlingRights[0] = false;
            else
                castlingRights[1] = false;
        } else if (move.getPieceMoved() == p.rook) {
            if (file == 7)
                castlingRights[2] = false;
            else
                castlingRights[3] = false;
        } else if (move.getPieceMoved() == -p.king) { // Change rights for white/black altogether
            castlingRights[0] = false;
            castlingRights[1] = false;
        } else if (move.getPieceMoved() == p.king) {
            castlingRights[2] = false;
            castlingRights[3] = false;
        }
    }
}
