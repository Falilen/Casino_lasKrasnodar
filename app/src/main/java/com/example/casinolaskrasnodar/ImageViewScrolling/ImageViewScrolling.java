package com.example.casinolaskrasnodar.ImageViewScrolling;

import android.animation.Animator;
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

    private static int ANIMATION_DUR = 120;
    ImageView current_image, next_image;

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

        next_image.setTranslationY(getHeight());

}

    public void SetValueRandom(int rotate_count) {
        // Генерация случайных изображений для прокрутки
        Random random = new Random();
        int image = random.nextInt(6);
        current_image.setVisibility(View.VISIBLE);// Случайное изображение

        // Убираем текущую картинку вниз (она уходит с экрана)
        current_image.animate().translationY(getHeight())
                .setDuration(ANIMATION_DUR)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // После завершения анимации, картинка обновляется
                        setImage(current_image, image);
                        current_image.setTranslationY(0); // Возвращаем картинку в исходное положение
                    }
                })
                .start();

        // Показываем следующую картинку, которая идет сверху
        next_image.setTranslationY(-next_image.getHeight()); // Ставим новую картинку за экраном (сверху)
        setImage(next_image, random.nextInt(6)); // Устанавливаем случайную картинку

        next_image.animate().translationY(0)
                .setDuration(ANIMATION_DUR)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(@NonNull Animator animation) {
                        // Ничего не делаем при старте анимации
                    }

                    @Override
                    public void onAnimationEnd(@NonNull Animator animation) {
                        if (old_value < rotate_count - 1) {
                            old_value++;
                            SetValueRandom(rotate_count); // Продолжаем анимацию
                        } else {
                            // Завершаем анимацию
                            old_value = 0;
                            eventEnd.eventEnd(image, rotate_count);
                            current_image.setVisibility(View.GONE);// Сообщаем о завершении
                        }
                    }

                    @Override
                    public void onAnimationCancel(@NonNull Animator animation) {
                        // Ничего не делаем при отмене анимации
                    }

                    @Override
                    public void onAnimationRepeat(@NonNull Animator animation) {
                        // Ничего не делаем при повторе
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
        if(value== Util.BAR)
            imageView.setImageResource(R.drawable.b2);
        else if (value == Util.SEVEN)
            imageView.setImageResource(R.drawable.b1);
        else if (value == Util.LEMON)
            imageView.setImageResource(R.drawable.b3);
        else if (value == Util.ORANGE)
            imageView.setImageResource(R.drawable.b4);
        else if (value == Util.TRIPLE)
            imageView.setImageResource(R.drawable.b5);

        else
            imageView.setImageResource(R.drawable.b6);


        imageView.setTag(value);
        lat_result=value;
        }

    public int getValue(){
    return Integer.parseInt(next_image.getTag().toString());
    }
}
