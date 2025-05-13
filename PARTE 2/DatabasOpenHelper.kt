package com.example.gestionbasedatos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class DatabasOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user_database.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_NAME = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LASTNAME = "lastname"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_EMAIL = "email"

        private const val CREATE_TABLE = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_LASTNAME TEXT NOT NULL,
                $COLUMN_AGE INTEGER NOT NULL,
                $COLUMN_GENDER TEXT NOT NULL,
                $COLUMN_PHONE TEXT NOT NULL,
                $COLUMN_EMAIL TEXT NOT NULL
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(name: String, lastname: String, age: Int, gender: String, phone: String, email: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LASTNAME, lastname)
            put(COLUMN_AGE, age)
            put(COLUMN_GENDER, gender)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email) // <-- Aquí habías olvidado ponerlo
        }
        return try {
            val result = db.insert(TABLE_NAME, null, values)
            db.close()
            result != -1L
        } catch (e: Exception) {
            db.close()
            false
        }
    }

    fun getAllUsers(): List<Map<String, Any>> {
        val db = readableDatabase
        val userList = mutableListOf<Map<String, Any>>()
        val cursor = db.query(TABLE_NAME, arrayOf(
            COLUMN_ID, COLUMN_NAME, COLUMN_LASTNAME, COLUMN_AGE, COLUMN_GENDER, COLUMN_PHONE, COLUMN_EMAIL
        ), null, null, null, null, null)

        if (cursor.moveToFirst()) {
            do {
                val user = mapOf(
                    COLUMN_ID to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    COLUMN_NAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    COLUMN_LASTNAME to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LASTNAME)),
                    COLUMN_AGE to cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                    COLUMN_GENDER to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER)),
                    COLUMN_PHONE to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)),
                    COLUMN_EMAIL to cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
                )
                userList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return userList
    }
    fun deleteUser(id: Int): Boolean {
        val db = writableDatabase
        return try {
            val result = db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
            db.close()
            result > 0 // Devuelve true si al menos una fila fue eliminada
        } catch (e: Exception) {
            db.close()
            false
        }
    }
    fun updateUser(
        id: Int,
        name: String,
        lastname: String,
        age: Int,
        gender: String,
        phone: String,
        email: String
    ): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LASTNAME, lastname)
            put(COLUMN_AGE, age)
            put(COLUMN_GENDER, gender)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email)
        }

        return try {
            val result = db.update(TABLE_NAME, values, "$COLUMN_ID=?", arrayOf(id.toString()))
            db.close()
            result > 0 // Devuelve true si al menos una fila fue actualizada
        } catch (e: Exception) {
            db.close()
            false
        }
    }


}
