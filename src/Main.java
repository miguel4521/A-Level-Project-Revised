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
        window.setScene(new Scene(root, gui.boardSize  + gui.sqSize * 3, gui.boardSize, Color.rgb(66, 66, 66)));
        window.setTitle("Chess");
        window.setResizable(true);
        window.show();

        gui.drawBoard(root);
        gui.drawPieces(startFen, root);
        mouseHandler.mouseClick(window);
        mouseHandler.doAIMove();

        window.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
    }
}