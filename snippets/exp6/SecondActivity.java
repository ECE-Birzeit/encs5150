package edu.birzeit.encs5150.exp6;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView usernameTextView = findViewById(R.id.textView_username);
        TextView passwordTextView = findViewById(R.id.textView_password);
        TextView saveCountTextView = findViewById(R.id.textView_saveCount);
        Button loadButton = findViewById(R.id.button_load);
        Button backButton = findViewById(R.id.button_back);

        // The same object MainActivity used: getInstance doesn't create a second one.
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usernameTextView.setText("Username: "
                        + sharedPrefManager.readString(SharedPrefManager.KEY_USERNAME, "(not saved)"));
                passwordTextView.setText("Password: "
                        + sharedPrefManager.readString(SharedPrefManager.KEY_PASSWORD, "(not saved)"));
                saveCountTextView.setText("Saved: "
                        + sharedPrefManager.readInt(SharedPrefManager.KEY_SAVE_COUNT, 0) + " times");
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // MainActivity is still underneath on the back stack
            }
        });
    }
}
