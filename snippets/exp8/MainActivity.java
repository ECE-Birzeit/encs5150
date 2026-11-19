package edu.birzeit.encs5150.exp8;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Exp8Rest";
    private static final String STUDENTS_URL = "https://encs5150.birzeit.me/api/students.json";
    private static final String ADD_STUDENT_URL = "https://jsonplaceholder.typicode.com/users";

    // One background thread for the requests, and a Handler to get back to the main thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Button getDataButton;
    private Button addButton;
    private ProgressBar progressBar;
    private TextView statusText;
    private LinearLayout studentList;
    private EditText nameInput;
    private EditText ageInput;

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

        getDataButton = findViewById(R.id.button_get);
        addButton = findViewById(R.id.button_add);
        progressBar = findViewById(R.id.progress);
        statusText = findViewById(R.id.text_status);
        studentList = findViewById(R.id.student_list);
        nameInput = findViewById(R.id.edit_name);
        ageInput = findViewById(R.id.edit_age);

        getDataButton.setOnClickListener(v -> getStudents());
        addButton.setOnClickListener(v -> addStudent());
    }

    /** GET the list of students and show it. */
    private void getStudents() {
        setBusy(true, "Connecting…");
        executor.execute(() -> {
            // Background thread: the network and the parsing, no views
            try {
                HttpManager.Response response = HttpManager.get(STUDENTS_URL);
                if (!response.isSuccessful()) {
                    showError("The server answered " + response.code);
                    return;
                }
                List<Student> students = StudentJsonParser.fromJsonArray(response.body);
                mainHandler.post(() -> {
                    // Main thread: now it is safe to touch the views
                    setBusy(false, "Got " + students.size() + " students");
                    studentList.removeAllViews();
                    for (Student student : students) {
                        addRow(student);
                    }
                });
            } catch (IOException e) {
                showError(describe(e));
            } catch (JSONException e) {
                showError("The reply is not the JSON we expected");
            }
        });
    }

    /** POST a new student as JSON and show what the server sent back. */
    private void addStudent() {
        String name = nameInput.getText().toString().trim();
        String ageText = ageInput.getText().toString().trim();
        if (name.isEmpty()) {
            nameInput.setError("Enter a name");
            return;
        }
        if (ageText.isEmpty()) {
            ageInput.setError("Enter an age");
            return;
        }
        int age = Integer.parseInt(ageText);   // inputType="number" allows digits only

        setBusy(true, "Sending…");
        executor.execute(() -> {
            try {
                String json = StudentJsonParser.toJson(name, age).toString();
                HttpManager.Response response = HttpManager.post(ADD_STUDENT_URL, json);
                if (!response.isSuccessful()) {
                    showError("The server answered " + response.code);
                    return;
                }
                Student created = StudentJsonParser.fromJson(new JSONObject(response.body));
                mainHandler.post(() -> {
                    setBusy(false, "The server answered " + response.code + " with:\n" + response.body.trim());
                    addRow(created);
                    nameInput.setText("");
                    ageInput.setText("");
                });
            } catch (IOException e) {
                showError(describe(e));
            } catch (JSONException e) {
                showError("The reply is not the JSON we expected");
            }
        });
    }

    private void addRow(Student student) {
        TextView row = new TextView(this);
        row.setText(student.toString());
        row.setTextSize(18);
        row.setPadding(0, 12, 0, 12);
        studentList.addView(row);
    }

    // Called from the background thread: hands the message to the main thread
    private void showError(String message) {
        Log.e(TAG, message);
        mainHandler.post(() -> setBusy(false, message));
    }

    private void setBusy(boolean busy, String message) {
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
        getDataButton.setEnabled(!busy);
        addButton.setEnabled(!busy);
        statusText.setText(message);
    }

    private static String describe(IOException e) {
        if (e instanceof UnknownHostException) {
            return "No internet connection, or the address is wrong";
        }
        if (e instanceof SocketTimeoutException) {
            return "The server took too long to answer";
        }
        return "Network error: " + e.getMessage();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdownNow();   // stop the background thread with the Activity
    }
}
