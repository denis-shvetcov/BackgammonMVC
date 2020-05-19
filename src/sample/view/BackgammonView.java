package sample.view;


import sample.model.BackgammonModel;
import sample.model.Check;
import sample.model.Player;
import sample.enums.CheckType;
import sample.enums.MoveType;
import sample.enums.RollType;
import sample.enums.Side;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.transform.Rotate;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class BackgammonView extends Pane {
    //Константы
    final public static int TALE_SIZE = 60;
    final public static int COLUMN_HEIGHT = TALE_SIZE / 2 * 16;
    final public static int FIELD_WIDTH = TALE_SIZE * 12;
    final public static int FIELD_HEIGHT = COLUMN_HEIGHT * 2;

    //Колонны
    final ColumnView[][] COLUMNS = new ColumnView[2][12];
    private final List<ColumnView> lightColumns = new ArrayList<>();

    // Картинки
    private final Image[] DICE_SIDES = new Image[6];
    private final ImageView arrowsImg = new ImageView();
    private final ImageView dice1 = new ImageView();
    private final ImageView dice2 = new ImageView();

    //Панели
    final OutHome outHome = new OutHome();
    final Pane field = new Pane();
    final Pane stuff = new Pane();

    private Button play = new Button("Play");

    private ColumnView oldColumn;
    private final Rotate rotate = new Rotate();
    private Player player;

    private BackgammonModel model;

    public BackgammonView(BackgammonModel model, Player player) throws FileNotFoundException {
        this.model = model;
        this.player = player;

        arrowsImg.setImage(new Image(new FileInputStream("src\\images\\arrows.png")));
        arrowsImg.setFitHeight(25);
        arrowsImg.setFitWidth(25);

        for (int i = 0; i < 6; i++) {
            DICE_SIDES[i] = new Image(new FileInputStream(String.format("src\\images\\dice%d.png", i + 1)));
        }

        rotate.setPivotX(FIELD_WIDTH / 2);
        rotate.setPivotY(COLUMN_HEIGHT);

        field.getTransforms().add(rotate);

        stuff.setTranslateX(FIELD_WIDTH);
        stuff.setPrefWidth(280);
        stuff.setPrefHeight(FIELD_HEIGHT);
        stuff.getStyleClass().add("stuff");

        VBox infoField = new VBox(20);

        infoField.setTranslateY(FIELD_HEIGHT / 2 - 200);
        infoField.setAlignment(Pos.CENTER);
        infoField.setPrefWidth(stuff.getPrefWidth());

        dice1.setFitWidth(80);
        dice1.setPreserveRatio(true);
        dice2.setFitWidth(80);
        dice2.setPreserveRatio(true);
        HBox diceBox = new HBox(10);
        diceBox.setAlignment(Pos.CENTER);
        diceBox.getChildren().addAll(dice1, dice2);

        infoField.getChildren().addAll(play, diceBox);

        outHome.setTranslateX((stuff.getPrefWidth() - outHome.getPrefWidth()) / 2);
        outHome.setTranslateY(FIELD_HEIGHT / 2);

        model.setConstants(TALE_SIZE, COLUMN_HEIGHT, outHome.getTranslateX(),
                outHome.getTranslateY(), outHome.getPrefWidth(), outHome.getPrefHeight());

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 12; j++) {
                ColumnView column = new ColumnView(j, Side.getByValue(i), TALE_SIZE, FIELD_HEIGHT / 2);
                COLUMNS[i][j] = column;
                field.getChildren().add(column);
            }
        }


        play.getStyleClass().add("play-btn");
        play.setOnAction(event -> {
            RollType type = model.rollDice();

            if (type == RollType.FIRST) {
                createChecks();
                play.setText("Передать ход");
                play.setGraphic(arrowsImg);
                play.setContentDisplay(ContentDisplay.LEFT);
                setValues();
            }

            if (type == RollType.SWITCH) {
                rotate.setAngle((rotate.getAngle() + 180) % 360);
                setValues();
            }

        });

        stuff.getChildren().addAll(outHome, infoField);

        getChildren().addAll(stuff, field);

    }

    public void setValues() {
        dice1.setImage(DICE_SIDES[model.getDice1Value() - 1]);
        dice2.setImage(DICE_SIDES[model.getDice2Value() - 1]);
    }

    public void createChecks() {

        for (Check check : model.getChecks()) {
            int checkType = check.getType().getValue();
            COLUMNS[checkType][0].addCheck();

            check.setTranslateX(checkType == 1 ? 0 : 11 * TALE_SIZE);
            check.setTranslateY(checkType == 0 ? (COLUMNS[checkType][0].getCheckNum() - 1) * (TALE_SIZE / 2.0) :
                    COLUMN_HEIGHT * 2 - (COLUMNS[checkType][0].getCheckNum() - 1) * (TALE_SIZE / 2.0) - TALE_SIZE);

            check.setOnMousePressed(event -> {
                lightUpColumns(model.lightUpColumns(check));
                if (check.canMoveOut) outHome.lightUp();
                oldColumn = findColumn(event.getSceneX(), event.getSceneY());
            });

            check.setOnMouseDragged(event -> {
                if (player.getType() == CheckType.LIGHT) {
                    check.setTranslateX(event.getSceneX() - TALE_SIZE / 2.0);
                    check.setTranslateY(event.getSceneY() - TALE_SIZE / 2.0);
                } else {
                    check.setTranslateX(rotatedX(event.getSceneX()) - TALE_SIZE / 2.0);
                    check.setTranslateY(rotatedY(event.getSceneY()) - TALE_SIZE / 2.0);
                }
            });


            check.setOnMouseReleased(event -> {
                MoveType type = model.move(event.getSceneX(), event.getSceneY(), check);

                if (type == MoveType.OUT) {
                    outHome.add(check);
                    outHome.lightOff();

                    oldColumn.removeCheck();

                    lightOffColumns();

                    field.getChildren().remove(check);
                }

                if (type == MoveType.NONE) {
                    move(oldColumn, oldColumn, check);
                }

                if (type == MoveType.NORMAL) move(oldColumn, findColumn(event.getSceneX(), event.getSceneY()), check);

                outHome.lightOff();
                lightOffColumns();
            });

            field.getChildren().add(check);
        }

    }

    private void move(ColumnView from, ColumnView to, Check check) {
        Side toSide = to.getSide();

        from.removeCheck();
        to.addCheck();

        check.setTranslateX(toSide == Side.DARK ? to.getColumnInd() * TALE_SIZE : (11 - to.getColumnInd()) * TALE_SIZE);
        check.setTranslateY(toSide == Side.LIGHT ? (to.getCheckNum() - 1) * (TALE_SIZE / 2.0) :
                FIELD_HEIGHT - (to.getCheckNum() - 1) * (TALE_SIZE / 2.0) - TALE_SIZE);
    }

    private ColumnView findColumn(double x, double y) {
        Side side;
        int columnInd;
        if (x >= 0 && x <= FIELD_WIDTH && y >= 0 && y <= FIELD_HEIGHT) {
            if (player.getType() == CheckType.LIGHT) {
                side = y >= COLUMN_HEIGHT ? Side.DARK : Side.LIGHT; // Сторона определяется в зависимости от поворота
                columnInd = side == Side.LIGHT ? 11 - ((int) x) / TALE_SIZE : ((int) x) / TALE_SIZE;
            } else {
                side = y < COLUMN_HEIGHT ? Side.DARK : Side.LIGHT;// Сторона определяется в зависимости от поворота
                columnInd = side == Side.DARK ? (int) rotatedX(x) / TALE_SIZE : 11 - (int) rotatedX(x) / TALE_SIZE;
            }
            return COLUMNS[side.getValue()][columnInd];
        }
        return null; // чтобы предотвратить выход за поле
    }

    private double rotatedX(double x) {
        return FIELD_WIDTH - x;
    }

    private double rotatedY(double Y) {
        return FIELD_HEIGHT - Y;
    }


    public void lightUpColumns(List<Map.Entry<Integer, Integer>> columns) {

        for (Map.Entry<Integer, Integer> sideInd : columns) {
            ColumnView column = COLUMNS[sideInd.getKey()][sideInd.getValue()];
            column.lightUp();
            lightColumns.add(column);
        }
    }

    private void lightOffColumns() {
        lightColumns.forEach(ColumnView::lightOff);
        lightColumns.clear();
    }

    public double getStuffWidth() {
        return stuff.getPrefWidth();
    }

}