package com.example.casinolaskrasnodar.ImageViewScrolling;

import static com.example.casinolaskrasnodar.ImageViewScrolling.Util.getRandomBonusSymbol;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.casinolaskrasnodar.Common;
import com.example.casinolaskrasnodar.R;
import java.util.Random;

public class ImageViewScrolling extends FrameLayout {


    private boolean isAnimating = false;
    private static int ANIMATION_DUR = 100;
    public ImageView current_image;
    ImageView next_image;

    int lat_result = 0,old_value = 0;

    IEventEnd eventEnd;

    public void  setEventEnd(IEventEnd eventEnd){
        this.eventEnd = eventEnd;
    }

    public ImageViewScrolling(Context context) {
        super(context);
        init(context);
    }

    public ImageViewScrolling(Context context,AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void setValue(int value) {
        // Устанавливаем тег до изменения изображения
        current_image.setTag(value);
        setImage(current_image, value);

        // Сбрасываем анимацию
        current_image.setTranslationY(0);
        next_image.setTranslationY(-next_image.getHeight());
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.image_view_scrolling,this);
        current_image = findViewById(R.id.current_image);
        next_image =  findViewById(R.id.next_image);

        Random random = new Random();
        int initialValue = random.nextInt(6); // 0-5 для обычных символов
        setImage(current_image, initialValue);
        setImage(next_image, random.nextInt(6));

        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            next_image.setTranslationY(getHeight());
        });

}

    public void SetValueRandom(int rotate_count) {
        if (isAnimating) return; // Защита от повторного вызова
        isAnimating = true;

        Random random = new Random();
        int image = Common.IS_SUPER_GAME ? Util.getRandomSymbol() : random.nextInt(6);

        // Скрыть next_image перед началом анимации
        next_image.setVisibility(View.INVISIBLE);
        current_image.setVisibility(View.VISIBLE);

        // Анимация текущего изображения вниз
        current_image.animate()
                .translationY(getHeight()) // Двигаем вниз за пределы экрана
                .setDuration(ANIMATION_DUR)
                .withEndAction(() -> {
                    // После завершения анимации:
                    current_image.setVisibility(View.INVISIBLE); // Скрываем текущее
                    setImage(current_image, image); // Обновляем изображение
                    current_image.setTranslationY(0); // Сброс позиции
                })
                .start();

        // Настройка следующего изображения
        setImage(next_image, image);
        next_image.setTranslationY(-next_image.getHeight()); // Начальная позиция над экраном
        next_image.setVisibility(View.VISIBLE); // Показать следующее

        // Анимация следующего изображения вверх
        next_image.animate()
                .translationY(0) // Двигаем в центр
                .setDuration(ANIMATION_DUR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        next_image.setVisibility(View.INVISIBLE); // Скрываем следующее
                        current_image.setVisibility(View.VISIBLE); // Показываем обновленное текущее
                        isAnimating = false;

                        if (old_value < rotate_count - 1) {
                            old_value++;
                            SetValueRandom(rotate_count);
                        } else {
                            old_value = 0;
                            eventEnd.eventEnd(image, rotate_count);
                        }
                    }
                })
                .start();
    }




    // Проверка на уникальность
    private boolean contains(int[] array, int value) {
        for (int i : array) {
            if (i == value) {
                return true;
            }
        }
        return false;
    }





    private void setImage(ImageView imageView, int value) {
        if (imageView == null) return;

        switch (value) {
            case Util.BAR: imageView.setImageResource(R.drawable.b2); break;
            case Util.SEVEN: imageView.setImageResource(R.drawable.b1); break;
            case Util.LEMON: imageView.setImageResource(R.drawable.b3); break;
            case Util.ORANGE: imageView.setImageResource(R.drawable.b4); break;
            case Util.TRIPLE: imageView.setImageResource(R.drawable.b5); break;
            case Util.SUPER_SYMBOL: imageView.setImageResource(R.drawable.free_spin); break;
            case Util.BROOMSTICK:
                imageView.setImageResource(R.drawable.broomstick);
                break;
            default: imageView.setImageResource(R.drawable.b6);
        }

        imageView.setTag(value);
    }

    public int getValue() {
        // Берем значение из current_image, а не next_image
        if (current_image.getTag() != null) {
            return (int) current_image.getTag();
        }
        return -1;
    }


    public void reset() {
        current_image.setVisibility(View.INVISIBLE);
        next_image.setVisibility(View.INVISIBLE);
        current_image.setTranslationY(0);
        next_image.setTranslationY(0);
    }


}
