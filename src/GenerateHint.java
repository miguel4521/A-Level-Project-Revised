import javafx.application.Platform;

import java.util.ArrayList;

public class GenerateHint implements Runnable{
    public static boolean hintGenerated = false;
    MoveGenerator moveGenerator = new MoveGenerator();
    AI ai = new AI();
    GUI gui = new GUI();
    public static Move move;

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
    }
}
