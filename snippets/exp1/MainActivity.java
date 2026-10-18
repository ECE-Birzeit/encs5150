package edu.birzeit.encs5150.exp1;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

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

        // Connect the views declared in activity_main.xml to Java objects.
        EditText nameEditText = findViewById(R.id.editText_Name);
        Button greetButton = findViewById(R.id.button_Greet);
        TextView greetingTextView = findViewById(R.id.textView_Greeting);

        // Run this code every time the button is tapped.
        greetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameEditText.getText().toString().trim();
                if (name.isEmpty()) {
                    nameEditText.setError("Please enter your name");
                } else {
                    greetingTextView.setText("Hello, " + name + "!");
                }
            }
        });
    }
}
