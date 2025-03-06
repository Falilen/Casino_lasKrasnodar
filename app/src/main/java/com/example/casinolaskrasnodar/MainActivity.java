package com.example.casinolaskrasnodar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.casinolaskrasnodar.ImageViewScrolling.IEventEnd;
import com.example.casinolaskrasnodar.ImageViewScrolling.ImageViewScrolling;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements IEventEnd {

    Button btn_up;
    ImageView btn_up2,btn_down;
    ImageViewScrolling image, image2, image3,image4, image5, image6,image7, image8, image9, image10, image11, image12, image13,image14,image15;
    TextView txt_score;

    int count_done=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        hideSystemUI();



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        btn_up = (Button) findViewById(R.id.btn_up);

        image = (ImageViewScrolling)findViewById(R.id.image);
        image2 = (ImageViewScrolling)findViewById(R.id.image2);
        image3 = (ImageViewScrolling)findViewById(R.id.image3);
        image4 = (ImageViewScrolling)findViewById(R.id.image4);
        image5 = (ImageViewScrolling)findViewById(R.id.image5);
        image6 = (ImageViewScrolling)findViewById(R.id.image6);
        image7 = (ImageViewScrolling)findViewById(R.id.image7);
        image8 = (ImageViewScrolling)findViewById(R.id.image8);
        image9 = (ImageViewScrolling)findViewById(R.id.image9);
        image10 = (ImageViewScrolling)findViewById(R.id.image10);
        image11 = (ImageViewScrolling)findViewById(R.id.image11);
        image12 = (ImageViewScrolling)findViewById(R.id.image12);
        image13 = (ImageViewScrolling)findViewById(R.id.image13);
        image14 = (ImageViewScrolling)findViewById(R.id.image14);
        image15 = (ImageViewScrolling)findViewById(R.id.image15);
        txt_score = (TextView)findViewById(R.id.txt_score);

        image.setEventEnd(MainActivity.this);
        image2.setEventEnd(MainActivity.this);
        image3.setEventEnd(MainActivity.this);
        image4.setEventEnd(MainActivity.this);
        image5.setEventEnd(MainActivity.this);
        image6.setEventEnd(MainActivity.this);
        image7.setEventEnd(MainActivity.this);
        image8.setEventEnd(MainActivity.this);
        image9.setEventEnd(MainActivity.this);
        image10.setEventEnd(MainActivity.this);
        image11.setEventEnd(MainActivity.this);
        image12.setEventEnd(MainActivity.this);
        image13.setEventEnd(MainActivity.this);
        image14.setEventEnd(MainActivity.this);
        image15.setEventEnd(MainActivity.this);

        btn_up.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (Common.SCORE >= 50)
                {
//                    btn_up.setVisibility(View.GONE);
//                    btn_down.setVisibility(View.VISIBLE);

                    image.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image2.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image3.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image4.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image5.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image6.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image7.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image8.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image9.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image10.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image11.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image12.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image13.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image14.SetValueRandom( new Random().nextInt((15-5)+1)+5);
                    image15.SetValueRandom( new Random().nextInt((15-5)+1)+5);

                    Common.SCORE -=50;
                    txt_score.setText(String.valueOf(Common.SCORE) + "$");
                }
                else {
                    Toast.makeText(MainActivity.this, "Нет деняг", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //@Override
//    public void eventEnd(int reuslt, int count) {
//if (count_done<2)
//    count_done++;
//else
//{
//
//    count_done=0;
//
//    if (image.getValue()==image2.getValue() && image2.getValue()==image3.getValue()){
//        Toast.makeText(this, "Большой Выйгрыш", Toast.LENGTH_SHORT).show();
//        Common.SCORE +=300;
//        txt_score.setText(String.valueOf(Common.SCORE + "$"));
//    }
//    else if (image.getValue()== image2.getValue() ||
//        image2.getValue()==image3.getValue()||
//        image3.getValue()==image.getValue())
//    {
//        Toast.makeText(this, "Выйгрыш", Toast.LENGTH_SHORT).show();
//        Common.SCORE +=100;
//        txt_score.setText(String.valueOf(Common.SCORE + "$"));
//    }
//    else
//    {
//        Toast.makeText(this, "Проигрыш", Toast.LENGTH_SHORT).show();
//
//    }
//}
//    }

    @Override
    public void eventEnd(int result, int count) {
        if (count_done < 2) {
            count_done++;
        } else {
            count_done = 0;

            // Представим игровое поле как матрицу
            int[][] grid = {
                    {image.getValue(), image2.getValue(), image3.getValue(), image10.getValue(), image13.getValue()},
                    {image4.getValue(), image5.getValue(), image6.getValue(), image11.getValue(), image14.getValue()},
                    {image7.getValue(), image8.getValue(), image9.getValue(), image12.getValue(), image15.getValue()}
            };

            // 18 типичных казино-линий, где каждая линия — это набор координат ячеек
            int[][][] lines = new int[][][]{
                    // Горизонтальные линии
                    {{0, 0}, {0, 1}, {0, 2}, {0, 3}, {0, 4}},  // Линия 1
                    {{1, 0}, {1, 1}, {1, 2}, {1, 3}, {1, 4}},  // Линия 2
                    {{2, 0}, {2, 1}, {2, 2}, {2, 3}, {2, 4}},  // Линия 3


                    // Диагональные линии
                    {{0, 0}, {1, 1}, {2, 2},{1,3},{0,4}},                // Линия 4
                    {{2, 0}, {1, 1}, {0, 2},{1,3},{2,4}},       //линия 5


            };

            boolean win = false;

            // Проверим все линии
            for (int i = 0; i < lines.length; i++) {
                int matchingCount = checkLine(grid, lines[i]);

                if (matchingCount >= 3) { // Если хотя бы 3 символа совпали (начинаем с выигрыша)
                    win = true;
                    int baseWin = 100; // Базовый выигрыш за 3 символа
                    int additionalWin = (matchingCount - 3) * 50; // Добавка за каждый дополнительный символ
                    int totalWin = baseWin + additionalWin;

                    Toast.makeText(this, "Выигрыш по линии №" + (i + 1) + " с " + matchingCount + " символами! Выигрыш: $" + totalWin, Toast.LENGTH_SHORT).show();

                    Common.SCORE += totalWin; // Увеличиваем счет
                    txt_score.setText(String.valueOf(Common.SCORE) + "$");
                    break; // После нахождения выигрышной линии можно остановить цикл
                }
            }

            if (!win) {
                //Toast.makeText(this, "Проигрыш", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int checkLine(int[][] grid, int[][] line) {
        int firstValue = grid[line[0][0]][line[0][1]];
        int matchingCount = 0; // Изначально совпадений нет

        // Проверяем каждый элемент линии
        for (int i = 0; i < line.length; i++) {
            // Если символ совпадает с первым символом
            if (grid[line[i][0]][line[i][1]] == firstValue) {
                matchingCount++;
            } else {
                // Прерываем проверку, если не совпало
                break;
            }
        }

        return matchingCount; // Возвращаем количество совпавших символов
    }
    private void hideSystemUI() {
        // Включение режима погружения (Immersive Mode)
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION   // Скрыть навигационные кнопки
                        | View.SYSTEM_UI_FLAG_FULLSCREEN        // Скрыть статус-бар (шторку)
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}