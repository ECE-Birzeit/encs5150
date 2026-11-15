package edu.birzeit.encs5150.exp7;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

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

        fragmentManager = getSupportFragmentManager();

        findViewById(R.id.button_add).setOnClickListener(v -> addFragment());
        findViewById(R.id.button_remove).setOnClickListener(v -> removeFragment());
        findViewById(R.id.button_replace).setOnClickListener(v -> replaceFragment());
        findViewById(R.id.button_detach).setOnClickListener(v -> detachFragment());
        findViewById(R.id.button_attach).setOnClickListener(v -> attachFragment());
    }

    private void addFragment() {
        if (fragmentManager.findFragmentById(R.id.fragment_container) != null) {
            Toast.makeText(this, "The box already has a fragment", Toast.LENGTH_SHORT).show();
            return;
        }
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, new MessageFragment())
                .commit();
    }

    private void removeFragment() {
        Fragment current = findCurrentOrWarn();
        if (current != null) {
            fragmentManager.beginTransaction().remove(current).commit();
        }
    }

    private void replaceFragment() {
        Fragment current = findCurrentOrWarn();
        if (current == null) {
            return;
        }
        if (current.isDetached()) {
            Toast.makeText(this, "Attach the fragment first", Toast.LENGTH_SHORT).show();
            return;
        }
        // Swap a MessageFragment for a NumberFragment, and back
        Fragment next = (current instanceof MessageFragment) ? new NumberFragment() : new MessageFragment();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, next)
                .commit();
    }

    private void detachFragment() {
        Fragment current = findCurrentOrWarn();
        if (current != null) {
            fragmentManager.beginTransaction().detach(current).commit();
        }
    }

    private void attachFragment() {
        Fragment current = findCurrentOrWarn();
        if (current != null) {
            fragmentManager.beginTransaction().attach(current).commit();
        }
    }

    // The Fragment in the box (attached or detached), or null with a Toast if the box is empty
    private Fragment findCurrentOrWarn() {
        Fragment current = fragmentManager.findFragmentById(R.id.fragment_container);
        if (current == null) {
            Toast.makeText(this, "Add a fragment first", Toast.LENGTH_SHORT).show();
        }
        return current;
    }
}
