package ch.in21_25a.cloudstorageapp;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class Dialog extends AppCompatDialogFragment {

    // Attribut für Dialog.java
    private EditText editTextFilename;
    private  DialogListener listener;
    private int fileIndex;
    private  String fileExtension;

    public Dialog (int fileIndex, String fileExtension) {
        this.fileIndex = fileIndex;
        this.fileExtension = fileExtension;
    }

    /**
     * Das Dialogfenster wird bei dieser methode erstellt.
     * @param savedInstanceState
     * @return android.app.Dialog
     * **/
    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Ein AlertDialo.builder wird erstellt
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Das LAyout wird in der aktuellen Activity erscheinen
            LayoutInflater inflater = getActivity().getLayoutInflater();
            // Die View für das Dialoffenster wird erstellt. layout_updatefile.xml wird mitgegeben
            View view = inflater.inflate(R.layout.layout_updatefile, null);

            // Dialogfenster wird erstellt, Titel wird mitgegeben und ein Negativbutton wird erstellt. EIn OnClicklistener wird hervorgerufen
            builder.setView(view).setTitle("Rename file").setNegativeButton("exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
                // Positivbutton mit OnClicklistener wird erstellt
            }).setPositiveButton("change", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Überprüft ob editTextFilename leer ist
                    if (!editTextFilename.getText().toString().isEmpty()) {
                        // Der letzte Buchstabe wird in die Variable gespeichert
                        String lastLetter = Character.toString(editTextFilename.getText().toString().charAt(editTextFilename.getText().toString().length() - 1));
                        // Überprüft ob der letzte Buchstabe ein Leerzeichen ist, ob die Länge kleinergleich 35 ist und ob Zeilenumbrüch vorhanden sind
                        if (!lastLetter.equals(" ") && editTextFilename.getText().toString().length() <= 35 && !editTextFilename.getText().toString().contains("\n") && !editTextFilename.getText().toString().contains("\r")) {
                            // Zum neuen Namen wird die Fileextension hinzugefügt
                            String fileName = editTextFilename.getText().toString() + "." + fileExtension;
                            // Der Name und der fileIndex wird dem listener (DialogListener) mitgegeben.
                            listener.applyFileName(fileName, fileIndex);
                        } else {
                            // Benutzerdefinierer Error-Code wird ausgegeben
                            String errorCode = "";

                            if (lastLetter.equals(" ")) {
                                errorCode = "Spaces at the end";
                            } else if (editTextFilename.getText().toString().contains("\n") || editTextFilename.getText().toString().contains("\r")) {
                                errorCode = "Line breakes";
                            } else if (editTextFilename.getText().toString().length() > 35) {
                                errorCode = "More than 35 characters";
                            }

                            Toast.makeText(getContext(), errorCode + " are " +
                                    "not allowed", Toast.LENGTH_LONG).show();
                            }
                    } else {
                        Toast.makeText(getContext(), "New filename must be at least one character long", Toast.LENGTH_LONG).show();
                    }
                }
            });

        // editTextFilename hat den neuen FileName
        editTextFilename = view.findViewById(R.id.rename_file);
        // Als Rückgabewert wird das Dialogfenster erstellt
        return builder.create();
    }

    /**
     * In der Methode wird der Listener initialsiert
     * @param context
     * **/
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // context wird gecastet
            listener = (DialogListener) context;
        } catch (ClassCastException e) {
            // Fehlermeldung, falls das Dialogfenster noch nicht implementiert wurde
            throw new ClassCastException(context.toString() + "must implement DialogListener");
        }
    }

    /**
     * Das Interface ist für das Dialogfenster zuständig. Es soll die Daten der Activity übergeben
     * **/
    public interface DialogListener {
        void applyFileName(String fileName, int position);
    }

    /**
     * Gibt den FileIndex in der Arraylist aus
     * @return getFileIndex
     * **/
    public int getFileIndex() {
        return fileIndex;
    }
}
