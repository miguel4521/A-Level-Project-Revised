import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;

public class MouseHandler {
    public static ArrayList<Move> moves = new ArrayList<>();
    GUI gui = new GUI();
    Board b = new Board();
    MakeMove makeMove = new MakeMove();
    MoveGenerator moveGenerator = new MoveGenerator();
    AI ai = new AI();
    private int squareSelected = -1;
    public static Thread task;
    int[] board;

    public void mouseClick(Stage stage) {
        moves = moveGenerator.generateLegalMoves();
        EventHandler<MouseEvent> eventHandler = e -> {
            int file = (int) e.getX() / gui.sqSize;
            int rank = (int) e.getY() / gui.sqSize;
            int square = b.getSquare(rank, file);
            if (square < 128)
                moveClick(square, rank, file);
        };
        stage.addEventFilter(MouseEvent.MOUSE_CLICKED, eventHandler);
    }

    private void moveClick(int square, int rank, int file) {
        gui.destroyCircles();
        if (!AI.thinking)
            board = Board.board;
        if (board[square] != 0) {
            gui.drawSquare(rank, file);
            gui.drawLegalMoves(square, moves);
        }
        if (squareSelected != square) {
            int[] moveID = new int[]{squareSelected, square};
            for (Move legalMove : moves) {
                if (Arrays.equals(legalMove.getMoveID(), moveID)) {
                    gui.removeSquare();
                    makeMove.makeMove(legalMove);
                    gui.moveImages(legalMove);
                    squareSelected = -1;
                    ai.addToChessNotationMoveLog(legalMove, moves);
                    Board.fenHistory.add(b.loadFenFromBoard());
                    //moves = moveGenerator.generateLegalMoves();
                    doAIMove();
                } else
                    squareSelected = square;
            }
        } else {
            gui.removeSquare();
            gui.destroyCircles();
            squareSelected = -1;
        }
    }

    public void doAIMove() {
        Runnable r = new AI();
        task = new Thread(r);
        task.start();
    }
}