import javafx.application.Platform;

import java.util.ArrayList;

public class GenerateHint implements Runnable {
    public static boolean hintGenerated = false;
    public static Move move;
    MoveGenerator moveGenerator = new MoveGenerator();
    AI ai = new AI();
    GUI gui = new GUI();

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
        if (GUI.showHint) {
            Platform.runLater(() -> {
                gui.drawHint(move);
                GUI.hintButton.setText("Hint generated");
            });
        }
        hintGenerated = true;
        if (GUI.cancelHint) {
            Platform.runLater(() -> gui.visualUndo());
            GUI.cancelHint = false;
        }
    }
}
