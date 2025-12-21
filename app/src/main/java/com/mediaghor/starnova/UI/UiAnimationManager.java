package com.mediaghor.starnova.UI;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.widget.TextView;

import java.util.Random;

public class UiAnimationManager {

    private Context context;
    private Random random;
    private int[] colors;

    public UiAnimationManager(Context context) {
        this.context = context;
        this.random = new Random();

        // Define 3 colors: Yellow, Light Green, Light Blue
        colors = new int[]{
                Color.YELLOW,
                Color.parseColor("#90EE90"), // Light Green
                Color.parseColor("#ADD8E6")  // Light Blue
        };
    }

    /**
     * Animate text word by word in a TextView with random color
     *
     * @param text     The string to display
     * @param textView The TextView to set
     * @param delay    Delay between words in milliseconds
     */
    public void setTextAnimated(String text, TextView textView, int delay) {
        if (text == null || textView == null) return;

        // Choose a random color from the 3 colors
        int color = colors[random.nextInt(colors.length)];
        textView.setTextColor(color);

        // Split text into words
        String[] words = text.split("\\s+");

        // Handler to post word by word
        Handler handler = new Handler();
        textView.setText(""); // Clear existing text

        for (int i = 0; i < words.length; i++) {
            final int index = i;
            handler.postDelayed(() -> {
                if (textView.getText().length() > 0) {
                    textView.append(" "); // Add space before new word
                }
                textView.append(words[index]);
            }, delay * i);
        }
    }
}
