import java.util.ArrayList;
import java.util.Objects;

public class MoveSearch {
    private final int CHECKMATE = (int) Double.POSITIVE_INFINITY;
    private
    MoveGenerator moveGenerator = new MoveGenerator();
    Evaluation e = new Evaluation();
    Board b = new Board();
    MoveOrdering moveOrdering = new MoveOrdering();
    MakeMove makeMove = new MakeMove();
    long timeStarted;
    public static boolean abortSearch = false;
    int currentIterativeSearchDepth;
    Move bestMoveThisIteration;
    Move bestMove;
    Move bestMoveInThisPosition;
    int bestEval;
    GUI gui = new GUI();
    int bestEvalThisIteration = bestEval = 0;

    private static int MIN_DISTANCE = 3;
    public static int MAX_DISTANCE = 4;

    public Move startSearch() {
        abortSearch = false;
        timeStarted = System.currentTimeMillis();
        for (int searchDepth = MIN_DISTANCE; searchDepth <= MAX_DISTANCE; searchDepth++) {
            moveSearch(MAX_DISTANCE, 0, -CHECKMATE, CHECKMATE, Board.whiteToMove ? 1 : -1);
            if (abortSearch || outOfTime())
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
        return (System.currentTimeMillis() - timeStarted) > 20000;
    }

    private int moveSearch(int depth, int plyFromRoot, int alpha, int beta, int turnMultiplier) {
        System.out.println(currentIterativeSearchDepth);
        if (abortSearch || outOfTime() && currentIterativeSearchDepth > 0)
            return 0;
        if (plyFromRoot > 0) {
            if (drawByRepetition()) // Prevent draw by repetition
                return -300;
            alpha = Math.max(alpha, -CHECKMATE + plyFromRoot);
            beta = Math.min(beta, CHECKMATE - plyFromRoot);
            if (alpha >= beta)
                return alpha;
        }
        if (depth == 0) {
            if (currentIterativeSearchDepth > 0)
            return quiesceSearch(alpha, beta, turnMultiplier);
            else
                return e.evaluate(Board.board) * turnMultiplier;
        }
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
                    //bestMove = move;
                }
            }
        }
        return alpha;
    }

    private boolean drawByRepetition() {
        int counter = 0;
        for (String fen : Board.fenHistory) {
            if (Objects.equals(fen, b.loadFenFromBoard()))
                counter++;
            if (counter > 0)
                return true;
        }
        return false;
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
