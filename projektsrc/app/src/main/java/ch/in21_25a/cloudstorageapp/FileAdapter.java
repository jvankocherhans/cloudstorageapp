package ch.in21_25a.cloudstorageapp;


import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ItemViewHolder> {

    // Attribut für FileAdapter
    private Context context;
    private List<UploadFile> uploads;
    private OnItemClickListener listener;

    /**
     * Kosntruktor von FileAdapter
     * @param context
     * @param uploads
     * **/
    public FileAdapter(Context context, List<UploadFile> uploads) {
        this.context = context;
        this.uploads = uploads;
    }


    /**
     * Die Klasse ist für die Datenanordnung innerhald der Recyclerview zuständig
     * **/
    public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        public TextView textViewName;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewName = itemView.findViewById(R.id.name_file);

            // Die Clicks auf die Items werden hier erfasst
            itemView.setOnClickListener(this);
            // Ein Context-Menu wird beim halten der Items erstellt
            itemView.setOnCreateContextMenuListener(this);
        }

        /**
         * @param view
         * **/
        @Override
        public void onClick(View view) {
            if (listener != null) {
                // Falls der listener gestzt wurde, dann wird die Position des geklickten Items zwischengespeichert
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Überprüft ob die geklickte Position noch gültig ist
                    listener.onItemClick(position);
                }
            }
        }

        /**
         * Methode erstellt das dynamische Contextmenu.
         * @param contextMenu
         * @param view
         * @param contextMenuInfo
         * **/
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            // MenuItems werden erstellt und numeriert
            MenuItem renameItem = contextMenu.add(Menu.NONE, 1, 1, "Rename");
            MenuItem downloadItem = contextMenu.add(Menu.NONE, 2, 2, "Download");
            MenuItem deleteItem = contextMenu.add(Menu.NONE, 3, 3, "Delete");

            // OnItemClicklistmer werden erstellt
            renameItem.setOnMenuItemClickListener(this);
            downloadItem.setOnMenuItemClickListener(this);
            deleteItem.setOnMenuItemClickListener(this);
        }

        /**
         * Methode ist für die Clicks im Context-Menu zuständig
         * @param menuItem
         * **/
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (listener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Im Switch-Statement wird überprüft, welche Items geklickt worden sind. Diese dem Listener als Übergabewert mitgegeben.
                    switch (menuItem.getItemId()) {
                        case 1:
                            listener.onRenameClick(position);
                            return true;
                        case 2:
                            listener.onDownloadClick(position);
                            return true;
                        case 3:
                            listener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Ein neuer ItemViewholder wird geladen
     * @param parent
     * @param viewType
     * @return new ItemViewHolder(v)
     * **/
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.file_item, parent, false);
        return new ItemViewHolder(v);
    }

    /**
     * Der Name der derzeitig aufgeladenen Datei wird in den Text hineigefügt
     * @param holder
     * @param position
     * **/
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        UploadFile uploadCurrent = uploads.get(position);
        holder.textViewName.setText(uploadCurrent.getFileName());
    }
    /**
     * Die Anzahl an Items in der ArrayList werden zurückgegeben
     * @return uploads.size()
     * **/
    @Override
    public int getItemCount() {
        return uploads.size();
    }

/**
 * Neues Inteface wird erstellt, welche die Clicks, an die StorageActivity weitergibt.
 * **/
    public interface OnItemClickListener {
        void onItemClick(int position);

        void onRenameClick(int position);

        void onDeleteClick(int position);

        void onDownloadClick(int position);
    }

    /**
     * Methode mit dem eigenen erstellten OnClickListemer wurde erstellt
     * @param listener
     * **/
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

}
