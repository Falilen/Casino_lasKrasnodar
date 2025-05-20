package com.example.casinolaskrasnodar;

import static android.view.View.VISIBLE;

import static androidx.constraintlayout.widget.ConstraintSet.GONE;

import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.BAR;
import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.BROOMSTICK;
import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.LEMON;
import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.ORANGE;
import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.SEVEN;

import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.SUPER_SYMBOL;
import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.TRIPLE;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.casinolaskrasnodar.ImageViewScrolling.Util;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioAttributes;
import android.widget.VideoView;

import com.example.casinolaskrasnodar.ImageViewScrolling.IEventEnd;
import com.example.casinolaskrasnodar.ImageViewScrolling.ImageViewScrolling;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;



public class MainActivity_bonus extends AppCompatActivity implements IEventEnd {




    private SupabaseManager supabaseManager;

    private WinLineView winLineView;
    private List<int[][]> currentWinningLines = new ArrayList<>();

    private ProgressBar spiritProgress;
    private int spiritCount = 0; // Счетчик духов
    private int freeSpinCount = 0;

    private final List<AnimatorSet> activeAnimations = new CopyOnWriteArrayList<>();

    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;
    private int winSoundId;


    private ValueAnimator currentAnimator;



    // UI элементы
    private  VideoView witchCharacter;
    private Button btnSpin;
    private TextView txtScore;
    private TextView txtWin;

    // Слоты (3 ряда x 5 колонок)
    private ImageViewScrolling[] slots = new ImageViewScrolling[15];

    // Логика игры
    private int countDone = 0;
    private final int TOTAL_SLOTS = 15;



    private int SPIN_COST = 50;



    int[][] lastSpinResults;
    private TextView txtBet;



    private int[][] createGridFromSlots() {
        int[][] grid = new int[3][5];
        for (int i = 0; i < slots.length; i++) {
            int row = i / 5;
            int col = i % 5;
            grid[row][col] = slots[i].getValue();
        }
        return grid;
    }

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
        put(3, SPIN_COST);
        put(4, SPIN_COST*3);
        put(5, SPIN_COST*5);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_bonus_game);

        freeSpinCount = 10;
        Util.IS_BONUS_GAME = true;
        Common.IS_SUPER_GAME = true;




        int[][] lastSpinResults = (int[][]) getIntent().getSerializableExtra("LAST_SPIN_RESULTS");
        if (lastSpinResults == null) {
            Log.e("BONUS_ERROR", "Результаты последнего прокрута не найдены");
            finish(); // Закрываем активность, если данных нет
            return;
        }
        initializeSlots();
        initializeSlotsWithLastSpinResults(lastSpinResults);



        witchCharacter= findViewById(R.id.witch_character);


        supabaseManager = new SupabaseManager();



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
        Util.AVAILABLE_SYMBOLS = new ArrayList<>(Arrays.asList(
                BAR, SEVEN, LEMON, ORANGE, TRIPLE, BROOMSTICK
        ));


        Util.AVAILABLE_SYMBOLS.remove((Integer) BROOMSTICK); // Удаляем обычную метлу
        Util.AVAILABLE_SYMBOLS.add(BROOMSTICK); // Добавляем бонусную метлу





        mediaPlayer = MediaPlayer.create(this, R.raw.casino_music); // ← Перенесено
        mediaPlayer.setLooping(true);
        mediaPlayer.setVolume(0.5f, 0.5f);
        initMusic();



        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.witch_attack);
        witchCharacter.setVideoURI(videoUri);
        witchCharacter.start();

        spiritProgress = findViewById(R.id.spirit_progress);
        spiritProgress.setProgress(0); // Начальное значение



        winLineView = findViewById(R.id.winLineView);
        setupSlotDimensions();
        hideSystemUI();



        initViews();
        setupSpinButton();
        loadUserBalance();
        // Используем полученную ставку в бонусной игре
        SPIN_COST = getIntent().getIntExtra("BET_VALUE", 1);

        updatePayTable();
        updateBetUI();



        View sideMenu = findViewById(R.id.side_menu);
        View overlay = findViewById(R.id.overlay);






        findViewById(R.id.btn_menu).setOnClickListener(v -> {
            sideMenu.setVisibility(VISIBLE);
            sideMenu.animate()
                    .alpha(1f)
                    .setDuration(200)
                    .start();

            overlay.setVisibility(VISIBLE);
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

    private void initializeSlots() {
        slots = new ImageViewScrolling[]{
                findViewById(R.id.image), findViewById(R.id.image2), findViewById(R.id.image3),
                findViewById(R.id.image4), findViewById(R.id.image5), findViewById(R.id.image6),
                findViewById(R.id.image7), findViewById(R.id.image8), findViewById(R.id.image9),
                findViewById(R.id.image10), findViewById(R.id.image11), findViewById(R.id.image12),
                findViewById(R.id.image13), findViewById(R.id.image14), findViewById(R.id.image15)
        };

        // Проверка на null
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == null) {
                Log.e("SLOT_ERROR", "Слот #" + i + " не найден");
                throw new RuntimeException("Слот #" + i + " не инициализирован");
            }
        }
    }

    private void initializeSlotsWithLastSpinResults(int[][] lastSpinResults) {
        for (int i = 0; i < slots.length; i++) {
            int row = i / 5; // Номер строки
            int col = i % 5; // Номер столбца

            int value = lastSpinResults[row][col]; // Значение из последнего прокрута
            slots[i].setValue(value); // Устанавливаем значение в слот
        }
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
        txtBet = findViewById(R.id.txt_bet);






        slots = new ImageViewScrolling[15];

        // Инициализация всех слотов
        int[] slotIds = {
                R.id.image, R.id.image2, R.id.image3, R.id.image4, R.id.image5,
                R.id.image6, R.id.image7, R.id.image8, R.id.image9, R.id.image10,
                R.id.image11, R.id.image12, R.id.image13, R.id.image14, R.id.image15
        };

        for (int i = 0; i < slotIds.length; i++) {
            ImageViewScrolling slot = findViewById(slotIds[i]);
            if (slot == null) {
                Log.e("SLOT_INIT", "Слот #" + i + " не найден");
                throw new RuntimeException("Слот #" + i + " не инициализирован");
            }
            slots[i] = slot;
            slots[i].setEventEnd(this);
        }
    }

    private void updatePayTable() {
        PAY_TABLE.clear();
        PAY_TABLE.put(3, SPIN_COST * 3);
        PAY_TABLE.put(4, SPIN_COST * 5);
        PAY_TABLE.put(5, SPIN_COST * 10);
    }


    private void updateBetUI() {

        txtBet.setText(SPIN_COST + " $");
    }

    private void setupSpinButton() {
        btnSpin.setOnClickListener(v -> {
            startSpin(false);

        });
    }

    private void startSpin(boolean isFreeSpin) {


        freeSpinCount--;


        if(!isFreeSpin && Common.SCORE < SPIN_COST) {
            Toast.makeText(this, "Недостаточно средств", Toast.LENGTH_SHORT).show();
            return;
        }


            updateBalanceOnServer(Common.SCORE);

        stopAnimations(); // Добавить в начало метода

        winLineView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> winLineView.setVisibility(View.GONE))
                .start();

        txtWin.setText("0" + "$");

        // Блокируем кнопку на время анимации
        btnSpin.setEnabled(false);
        txtScore.setText(Common.SCORE + "$");

        // Генерируем общее количество вращений
        int spinCount = 5 + new Random().nextInt(5);

        // Запускаем анимацию для всех слотов
        for(ImageViewScrolling slot : slots) {
            slot.SetValueRandom(spinCount);
        }

        resetSlotsVisibility(); // Сбрасываем видимость всех слотов

        // Разблокируем кнопку через 3.5 секунды
        new Handler().postDelayed(() -> btnSpin.setEnabled(true), 2000);
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





        int broomCount = countBrooms(grid);
        updateSpiritCount(broomCount);
        animateBroomCollection();


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

            Common.TOTAL_BONUS_WIN += totalWin; // Добавляем выигрыш к общему

            txtScore.setText(String.valueOf(Common.SCORE) + "$");
            Log.e("checkWin", "Выигрыш: $" + totalWin);



        } else {
            // Нет выигрыша

        }
    }

    private int countBrooms(int[][] grid) {
        int count = 0;
        for (int[] row : grid) {
            for (int symbol : row) {
                if (symbol == BROOMSTICK) {
                    count++;
                }
            }
        }
        Log.e("tupo", "Количество метёл" + count);
        return count;
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
                    Log.e("updateBalanceOnServer", "Баланс обновлен");
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e("BD", "Ошибка обновления: " + e.getMessage());
                });
            }
        });
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
                        Log.e("uuuuu", "Пользователь не найден");
                    } else {
                        Log.e("БД", "Ошибка загрузки баланса");
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




    private int selectTargetSymbol(int[][] grid) {
        Map<Integer, Integer> symbolCounts = new HashMap<>();
        for (int[] row : grid) {
            for (int symbol : row) {
                if (symbol != BROOMSTICK && symbol != SUPER_SYMBOL) {
                    symbolCounts.merge(symbol, 1, Integer::sum);
                }
            }
        }
        return symbolCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(BAR); // По умолчанию удаляем BAR
    }

    private void animateBroomCollection() {
        for (ImageViewScrolling slot : slots) {
            if (slot.getValue() == Util.BROOMSTICK) { // Только для метел
                ImageView broom = slot.current_image;
                if (broom == null) continue;

                // Создаем и анимируем "дух"
                animateSpiritFlight(broom);

            }
        }
    }

    private void resetSlotsWithNewSymbols() {
        for (ImageViewScrolling slot : slots) {




            // Устанавливаем новое значение
            int newValue = Util.getRandomSymbol();
            slot.setValue(newValue);
            slot.current_image.setVisibility(View.VISIBLE);
        }
        int[][] grid = createGridFromSlots(); // Получаем текущую сетку
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                int newValue = Util.getRandomBonusSymbol(); // Генерация нового символа
                grid[row][col] = newValue;
            }
        }
        syncSlotsWithGrid(grid); // Синхронизируем слоты с новой сеткой

    }
    private void syncSlotsWithGrid(int[][] grid) {
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 5; col++) {
                if (index < slots.length) {
                    slots[index].setValue(grid[row][col]);
                    index++;
                }
            }
        }
    }

    private void resetSlotsVisibility() {
        for (ImageViewScrolling slot : slots) {
            if (slot != null && slot.current_image != null) {
                slot.current_image.setVisibility(View.VISIBLE); // Делаем видимым
                slot.current_image.setAlpha(1f); // Восстанавливаем непрозрачность
            }
        }
    }


    private void highlightDestroyedSymbol(int symbol) {
        for (ImageViewScrolling slot : slots) {
            if (slot.getValue() == symbol) {
                // Анимация взрыва/исчезновения
                slot.current_image.animate()
                        .scaleX(0f) // Уменьшение по горизонтали
                        .scaleY(0f) // Уменьшение по вертикали
                        .alpha(0f)  // Плавное исчезновение
                        .setDuration(500) // Длительность анимации
                        .withEndAction(() -> {
                            slot.current_image.setVisibility(View.INVISIBLE);
                            slot.setValue(-1); // Устанавливаем значение "пусто"// Скрываем символ
                            slot.setValue(Util.getRandomBonusSymbol()); // Генерируем новый символ

                        })
                        .start();
            }
        }
    }


    private void animateSpiritFlight(ImageView broom) {
        if (witchCharacter == null || broom == null) return;

        // Получаем координаты ведьмы
        int[] witchLocation = new int[2];
        spiritProgress.getLocationOnScreen(witchLocation);
        int witchCenterX = witchLocation[0] + spiritProgress.getWidth() / 2;
        int witchCenterY = witchLocation[1] + spiritProgress.getHeight() / 2;

        // Получаем координаты метлы
        int[] broomLocation = new int[2];
        broom.getLocationOnScreen(broomLocation); // Координаты относительно экрана

        // Создаем "дух" (голубую точку)
        ImageView spirit = new ImageView(this);
        spirit.setImageResource(R.drawable.blue_dot); // Замените на ваш ресурс
        spirit.setLayoutParams(new ViewGroup.LayoutParams(
                200, // Ширина
                200  // Высота
        ));
        spirit.setX(broomLocation[0] + broom.getWidth() / 2 - 25); // Центрируем относительно метлы
        spirit.setY(broomLocation[1] + broom.getHeight() / 2 - 25);

        // Добавляем "дух" в контейнер
        FrameLayout spiritContainer = findViewById(R.id.spirit_container);
        if (spiritContainer != null) {
            spiritContainer.addView(spirit);
        } else {
            Log.e("SPIRIT_ERROR", "Контейнер для духов не найден");
            return;
        }

        // Анимация полета к ведьме
        ObjectAnimator animX = ObjectAnimator.ofFloat(
                spirit,
                "x",
                spirit.getX(),
                witchCenterX - spirit.getWidth() / 2 // Центрируем относительно ведьмы
        );
        ObjectAnimator animY = ObjectAnimator.ofFloat(
                spirit,
                "y",
                spirit.getY(),
                witchCenterY - spirit.getHeight() / 2
        );

        AnimatorSet set = new AnimatorSet();
        set.playTogether(animX, animY);
        set.setDuration(800);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                spiritContainer.removeView(spirit); // Удаляем "дух" после завершения анимации
            }
        });
        set.start();





    }


    private void updateSpiritCount(int addedValue) {
        int newSpiritCount = spiritCount + addedValue;

        if (newSpiritCount >= 10) {
            int overflow = newSpiritCount - 10;
            spiritCount = 10; // Устанавливаем максимум
            animateSpiritProgress(10); // Анимация до максимума

            // После завершения анимации активируем силу духов
            new Handler().postDelayed(() -> {
                spiritCount = overflow; // Сохраняем остаток для следующего этапа
            }, 1000); // Задержка для завершения анимации
        } else {
            spiritCount = newSpiritCount;
            animateSpiritProgress(spiritCount); // Обычная анимация
        }
    }

    private void animateSpiritProgress(int targetProgress) {
        // Отменяем предыдущую анимацию
        if (currentAnimator != null) {
            currentAnimator.cancel();
        }

        // Нормализуем значение прогресса
        int normalizedProgress = Math.min(targetProgress, spiritProgress.getMax());

        // Анимация прогресса
        ValueAnimator animator = ValueAnimator.ofInt(spiritProgress.getProgress(), normalizedProgress);
        animator.setDuration(500);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            spiritProgress.setProgress(progress);

            // Анимация цвета прогресса

        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentAnimator = null;
                if (freeSpinCount == 0)
                {
                    showBonusEndDialog();
                }
                // Если достигнут максимум, активируем силу духов
                if (normalizedProgress >= spiritProgress.getMax()) {
                    activateSpiritPower();
                }
            }
        });

        currentAnimator = animator;
        animator.start();
    }

    private int interpolateColor(int startColor, int endColor, float fraction) {
        int red = (int) (Color.red(startColor) + fraction * (Color.red(endColor) - Color.red(startColor)));
        int green = (int) (Color.green(startColor) + fraction * (Color.green(endColor) - Color.green(startColor)));
        int blue = (int) (Color.blue(startColor) + fraction * (Color.blue(endColor) - Color.blue(startColor)));
        return Color.rgb(red, green, blue);
    }
    private void activateSpiritPower() {
        Log.e("BROOM_ERROR", "Удаление");
        witchCharacter.start();

        // 1. Сброс прогресса
        spiritCount = 0;
        spiritProgress.setProgress(0);

        // 2. Выбор символа для уничтожения
        int targetSymbol = Util.getRandomBonusSymbol(); // Используем список для бонусной игры
        if (targetSymbol == -1) return;

        // 3. Удаляем символ из доступных
        if (Util.AVAILABLE_BONUS_SYMBOLS.size() <= 1) {
            Log.e("BROOM_ERROR", "Невозможно удалить символ - список пуст");
            return;
        }

        Util.removeSymbol(targetSymbol);
        Log.d("SYMBOL_REMOVE", "Символ удален: " + targetSymbol);

        // 4. Анимация опустошения прогресса
        ObjectAnimator progressAnim = ObjectAnimator.ofInt(
                spiritProgress,
                "progress",
                10,
                0
        );
        progressAnim.setDuration(500);
        progressAnim.start();





        // 6. Визуальная индикация
        highlightDestroyedSymbol(targetSymbol);

        //resetSlotsWithNewSymbols();

    }




    @Override
    protected void onDestroy() {
        Log.e("Game over","Конец бонуски");
        // Восстанавливаем все символы
        Util.IS_BONUS_GAME = false;
        Util.AVAILABLE_SYMBOLS.remove((Integer) BROOMSTICK);
        Util.AVAILABLE_SYMBOLS.add(BROOMSTICK);

        super.onDestroy();

    }


    private void showBonusEndDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_bonus_end, null);
        builder.setView(dialogView);

        // Анимация заголовка
        TextView title = dialogView.findViewById(R.id.title);
        title.animate()
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        // Анимация кнопки
        Button returnButton = dialogView.findViewById(R.id.action_button);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(returnButton, "scaleX", 0.9f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(returnButton, "scaleY", 0.9f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.setDuration(800);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();

        // Создаем диалог
        AlertDialog dialog = builder.create();

        TextView totalWinText = dialogView.findViewById(R.id.win_text);


        // Блокируем закрытие диалога
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int totalWin = Common.TOTAL_BONUS_WIN; // Предположим, что вы отслеживаете общий выигрыш
        totalWinText.setText("Общий выигрыш: " + totalWin + "$");


        // Настройка прозрачного фона и анимации
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setWindowAnimations(R.style.DialogAnimation);

        // Обработчик нажатия на кнопку
        returnButton.setOnClickListener(v -> {
            Common.IS_SUPER_GAME = false;
            Common.TOTAL_BONUS_WIN = 0;

            stopAnimations();
            Common.SCORE += totalWin;

            dialog.dismiss(); // Закрываем диалог
            // Запускаем новую активность для бонусной игры
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            // Закрываем текущую активность
            finish();
        });

        // Показываем диалог с проверкой
        if (!isFinishing() && !isDestroyed()) {
            dialog.show();
        }

        dialog.show();
    }

}




