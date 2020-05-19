package sample.view;


import sample.enums.CheckType;
import sample.enums.Side;
import javafx.scene.layout.VBox;
import sample.view.BackgammonView;

public class ColumnView extends VBox {
    private int columnInd;
    private CheckType checkType;
    private Side side;
    private int checkNum;

    ColumnView(int columnInd, Side side, int width, int height) {
        this.columnInd = columnInd;
        this.checkType = CheckType.EMPTY;
        this.side = side;

        setPrefWidth(width);
        setPrefHeight(height);

        setTranslateX(side == Side.LIGHT ? (11 - columnInd) * BackgammonView.TALE_SIZE : columnInd * BackgammonView.TALE_SIZE);
        setTranslateY(side == Side.LIGHT ? 0 : height);

        getStyleClass().add(columnInd < 6 ? "simple-column" : "home-column");
    }

    public int getColumnInd() {
        return columnInd;
    }

    public int getCheckNum() { return checkNum; }

    public void addCheck() { checkNum++; }

    public void removeCheck() { checkNum--; }

    public void lightUp() { getStyleClass().add("lightup-column"); }

    public void lightOff() { getStyleClass().remove("lightup-column"); }

    public Side getSide() { return side; }
}
