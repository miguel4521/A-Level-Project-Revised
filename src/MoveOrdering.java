import java.util.ArrayList;
import java.util.Collections;

public class MoveOrdering {
    Evaluation e = new Evaluation();
    Piece p = new Piece();

    public void orderMoves(ArrayList<Move> moves) {
        ArrayList<Integer> moveScores = new ArrayList<>();
        for (Move move : moves) {
            int score = 0;
            // Always make it positive
            int pieceMoved = move.getPieceMoved();
            int pieceCaptured = move.getPieceCaptured();

            // Prioritise capturing opponent's most valuable pieces with less valuable pieces
            if (pieceCaptured != 0)
                score = 20 * e.pieceValues.get(p.plusOrMinus(pieceMoved))
                        - e.pieceValues.get(p.plusOrMinus(pieceCaptured));

            // Promoting a pawn is good
            if (move.isPromotion())
                score += e.pieceValues.get(5);
            moveScores.add(score);
        }
        sortMoves(moveScores, moves);
    }

    private void sortMoves(ArrayList<Integer> moveScores, ArrayList<Move> moves) {
        for (int i = 0; i < moveScores.size(); i++) {
            for (int j = i + 1; j < moveScores.size(); j++) {
                int swapIndex = j - 1;
                if (moveScores.get(swapIndex) < moveScores.get(j)) {
                    Collections.swap(moveScores, i, j);
                    Collections.swap(moves, i, j);
                }
            }
        }
    }
}
