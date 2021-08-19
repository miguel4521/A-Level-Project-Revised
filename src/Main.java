import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
    public static GridPane root;
    public final String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    GUI gui = new GUI();
    MouseHandler mouseHandler = new MouseHandler();

    @Override
    public void start(Stage window) {
        root = new GridPane();
        // The colour is the background colour of the board
        window.setScene(new Scene(root, GUI.boardSize + GUI.sqSize * 3, GUI.boardSize));
        window.setTitle("Chess");
        window.setResizable(true);
        root.setStyle("-fx-background-color: rgb(34, 40, 49)");

        gui.drawBoard(root);
        gui.drawPieces(startFen, root);
        mouseHandler.mouseClick(window);
        mouseHandler.doAIMove();

        gui.drawDifficultyPanel();
        gui.drawEvaluation();
        gui.undoButton();

        window.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        //root.setGridLinesVisible(true);
        window.show();
    }

    public static void main(String[] args) {launch(args);}
}
