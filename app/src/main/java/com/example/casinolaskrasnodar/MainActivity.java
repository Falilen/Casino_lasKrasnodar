package com.example.casinolaskrasnodar;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.casinolaskrasnodar.ImageViewScrolling.IEventEnd;
import com.example.casinolaskrasnodar.ImageViewScrolling.ImageViewScrolling;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements IEventEnd {

    // UI элементы
    private Button btnSpin;
    private TextView txtScore;

    // Слоты (3 ряда x 5 колонок)
    private ImageViewScrolling[] slots = new ImageViewScrolling[15];

    // Логика игры
    private int countDone = 0;
    private final int TOTAL_SLOTS = 15;
    private final int SPIN_COST = 50;

    // Выигрышные линии
    private final int[][][] WIN_LINES = {
            // Горизонтальные
            {{0,0}, {0,1}, {0,2}, {0,3}, {0,4}},
            {{1,0}, {1,1}, {1,2}, {1,3}, {1,4}},
            {{2,0}, {2,1}, {2,2}, {2,3}, {2,4}},

            // Диагонали
            {{0,0}, {1,1}, {2,2}, {1,3}, {0,4}},
            {{2,0}, {1,1}, {0,2}, {1,3}, {2,4}},

            // Дополнительные линии
            {{0,0}, {1,1}, {0,2}, {1,3}, {0,4}},
            {{2,0}, {1,1}, {2,2}, {1,3}, {2,4}}
    };

    // Платёжная таблица
    private final Map<Integer, Integer> PAY_TABLE = new HashMap<Integer, Integer>() {{
        put(3, 50);
        put(4, 100);
        put(5, 200);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        hideSystemUI();

        initViews();
        setupSpinButton();
    }

    private void initViews() {
        btnSpin = findViewById(R.id.btn_up);
        txtScore = findViewById(R.id.txt_score);

        // Инициализация всех слотов
        int[] slotIds = {
                R.id.image, R.id.image2, R.id.image3, R.id.image4, R.id.image5,
                R.id.image6, R.id.image7, R.id.image8, R.id.image9, R.id.image10,
                R.id.image11, R.id.image12, R.id.image13, R.id.image14, R.id.image15
        };

        for(int i = 0; i < TOTAL_SLOTS; i++) {
            slots[i] = findViewById(slotIds[i]);
            slots[i].setEventEnd(this);
        }
    }

    private void setupSpinButton() {
        btnSpin.setOnClickListener(v -> {
            if(Common.SCORE >= SPIN_COST) {
                startSpin();
            } else {
                Toast.makeText(this, "Недостаточно средств", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startSpin() {
        // Блокируем кнопку на время анимации
        btnSpin.setEnabled(false);
        Common.SCORE -= SPIN_COST;
        txtScore.setText(String.valueOf(Common.SCORE) + "$");

        // Генерируем общее количество вращений
        int spinCount = 5 + new Random().nextInt(5);

        // Запускаем анимацию для всех слотов
        for(ImageViewScrolling slot : slots) {
            slot.SetValueRandom(spinCount);
        }

        // Разблокируем кнопку через 3.5 секунды
        new Handler().postDelayed(() -> btnSpin.setEnabled(true), 1100);
    }

    @Override
    public void eventEnd(int result, int count) {
        countDone++;

        if(countDone == TOTAL_SLOTS) {
            countDone = 0;

            // Создаём матрицу значений
            int[][] grid = {
                    {slots[0].getValue(), slots[1].getValue(), slots[2].getValue(), slots[9].getValue(), slots[12].getValue()},
                    {slots[3].getValue(), slots[4].getValue(), slots[5].getValue(), slots[10].getValue(), slots[13].getValue()},
                    {slots[6].getValue(), slots[7].getValue(), slots[8].getValue(), slots[11].getValue(), slots[14].getValue()}
            };

            logSlotGrid(grid);
            checkWin(grid);
        }
    }

    private void checkWin(int[][] grid) {
        int totalWin = 0;

        for(int[][] line : WIN_LINES) {
            int matched = checkLine(grid, line);
            if(matched >= 3) {
                totalWin += PAY_TABLE.get(matched);
                logWinningLine(line, grid);
            }
        }

        if(totalWin > 0) {
            Common.SCORE += totalWin;
            txtScore.setText(String.valueOf(Common.SCORE) + "$");
            Toast.makeText(this, "Выигрыш: $" + totalWin, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Попробуйте ещё раз", Toast.LENGTH_SHORT).show();
        }
    }

    private int checkLine(int[][] grid, int[][] line) {
        int firstSymbol = grid[line[0][0]][line[0][1]];
        int count = 1;

        for(int i = 1; i < line.length; i++) {
            int currentSymbol = grid[line[i][0]][line[i][1]];
            if(currentSymbol == firstSymbol) {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    private void logSlotGrid(int[][] grid) {
        StringBuilder sb = new StringBuilder("\n┌─────┬─────┬─────┬─────┬─────┐\n");
        for(int i = 0; i < grid.length; i++) {
            sb.append("│ ");
            for(int j = 0; j < grid[i].length; j++) {
                sb.append(String.format("%2d  │ ", grid[i][j]));
            }
            sb.append("\n");
            if(i < grid.length - 1) sb.append("├─────┼─────┼─────┼─────┼─────┤\n");
        }
        sb.append("└─────┴─────┴─────┴─────┴─────┘");
        Log.d("SLOT_DEBUG", "Текущая сетка:" + sb.toString());
    }

    private void logWinningLine(int[][] line, int[][] grid) {
        Log.d("WIN_DEBUG", "Выигрышная линия: " + Arrays.deepToString(line));
        for(int[] pos : line) {
            Log.d("WIN_DEBUG", String.format("[%d][%d] = %d",
                    pos[0], pos[1], grid[pos[0]][pos[1]]));
        }
    }

    private void hideSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}