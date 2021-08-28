import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    public static GridPane root;
    public final String startFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR";
    GUI gui = new GUI();
    MouseHandler mouseHandler = new MouseHandler();

    @Override
    public void start(Stage window) {
        root = new GridPane();
        window.setScene(new Scene(root, GUI.boardSize + GUI.sqSize * 3, GUI.boardSize));
        window.setTitle("Chess");
        window.setResizable(false);
        root.setStyle("-fx-background-color: rgb(34, 40, 49)");
        window.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("Assets/Chess-icon.png"))));

        gui.drawBoard("rgb(248, 220, 180)", "rgb(184, 140, 100)", true);
        gui.drawPieces(startFen);
        mouseHandler.mouseClick(window);

        gui.createWelcomeScreen(false);

        window.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });
        //root.setGridLinesVisible(true);
        window.show();
    }

    public static void main(String[] args) {launch(args);}
}
