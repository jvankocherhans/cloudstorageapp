package ch.in21_25a.cloudstorageapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StorageCapacityActivity extends AppCompatActivity {

    // Attribute für StorageCapacityActivity
    private DatabaseReference databaseRef;
    private FileAdapter fileAdapter;
    private ValueEventListener dbListener;
    private List<UploadFile> uploads;
    private ProgressBar progressBarStorageUsage;
    private TextView currentUsageInPercentage;
    private TextView getCurrentUsageInText;
    // Die Konstante gibt den Speicherplatz an, den Firebase hergibt.
    final private double STORAGECAPACITY_IN_GB = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_capacity);
        // Entfernt das bereits vorhandene Actionmenu
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Bottom Navigation wird instanzziiert. Zudem wird das selected Item für diese Activity bestimmt.
        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.storage_capacity_item);

        // Initialisierung für die Speicher-Progressbar
        progressBarStorageUsage = findViewById(R.id.progressBarStorageUsage);
        currentUsageInPercentage = findViewById(R.id.currentUsage_Percentage);
        getCurrentUsageInText = findViewById(R.id.currentUsage_Text);

        // Default Werte werden gesetzt
        currentUsageInPercentage.setText("null");
        getCurrentUsageInText.setText("no connection");

        // Uploads wird als Arraylist initialisert
        uploads = new ArrayList<>();

        // fileAdapter-Objekt wird instanziiert
        fileAdapter = new FileAdapter(StorageCapacityActivity.this, uploads);

        // Initialisierung für Firebase
        databaseRef =  FirebaseDatabase.getInstance().getReference("root");

        // databaseRef ValueEventlistener wird erstellt
        dbListener = databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                uploads.clear();
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    UploadFile upload = postSnapshot.getValue(UploadFile.class);
                    upload.setKey(postSnapshot.getKey());
                    uploads.add(upload);
                }

                // Die Anzeigen werden nach dem pushen der Files gesetzt
                progressBarStorageUsage.setProgress((int) ((100 / STORAGECAPACITY_IN_GB) * calculateCapacity()));
                currentUsageInPercentage.setText(Integer.toString((int) ((100 / STORAGECAPACITY_IN_GB) * calculateCapacity()))  + "%");
                getCurrentUsageInText.setText(calculateCapacity() + "GB out of " + STORAGECAPACITY_IN_GB + "GB used");

                // Benachritigung, dass Daten gändert worden sind
                fileAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Falls eine Permission fehlt, dann wird das beim Diagramm ausgegeben
                Toast.makeText(StorageCapacityActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                currentUsageInPercentage.setText("null");
                getCurrentUsageInText.setText("No read permission");
            }
        });

        // OnClick-Listener für die Items im Bottom Navigation
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Switch-Verzweigung, um die Activities zu wechseln.
                switch (item.getItemId()) {
                    case R.id.storage_item:
                        startActivity(new Intent(getApplicationContext(), StorageActivity.class));
                        finish();
                        overridePendingTransition(0, 0);
                        break;
                    case R.id.storage_capacity_item:
                        // Leer, weil: Aktuelle Activity ist ist StorageCapacity
                        break;
                }
                return true;
            }
        });
    }

    /**
     * Verhindert, dass man mit dem "Back-Button" in die anderen Activities kommt.
     * **/
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    /**
     * Die Methode Geht die Arrayliste durche, wo alle Dateneinträge gespeichert wurden,
     * instanziiert ein neues UploadFile Objekt und rechnet dann dei Summe
     * aus allen Dateigrössen zusammen.
     * @return totalFileCapacityInGB
     * **/
    public double calculateCapacity() {

        double totalFileCapacityInGB = 0;

        for (int i = 0; i < uploads.size(); i++) {
            // Arraylist wird durchgegenagen und alle Objekte werden instanziiert
            UploadFile selectedItem = uploads.get(i);
            // Die Dateigrössen werden nach und nach in totalFileCapacityInGB als GB gespeichert
            totalFileCapacityInGB += selectedItem.getFileSize() / Math.pow(1000, 3);

            // Falls die totalFileCapacityInGB grösser als 5 ist, wird diese auf 5 zurückgesetzt
            if (totalFileCapacityInGB > 5) {
                totalFileCapacityInGB = 5;
            }
        }
        // Return Wert wird aufgerundet
        return Math.round(totalFileCapacityInGB * 100)/100.0;
    }

    /**
     * Unnötige Eventlistener von der Datenbank Referenzierung werden vom Ram entfernt.
     * **/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseRef.removeEventListener(dbListener);
    }

}