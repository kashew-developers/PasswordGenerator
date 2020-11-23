package `in`.kashewdevelopers.passwordgenerator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SavedDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        const val DB_NAME = "saved"
        const val DB_VERSION = 1

        const val PSWD = "password"
        const val TIME = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val creatingDb = "CREATE TABLE $DB_NAME (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$PSWD TEXT UNIQUE ON CONFLICT REPLACE," +
                "$TIME DATETIME DEFAULT CURRENT_TIMESTAMP);"

        db?.execSQL(creatingDb)
    }

    fun insert(db: SQLiteDatabase, password: String) {
        val data = ContentValues()
        data.put(PSWD, password)

        db.insert(DB_NAME, null, data)
    }

    fun get(db: SQLiteDatabase): Cursor {
        return db.query(DB_NAME, null, null, null,
                null, null, "$TIME DESC")
    }

    fun deleteAll(db: SQLiteDatabase) {
        db.delete(DB_NAME, null, null)
    }

    fun delete(db: SQLiteDatabase, password: String) {
        db.delete(DB_NAME, "$PSWD = ?", arrayOf(password))
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

}