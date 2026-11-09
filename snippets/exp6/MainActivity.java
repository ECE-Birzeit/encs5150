package edu.birzeit.encs5150.exp6;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

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

        EditText usernameEditText = findViewById(R.id.editText_username);
        EditText passwordEditText = findViewById(R.id.editText_password);
        CheckBox rememberCheckBox = findViewById(R.id.checkBox_remember);
        Button saveButton = findViewById(R.id.button_save);
        Button secondButton = findViewById(R.id.button_second);

        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);

        // Fill the form with what was saved last time. The second argument
        // is returned when the key has never been saved.
        usernameEditText.setText(sharedPrefManager.readString(SharedPrefManager.KEY_USERNAME, ""));
        boolean remember = sharedPrefManager.readBoolean(SharedPrefManager.KEY_REMEMBER_PASSWORD, false);
        rememberCheckBox.setChecked(remember);
        if (remember) {
            passwordEditText.setText(sharedPrefManager.readString(SharedPrefManager.KEY_PASSWORD, ""));
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = usernameEditText.getText().toString().trim();
                if (username.isEmpty()) {
                    usernameEditText.setError("Please enter a username");
                    return;
                }

                // String
                sharedPrefManager.writeString(SharedPrefManager.KEY_USERNAME, username);

                // boolean, and a value that is saved only when it is allowed
                boolean rememberPassword = rememberCheckBox.isChecked();
                sharedPrefManager.writeBoolean(SharedPrefManager.KEY_REMEMBER_PASSWORD, rememberPassword);
                if (rememberPassword) {
                    sharedPrefManager.writeString(SharedPrefManager.KEY_PASSWORD,
                            passwordEditText.getText().toString());
                } else {
                    sharedPrefManager.remove(SharedPrefManager.KEY_PASSWORD);
                }

                // int: read the old value, add one, write it back
                int saveCount = sharedPrefManager.readInt(SharedPrefManager.KEY_SAVE_COUNT, 0);
                sharedPrefManager.writeInt(SharedPrefManager.KEY_SAVE_COUNT, saveCount + 1);

                Toast.makeText(MainActivity.this, "Saved", Toast.LENGTH_SHORT).show();
            }
        });

        secondButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
    }
}
