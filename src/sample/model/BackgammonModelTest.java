package sample.model;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.stubbing.answers.ReturnsElementsOf;
import sample.enums.CheckType;
import sample.enums.MoveType;
import sample.enums.Side;
import sample.view.BackgammonView;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;


import static org.junit.jupiter.api.Assertions.*;

class BackgammonModelTest {

    @Test
    void kushFirst() {

        Player player = new Player();
        BackgammonModel model = Mockito.spy(new BackgammonModel(player));
        List values = List.of(4, 4, 2, 3, 4, 2, 3, 3);
        Mockito.when(model.setValue()).then(new ReturnsElementsOf(values));
        Map.Entry<Double, Double> coords;
        MoveType type;

        //начало партии с дублем четверок первым ходом

        model.rollDice();
        // дубль из 4, значит можно снять две фишки с головы
        //переместили первую фишку первый раз на четыре столба
        coords = findCoords(Side.LIGHT, 4, player);

        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 0));
        assertTrue(type == MoveType.NORMAL);

        //переместили первую фишку второй раз на четыре столба
        coords = findCoords(Side.LIGHT, 8, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 4));
        assertTrue(type == MoveType.NORMAL);

        //переместили вторую фишку первый раз на четыре столба
        coords = findCoords(Side.LIGHT, 4, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 0));
        assertTrue(type == MoveType.NORMAL);

        //переместили вторую фишку второй раз на четыре столба
        coords = findCoords(Side.LIGHT, 8, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 4));
        assertTrue(type == MoveType.NORMAL);
        assertFalse(player.hasMoves()); //проверка, что больше нет ходов


        model.rollDice();

        //проверка, что фишка с головы не может передвинуться на семь столбов
        coords = findCoords(Side.DARK, 7, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 0));
        assertTrue(type == MoveType.NONE);

        //передвинем фишку с головы сразу на пять столбов
        coords = findCoords(Side.DARK, 5, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 0));
        assertTrue(type == MoveType.NORMAL);
        assertFalse(player.hasMoves());  //проверка, что больше нет ходов


        model.rollDice();

        //проверим, что фишка не может передвинуться, так как столб занят фишками соперника
        coords = findCoords(Side.DARK, 0, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 8));
        assertTrue(type == MoveType.NONE);

        //переместим фишку с головы на шесть столбов
        coords = findCoords(Side.LIGHT, 6, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 0));
        assertTrue(type == MoveType.NORMAL);
        assertFalse(player.hasMoves());  //проверка, что больше нет ходов


        model.rollDice();
        //переместим фишку с пятого столба на шесть столбов
        coords = findCoords(Side.DARK, 11, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 5));
        assertTrue(type == MoveType.NORMAL);

        //переместим фишку с головы на три столба
        coords = findCoords(Side.DARK, 3, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 0));
        assertTrue(type == MoveType.NORMAL);

        //переместим фишку с третьего столба на три столба
        coords = findCoords(Side.DARK, 6, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 3));
        assertTrue(type == MoveType.NORMAL);
        assertFalse(player.hasMoves());  //проверка, что больше нет ходов
    }

    @Test
    void noKushFirst() {

        Player player = new Player();
        BackgammonModel model = Mockito.spy(new BackgammonModel(player));
        List values = List.of(1, 2, 2, 3);
        Mockito.when(model.setValue()).then(new ReturnsElementsOf(values));
        Map.Entry<Double, Double> coords;
        MoveType type;


        // начало партии без дубля первым ходом


        model.rollDice();
        //переместим фишку с головы на два столба
        coords = findCoords(Side.LIGHT, 2, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 0));
        assertTrue(type == MoveType.NORMAL);

        //переместим фишку с второго столба на один столб
        coords = findCoords(Side.LIGHT, 3, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(0, 2));
        assertTrue(type == MoveType.NORMAL);
        assertFalse(player.hasMoves());


        model.rollDice();

        //проверка, что нельзя уйти за поле
        coords = findCoords(Side.DARK, 30, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 0));
        assertTrue(type == MoveType.NONE);

        //переместим фишку с головы на пять столбов
        coords = findCoords(Side.DARK, 5, player);
        type = model.move(coords.getKey(), coords.getValue(), model.getLastCheck(1, 0));
        assertTrue(type == MoveType.NORMAL);
        assertFalse(player.hasMoves());
    }

    private Map.Entry<Double, Double> findCoords(Side side, int columnInd, Player player) {
        if (player.getType() == CheckType.LIGHT) {
            if (side == Side.LIGHT)
                return new AbstractMap.SimpleEntry((11 - columnInd + 0.5) * BackgammonView.TALE_SIZE, (side.getValue() + 0.5) * BackgammonView.COLUMN_HEIGHT);
            else
                return new AbstractMap.SimpleEntry((columnInd + 0.5) * BackgammonView.TALE_SIZE, (side.getValue() + 0.5) * BackgammonView.COLUMN_HEIGHT);
        }

        if (side == Side.DARK)
            return new AbstractMap.SimpleEntry((11 - columnInd + 0.5) * BackgammonView.TALE_SIZE, 0.5 * BackgammonView.COLUMN_HEIGHT);
        else
            return new AbstractMap.SimpleEntry((columnInd + 0.5) * BackgammonView.TALE_SIZE, (side.getValue() + 0.5) * BackgammonView.COLUMN_HEIGHT);
    }


}