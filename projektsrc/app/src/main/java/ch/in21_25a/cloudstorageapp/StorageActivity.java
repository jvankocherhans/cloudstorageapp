package ch.in21_25a.cloudstorageapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.OpenableColumns;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.nio.file.FileSystem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class StorageActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, FileAdapter.OnItemClickListener, Dialog.DialogListener {
    // Attribute von Storage Activity
    public static final String EXTRA_FILEURL = "ch.in21_25a.cloudstorageapp.EXTRA_FILEURL";
    private static final int PICK_FILE_REQUEST = 1;
    private Uri fileUri;
    private String fileName;
    private int filesize;
    private SearchView searchView;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private DatabaseReference databaseRef;
    private ValueEventListener dbListener;
    private RecyclerView recyclerView;
    private FileAdapter fileAdapter;
    private ProgressBar progressCircle;
    private List<UploadFile> uploads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage);
        // Entfernt das bereits vorhandene Actionmenu
        Objects.requireNonNull(getSupportActionBar()).hide();

        // Firebase Referenzierung
        storageRef = FirebaseStorage.getInstance().getReference("root"); // Deteien werden im Ordner "root" abgelegt
        databaseRef = FirebaseDatabase.getInstance().getReference("root");
        storage = FirebaseStorage.getInstance();

        // Bottom Navigation wird instanzziiert. Zudem wird das selected Item f??r diese Activity bestimmt.
        BottomNavigationView bottomNav = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.storage_item);

        // Referenzierung fuer Recycler View
        recyclerView = findViewById(R.id.recyclerview_file);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Referenzierung fuer Progress Bar
        progressCircle = findViewById(R.id.load_files);

        // Searchview wird referenziert
        searchView = findViewById(R.id.search_item);

        // "uploads" wird als ArrayList initialisiert
        uploads = new ArrayList<>();

        // Fileadapter-Objekt wird instanziiert
        fileAdapter = new FileAdapter(StorageActivity.this, uploads);

        // Die fileAdpeter-Klasse wird als Adpter f??r die Recyclerview verwendet
        recyclerView.setAdapter(fileAdapter);

        fileAdapter.setOnItemClickListener(StorageActivity.this);

        // Beim Aufruf der Activity sollen alle Daten in die RecyclerView gepusht werden
        pushFiles("");

        // SearchView QueryTextListener wid erstellt
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                // Wird ausgef??hrt, wenn der Text-Submitted wird
                pushFiles(s.toLowerCase());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Wird ausgef??rt, wenn der Text sich ver??ndert
                pushFiles(s.toLowerCase());
                return false;
            }
        });

        // OnClick-Listener f??r die Items im Bottom Navigation
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Switch-Verzweigung, um die Activities zu wechseln.
                switch (item.getItemId()) {
                    case R.id.storage_item:
                        // Leer, weil: Aktuelle Activity ist ist Storage
                        break;
                    case R.id.storage_capacity_item:
                        startActivity(new Intent(getApplicationContext(), StorageCapacityActivity.class));
                        finish();
                        // Animationen werden ausgeschalten
                        overridePendingTransition(0, 0);
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
     * Die Klasse push Files ist daf??r zust??ndig, dass die aktuellen Daten von der Firebase Storage in die RecyclerView gepusht werden.
     *
     * @param inputSearch
     * **/
    public void pushFiles(String inputSearch) {
        dbListener = databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                uploads.clear();

                // Falls der ??bergabe Paramter nicht leer ist, werden alle Daten von der Realtime-Database in die Arraylist gespeichert,
                // sofern sie mit der Condition/inputSearch ??bereinstimmen
                if (!inputSearch.isEmpty()) {
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        UploadFile upload = postSnapshot.getValue(UploadFile.class);
                        if (upload.getFileName().toLowerCase().contains(inputSearch)) {
                            upload.setKey(postSnapshot.getKey());
                            uploads.add(upload);
                        }
                    }

                } else {
                    // Falls der ??bergabeparamter leer ist, dann werden alle Daten in die Arraylist "uploads" gespeichert
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        UploadFile upload = postSnapshot.getValue(UploadFile.class);
                            upload.setKey(postSnapshot.getKey());
                            uploads.add(upload);
                    }
                }
                // FileAdapter Klasse passt sich den ??nderungen an und updatet somit die Recylclerview
                fileAdapter.notifyDataSetChanged();
                // Bei Erfolg wird der Loading-Balken unsichtbar gemacht
                progressCircle.setVisibility(View.INVISIBLE);
            }

            // Bei Abbruch der Aktion, wird der Database Error als Toast-Message erscheinen
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StorageActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                progressCircle.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Die Methode ruft das PopUp Fenster auf.
     * **/
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        popup.setOnMenuItemClickListener(this);
        popup.inflate(R.menu.popup_menu_activity_storage);
        popup.show();
    }

    /**
     * Methode ist fuer das PopUp Fenster Zustaendig. Aktion, wenn item ausgewaehlt wird.
     * **/
    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.upload_data:
                // Methode "openFilechooser" wird aufgerufen
                openFileChooser();
                return true;

            default:
                return false;
        }
    }

    /**
     * Oeffnet die lokalen Dateien und zeigt dabei alle Dateitypen an.
     * **/
    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Intent ist erf??llt, wenn eine Datei ausgew??lt worden ist
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    /**
     * @param requestCode
     * @param resultCode
     * **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Falls die
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            // Die FileUri wird in die MVariable "fileUri" gespeichert
            fileUri = data.getData();
            // Die Methode "getUriFileName" wird aufgerufen
            getUriFileName(fileUri);
            // Die "getUriFileSizeKB" wird aufgerufen
            getUriFileSizeKB(fileUri);
            // Die Methode "uploadFile" wird aufgerufen
            uploadFile();
        }
    }

    /**
     * Methode welche den Namen der Uri ausliest.
     * Tutorial von https://www.youtube.com/watch?v=Y2vgJ73Cui8
     * @param fileUri
     * **/
    @SuppressLint("Range")
    public void getUriFileName(Uri fileUri) {
        try (Cursor curser = this.getContentResolver().query(fileUri, null, null, null, null)){
            if (curser != null && curser.moveToFirst()) {
                // Der Name der Datei wird in die MVariable "FileName" gespeichert
                fileName = curser.getString(curser.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }
        }
    }

    /**
     * Methode welche die Gr??sse der Datei in Kilobyte in die Variable filesize speichert.
     * Tutorial von https://www.youtube.com/watch?v=Y2vgJ73Cui8
     * @param fileUri
     * **/
    @SuppressLint("Range")
    public void getUriFileSizeKB(Uri fileUri) {
        try (Cursor curser = this.getContentResolver().query(fileUri, null, null, null, null)){
            if (curser != null && curser.moveToFirst()) {
                int sizeIndex = curser.getColumnIndex(OpenableColumns.SIZE);
                int byte_size = -1;
                if(!curser.isNull(sizeIndex)) {
                    byte_size = curser.getInt(sizeIndex);
                }
                if (byte_size > -1) {
                    filesize = byte_size;
                }

            }
        }
    }

    /**
     * Diese Methode gibt den Dateityp zurueck.
     * @param uri
     * @return mime.getExtensionFromMimeType(cR.getType(uri)); // Dateiendung
     * **/
    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cR.getType(uri));
    }

    /**
     * Diese Methode l??dt die Datei in die Cloud hoch un erstellt zus??tzlich einen Eintrag in der Realtime Database.
     *
     * **/
    private void uploadFile() {
        // Falls die FileUri vorhanden ist, wird der Block ausgef??hrt
        if(fileUri != null) {
            // Dateiendung wird in eine seperate Variable abgespeichert
            String fileExtension = getFileExtension(fileUri);

            // Die Datei bekommt ein unwillkuerliche die derzeitige Zeit in Milisekunden als Namen. So werden doppelte Dateinamen verhindert.
            StorageReference fileReference = storageRef.child(System.currentTimeMillis() + "." + fileExtension);

            // Die Datei wird in die Firebase Storage hochgeladen. Success- und Failure-Listener wurden erstellt.
            fileReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Successmessage erscheint
                    Toast.makeText(StorageActivity.this, "upload successful!", Toast.LENGTH_LONG).show();

                    // Falls die Metadaten des Snapshots nicht leer sind, wird die Erstellung eines Eintrags in der Datenbank fortgef??hrt.
                    if (taskSnapshot.getMetadata() != null) {
                        // Es wird ??berpr??ft, ob eine Referenz vorhanden ist
                        if (taskSnapshot.getMetadata().getReference() != null) {
                            // Die Download-Url wird in Task result gespeichert
                            Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                            // Ein Success- und Failure-listener werden erstellt -> Stammen von Task
                            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // Instanziierung eines UploadFile-Objkets und ??bergabe der geforderten Attribute.
                                    UploadFile upload = new UploadFile(fileName.toString().trim(), fileExtension, uri.toString(), filesize);
                                    // Ein Key f??r den derzeiteigen Push der Daten wird erstellt
                                    String uploadID = databaseRef.push().getKey();
                                    // Der Key wird als Eintrag verwendet und die Attribute des "UploadFile" werden als Daten angeh??ngt
                                    databaseRef.child(uploadID).setValue(upload);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Falls beim Vorgehen etwas scheitert -> Fehlermeldung
                                    Toast.makeText(StorageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Berechtigungsdefizite oder andere Fehler werden angezeigt
                    Toast.makeText(StorageActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            // Falls eine fehlerhafte Datei ausgewaehlt wird.
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ist eine On-Click Methode von der Implementierung: FileAdapter.OnItemClickListener
     * Die Methode ist f??r das ??ffnen von g??ltigen Bilddateien in einer neuen Activity zust??ndig
     * @param position
     * **/
    @Override
    public void onItemClick(int position) {
        // Alle g??ltigen Bilderformate werden in einem Array gespeichert
        String[] fileExtensions = new String[] {"jpg", "png", "gif", "jpeg"};

        // Ein UploadFile Objekt wird instanziiert und als ??bergabe bekommt dieses die Werte des aktuellen Index der Arraylist
        UploadFile selectedItem = uploads.get(position);

        // Foreach-Loop geht alle Fileextensions durch, wenn das FB-Objekt mit den Kriterien ??bereinstimmt,
        // dann wird eine neue Activity erstellt.
        for (String imageFile : fileExtensions) {
            // ??berpr??ft, ob Fileextensiion, die gleiche wie der derzeitige Index ist.
            if (imageFile.equals(selectedItem.getFileExtension())) {
                // ??ffnet eine neue Activity, wo die FileUrl mitgegeben wird.
                Intent intent = new Intent(this, ShowImageActivity.class);
                intent.putExtra(EXTRA_FILEURL, selectedItem.getFileUrl());
                startActivity(intent);
            }
        }
    }

    /**
     * Ist eine On-Click Context-Menu Methode von der Implementierung: FileAdapter.OnItemClickListener
     * Methode, welche aufgerufen wird, falls "Rename" beim Kontextmenu ausgew??hlt wird.
     * Diese Methode ??ffnet ein Dialogfenster, wo ein neuer Name f??r die Datei angegeben werden kann.
     * @param position
     * **/
    @Override
    public void onRenameClick(int position) {
        UploadFile selectedItem = uploads.get(position);
        String selectedKey = selectedItem.getKey();
        // ??ffnet das Dialogfenster. Mitgegeben werden die Position, und die Dateiendung der ausgew??hlten Datei.
        openDialog(position, selectedItem.getFileExtension());
    }

    /**
     * Methode, welche aufgerufen wird, falls "Download" beim Kontextmenu ausgew??hlt wird.
     * Diese Methode ist f??r das herunterladen der ausgew??hlten Datei zust??ndig.
     * @param position
     * **/
    @Override
    public void onDownloadClick(int position) {

        UploadFile selectedItem = uploads.get(position);
        // Referenzierung zum Download-Ordner
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        // Methodenaufruf um die Datei herunterzuladen. Mitgabe vom Filenamen, dem Ziel-Speicherort und der FileUrl
        downloadFile(StorageActivity.this, selectedItem.getFileName(), "", downloads.toString() ,selectedItem.getFileUrl());
    }

    /**
     * Methode welche beim Aufruf die ausgew??hlte Datei l??scht.
     * @param position
     * **/
    @Override
    public void onDeleteClick(int position) {
        UploadFile selectedItem = uploads.get(position);

        // Referenzierung wird zum FireBase Storage wird mit der FileUrl hergestellt.
        StorageReference fileRef = storage.getReferenceFromUrl(selectedItem.getFileUrl());
        // Mithilfe von der Referenzierung wird die Datei im Storage gel??scht. Successlistener wird erstellt.
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                // Bei Erfolg wird der Eintrag in der Datenbank ebenfalls gel??scht.
                databaseRef.child(selectedItem.getKey()).removeValue();
                // Toast Message "Item Deleted"
                Toast.makeText(StorageActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Diese Methode ist f??r das herunterladen einer mitgegebenen Datei zust??ndig.
     * @param context
     * @param fileName
     * @param fileExtension
     * @param destinationDirectory
     * @param fileUrl
     * **/
    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String fileUrl) {
        // DownloadManager Objekt wird erstellt, mit dem Download-Service
        DownloadManager downloadmanager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        // Die FileUrl wird in ein Uri-Objekt gecastet
        Uri uri = Uri.parse(fileUrl);
        // Neuer Download Request wird erstellt mit der uri
        DownloadManager.Request request = new DownloadManager.Request(uri);
        // Notification wird bei Herunterladen der Datei angezeigt
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // Download wird automatisch gestartet, wenn der Downloadmanger bereit ist
        downloadmanager.enqueue(request);
    }

    /**
     * Methode wird aufgeufen, wenn eine Datei umbennent wird.
     * @param fileIndex
     * @param fileExtension
     * **/
    public void openDialog(int fileIndex, String fileExtension) {
        // Neues Dialog-Objekt wird beim Aufruf erstellt
        Dialog dialog = new Dialog(fileIndex, fileExtension);
        // Das Dialogobjekt ruft das Dialog Fragment auf
        dialog.show(getSupportFragmentManager(), "Dialog");
    }

    /**
     * Methode wird ausgef??hrt wenn applyFileName in Dialog.java ausgef??hrt wird. Methode f??gt die n??tigen Parameter in eine Hashmap ein.
     * @param fileName
     * @param position
     * **/
    @Override
    public void applyFileName(String fileName, int position) {
        UploadFile selectedItem = uploads.get(position);

        // Hashmap mit den ver??nderten und nicht ver??nderten Attributen wird erstellt.
        HashMap fileRename = new HashMap();
        fileRename.put("fileName",fileName);
        fileRename.put("fileUrl", selectedItem.getFileUrl());
        fileRename.put("fileSize", selectedItem.getFileSize());

        // Angegebener Eintrag wird mithilfe des Schl??ssels und der Hashmap aktualisiert.
        databaseRef.child(selectedItem.getKey()).updateChildren(fileRename).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // Wenn die Task abschlossen ist, erscheint eine Nachricht.
                Toast.makeText(StorageActivity.this, "Success!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Unn??tige Eventlistener von der Datenbank Referenzierung werden vom Ram entfernt.
     * **/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseRef.removeEventListener(dbListener);
    }

}
