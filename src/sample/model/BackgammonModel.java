package sample.model;

import sample.enums.CheckType;
import sample.enums.MoveType;
import sample.enums.RollType;
import sample.enums.Side;

import java.util.*;

public class BackgammonModel {
    //Константы
    private int TALE_SIZE = 60;
    private int COLUMN_HEIGHT = TALE_SIZE / 2 * 16;
    private int FIELD_WIDTH = TALE_SIZE * 12;
    private int FIELD_HEIGHT = COLUMN_HEIGHT * 2;
    private double OUTHOME_TRANSLATE_X;
    private double OUTHOME_TRANSLATE_Y;
    private double OUTHOME_WIDTH;
    private double OUTHOME_HEIGHT;

    public void setConstants(int TALE_SIZE, int COLUMN_HEIGHT, double OUTHOME_TRANSLATE_X, double OUTHOME_TRANSLATE_Y,
                             double OUTHOME_WIDTH, double OUTHOME_HEIGHT) {
        this.TALE_SIZE = TALE_SIZE;
        this.COLUMN_HEIGHT = TALE_SIZE / 2 * 16;
        this.FIELD_WIDTH = TALE_SIZE * 12;
        this.FIELD_HEIGHT = COLUMN_HEIGHT * 2;
        this.OUTHOME_TRANSLATE_X = OUTHOME_TRANSLATE_X;
        this.OUTHOME_TRANSLATE_Y = OUTHOME_TRANSLATE_Y;
        this.OUTHOME_WIDTH = OUTHOME_WIDTH;
        this.OUTHOME_HEIGHT = OUTHOME_HEIGHT;
    }

    //Колонны
    private ColumnModel[][] COLUMNS = new ColumnModel[2][12];

    //Фишки
    private final HashSet<Check> MOVABLE_CHECKS = new HashSet<>(15);
    private final List<Check> checks = new ArrayList<>(24);
    private final Set<Check> LIGHT_HOME = new HashSet<>(15);
    private final Set<Check> DARK_HOME = new HashSet<>(15);

    // Флаги
    private boolean lightFirstRoll = true;
    private boolean darkFirstRoll = true;
    private boolean movableExists = true;
    private boolean fullLightHome = false;
    private boolean fullDarkHome = false;

    private int dice1Value;
    private int dice2Value;

    private Player player;

    public BackgammonModel(Player player) {
        this.player = player;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 12; j++) {
                COLUMNS[i][j] = new ColumnModel(j, Side.getByValue(i));
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int k = 0; k < 15; k++) {
                //добавляем фишки в первый столб
                Check check = new Check(CheckType.getByValue(i), player, COLUMNS[i][0]);
                COLUMNS[i][0].addCheck(check);
                checks.add(check);
            }
        }
    }



    private boolean checkIntersectsOutHome(double x, double y) {
        return x >= FIELD_WIDTH + OUTHOME_TRANSLATE_X &&
                x <= FIELD_WIDTH + OUTHOME_TRANSLATE_X + OUTHOME_WIDTH &&
                y >= OUTHOME_TRANSLATE_Y &&
                y <= OUTHOME_TRANSLATE_Y + OUTHOME_HEIGHT;
    }

    public RollType rollDice() {
        // первый бросок
        if (player.getType() == null) {
            player.switchType();
            prepareField();
            return RollType.FIRST;
        } else {
            // Если есть возможность походить, то разворачиваем доску и передаем ход, иначе оповещаем, что не все ходы использованы
            if (!movableExists) {
                player.switchType();
                prepareField();
                return RollType.SWITCH;
            }
        }
        return RollType.NONE;
    }


    private void prepareField() {

        dice1Value = setValue();
        dice2Value = setValue();


        player.setMoves(dice1Value, dice2Value);

        //"блокируем" фишки соперника
        Arrays.stream(COLUMNS).forEach(array -> Arrays.stream(array).forEach(column -> {
            if (column.getType() == player.getType()) {
                column.disableLast(false);
            } else {
                column.disableLast(true);
            }
        }));

        lightUpMovable();
    }

    int setValue() {
        return ((int) (Math.random() * 6)) + 1;
    }

    private ColumnModel findColumn(double x, double y) {
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

    private MoveType tryMove(ColumnModel newColumn, Check check) {
        if (check != null) {
            ColumnModel oldColumn = check.getOldColumn();
            CheckType checkType = check.getType();
            CheckType newColumnType = newColumn.getType();

            if ((newColumnType == checkType || newColumnType == CheckType.EMPTY) &&
                    newColumn != oldColumn && player.hasMoves()) {

                int columnDelta = columnDelta(newColumn, oldColumn);

                if (player.contains(columnDelta)) {
                    //проверка, что фишка не может быть передвинута на другую сторону, иначе ход по полю зациклится
                    if (check.moved + columnDelta <= 23) {
                        return MoveType.NORMAL;
                    } else
                        return MoveType.NONE;
                }
            }
        }
        return MoveType.NONE;
    }

    public static int columnDelta(ColumnModel newColumn, ColumnModel oldColumn) {
        if (newColumn.getSide() == oldColumn.getSide())
            return newColumn.getColumnInd() - oldColumn.getColumnInd();
        else
            return newColumn.getColumnInd() + 12 - oldColumn.getColumnInd();
    }

    public List<Map.Entry<Integer, Integer>> lightUpColumns(Check check) {
        List<Map.Entry<Integer, Integer>> columns = new ArrayList<>();

        ColumnModel from = check.getOldColumn();
        Side fromSide = from.getSide();
        int fromColumnInd = from.getColumnInd();

        for (int move : player.getMoves()) {
            ColumnModel to;

            if (fromSide == Side.DARK) {
                if (from.getColumnInd() + move < 12)
                    to = COLUMNS[1][fromColumnInd + move];
                else
                    to = COLUMNS[0][(fromColumnInd + move) % 12];
            } else {
                if (from.getColumnInd() + move < 12)
                    to = COLUMNS[0][fromColumnInd + move];
                else
                    to = COLUMNS[1][(fromColumnInd + move) % 12];
            }

            if (tryMove(to, check) == MoveType.NORMAL && (move == player.move1 || move == player.move2)) {
                columns.add(Map.entry(to.getSide().getValue(), to.getColumnInd()));
                continue;
            }

            if (tryMove(to, check) == MoveType.NORMAL && !columns.isEmpty()) {
                columns.add(Map.entry(to.getSide().getValue(), to.getColumnInd()));
            }
        }

        return columns;
    }

    //подсвечивает фишки, которые могут куда-то сдвинуться, также устанавливается флаг, существуют ли они вообще
    // подсвечиваются в начале хода и каждый раз, когда игрок ставит фишку
    private void lightUpMovable() {
        removeMovable(); // удаляем светящиеся фишки

        if (player.hasMoves()) {
            CheckType playerType = player.getType();
            Arrays.stream(COLUMNS).forEach(array -> Arrays.stream(array).forEach(column -> {

                Check lastCheck = column.getLastCheck();
                Side fromSide = column.getSide();
                int fromColumnInd = column.getColumnInd();

                if (lastCheck != null && lastCheck.getType() == playerType && !lastCheck.isDisabled()) {

                    for (int move : player.getMoves()) {
                        ColumnModel to;

                        if (fromSide == Side.DARK) {
                            if (fromColumnInd + move < 12)
                                to = COLUMNS[1][fromColumnInd + move];
                            else
                                to = COLUMNS[0][(fromColumnInd + move) % 12];
                        } else {
                            if (fromColumnInd + move < 12)
                                to = COLUMNS[0][fromColumnInd + move];
                            else
                                to = COLUMNS[1][(fromColumnInd + move) % 12];
                        }


                        if (tryMove(to, lastCheck) == MoveType.NORMAL && (move == player.move1 || move == player.move2)) {
                            lastCheck.lightUpMovable();
                            MOVABLE_CHECKS.add(lastCheck);
                            continue;
                        }

                        // если в списке movable уже есть эта фишка, значит можно реализовать "двойной ход", т.к. путь свободен
                        if (tryMove(to, lastCheck) == MoveType.NORMAL && MOVABLE_CHECKS.contains(lastCheck)) {
                            lastCheck.lightUpMovable();
                            MOVABLE_CHECKS.add(lastCheck);
                        }
                    }
                }
            }));
            //подсветим фишки, которые можно вывести из дома
            lightUpMoveOut();
        }
        movableExists = MOVABLE_CHECKS.size() > 0;
    }

    private void lightUpMoveOut() {
        if (checkFullHome()) {
            CheckType oppositeCheckType = player.getType() == CheckType.LIGHT ? CheckType.DARK : CheckType.LIGHT;

            List<Integer> moves = player.getMoves();
            moves.remove(Integer.valueOf(player.multyMove));

            boolean isFound = false;
            //проверяется наличие фишек на позициях, соответствующих значениям кубика
            for (int move : moves) {
                Check check = COLUMNS[oppositeCheckType.getValue()][12 - move].getLastCheck();

                if (check != null && check.getType() == player.getType()) {
                    check.lightUpMoveOut(move);
                    MOVABLE_CHECKS.add(check);
                    isFound = true;
                }
            }
            int toMoveOut = Collections.max(moves);
            //если фишка не найдена, то нужно проверить наличие фишек до позиции, соответствующей максимальному значению
            //кубиков, так как при их наличии нельзя будет  выводить шашки с полей низшего разряда, если в полях
            // высшего разряда шашек нет
            if (!isFound) {
                for (int i = 6; i > toMoveOut; i--) {
                    Check check = COLUMNS[oppositeCheckType.getValue()][12 - i].getLastCheck();
                    if (check != null && check.getType() == player.getType()) {
                        isFound = true;
                    }
                }
            }

            if (!isFound) {
                for (int i = toMoveOut; i > 0; i--) {
                    Check check = COLUMNS[oppositeCheckType.getValue()][12 - i].getLastCheck();

                    if (check != null && check.getType() == player.getType()) {
                        check.lightUpMoveOut(toMoveOut);
                        MOVABLE_CHECKS.add(check);
                        return;
                    }
                }
            }
        }
    }

    public MoveType move(double x, double y, Check check) {
        MoveType moveResult;

        if (checkIntersectsOutHome(x, y) && check.canMoveOut) {
            check.moveOut();

            lightUpMovable();

            return MoveType.OUT;
        }

        ColumnModel newColumn = findColumn(x, y);
        newColumn = newColumn == null ? check.getOldColumn() : newColumn;

        if ((lightFirstRoll || darkFirstRoll) && kushFirstRoll())
            check.kushFirst = true;

        if ((moveResult = tryMove(newColumn, check)) == MoveType.NORMAL) check.move(newColumn);

        //пополнение фишек в доме
        if (check.isHome()) {
            if (check.getType() == CheckType.LIGHT)
                LIGHT_HOME.add(check);
            else
                DARK_HOME.add(check);
        }

        setFullHome();

        lightUpMovable();

        return moveResult;
    }

    private void removeMovable() {
        MOVABLE_CHECKS.forEach(Check::lightOff);
        MOVABLE_CHECKS.clear();
    }


    // Проверка куша первым броском, при котором на костях либо 3, либо 4, либо 6
    private boolean kushFirstRoll() {
        boolean result = false;
        if (player.getType() == CheckType.LIGHT) {
            if (lightFirstRoll && dice1Value == dice2Value && (dice1Value == 3 || dice1Value == 4 || dice1Value == 6)) {
                result = true;
            }
            lightFirstRoll = false;
        } else {
            if (darkFirstRoll && dice1Value == dice2Value && (dice1Value == 3 || dice1Value == 4 || dice1Value == 6)) {
                result = true;
            }
            darkFirstRoll = false;
        }
        return result;
    }

    private boolean checkFullHome() {
        return player.getType() == CheckType.LIGHT ? fullLightHome : fullDarkHome;
    }

    private void setFullHome() {
        if (LIGHT_HOME.size() == 15) fullLightHome = true;
        if (DARK_HOME.size() == 15) fullDarkHome = true;
    }

    Check getLastCheck(int side, int ind) { return COLUMNS[side][ind].getLastCheck(); }

    private double rotatedX(double x) {
        return FIELD_WIDTH - x;
    }

    public int getDice1Value() {
        return dice1Value;
    }

    public int getDice2Value() {
        return dice2Value;
    }

    public List<Check> getChecks() {
        return checks;
    }
}
