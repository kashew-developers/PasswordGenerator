package in.kashewdevelopers.passwordgenerator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SavedDbHelper extends SQLiteOpenHelper {

    // table details
    private static final String DB_NAME = "saved";
    private static final int DB_VERSION = 1;


    // columns in table
    static String PSWD = "password";
    private static String TIME = "timestamp";


    SavedDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String creatingDb = "CREATE TABLE " + DB_NAME + " (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                PSWD + " TEXT UNIQUE ON CONFLICT REPLACE," +
                TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

        db.execSQL(creatingDb);
    }


    void insert(SQLiteDatabase db, String password) {
        ContentValues data = new ContentValues();
        data.put(PSWD, password);

        db.insert(DB_NAME, null, data);
    }


    Cursor get(SQLiteDatabase db) {
        return db.query(DB_NAME, null, null, null, null,
                null, TIME + " DESC");
    }


    void deleteAll(SQLiteDatabase db) {
        db.delete(DB_NAME, null, null);
    }


    void delete(SQLiteDatabase db, String password) {
        db.delete(DB_NAME, PSWD + " = ?", new String[]{password});
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }

}
