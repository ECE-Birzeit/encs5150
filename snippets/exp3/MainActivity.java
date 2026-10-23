package edu.birzeit.encs5150.exp3;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private static final String MY_CHANNEL_ID = "my_channel_1";
    private static final String MY_CHANNEL_NAME = "My channel";
    private static final int NOTIFICATION_ID = 123;
    private static final String NOTIFICATION_TITLE = "Notification Title";
    private static final String NOTIFICATION_BODY = "This is the body of my notification";
    private static final String TOAST_TEXT = "This is my toast message";

    // Shows the "Allow notifications?" dialog (Android 13+) and receives the answer.
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    createNotification(NOTIFICATION_TITLE, NOTIFICATION_BODY);
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
                }
            });

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

        createNotificationChannel();

        Button dialButton = findViewById(R.id.button_dial);
        Button gmailButton = findViewById(R.id.button_gmail);
        Button mapsButton = findViewById(R.id.button_maps);
        Button notificationButton = findViewById(R.id.button_notification);
        Button toastButton = findViewById(R.id.button_toast);

        dialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ACTION_DIAL only opens the dialer with the number filled in. It needs no permission.
                Intent dialIntent = new Intent();
                dialIntent.setAction(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:+97022982000"));
                startActivitySafely(dialIntent);
            }
        });

        gmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // "mailto:" makes only email apps answer this Intent.
                Intent gmailIntent = new Intent();
                gmailIntent.setAction(Intent.ACTION_SENDTO);
                gmailIntent.setData(Uri.parse("mailto:"));
                gmailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"your.email@example.com"});
                gmailIntent.putExtra(Intent.EXTRA_SUBJECT, "My Subject");
                gmailIntent.putExtra(Intent.EXTRA_TEXT, "Content of the message");
                startActivitySafely(gmailIntent);
            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mapsIntent = new Intent();
                mapsIntent.setAction(Intent.ACTION_VIEW);
                mapsIntent.setData(Uri.parse("geo:31.9590,35.1820?q=Birzeit+University"));
                startActivitySafely(mapsIntent);
            }
        });

        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNotification(NOTIFICATION_TITLE, NOTIFICATION_BODY);
            }
        });

        toastButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast toast = Toast.makeText(MainActivity.this, TOAST_TEXT, Toast.LENGTH_SHORT);
                toast.show();
            }
        });
    }

    // An implicit Intent crashes the app if no installed app can handle it, so catch that case.
    private void startActivitySafely(Intent intent) {
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No app installed to handle this", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        // The Compat class skips this on Android 7 (API 24-25), which has no channels.
        NotificationChannelCompat channel = new NotificationChannelCompat.Builder(
                MY_CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_DEFAULT)
                .setName(MY_CHANNEL_NAME)
                .build();
        NotificationManagerCompat.from(this).createNotificationChannel(channel);
    }

    public void createNotification(String title, String body) {
        // Android 13+ asks the user first. The launcher calls this method again if they allow it.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }

        // Tapping the notification opens MainActivity. FLAG_IMMUTABLE is required on Android 12+.
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MY_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, builder.build());
    }
}
