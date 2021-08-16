import javafx.animation.PathTransition;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GUI {
    private static final StackPane startTile = new StackPane(), endTile = new StackPane();
    private static final StackPane square = new StackPane();
    private static final ArrayList<Circle> circles = new ArrayList<>();
    public static ImageView[][] images = new ImageView[8][8];
    final int dimension = 8;
    Piece p = new Piece();
    Board b = new Board();
    boolean errorFound = false;
    int boardSize = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
    int sqSize = boardSize / dimension;

    private static void delImage(ImageView image) {
        image.setImage(null);
    }

    public void drawBoard(GridPane root) {
        String colour;
        // This is the start of the nested for loop
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                // initialises each "pane"/tile
                StackPane boardTile = new StackPane();
                // This makes every other colour alter, e.g. first tile is light, next tile is dark
                if ((row + col) % 2 == 0)
                    colour = "rgb(248,220,180)";
                else
                    colour = "rgb(184,140,100)";
                // sets the colour of the tile
                boardTile.setStyle("-fx-background-color: " + colour + ";");
                boardTile.setViewOrder(3);
                // adds the tile to the window, and sets where the pane needs to go
                root.add(boardTile, col, row);
            }
        }
        for (int i = 0; i < dimension; i++) {
            // Set Constraints
            root.getColumnConstraints().add(new ColumnConstraints(sqSize));
            root.getRowConstraints().add(new RowConstraints(sqSize));
        }
    }

    public void drawPieces(String fen, GridPane root) {
        int[] board = b.loadFromFen(fen);
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 16; file++) {
                if ((b.getSquare(rank, file) & 0x88) != 0)
                    continue;
                int piece = board[b.getSquare(rank, file)];
                if (piece != p.none) {
                    images[rank][file] = placeImage(rank, file, piece, root);
                    images[rank][file].setViewOrder(1);
                }
            }
        }
    }

    private ImageView placeImage(int rank, int file, int piece, GridPane root) {
        Image image = loadImage("src/assets/" + piece + ".png");
        ImageView imageView = new ImageView(image);
        imageView.setX(file * sqSize);
        imageView.setY(rank * sqSize);
        imageView.setFitHeight(sqSize);
        imageView.setFitWidth(sqSize);

        root.add(imageView, file, rank);
        return imageView;
    }

    private Image loadImage(String FileName) {
        // Image is initialised
        Image img = null;
        try {
            // this gets the image from the directory entered
            img = new Image(new FileInputStream(FileName));
        } catch (FileNotFoundException e) {
            // Checks if an error has been found before, so that multiple alerts aren't shown
            if (!errorFound) {
                errorFound = true;
                // Initialises window
                Stage errorWindow = new Stage();
                errorWindow.initModality(Modality.APPLICATION_MODAL);

                Label label = new Label();
                label.setText("Error, image of piece cannot be found.\nRe-download the application");
                Button closeButton = new Button("Ok");
                closeButton.setOnAction(event -> errorWindow.close());

                VBox layout = new VBox(10);
                layout.getChildren().addAll(label, closeButton);
                layout.setAlignment(Pos.CENTER);

                Scene scene = new Scene(layout);
                errorWindow.setScene(scene);
                errorWindow.setTitle("Error");
                errorWindow.initStyle(StageStyle.UTILITY);
                errorWindow.show();
            }
        }
        return img;
    }

    public void moveImages(Move move) {
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());
        showLastMove(move);
        moveAnimation(move);
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        ImageView image = null;
        if (images[endRank][endFile] != null)
            image = images[endRank][endFile];
        images[endRank][endFile] = images[startRank][startFile];
        images[startRank][startFile] = null;
        // Removes images for en passant
        if (move.isEnPassantMove()) {
            if (Board.whiteToMove) {
                image = images[endRank - 1][endFile];
                images[endRank - 1][endFile] = null;
            } else {
                image = images[endRank + 1][endFile];
                images[endRank + 1][endFile] = null;
            }
        }
        if (move.isCastle())
            if (startFile - endFile == 2) {
                images[endRank][3] = images[endRank][0];
                images[endRank][0] = null;
            } else {
                images[endRank][5] = images[endRank][7];
                images[endRank][7] = null;
            }
        if (move.isPromotion()) {
            images[endRank][endFile].setImage(null);
            images[endRank][endFile] = placeImage(endRank, endFile, move.getPieceMoved() > 0 ? 5 : -5, Main.root);
        }
        ImageView finalImage = image;
        executorService.schedule(() -> {
            assert finalImage != null;
            delImage(finalImage);
        }, 150, TimeUnit.MILLISECONDS);

    }

    private void moveAnimation(Move move) {
        Line path = new Line();
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());
        path.setStartX(startFile * sqSize + (sqSize >> 1));
        path.setStartY(startRank * sqSize + (sqSize >> 1));
        path.setEndX(endFile * sqSize + (sqSize >> 1));
        path.setEndY(endRank * sqSize + (sqSize >> 1));

        PathTransition transition = new PathTransition();
        ImageView image = images[startRank][startFile];
        transition.setPath(path);
        transition.setNode(image);
        transition.setDuration(Duration.seconds(0.2));
        transition.play();
        if (move.isCastle()) {
            Line rookPath = new Line();
            rookPath.setStartY(startRank * sqSize + (sqSize >> 1));
            rookPath.setEndY(endRank * sqSize + (sqSize >> 1));
            ImageView rookImage;
            if (startFile - endFile == 2) { // Left castle
                rookPath.setStartX(sqSize >> 1);
                rookPath.setEndX(3 * sqSize + (sqSize >> 1));
                rookImage = images[startRank][0];
            } else {
                rookPath.setStartX(7 * sqSize + (sqSize >> 1));
                rookPath.setEndX(5 * sqSize + (sqSize >> 1));
                rookImage = images[startRank][7];
            }
            PathTransition rookTransition = new PathTransition();
            rookTransition.setPath(rookPath);
            rookTransition.setNode(rookImage);
            rookTransition.setDuration(Duration.seconds(0.2));
            rookTransition.play();
        }
    }

    private void showLastMove(Move move) {
        String startSqColour, endSqColour;
        Main.root.getChildren().remove(startTile);
        Main.root.getChildren().remove(endTile);
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());

        startSqColour = (startRank + startFile) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";
        endSqColour = (endRank + endFile) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";

        startTile.setStyle("-fx-background-color: " + startSqColour + ";");
        endTile.setStyle("-fx-background-color: " + endSqColour + ";");
        startTile.setViewOrder(2);
        endTile.setViewOrder(2);

        Main.root.add(startTile, startFile, startRank);
        Main.root.add(endTile, endFile, endRank);
    }

    public void drawSquare(int rank, int file) {
        Main.root.getChildren().remove(square);
        String colour = (rank + file) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";
        square.setStyle("-fx-background-color: " + colour + ";");
        square.setViewOrder(2);
        Main.root.add(square, file, rank);
    }

    public void removeSquare() {
        Main.root.getChildren().remove(square);
    }

    public void drawLegalMoves(int square, ArrayList<Move> moves) {
        for (Move move : moves) {
            if (move.getStartSq() == square)
                drawCircle(move.getEndSq());
        }
    }

    private void drawCircle(int endSquare) {
        int rank = b.getRank(endSquare), file = b.getFile(endSquare);
        Circle circle = new Circle(0, 0, (double) sqSize / 7);
        circle.setOpacity(0.2);
        GridPane.setHalignment(circle, HPos.CENTER);
        GridPane.setValignment(circle, VPos.CENTER);
        Main.root.add(circle, file, rank);
        circles.add(circle);
    }

    public void destroyCircles() {
        for (Circle circle : circles)
            Main.root.getChildren().remove(circle);
        circles.clear();
    }
}
