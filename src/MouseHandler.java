import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;

public class MouseHandler {
    public static ArrayList<Move> moves = new ArrayList<>();
    public static int squareSelected;
    public static Thread task;
    GUI gui = new GUI();
    Board b = new Board();
    MakeMove makeMove = new MakeMove();
    AI ai = new AI();

    public void mouseClick(Stage stage) {
        EventHandler<MouseEvent> eventHandler = e -> {
            int file = (int) e.getX() / GUI.sqSize;
            int rank = (int) e.getY() / GUI.sqSize;
            int square = b.getSquare(rank, file);
            if (square < 128)
                moveClick(square, rank, file);
        };
        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
    }

    private static Move move;
    public static boolean cancelMove = false;

    private void moveClick(int square, int rank, int file) {
        gui.destroyCircles();
        if (rank > 7 || file > 7)
            return;
        if (GUI.images[rank][file] != null)
            gui.drawSquare(rank, file);
        if (!AI.thinking) {
            if (GUI.images[rank][file] != null)
                gui.drawLegalMoves(square, moves);
            if (squareSelected != square) {
                int[] moveID = new int[]{squareSelected, square};
                for (Move legalMove : moves) {
                    if (Arrays.equals(legalMove.getMoveID(), moveID)) {
                        move = legalMove;
                        if (!GenerateHint.hintGenerated) {
                            MoveSearch.abortSearch = true;
                            cancelMove = true;
                            GUI.cancelHint = true;
                        }
                        else
                            completeMove();
                        break;
                    } else
                        squareSelected = square;
                }
            } else {
                gui.destroyCircles();
                gui.removeSquare();
                squareSelected = -1;
            }
        }
    }

    public void doAIMove() {
        Runnable r = new AI();
        task = new Thread(r);
        task.start();
    }

    public void completeMove() {
        gui.removeHint();
        gui.removeSquare();
        GUI.showHint = false;
        GenerateHint.move = null;
        GUI.tipsText.setText("");

        GUI.hintButton.setText("AI's Move");
        makeMove.makeMove(move);
        gui.moveImages(move);
        ai.addToChessNotationMoveLog(move, moves);
        Board.fenHistory.add(b.loadFenFromBoard());
        squareSelected = -1;
        doAIMove();
        move = null;
        GUI.tipsText.setText("");
        //moves = moveGenerator.generateLegalMoves();
    }
}
