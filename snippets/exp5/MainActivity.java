package edu.birzeit.encs5150.exp5;

import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final int SWITCH_DURATION = 500; // milliseconds

    private boolean bulbOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Part 1: drawable animations ---

        ImageView switchImageView = findViewById(R.id.imageView_switch);
        ImageView blinkImageView = findViewById(R.id.imageView_blink);
        Button switchButton = findViewById(R.id.button_switch);
        Button blinkButton = findViewById(R.id.button_blink);

        // android:src="@drawable/bulb_switch" is a <transition>, so Android
        // turned it into a TransitionDrawable when it loaded the layout.
        TransitionDrawable switchDrawable = (TransitionDrawable) switchImageView.getDrawable();

        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bulbOn) {
                    switchDrawable.reverseTransition(SWITCH_DURATION); // fade back to "off"
                    switchButton.setText("Switch on");
                } else {
                    switchDrawable.startTransition(SWITCH_DURATION);   // fade to "on"
                    switchButton.setText("Switch off");
                }
                bulbOn = !bulbOn;
            }
        });

        // Likewise, an <animation-list> becomes an AnimationDrawable.
        AnimationDrawable blinkDrawable = (AnimationDrawable) blinkImageView.getDrawable();

        blinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (blinkDrawable.isRunning()) {
                    blinkDrawable.stop();
                    blinkDrawable.selectDrawable(0); // back to the first frame ("off")
                    blinkButton.setText("Blink");
                } else {
                    blinkDrawable.start();
                    blinkButton.setText("Stop");
                }
            }
        });

        // --- Part 2: tween animations ---

        ImageView tweenImageView = findViewById(R.id.imageView_tween);
        Button rotateButton = findViewById(R.id.button_rotate);
        Button scaleButton = findViewById(R.id.button_scale);
        Button translateButton = findViewById(R.id.button_translate);
        Button fadeButton = findViewById(R.id.button_fade);
        Button tourButton = findViewById(R.id.button_tour);

        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweenImageView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.rotate));
            }
        });

        scaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweenImageView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale));
            }
        });

        translateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweenImageView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.translate));
            }
        });

        fadeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweenImageView.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade));
            }
        });

        // The tour is loaded once and kept in an Animation object, so a
        // listener can disable the buttons while the tour is playing.
        Button[] tweenButtons = {rotateButton, scaleButton, translateButton, fadeButton, tourButton};
        Animation tour = AnimationUtils.loadAnimation(this, R.anim.tour);
        tour.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                for (Button button : tweenButtons) {
                    button.setEnabled(false);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                for (Button button : tweenButtons) {
                    button.setEnabled(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        tourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweenImageView.startAnimation(tour);
            }
        });
    }
}
