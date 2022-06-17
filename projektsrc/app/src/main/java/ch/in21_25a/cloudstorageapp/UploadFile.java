package ch.in21_25a.cloudstorageapp;

import com.google.firebase.database.Exclude;

/*
 * Diese Klasse ist fuer die Uploas der Files.
 * Sie enthaelt die Url Informationen und den Namen der Datei.
 * */
public class UploadFile {

    // Attribute fuer UploadFile
    private String fileName;
    private String fileExtension;
    private String fileUrl;
    private long fileSize;
    private String key;

    /*
     * Leerer Konstruktor (Wird von Firebase gebraucht)
     * */
    public UploadFile() {

    }

    /**
     * Konstruktor von UploadFile
     * @param fileName
     * @param fileExtension
     * @param fileUrl
     * @param fileSize
     * **/
    public UploadFile(String fileName, String fileExtension, String fileUrl, int fileSize) {

        // Falls der filename leer sien sollte, wird dieser auf "No name" geändert
        if (fileName.trim().equals("")) {
            fileName = "No Name";
        }

        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileUrl = fileUrl;
        this.fileSize = fileSize;
    }

    // Getter und Setter der Klasse //

    /**
     * Gibt den Namen der Datei zurück
     * @return fileName
     * **/
    public String getFileName() {
        return fileName;
    }

    /**
     * Setzt den Übergabeparamter als neunen filenamen
     * @param fileName
     * **/
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gibt die Fileextension zurück
     * @return fileExtension
     * **/
    public  String getFileExtension() {
        return fileExtension;
    }

    /**
     * Setzt den Paramter als neue fileExtension
     * @param fileExtension
     * **/
    public  void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    /**
     * Gibt die fileUrl zurück
     * @return fileUrl
     * **/
    public String getFileUrl() {
        return fileUrl;
    }

    /**
     * Setzte den Paramter als neue fileUrl
     * @param fileUrl
     * **/
    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    /**
     * Gibt die Dateigrösse zurück
     * @return fileSize
     * **/
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Setzte den Übergabewert als neue Filegrösse
     * @param fileSize
     * **/
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * Gibt den Filekey zurück
     * @return key
     * **/
    @Exclude
    public String getKey() {
        return key;
    }

    /**
     * Setzt den Übergabeparamter als neuen key
     * @param key
     * **/
    @Exclude
    public void setKey(String key) {
        this.key = key;
    }
}