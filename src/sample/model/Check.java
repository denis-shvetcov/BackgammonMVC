package sample.model;


import sample.enums.CheckType;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import sample.view.BackgammonView;


public class Check extends Pane {
    private int taleSize = BackgammonView.TALE_SIZE;
    private double halfTaleSize = taleSize / 2.0;

    private Circle check;
    private CheckType type;

    public boolean canMoveOut;
    public int toMoveOut;

    boolean kushFirst = false;
    boolean home;

    int moved;

    private ColumnModel oldColumn;

    private Player player;

    public Check(CheckType type, Player player, ColumnModel oldColumn) {
        this.type = type;
        this.player = player;
        this.home = false;
        this.moved = 0;
        this.canMoveOut = false;
        this.oldColumn = oldColumn;

        setPrefHeight(taleSize);
        setPadding(new Insets(taleSize * 0.03));

        setDisable(true);

        check = new Circle(halfTaleSize, halfTaleSize, halfTaleSize - taleSize * 0.03);

        check.getStyleClass().add("check");
        check.getStyleClass().add(type == CheckType.LIGHT ? "light-check" : "dark-check");

        getChildren().add(check);
    }

    public void move(ColumnModel newColumn) {
        oldColumn.removeCheck();
        if (moved != 0 || kushFirst) {
            oldColumn.disableLast(false);
            kushFirst = false;
        }

        newColumn.disableLast(true);
        newColumn.addCheck(this);

        int move = columnDelta(newColumn, oldColumn);
        moved += move;
        player.move(move);

        checkHome();

        oldColumn = newColumn;
    }


    public static int columnDelta(ColumnModel newColumn, ColumnModel oldColumn) {
        if (newColumn.getSide() == oldColumn.getSide())
            return newColumn.getColumnInd() - oldColumn.getColumnInd();
        else
            return newColumn.getColumnInd() + 12 - oldColumn.getColumnInd();
    }

    public CheckType getType() {
        return type;
    }

    public ColumnModel getOldColumn() {
        return oldColumn;
    }

    private void checkHome() { home = moved > 17; }

    public void lightUpMovable() {
        ObservableList<String> styles = check.getStyleClass();
        if (!styles.contains("movable-check")) styles.add("movable-check");
    }

    public void lightUpMoveOut(int move) {
        ObservableList<String> styles = check.getStyleClass();
        toMoveOut = move;
        if (!styles.contains("can-move-out")){
            styles.add("can-move-out");
            canMoveOut = true;
        }
    }

    public void lightOff() {
        check.getStyleClass().remove("movable-check");
        check.getStyleClass().remove("can-move-out");
        canMoveOut = false;
    }

    public boolean isHome() {
        return home;
    }

    public void moveOut() {
        player.move(toMoveOut);
        oldColumn.removeCheck();
        oldColumn.disableLast(false);
    }
}
