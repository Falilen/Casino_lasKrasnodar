package com.example.casinolaskrasnodar.ImageViewScrolling;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
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

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.image_view_scrolling,this);
        current_image = (ImageView)getRootView().findViewById(R.id.current_image);
        next_image = (ImageView)getRootView().findViewById(R.id.next_image);

        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            next_image.setTranslationY(getHeight());
        });

}

    public void SetValueRandom(int rotate_count) {
        if (isAnimating) return;
        isAnimating = true;

        Random random = new Random();
        int image = random.nextInt(6);

        // Скрыть next_image перед началом анимации
        next_image.setVisibility(View.INVISIBLE);
        current_image.setVisibility(View.VISIBLE);

        // Анимация текущего изображения вниз
        current_image.animate()
                .translationY(getHeight())
                .setDuration(ANIMATION_DUR)
                .withEndAction(() -> {
                    // После завершения анимации:
                    current_image.setVisibility(View.INVISIBLE); // Скрыть текущее
                    setImage(current_image, image); // Обновить изображение
                    current_image.setTranslationY(0);
                })
                .start();

        // Настройка следующего изображения
        next_image.setTranslationY(-next_image.getHeight());
        setImage(next_image, random.nextInt(6));
        next_image.setVisibility(View.VISIBLE); // Показать следующее

        // Анимация следующего изображения вверх
        next_image.animate()
                .translationY(0)
                .setDuration(ANIMATION_DUR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        next_image.setVisibility(View.INVISIBLE); // Скрыть следующее
                        current_image.setVisibility(View.VISIBLE); // Показать обновленное текущее
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
        imageView.setVisibility(View.VISIBLE);

        if (value == Util.BAR) {
            imageView.setImageResource(R.drawable.b2);
        } else if (value == Util.SEVEN) {
            imageView.setImageResource(R.drawable.b1);
        } else if (value == Util.LEMON) {
            imageView.setImageResource(R.drawable.b3);
        } else if (value == Util.ORANGE) {
            imageView.setImageResource(R.drawable.b4);
        } else if (value == Util.TRIPLE) {
            imageView.setImageResource(R.drawable.b5);
        } else {
            imageView.setImageResource(R.drawable.b6);
        }

        imageView.setTag(value);
        lat_result = value;
    }

    public int getValue() {
        // Возвращаем значение из current_image, а не next_image
        if (current_image.getTag() != null) {
            return Integer.parseInt(current_image.getTag().toString());
        }
        return -1; // Значение по умолчанию при ошибке
    }


    public void reset() {
        current_image.setVisibility(View.INVISIBLE);
        next_image.setVisibility(View.INVISIBLE);
        current_image.setTranslationY(0);
        next_image.setTranslationY(0);
    }


}
