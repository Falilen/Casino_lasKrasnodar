package com.example.casinolaskrasnodar.ImageViewScrolling;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Util {
    // Обычные символы
    public static final int BAR = 0;
    public static final int SEVEN = 1;
    public static final int LEMON = 2;
    public static final int ORANGE = 3;
    public static final int TRIPLE = 4;
    public static final int SUPER_SYMBOL = 5;
    public static final int BROOMSTICK = 6; // Для основной игры

    // Динамический список доступных символов
    public static List<Integer> AVAILABLE_SYMBOLS = new ArrayList<>();



    public static List<Integer> AVAILABLE_BONUS_SYMBOLS = new ArrayList<>(Arrays.asList(
            BAR, SEVEN, LEMON, ORANGE, TRIPLE, BROOMSTICK
    ));

    public static boolean IS_BONUS_GAME = false;
    static {
        // Инициализация для основной игры
        AVAILABLE_SYMBOLS.add(BAR);
        AVAILABLE_SYMBOLS.add(SEVEN);
        AVAILABLE_SYMBOLS.add(LEMON);
        AVAILABLE_SYMBOLS.add(ORANGE);
        AVAILABLE_SYMBOLS.add(TRIPLE);
        AVAILABLE_SYMBOLS.add(SUPER_SYMBOL);
        AVAILABLE_SYMBOLS.add(BROOMSTICK); // Добавляем обычную метлу
    }

    public static int getRandomSymbol() {
        if (IS_BONUS_GAME) {
            return getWeightedRandom(AVAILABLE_BONUS_SYMBOLS);
        } else {
            return getWeightedRandom(AVAILABLE_SYMBOLS);
        }
    }
    private static int getWeightedRandom(List<Integer> symbols) {
        // Определяем веса для каждого символа
        Map<Integer, Integer> weights = new HashMap<>();
        weights.put(BAR, 10);         // Обычная частота
        weights.put(SEVEN, 10);       // Обычная частота
        weights.put(LEMON, 10);       // Обычная частота
        weights.put(ORANGE, 10);      // Обычная частота
        weights.put(TRIPLE, 10);      // Обычная частота
        weights.put(SUPER_SYMBOL, 2); // Реже
        weights.put(BROOMSTICK, 7);   // Очень редко

        // Создаем список с учетом весов
        List<Integer> weightedSymbols = new ArrayList<>();
        for (int symbol : symbols) {
            if (weights.containsKey(symbol)) {
                for (int i = 0; i < weights.get(symbol); i++) {
                    weightedSymbols.add(symbol);
                }
            }
        }

        // Возвращаем случайный символ из взвешенного списка
        Random random = new Random();
        return weightedSymbols.get(random.nextInt(weightedSymbols.size()));
    }



    public static int getRandomBonusSymbol() {
        if (AVAILABLE_BONUS_SYMBOLS.isEmpty()) {
            Log.e("BONUS_SYMBOL_ERROR", "Бонусный список пуст");
            return BAR;
        }
        List<Integer> filteredSymbols = new ArrayList<>(AVAILABLE_BONUS_SYMBOLS);
        filteredSymbols.removeIf(symbol -> symbol == BROOMSTICK);

        if (filteredSymbols.isEmpty()) {
            Log.w("SYMBOL_WARNING", "Все символы, кроме BROOMSTICK, удалены. Возвращаем случайный символ.");
            return AVAILABLE_BONUS_SYMBOLS.get(new Random().nextInt(AVAILABLE_BONUS_SYMBOLS.size()));
        }

        return filteredSymbols.get(new Random().nextInt(filteredSymbols.size()));
    }

    public static void removeSymbol(int symbol) {

        if (symbol == BROOMSTICK) {
            Log.w("REMOVE_SYMBOL", "Символ метлы защищен от удаления");
            return;
        }

        if (IS_BONUS_GAME) {
            if (AVAILABLE_BONUS_SYMBOLS.size() > 1) { // ← Проверка
                AVAILABLE_BONUS_SYMBOLS.remove((Integer) symbol);
                Log.e("SYMBOL_REMOVE", "Удалёен символ" + (Integer) symbol + AVAILABLE_BONUS_SYMBOLS);
            } else {
                Log.e("SYMBOL_REMOVE", "Нельзя удалить последний символ");
            }
        } else {
            if (AVAILABLE_SYMBOLS.size() > 1) { // ← Проверка
                AVAILABLE_SYMBOLS.remove((Integer) symbol);
            } else {
                Log.e("SYMBOL_REMOVE", "Нельзя удалить последний символ");
            }
        }
    }
}
