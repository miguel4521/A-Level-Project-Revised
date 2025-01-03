import java.util.ArrayList;
import java.util.HashMap;

public class MoveSearch {
    public static boolean abortSearch = false;
    private final int CHECKMATE = (int) Double.POSITIVE_INFINITY;
    private final HashMap<Integer, Integer> minDistanceFromMax = new HashMap<>() {{
        put(1, 1);
        put(2, 1);
        put(3, 2);
        put(4, 2);
    }};
    private final MoveGenerator moveGenerator = new MoveGenerator();
    Evaluation e = new Evaluation();
    MoveOrdering moveOrdering = new MoveOrdering();
    MakeMove makeMove = new MakeMove();
    long timeStarted;
    int currentIterativeSearchDepth;
    Move bestMoveThisIteration;
    Move bestMove;
    Move bestMoveInThisPosition;
    int bestEval;
    GUI gui = new GUI();
    int bestEvalThisIteration = bestEval = 0;

    public Move startSearch() {
        abortSearch = false;
        timeStarted = System.currentTimeMillis();
        for (int searchDepth = minDistanceFromMax.get(GUI.difficulty); searchDepth <= GUI.difficulty; searchDepth++) {
            moveSearch(searchDepth, 0, -CHECKMATE, CHECKMATE, Board.whiteToMove ? 1 : -1);
            if (abortSearch || outOfTime() && currentIterativeSearchDepth > 0)
                break;
            else {
                currentIterativeSearchDepth = searchDepth;
                bestMove = bestMoveThisIteration;
                bestEval = bestEvalThisIteration;
            }
        }
        return bestMove;
    }

    private boolean outOfTime() {
        return (System.currentTimeMillis() - timeStarted) > 30000;
    }

    private int moveSearch(int depth, int plyFromRoot, int alpha, int beta, int turnMultiplier) {
        if (abortSearch || outOfTime() && currentIterativeSearchDepth > 0)
            return 0;
        if (plyFromRoot > 0) {
            alpha = Math.max(alpha, -CHECKMATE + plyFromRoot);
            beta = Math.min(beta, CHECKMATE - plyFromRoot);
            if (alpha >= beta)
                return alpha;
        }
        if (depth == 0)
            return quiesceSearch(alpha, beta, turnMultiplier);
        ArrayList<Move> moves = moveGenerator.generateLegalMoves();
        moveOrdering.orderMoves(moves);
        if (moves.isEmpty()) {
            if (moveGenerator.inCheck())
                return -(CHECKMATE - plyFromRoot);
            else
                return 0;
        }
        for (Move move : moves) {
            makeMove.makeMove(move);
            int eval = -moveSearch(depth - 1, plyFromRoot + 1, -beta, -alpha, -turnMultiplier);
            makeMove.undoMove();
            if (eval >= beta)
                return beta;
            if (eval > alpha) {
                bestMoveInThisPosition = move;
                alpha = eval;
                if (plyFromRoot == 0) {
                    bestEvalThisIteration = eval;
                    bestMoveThisIteration = move;
                    gui.updateEvaluation(bestEvalThisIteration);
                }
            }
        }
        return alpha;
    }

    private int quiesceSearch(int alpha, int beta, int turnMultiplier) {
        int eval = e.evaluate(Board.board) * turnMultiplier;
        if (abortSearch)
            return 0;
        if (eval >= beta)
            return beta;
        if (eval > alpha)
            alpha = eval;
        ArrayList<Move> moves = moveGenerator.generateLegalMoves();
        moves.removeIf(captureMoves -> !captureMoves.isCaptureMove());
        moveOrdering.orderMoves(moves);
        for (Move move : moves) {
            makeMove.makeMove(move);
            eval = -quiesceSearch(-beta, -alpha, -turnMultiplier);
            makeMove.undoMove();
            if (eval >= beta)
                return beta;
            if (eval > alpha)
                alpha = eval;
        }
        return alpha;
    }
}
