import javafx.animation.PathTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GUI {
    static final int dimension = 8;
    private static final StackPane startTile = new StackPane(), endTile = new StackPane(),
            startHintTile = new StackPane(), endHintTile = new StackPane();
    private static final ArrayList<Node> circles = new ArrayList<>();
    private static final StackPane square = new StackPane();
    private static final ArrayList<ImageView> capturedPieceImages = new ArrayList<>();
    private static final ToggleGroup boardThemeGroup = new ToggleGroup(), chooseColourGroup = new ToggleGroup();
    public static Button hintButton = new Button("Hint?");
    public static ImageView[][] images = new ImageView[8][8];
    public static boolean showHint = false;
    public static boolean cancelHint = false;
    public static Text tipsText = new Text("");
    public static boolean newGame = false;
    static int boardSize = (int) (Toolkit.getDefaultToolkit().getScreenSize().height * 0.9);
    static int sqSize = boardSize / dimension;
    public static Text evaluationText = new Text(sqSize, sqSize, "0.0");
    private static int whitePiecesCaptured = 0;
    private static int blackPiecesCaptured = 0;
    private static ArrayList<Node> boardNodes = new ArrayList<>();
    Piece p = new Piece();
    Board b = new Board();
    MakeMove makeMove = new MakeMove();
    MoveGenerator moveGenerator = new MoveGenerator();

    private static void delImage(ImageView image) {
        image.setImage(null);
    }

    public void drawBoard(String lightColour, String darkColour, boolean init) {
        for (Node node : boardNodes)
            Main.root.getChildren().remove(node);
        boardNodes.clear();
        String colour;
        // This is the start of the nested for loop
        for (int row = 0; row < dimension; row++) {
            for (int col = 0; col < dimension; col++) {
                // initialises each "pane"/tile
                StackPane boardTile = new StackPane();
                // This makes every other colour alter, e.g. first tile is light, next tile is dark
                if ((row + col) % 2 == 0)
                    colour = lightColour;
                else
                    colour = darkColour;
                // sets the colour of the tile
                boardTile.setStyle("-fx-background-color: " + colour + ";");
                boardTile.setViewOrder(3);
                // adds the tile to the window, and sets where the pane needs to go
                Main.root.add(boardTile, col, row);
                boardNodes.add(boardTile);
            }
        }
        if (init) {
            for (int i = 0; i < dimension; i++) {
                // Set Constraints
                Main.root.getColumnConstraints().add(new ColumnConstraints(sqSize));
                Main.root.getRowConstraints().add(new RowConstraints(sqSize));
            }
        }
    }

    public void drawPieces(String fen) {
        int[] board = b.loadFromFen(fen);
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 16; file++) {
                if ((b.getSquare(rank, file) & 0x88) != 0)
                    continue;
                int piece = board[b.getSquare(rank, file)];
                if (piece != p.none) {
                    images[rank][file] = placeImage(rank, file, piece, Main.root);
                    images[rank][file].setViewOrder(1);
                }
            }
        }
    }

    private ImageView placeImage(int rank, int file, int piece, GridPane root) {
        Image image = loadImage("/Assets/" + piece + ".png");
        ImageView imageView = new ImageView(image);
        imageView.setX(file * sqSize);
        imageView.setY(rank * sqSize);
        imageView.setFitHeight(sqSize);
        imageView.setFitWidth(sqSize);
        root.add(imageView, file, rank);
        return imageView;
    }

    private Image loadImage(String FileName) {
        return new Image(Objects.requireNonNull(this.getClass().getResourceAsStream(FileName)));
    }

    public void moveImages(Move move) {
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());
        if (move.isUndoMove() && move.isCaptureMove())
            removeCapturedPiece();
        else
            drawCapturedPiece(move.getPieceCaptured());
        // For undo moves, add piece that was captured
        if (images[startRank][startFile] == null)
            images[startRank][startFile] = placeImage(startRank, startFile, move.getPieceMoved(), Main.root);
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
            if (move.isUndoMove()) {
                if (startFile - endFile == -2) {
                    images[endRank][0] = images[endRank][3];
                    images[endRank][3] = null;
                } else {
                    images[endRank][7] = images[endRank][5];
                    images[endRank][5] = null;
                }
            } else {
                if (startFile - endFile == 2) {
                    images[endRank][3] = images[endRank][0];
                    images[endRank][0] = null;
                } else {
                    images[endRank][5] = images[endRank][7];
                    images[endRank][7] = null;
                }
            }
        if (move.isPromotion()) {
            images[endRank][endFile].setImage(null);
            if (move.isUndoMove())
                images[endRank][endFile] = placeImage(endRank, endFile, move.getPieceMoved() > 0 ? 1 : -1, Main.root);
            else
                images[endRank][endFile] = placeImage(endRank, endFile, move.getPieceMoved() > 0 ? 5 : -5, Main.root);
        }
        ImageView finalImage = image;
        executorService.schedule(() -> {
            assert finalImage != null;
            delImage(finalImage);
        }, 150, TimeUnit.MILLISECONDS);
        // For undo moves add captured piece back
        if (move.getPieceCaptured() != 0 && move.isUndoMove())
            images[startRank][startFile] = placeImage(startRank, startFile, move.getPieceCaptured(), Main.root);
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
        image.toFront();
        transition.setPath(path);
        transition.setNode(image);
        transition.setDuration(Duration.seconds(0.2));
        transition.play();
        if (move.isCastle()) {
            ImageView rookImage;
            Line rookPath = new Line();
            rookPath.setStartY(startRank * sqSize + (sqSize >> 1));
            rookPath.setEndY(endRank * sqSize + (sqSize >> 1));
            if (move.isUndoMove()) {
                if (startFile - endFile == -2) {
                    rookPath.setStartX(3 * sqSize + (sqSize >> 1));
                    rookPath.setEndX(sqSize >> 1);
                    rookImage = images[startRank][3];
                } else {
                    rookPath.setStartX(5 * sqSize + (sqSize >> 1));
                    rookPath.setEndX(7 * sqSize + (sqSize >> 1));
                    rookImage = images[startRank][5];
                }
            } else {
                if (startFile - endFile == 2) { // Left castle
                    rookPath.setStartX(sqSize >> 1);
                    rookPath.setEndX(3 * sqSize + (sqSize >> 1));
                    rookImage = images[startRank][0];
                } else {
                    rookPath.setStartX(7 * sqSize + (sqSize >> 1));
                    rookPath.setEndX(5 * sqSize + (sqSize >> 1));
                    rookImage = images[startRank][7];
                }
            }
            PathTransition rookTransition = new PathTransition();
            rookTransition.setPath(rookPath);
            rookTransition.setNode(rookImage);
            rookTransition.setDuration(Duration.seconds(0.2));
            rookTransition.play();
        }
    }

    private void showLastMove(Move move) {
        Main.root.getChildren().remove(startTile);
        Main.root.getChildren().remove(endTile);
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());

        String startSqColour = (startRank + startFile) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";
        String endSqColour = (endRank + endFile) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";

        drawMove(startRank, startFile, endRank, endFile, startSqColour, endSqColour, startTile, endTile);
    }

    public void drawSquare(int rank, int file) {
        Main.root.getChildren().remove(square);
        String colour = (rank + file) % 2 == 0 ? "rgb(248,236,92)" : "rgb(224,196,52)";
        square.setStyle("-fx-background-color: " + colour + ";");
        square.setViewOrder(2);
        Main.root.add(square, file, rank);
    }

    public void drawCaptureCircle(int endSquare) {
        int rank = b.getRank(endSquare), file = b.getFile(endSquare);
        double strokeWidth = (double) sqSize / 12;
        Arc ring = new Arc(0, 0, (double) sqSize / 2 - strokeWidth, (double) sqSize / 2 - strokeWidth, 0, 360);
        ring.setStrokeWidth(strokeWidth);
        ring.setStrokeType(StrokeType.OUTSIDE);
        ring.setOpacity(0.2);
        ring.setStrokeLineCap(StrokeLineCap.BUTT);
        ring.setStroke(Color.BLACK);
        ring.setFill(null);
        GridPane.setHalignment(ring, HPos.CENTER);
        GridPane.setValignment(ring, VPos.CENTER);
        Main.root.add(ring, file, rank);
        circles.add(ring);
    }

    public void removeSquare() {
        Main.root.getChildren().remove(square);
    }

    public void drawLegalMoves(int square, ArrayList<Move> moves) {
        for (Move move : moves) {
            if (move.getStartSq() == square) {
                if (move.getPieceCaptured() == 0)
                    drawEmptyCircle(move.getEndSq());
                else
                    drawCaptureCircle(move.getEndSq());
            }
        }
    }

    private void drawEmptyCircle(int endSquare) {
        int rank = b.getRank(endSquare), file = b.getFile(endSquare);
        Circle circle = new Circle(0, 0, (double) sqSize / 7);
        circle.setOpacity(0.2);
        GridPane.setHalignment(circle, HPos.CENTER);
        GridPane.setValignment(circle, VPos.CENTER);
        Main.root.add(circle, file, rank);
        circles.add(circle);
    }

    public void destroyCircles() {
        for (Node circle : circles)
            Main.root.getChildren().remove(circle);
        circles.clear();
    }

    public void createGameScreen() {
        newGameButton();
        drawDifficultyPanel();
        drawEvaluation();
        undoButton();
        drawCapturedPiecesPanel();
        createHintButton();
        tipsPanel();
    }

    public void createWelcomeScreen() {
        difficultySlider();
        chooseBrownTheme();
        chooseBlueTheme();
        choosePurpleTheme();
        chooseColour();
        startGameButton();
    }

    private void chooseColour() {
        double sqSize = GUI.sqSize;
        createPanel(4);
        RadioButton whiteRadio = new RadioButton("White");
        whiteRadio.setTextFill(Color.rgb(219, 216, 214));
        whiteRadio.setSelected(true);
        Font font = Font.font("Segoe UI", FontWeight.BOLD, sqSize / 6);
        whiteRadio.setFont(font);
        whiteRadio.setTranslateX(sqSize / 10);
        whiteRadio.setToggleGroup(chooseColourGroup);
        whiteRadio.setUserData("white");
        Main.root.add(whiteRadio, 8, 4);

        RadioButton blackRadio = new RadioButton("Black");
        blackRadio.setTextFill(Color.rgb(219, 216, 214));
        blackRadio.setFont(font);
        blackRadio.setTranslateX(sqSize + sqSize / 5);
        blackRadio.setToggleGroup(chooseColourGroup);
        blackRadio.setUserData("black");
        Main.root.add(blackRadio, 8, 4);
    }

    private void chooseBrownTheme() {
        createPanel(1);
        double sqSize = GUI.sqSize;
        Rectangle lightSquare = new Rectangle(0, 0, sqSize, sqSize);
        lightSquare.setFill(Color.rgb(248, 220, 180));
        lightSquare.setTranslateX(sqSize * 2);
        Main.root.add(lightSquare, 8, 1);
        Rectangle darkSquare = new Rectangle(0, 0, sqSize, sqSize);
        darkSquare.setFill(Color.rgb(184, 140, 100));
        darkSquare.setTranslateX(sqSize);
        Main.root.add(darkSquare, 8, 1);

        RadioButton brownRadio = new RadioButton("Brown");
        brownRadio.setTextFill(Color.rgb(219, 216, 214));
        Font font = Font.font("Segoe UI", FontWeight.BOLD, sqSize / 6);
        brownRadio.setFont(font);
        brownRadio.setTranslateX(sqSize / 10);
        brownRadio.setSelected(true);
        brownRadio.setToggleGroup(boardThemeGroup);
        Main.root.add(brownRadio, 8, 1);

        brownRadio.setOnAction(actionEvent -> drawBoard("rgb(248, 220, 180)", "rgb(184, 140, 100)", false));
    }

    private void chooseBlueTheme() {
        createPanel(2);
        double sqSize = GUI.sqSize;
        Rectangle lightSquare = new Rectangle(0, 0, sqSize, sqSize);
        lightSquare.setTranslateX(sqSize * 2);
        lightSquare.setFill(Color.rgb(240, 236, 212));
        Main.root.add(lightSquare, 8, 2);
        Rectangle darkSquare = new Rectangle(0, 0, sqSize, sqSize);
        darkSquare.setFill(Color.rgb(80, 116, 156));
        darkSquare.setTranslateX(sqSize);
        Main.root.add(darkSquare, 8, 2);

        RadioButton blueRadio = new RadioButton("Blue");
        blueRadio.setTextFill(Color.rgb(219, 216, 214));
        Font font = Font.font("Segoe UI", FontWeight.BOLD, sqSize / 6);
        blueRadio.setFont(font);
        blueRadio.setTranslateX(sqSize / 10);
        blueRadio.setToggleGroup(boardThemeGroup);
        Main.root.add(blueRadio, 8, 2);

        blueRadio.setOnAction(actionEvent -> drawBoard("rgb(219, 216, 214)", "rgb(80, 116, 156)", false));
    }

    private void choosePurpleTheme() {
        createPanel(3);
        double sqSize = GUI.sqSize;
        Rectangle lightSquare = new Rectangle(0, 0, sqSize, sqSize);
        lightSquare.setTranslateX(sqSize * 2);
        lightSquare.setFill(Color.rgb(240, 236, 236));
        Main.root.add(lightSquare, 8, 3);
        Rectangle darkSquare = new Rectangle(0, 0, sqSize, sqSize);
        darkSquare.setFill(Color.rgb(144, 116, 180));
        darkSquare.setTranslateX(sqSize);
        Main.root.add(darkSquare, 8, 3);

        RadioButton purpleRadio = new RadioButton("Purple");
        purpleRadio.setTextFill(Color.rgb(219, 216, 214));
        Font font = Font.font("Segoe UI", FontWeight.BOLD, sqSize / 6);
        purpleRadio.setFont(font);
        purpleRadio.setTranslateX(sqSize / 10);
        purpleRadio.setToggleGroup(boardThemeGroup);
        Main.root.add(purpleRadio, 8, 3);

        purpleRadio.setOnAction(actionEvent -> drawBoard("rgb(240, 236, 236)", "rgb(144, 116, 180)", false));
    }

    public static int difficulty;

    private void difficultySlider() {
        double sqSize = GUI.sqSize;
        Slider slider = new Slider();
        slider.setMin(1);
        slider.setMax(4);
        slider.setValue(1);
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(0);
        slider.setBlockIncrement(1);
        slider.setSnapToTicks(true);
        slider.setStyle("-fx-padding: " + sqSize / 10);
        slider.setTranslateY(sqSize / 3);

        Text text = new Text(0, 0, "Difficulty");
        Font font = Font.font("Segoe UI", sqSize / 3);
        text.setFill(Color.rgb(219, 216, 214));
        text.setFont(font);
        GridPane.setHalignment(text, HPos.CENTER);
        text.setTranslateY(-sqSize / 10);

        createPanel(0);
        Main.root.add(slider, 8, 0);
        Main.root.add(text, 8, 0);

        difficulty = 1;

        slider.valueProperty().addListener((observableValue, number, t1) -> {
            difficulty = t1.intValue();
            System.out.println(difficulty);
        });
    }

    private void startGameButton() {
        Button button = new Button("Start Game");
        button.setPrefWidth(sqSize * 3);
        button.setPrefHeight(sqSize);
        Font font = Font.font("Segoe UI", FontWeight.NORMAL, (double) sqSize / 5);
        button.setFont(font);
        button.setTextFill(Color.rgb(219, 216, 214));
        button.setTextAlignment(TextAlignment.CENTER);
        button.setStyle("-fx-background-color: rgb(57, 62, 70);" + "-fx-border-color: rgb(34, 40, 49)");
        Main.root.add(button, 8, 6);
        button.setOnAction(actionEvent -> startGame());
    }

    private void startGame() {
        MouseHandler mouseHandler = new MouseHandler();
        createGameScreen();
        if (Objects.equals(chooseColourGroup.getSelectedToggle().getUserData().toString(), "white")) {
            MouseHandler.moves = moveGenerator.generateLegalMoves();
            generateHint();
        } else
            mouseHandler.doAIMove();
    }

    private void createPanel(int row) {
        double sqSize = GUI.sqSize;
        // Create the panel
        Rectangle panel = new Rectangle(0, 0, sqSize * 3, sqSize);
        panel.setFill(Color.rgb(57, 62, 70));
        panel.setStroke(Color.rgb(34, 40, 49));
        Main.root.add(panel, 8, row);
    }

    private void drawDifficultyPanel() {
        double sqSize = GUI.sqSize;
        createPanel(0);
        // Create the text
        Text text = new Text(sqSize, sqSize, "Difficulty - Hard\nELO 1500");
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 3));
        GridPane.setHalignment(text, HPos.CENTER);
        GridPane.setValignment(text, VPos.CENTER);
        text.setFill(Color.rgb(219, 216, 214));
        Main.root.add(text, 8, 0);
    }

    private void drawEvaluation() {
        createPanel(1);
        double sqSize = GUI.sqSize;
        evaluationText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 3));
        GridPane.setHalignment(evaluationText, HPos.CENTER);
        GridPane.setValignment(evaluationText, VPos.CENTER);
        evaluationText.setFill(Color.rgb(219, 216, 214));
        Main.root.add(evaluationText, 8, 1);
    }

    public void updateEvaluation(double evaluation) {
        if (evaluation == -1)
            evaluationText.setText("Book Move");
        else
            evaluationText.setText((AI.thinking ? -1 : 1) * evaluation / 10 + "");
    }

    private void undoButton() {
        double sqSize = GUI.sqSize;
        Button button = new Button("Undo");
        createButton(sqSize, sqSize, button);
        button.setOnAction(actionEvent -> undoClicked());
        Main.root.add(button, 8, 6);
    }

    private void undoClicked() {
        if (!AI.thinking) {
            if (!GenerateHint.hintGenerated) {
                MoveSearch.abortSearch = true;
                showHint = false;
                hintButton.setText("Hint?");
                cancelHint = true;
                GenerateHint.move = null;
            } else
                visualUndo();
        }
    }

    public void visualUndo() {
        undo();
        generateHint();
    }

    private void drawCapturedPiecesPanel() {
        double sqSize = GUI.sqSize;
        Rectangle whiteContainer = new Rectangle(0, 0, sqSize * 3, sqSize / 2);
        Rectangle blackContainer = new Rectangle(0, 0, sqSize * 3, sqSize / 2);
        whiteContainer.setFill(Color.rgb(57, 62, 70));
        blackContainer.setFill(Color.rgb(57, 62, 70));
        GridPane.setValignment(whiteContainer, VPos.TOP);
        GridPane.setValignment(blackContainer, VPos.BOTTOM);
        whiteContainer.setStroke(Color.rgb(34, 40, 49));
        blackContainer.setStroke(Color.rgb(34, 40, 49));
        Main.root.add(whiteContainer, 8, 7);
        Main.root.add(blackContainer, 8, 7);
    }

    private void drawCapturedPiece(int capturedPiece) {
        double sqSize = GUI.sqSize;
        if (capturedPiece == 0)
            return;
        Image image = loadImage("/Assets/" + capturedPiece + ".png");
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(sqSize / 2);
        imageView.setFitWidth(sqSize / 2);
        if (capturedPiece > 0) {
            GridPane.setValignment(imageView, VPos.TOP);
            imageView.setTranslateX(whitePiecesCaptured * sqSize / 5.5);
            whitePiecesCaptured++;
        } else {
            GridPane.setValignment(imageView, VPos.BOTTOM);
            imageView.setTranslateX(blackPiecesCaptured * sqSize / 5.5);
            blackPiecesCaptured++;
        }
        capturedPieceImages.add(imageView);
        Main.root.add(imageView, 8, 7);
    }

    private void removeCapturedPiece() {
        if (!capturedPieceImages.isEmpty()) {
            ImageView image = capturedPieceImages.get(capturedPieceImages.size() - 1);
            capturedPieceImages.remove(capturedPieceImages.size() - 1);
            Main.root.getChildren().remove(image);
            if (Board.whiteToMove)
                blackPiecesCaptured--;
            else
                whitePiecesCaptured--;
        }
    }

    private void newGameButton() {
        double sqSize = GUI.sqSize;
        Button button = new Button("New Game");
        createButton(sqSize, sqSize, button);
        button.setTranslateX(sqSize);
        button.setOnAction(actionEvent -> createNewGame());
        Main.root.add(button, 8, 6);
    }

    private void createButton(double width, double height, Button button) {
        button.setPrefWidth(width);
        button.setPrefHeight(height);
        Font font = Font.font("Segoe UI", FontWeight.NORMAL, width / 5);
        button.setFont(font);
        button.setTextFill(Color.rgb(219, 216, 214));
        button.setWrapText(true);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setStyle("-fx-background-color: rgb(57, 62, 70);" + "-fx-border-color: rgb(34, 40, 49)");
    }

    private void createNewGame() {
        newGame = true;
        if (!AI.thinking) {
            if (!GenerateHint.hintGenerated) {
                MoveSearch.abortSearch = true;
                showHint = false;
                hintButton.setText("Hint?");
                cancelHint = true;
                System.out.println("test");
            } else
                visualNewGame();
        }
    }

    public void visualNewGame() {
        MouseHandler mouseHandler = new MouseHandler();
        newGame = false;
        for (int i = MakeMove.moveLog.size(); i-- > 0; )
            undo();
        if (MakeMove.moveLog.size() == 1) {
            Move AIMove = makeMove.undoMove().setUndoMove();
            int startSq = AIMove.getStartSq(), endSq = AIMove.getEndSq();
            AIMove.setStartSq(endSq);
            AIMove.setEndSq(startSq);
            moveImages(AIMove);
            MouseHandler.moves = moveGenerator.generateLegalMoves();
            AI.chessNotationMoveLog.remove(AI.chessNotationMoveLog.size() - 1);
            if (!Board.fenHistory.isEmpty())
                Board.fenHistory.remove(Board.fenHistory.size() - 1);
            evaluationText.setText("Evaluation reset");
        }
        mouseHandler.doAIMove();
    }

    private void undo() {
        if (MakeMove.moveLog.size() > 1) {
            Move AIMove = makeMove.undoMove().setUndoMove();
            int startSq = AIMove.getStartSq(), endSq = AIMove.getEndSq();
            AIMove.setStartSq(endSq);
            AIMove.setEndSq(startSq);
            moveImages(AIMove);
            Move playerMove = makeMove.undoMove().setUndoMove();
            startSq = playerMove.getStartSq();
            endSq = playerMove.getEndSq();
            playerMove.setStartSq(endSq);
            playerMove.setEndSq(startSq);
            moveImages(playerMove);
            MouseHandler.moves = moveGenerator.generateLegalMoves();
            AI.chessNotationMoveLog.remove(AI.chessNotationMoveLog.size() - 1);
            AI.chessNotationMoveLog.remove(AI.chessNotationMoveLog.size() - 1);
            Board.fenHistory.remove(Board.fenHistory.size() - 1);
            Board.fenHistory.remove(Board.fenHistory.size() - 1);
            evaluationText.setText("Evaluation reset");
            Main.root.getChildren().remove(startHintTile);
            Main.root.getChildren().remove(endHintTile);
        }
    }

    private void createHintButton() {
        double sqSize = GUI.sqSize;
        Font font = Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 3);
        hintButton.setFont(font);
        hintButton.setTextFill(Color.rgb(219, 216, 214));
        hintButton.setTextAlignment(TextAlignment.CENTER);
        hintButton.setStyle("-fx-background-color: rgb(57, 62, 70);" + "-fx-border-color: rgb(34, 40, 49);");
        hintButton.setPrefHeight(sqSize);
        hintButton.setPrefWidth(sqSize * 3);
        hintButton.setOnAction(actionEvent -> showHint());
        Main.root.add(hintButton, 8, 2);
    }

    public void generateHint() {
        Runnable r = new GenerateHint();
        Thread task = new Thread(r);
        task.start();
    }

    private void showHint() {
        if (!AI.thinking && !GenerateHint.hintGenerated) {
            hintButton.setText("Generating Hint");
            showHint = true;
        } else if (GenerateHint.hintGenerated && !AI.thinking) {
            hintButton.setText("Hint generated");
            drawHint(GenerateHint.move);
        }
    }

    public void drawHint(Move move) {
        Main.root.getChildren().remove(startHintTile);
        Main.root.getChildren().remove(endHintTile);
        int startRank = b.getRank(move.getStartSq()), startFile = b.getFile(move.getStartSq()),
                endRank = b.getRank(move.getEndSq()), endFile = b.getFile(move.getEndSq());

        String startSqHintColour = (startRank + startFile) % 2 == 0 ? "#A3BF45" : "#81A140";
        String endSqHintColour = (endRank + endFile) % 2 == 0 ? "#A3BF45" : "#81A140";

        drawMove(startRank, startFile, endRank, endFile, startSqHintColour, endSqHintColour, startHintTile, endHintTile);
    }

    private void drawMove(int startRank, int startFile, int endRank, int endFile, String startSqColour, String endSqColour, StackPane startHintTile, StackPane endHintTile) {
        startHintTile.setStyle("-fx-background-color: " + startSqColour + ";");
        endHintTile.setStyle("-fx-background-color: " + endSqColour + ";");
        startHintTile.setViewOrder(2);
        endHintTile.setViewOrder(2);

        tipsText.setText(GenerateHint.hintText);
        GenerateHint.hintText = "";

        Main.root.add(startHintTile, startFile, startRank);
        Main.root.add(endHintTile, endFile, endRank);
    }

    public void removeHint() {
        Main.root.getChildren().remove(startHintTile);
        Main.root.getChildren().remove(endHintTile);
    }

    public void tipsPanel() {
        double sqSize = GUI.sqSize;
        Rectangle rectangle = new Rectangle(0, 0, sqSize * 3, sqSize * 3);
        rectangle.setFill(Color.rgb(57, 62, 70));
        rectangle.setStroke(Color.rgb(34, 40, 49));
        tipsText.setFill(Color.rgb(219, 216, 214));
        Font font = Font.font("Segoe UI", FontWeight.NORMAL, sqSize / 5);
        tipsText.setFont(font);
        tipsText.setWrappingWidth(sqSize * 3 - sqSize / 8);
        GridPane.setHalignment(tipsText, HPos.CENTER);
        Main.root.add(rectangle, 8, 4);
        Main.root.add(tipsText, 8, 4);
    }
}
