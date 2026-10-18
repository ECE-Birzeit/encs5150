package edu.birzeit.encs5150.exp2;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AddCustomerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_customer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fill the gender spinner from a String array through an ArrayAdapter.
        String[] options = {"Male", "Female"};
        Spinner genderSpinner = findViewById(R.id.spinner_Gender);
        ArrayAdapter<String> genderAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(genderAdapter);

        EditText idEditText = findViewById(R.id.editText_Id);
        EditText nameEditText = findViewById(R.id.editText_Name);
        EditText phoneEditText = findViewById(R.id.editText_Phone);
        Button addCustomerButton = findViewById(R.id.button_Add_Customer);

        addCustomerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String idText = idEditText.getText().toString().trim();
                String name = nameEditText.getText().toString().trim();
                String phone = phoneEditText.getText().toString().trim();

                // Mark every empty field, then stop if any was empty.
                boolean valid = true;
                if (idText.isEmpty()) {
                    idEditText.setError("Id is required");
                    valid = false;
                }
                if (name.isEmpty()) {
                    nameEditText.setError("Name is required");
                    valid = false;
                }
                if (phone.isEmpty()) {
                    phoneEditText.setError("Phone is required");
                    valid = false;
                }
                if (!valid) {
                    return;
                }

                // inputType="number" and maxLength="9" guarantee idText is a small number.
                long id = Long.parseLong(idText);
                String gender = genderSpinner.getSelectedItem().toString();
                Customer.customersArrayList.add(new Customer(id, name, phone, gender));

                // Close this screen. MainActivity is still underneath and reappears.
                finish();
            }
        });
    }
}
