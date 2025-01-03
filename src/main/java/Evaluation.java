import java.util.HashMap;
import java.util.Map;

public class Evaluation {
    Board b = new Board();
    PieceSquareTable pieceSquareTable = new PieceSquareTable();
    Piece p = new Piece();
    public final Map<Integer, Integer> pieceValues = new HashMap<>() {{
        put(p.king, 0);
        put(p.queen, 900);
        put(p.rook, 500);
        put(p.bishop, 330);
        put(p.knight, 320);
        put(p.pawn, 100);
    }};
    private final float endGameMaterialStart = pieceValues.get(p.rook) * 2 + pieceValues.get(p.bishop) + pieceValues.get(p.knight);

    public int evaluate(int[] board) {
        int whiteEval = 0;
        int blackEval = 0;

        int whiteMaterial = countMaterial(board, true, true);
        int blackMaterial = countMaterial(board, false, true);

        int whiteMaterialWithoutPawns = countMaterial(board, true, false);
        int blackMaterialWithoutPawns = countMaterial(board, false, false);
        float whiteEndGameWeight = endGamePhaseWeight(whiteMaterialWithoutPawns);
        float blackEndGameWeight = endGamePhaseWeight(blackMaterialWithoutPawns);

        whiteEval += whiteMaterial;
        blackEval += blackMaterial;
        whiteEval += forceKingToCornerEndgameEval(Board.whiteKingLocation, Board.blackKingLocation, whiteEndGameWeight);
        blackEval += forceKingToCornerEndgameEval(Board.blackKingLocation, Board.whiteKingLocation, blackEndGameWeight);

        whiteEval += evaluatePieceSquareTables(board, true);
        blackEval += evaluatePieceSquareTables(board, false);

        return whiteEval - blackEval;
    }

    private float endGamePhaseWeight(int materialWithoutPawns) {
        float multiplier = 1 / endGameMaterialStart;
        return 1 - Math.min(1, materialWithoutPawns * multiplier);
    }

    private int countMaterial(int[] board, boolean countWhite, boolean countPawns) {
        int material = 0;
        for (int sq = 0; sq < 120; sq++) {
            if (!b.isSq(sq))
                continue;
            int piece = board[sq];
            if (p.isPiece(piece, p.pawn) && !countPawns)
                continue;
            if (piece > 0 && countWhite)
                material += pieceValues.get(p.plusOrMinus(piece));
            else if (piece < 0 && !countWhite)
                material += pieceValues.get(p.plusOrMinus(piece));
        }
        return material;
    }

    private int evaluatePieceSquareTables(int[] board, boolean evaluateWhite) {
        int value = 0;
        for (int sq = 0; sq < 120; sq++) {
            if (!b.isSq(sq))
                continue;
            int piece = board[sq];
            if (p.isPiece(piece, p.king))
                continue;
            if (piece > 0 && evaluateWhite)
                value += evaluatePieceSquareTable(p.plusOrMinus(piece), true, sq);
            else if (piece < 0 && !evaluateWhite)
                value += evaluatePieceSquareTable(p.plusOrMinus(piece), false, sq);
        }
        return value;
    }

    private int evaluatePieceSquareTable(int piece, boolean isWhite, int sq) {
        return pieceSquareTable.read(piece, isWhite, sq);
    }

    private int forceKingToCornerEndgameEval(int friendlyKingSquare, int opponentKingSquare, float endgameWeight) {
        int eval = 0;
        int opponentKingRank = b.getRank(opponentKingSquare);
        int opponentKingFile = b.getFile(opponentKingSquare);

        int opponentKingDstToCentreFile = Math.max(3 - opponentKingFile, opponentKingFile - 4);
        int opponentKingDstToCentreRank = Math.max(3 - opponentKingRank, opponentKingRank - 4);
        int opponentKingDstFromCentre = opponentKingDstToCentreFile + opponentKingDstToCentreRank;
        eval += opponentKingDstFromCentre;

        int friendlyKingRank = b.getRank(friendlyKingSquare);
        int friendlyKingFile = b.getFile(friendlyKingRank);

        int dstBetweenKingsFile = Math.abs(friendlyKingFile - opponentKingFile);
        int dstBetweenKingsRank = Math.abs(friendlyKingRank - opponentKingRank);

        int dstBetweenKings = dstBetweenKingsFile + dstBetweenKingsRank;

        eval += 14 - dstBetweenKings;

        return (int) (eval * 10 * endgameWeight);
    }
}
