package sample.model;

import sample.enums.CheckType;
import sample.enums.Side;
import sample.model.Check;

import java.util.ArrayList;
import java.util.List;

public class ColumnModel {

    private int columnInd;
    private CheckType checkType;
    private Side side;
    List<Check> checks = new ArrayList<>();


    public ColumnModel(int columnInd, Side side) {
        this.columnInd = columnInd;
        this.checkType = CheckType.EMPTY;
        this.side = side;
    }

    public int getColumnInd() {
        return columnInd;
    }

    public int getCheckNum() {
        return checks.size();
    }

    public Side getSide() {
        return side;
    }

    public CheckType getType() { return checkType; }


    public void addCheck(Check check) {
        if (checkType == CheckType.EMPTY) checkType = check.getType();
        checks.add(check);
    }

    public void removeCheck() {
        checks.remove(checks.size() - 1);
        if (checks.size() == 0) checkType = CheckType.EMPTY;
    }

    public Check getLastCheck() {
        return getCheckNum()>0 ? checks.get(getCheckNum()-1) : null;
    }

    public void disableLast(boolean value) {
        if (checks.size()>0) getLastCheck().setDisable(value);
    }

}
