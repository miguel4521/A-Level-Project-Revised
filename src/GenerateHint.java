import javafx.application.Platform;

import java.util.ArrayList;

public class GenerateHint implements Runnable {
    public static boolean hintGenerated = false;
    public static Move move;
    public static String hintText = "";
    MoveGenerator moveGenerator = new MoveGenerator();
    AI ai = new AI();
    GUI gui = new GUI();
    Piece p = new Piece();
    MouseHandler mouseHandler = new MouseHandler();

    @Override
    public void run() {
        hintGenerated = false;
        ArrayList<Move> legalMoves = moveGenerator.generateLegalMoves();
        if (legalMoves.isEmpty()) {
            if (moveGenerator.inCheck())
                System.out.println("Checkmate");
            else
                System.out.println("Stalemate");
            return;
        }
        move = ai.generateMove(legalMoves);
        if (move.isPromotion())
            hintText = "The hint suggests that you promote your pawn. When a pawn is promoted it automatically turns " +
                    "into a queen.";
        else if (move.isEnPassantMove())
            hintText = "The hint suggests that you do an en passant move. En passant is a special pawn capture move " +
                    "that can only occur after a pawn has advanced two squares from its starting rank (row). " +
                    "The attacking pawn captures the other pawn as if it only moved one square. This is because " +
                    "it captures the pawn 'as it passes' through the first square.";
        else if (move.isCastle())
            hintText = "The hint suggests that you 'castle'. Castling consists of moving the king two squares towards " +
                    "the rook on the player's first rank, then moving the rook to the square that the king crossed." +
                    " This move is typically a defensive move to protect the king from the middle of the board.";
        else if (move.isBookMove())
            hintText = "The hint suggests that you do a book move. A book move is a set of moves done at the beginning" +
                    " of the game to put your pieces into a better position. This normally doesn't result in an " +
                    "advantage in material.";
        else if (move.isCaptureMove())
            hintText = "The hint suggests that you capture the " +
                    p.pieceIDToString.get(p.plusOrMinus(move.getPieceCaptured())) + " with your " + p.pieceIDToString.get(p.plusOrMinus(move.getPieceMoved()))
                    + ". The piece captured is worth " + p.pieceIDToValue.get(p.plusOrMinus(move.getPieceCaptured())) +
                    " point(s).";
        else
            hintText = "The hint suggests that you move the " + p.pieceIDToString.get(p.plusOrMinus(move.getPieceMoved())) +
                    " into a better position.";
        if (GUI.showHint) {
            Platform.runLater(() -> {
                gui.drawHint(move);
                GUI.hintButton.setText("Hint generated");
            });
        }
        hintGenerated = true;
        if (GUI.cancelHint) {
            Platform.runLater(() -> {
                if (MouseHandler.cancelMove) {
                    mouseHandler.completeMove();
                    MouseHandler.cancelMove = false;
                } else {
                    if (GUI.newGame)
                        gui.visualNewGame();
                    else
                        gui.visualUndo();
                }
            });
            GUI.cancelHint = false;
        }
    }
}
