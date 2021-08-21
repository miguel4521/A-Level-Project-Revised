import java.util.ArrayList;
import java.util.Objects;

public class MoveSearch {
    private final int CHECKMATE = (int) Double.POSITIVE_INFINITY;
    MoveGenerator moveGenerator = new MoveGenerator();
    Evaluation e = new Evaluation();
    Board b = new Board();
    MoveOrdering moveOrdering = new MoveOrdering();
    MakeMove makeMove = new MakeMove();
    long timeStarted;
    boolean abortSearch = false;
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
        timeStarted = System.currentTimeMillis();
        for (int searchDepth = MIN_DISTANCE; searchDepth <= MAX_DISTANCE; searchDepth++) {
            moveSearch(MAX_DISTANCE, 0, -CHECKMATE, CHECKMATE, Board.whiteToMove ? 1 : -1);
            if (abortSearch)
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
        abortSearch = outOfTime();
        if (abortSearch)
            return 0;
        if (plyFromRoot > 0) {
            if (drawByRepetition()) // Prevent draw by repetition
                return 0;
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
                    bestMove = move;
                }
            }
        }
        return alpha;
    }

/*    private boolean drawByRepetition() {
        int counter = 0;
        for (String fen : Board.fenHistory) {
            if (Objects.equals(fen, b.loadFenFromBoard()))
                counter++;
            if (counter > 1)
                return true;
        }
        return false;
    }*/

    private boolean drawByRepetition() {
        Move[] chainList = new Move[24];
        Move m;
        int c = 0, i;
        for (i = 0; i < 24; i++)
            chainList[i] = null;
        loop:
        for (i = MakeMove.moveLog.size(); i -- > 0;) {
            m = MakeMove.moveLog.get(i);
            if (!m.isIrreversible()) {
                for (i = 0; i < 24; i++) {
                    if (chainList[i] != null && m.getEndSq() == chainList[i].getStartSq()) {
                        if (m.getStartSq() == chainList[i].getEndSq()) {
                            if (c-- == 0)
                                return true;
                            chainList[i] = null;
                            continue loop;
                        }
                        chainList[i].setStartSq(m.getStartSq());
                        continue loop;
                    }
                }
                for (i = 0; i < 24; i++) {
                    if (chainList[i] == null) {
                        chainList[i] = m;
                        c++;
                        continue loop;
                    }
                }
            }
        }
        return false;
    }

    private int quiesceSearch(int alpha, int beta, int turnMultiplier) {
        int eval = e.evaluate(Board.board) * turnMultiplier;
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
