package ch.in21_25a.cloudstorageapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.Objects;

public class ShowImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        // Entfernt das bereits vorhandene Actionmenu
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Übergegebne FileUrl wird aus dem Intent in eine Variable abgspeichert.
        Intent intent = getIntent();
        String fileUrl = intent.getStringExtra(StorageActivity.EXTRA_FILEURL);
        
        // Mithilfe von Picasso wird das Bild in die referenzierte ImageView geladen
        ImageView showImage = findViewById(R.id.showImage);
        Picasso.with(this).load(fileUrl).into(showImage);

    }

    public void backStorageActivity(View v) {
        Intent intent = new Intent(this, StorageActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}