package edu.birzeit.encs5150.exp2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Fields, because onResume() and the delete dialog both use them.
    private LinearLayout customersLinearLayout;
    private DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBaseHelper = new DataBaseHelper(this);

        // 1. Create the views.
        LinearLayout rootLinearLayout = new LinearLayout(this);
        rootLinearLayout.setOrientation(LinearLayout.VERTICAL);

        Button addButton = new Button(this);
        addButton.setText("Add Customer");
        addButton.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        ScrollView scrollView = new ScrollView(this);
        customersLinearLayout = new LinearLayout(this);
        customersLinearLayout.setOrientation(LinearLayout.VERTICAL);

        // 2. Build the hierarchy: root -> [button, scrollView -> customers list].
        rootLinearLayout.addView(addButton);
        scrollView.addView(customersLinearLayout);
        rootLinearLayout.addView(scrollView);

        // 3. Show it. No XML layout is used for this screen.
        setContentView(rootLinearLayout);

        // Keep the content clear of the status and navigation bars, plus a 16dp margin.
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        ViewCompat.setOnApplyWindowInsetsListener(rootLinearLayout, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + margin, systemBars.top + margin,
                    systemBars.right + margin, systemBars.bottom + margin);
            return insets;
        });

        // 4. Open AddCustomerActivity with an explicit Intent.
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddCustomerActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Called on first launch and every time we return from AddCustomerActivity.
        showCustomers();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataBaseHelper.close();
    }

    // Rebuilds the list from the database.
    private void showCustomers() {
        customersLinearLayout.removeAllViews();

        List<Customer> customers = dataBaseHelper.getAllCustomers();
        if (customers.isEmpty()) {
            TextView emptyTextView = new TextView(this);
            emptyTextView.setText("No customers yet. Tap Add Customer.");
            customersLinearLayout.addView(emptyTextView);
            return;
        }

        for (Customer customer : customers) {
            TextView customerTextView = new TextView(this);
            // AppCompat's styles live in androidx.appcompat.R, not in your app's R.
            customerTextView.setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Medium);
            customerTextView.setText(customer.toString());
            customerTextView.setPadding(0, 0, 0, 24);

            // Long-press a customer to delete it.
            customerTextView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    confirmDelete(customer);
                    return true;
                }
            });
            customersLinearLayout.addView(customerTextView);
        }
    }

    // Asks before deleting, because a deleted row can't be brought back.
    private void confirmDelete(Customer customer) {
        new AlertDialog.Builder(this)
                .setTitle("Delete customer?")
                .setMessage("Delete " + customer.getName() + " (Id " + customer.getCustomerId() + ")?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dataBaseHelper.deleteCustomer(customer.getCustomerId());
                    showCustomers();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
