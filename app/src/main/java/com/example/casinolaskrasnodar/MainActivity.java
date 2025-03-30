package com.example.casinolaskrasnodar;

import static android.view.View.VISIBLE;

import static androidx.constraintlayout.widget.ConstraintSet.GONE;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.casinolaskrasnodar.SupabaseManager;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.media.AudioAttributes;

import com.example.casinolaskrasnodar.ImageViewScrolling.IEventEnd;
import com.example.casinolaskrasnodar.ImageViewScrolling.ImageViewScrolling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity implements IEventEnd {


    private SupabaseManager supabaseManager;

    private WinLineView winLineView;
    private List<int[][]> currentWinningLines = new ArrayList<>();



    private final List<AnimatorSet> activeAnimations = new CopyOnWriteArrayList<>();

    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private int winSoundId;


    // UI элементы
    private Button btnSpin;
    private TextView txtScore;
    private TextView txtWin;

    // Слоты (3 ряда x 5 колонок)
    private ImageViewScrolling[] slots = new ImageViewScrolling[15];

    // Логика игры
    private int countDone = 0;
    private final int TOTAL_SLOTS = 15;
    private final int SPIN_COST = 50;




    private static final int[][] SLOT_MAP = {
            // Ряд 0 (верхний)
            {0, 1, 2, 9, 12},

            // Ряд 1 (средний)
            {3, 4, 5, 10, 13},

            // Ряд 2 (нижний)
            {6, 7, 8, 11, 14}
    };
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


            {{0,0}, {0,1}, {1,2}, {0,3}, {0,4}},
            {{2,0}, {2,1}, {1,2}, {2,3}, {2,4}},

            {{1,0}, {0,1}, {0,2}, {0,3}, {1,4}},
            {{1,0}, {2,1}, {2,2}, {2,3}, {1,4}},

            {{1,0}, {0,1}, {1,2}, {0,3}, {1,4}},
            {{1,0}, {2,1}, {1,2}, {2,3}, {1,4}},

            {{0,0}, {1,1}, {0,2}, {1,3}, {0,4}},
            {{2,0}, {1,1}, {2,2}, {1,3}, {2,4}},

            {{1,0}, {1,1}, {0,2}, {1,3}, {1,4}},
            {{1,0}, {1,1}, {2,2}, {1,3}, {1,4}}




    };

    // Платёжная таблица
    private final Map<Integer, Integer> PAY_TABLE = new HashMap<Integer, Integer>() {{
        put(3, 200);
        put(4, 400);
        put(5, 1000);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        supabaseManager = new SupabaseManager();
        checkAuth();



        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(3)
                .setAudioAttributes(attributes)
                .build();

        // Загрузка звука
        winSoundId = soundPool.load(this, R.raw.win_sound, 1);



        mediaPlayer = MediaPlayer.create(this, R.raw.casino_music); // ← Перенесено
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f, 0.5f);
        initMusic();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);



        winLineView = findViewById(R.id.winLineView);
        setupSlotDimensions();
        hideSystemUI();
        loadUserBalance();
        initViews();
        setupSpinButton();




        View sideMenu = findViewById(R.id.side_menu);
        View overlay = findViewById(R.id.overlay);



        findViewById(R.id.btn_menu).setOnClickListener(v -> {
            sideMenu.setVisibility(View.VISIBLE);
            sideMenu.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            overlay.setVisibility(View.VISIBLE);
            overlay.animate()
                    .alpha(0.7f)
                    .setDuration(200)
                    .start();
        });

        // Скрытие меню
        overlay.setOnClickListener(v -> {
            sideMenu.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> sideMenu.setVisibility(View.INVISIBLE))
                    .start();

            overlay.animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(() -> overlay.setVisibility(View.GONE))
                    .start();
        });


        findViewById(R.id.menu_slots).setOnClickListener(v -> {
            // Обработка перехода к слотам
        });

        findViewById(R.id.menu_blackjack).setOnClickListener(v -> {
            // Обработка перехода к блэкджеку
        });

        findViewById(R.id.menu_settings).setOnClickListener(v -> {
            // Обработка перехода к настройкам
        });

        findViewById(R.id.menu_exit).setOnClickListener(v -> {
            finish();
        });

    }


    private void setupSlotDimensions() {
        findViewById(R.id.frame_bar).getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        FrameLayout frame = findViewById(R.id.frame_bar);
                        int width = frame.getWidth() / 5;
                        int height = frame.getHeight() / 3;
                        winLineView.setCellSize(width, height);
                        frame.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                });
    }

    private void initViews() {
        btnSpin = findViewById(R.id.btn_up);
        txtScore = findViewById(R.id.txt_score);
        txtWin =  findViewById(R.id.txt_win);

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

        stopAnimations(); // Добавить в начало метода

        winLineView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> winLineView.setVisibility(View.GONE))
                .start();

        txtWin.setText("0" + "$");

        // Блокируем кнопку на время анимации
        btnSpin.setEnabled(false);
        Common.SCORE -= SPIN_COST;
        txtScore.setText(String.valueOf(Common.SCORE) + "$");
        updateBalanceOnServer(Common.SCORE);

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
        currentWinningLines.clear();
        int totalWin = 0;

        for(int[][] line : WIN_LINES) {
            int matched = checkLine(grid, line);
            if(matched >= 3) {
                // Берем только совпавшую часть линии
                int[][] winningPart = Arrays.copyOf(line, matched);
                currentWinningLines.add(winningPart);
                soundPool.play(winSoundId, 1.0f, 1.0f, 0, 0, 1.0f);

                totalWin += PAY_TABLE.get(matched);
                logWinningLine(winningPart, grid);
            }
        }

        if(totalWin > 0) {
            showWinAnimations();
            Common.SCORE += totalWin;
            updateBalanceOnServer(Common.SCORE);
            txtWin.setText(totalWin + "$");
            txtScore.setText(String.valueOf(Common.SCORE) + "$");
            Log.e("checkWin", "Выигрыш: $" + totalWin);
        } else {

        }
    }


    private void showWinAnimations() {

        winLineView.startBlinkAnimation();
        // Анимация линий
        winLineView.setWinningLines(currentWinningLines);
        winLineView.setVisibility(VISIBLE);
        winLineView.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        // Анимация символов
        animateWinningSymbols();
    }

    private void animateWinningSymbols() {
        for(int[][] line : currentWinningLines) {
            for(int[] pos : line) {
                int realIndex = getRealIndex(pos[0], pos[1]);
                if(realIndex != -1 && realIndex < slots.length) {
                    Log.d("ANIM_DEBUG", "Animating: " + realIndex);
                    animateSlot(slots[realIndex].current_image);
                }
            }
        }
    }
    private int getRealIndex(int row, int col) {
        if(row >= 0 && row < 3 && col >= 0 && col < 5) {
            return SLOT_MAP[row][col];
        }
        return -1; // Ошибка
    }

    private void animateSlot(ImageView symbol) {
        if(symbol == null) return;

        // Отмена предыдущих анимаций
        symbol.animate().cancel();
        symbol.setScaleX(1f);
        symbol.setScaleY(1f);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(symbol, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(symbol, "scaleY", 1f, 1.2f, 1f);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(500);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                activeAnimations.remove(animation);
            }
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        activeAnimations.add(set);
        set.start();
    }


    private void stopAnimations() {
        List<AnimatorSet> animationsCopy = new ArrayList<>(activeAnimations);
        // Останавливаем все активные анимации
        for (AnimatorSet animator : animationsCopy) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
        }
        activeAnimations.clear();

        // Сбрасываем состояние всех изображений
        for(ImageViewScrolling slot : slots) {
            if(slot != null && slot.current_image != null) {
                slot.current_image.setScaleX(1f);
                slot.current_image.setScaleY(1f);
            }
        }

        // Сбрасываем линии выигрыша
        winLineView.clearAnimation();
        winLineView.setVisibility(View.GONE);
    }


    private int checkLine(int[][] grid, int[][] line) {
        int firstSymbol = grid[line[0][0]][line[0][1]];
        int count = 1;

        for(int i = 1; i < line.length; i++) {
            if(grid[line[i][0]][line[i][1]] != firstSymbol) break;
            count++;
        }
        return count >= 3 ? count : 0;
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

    private void updateBalanceOnServer(int newBalance) {
        supabaseManager.updateUserBalance(Common.AUTH_TOKEN, newBalance, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Баланс обновлен", Toast.LENGTH_SHORT).show();
                    Log.e("updateBalanceOnServer", "Баланс обновлен");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Ошибка обновления: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }



    private void checkAuth() {
        if (Common.AUTH_TOKEN == null) {
            Log.e("AuthCheck", "User not authenticated, redirecting...");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }


    private void loadUserBalance() {
        if (Common.AUTH_TOKEN == null) return;

        supabaseManager.fetchUserBalance(Common.AUTH_TOKEN, new SupabaseManager.SupabaseCallback() {
            @Override
            public void onSuccess(String balance) {
                runOnUiThread(() -> {
                    Common.SCORE = Integer.parseInt(balance);
                    txtScore.setText(Common.SCORE + "$");
                    btnSpin.setEnabled(Common.SCORE >= SPIN_COST);
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    if (e.getMessage().contains("Пользователь не найден")) {
                        Log.e("uuuuu", "Конченная мразота");
                    } else {
                        Toast.makeText(MainActivity.this,
                                "Ошибка загрузки баланса",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }


    private void initMusic() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        } else {
            Log.e("MusicError", "MediaPlayer not initialized");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause(); // Пауза при сворачивании
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start(); // Возобновление при возврате
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Остановка и освобождение при закрытии
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBackPressed() {
        // Остановка музыки при нажатии "Назад"
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onBackPressed();
    }
}




