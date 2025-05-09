
package com.example.gestionbasedatos

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast

// Clase para manejar la base de datos SQLite
class DatabaseOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "users.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "users"

        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_LASTNAME = "lastname"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_GENDER = "gender"
        private const val COLUMN_PHONE = "phone"
        private const val COLUMN_EMAIL = "email"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_LASTNAME TEXT,
                $COLUMN_AGE INTEGER,
                $COLUMN_GENDER TEXT,
                $COLUMN_PHONE TEXT,
                $COLUMN_EMAIL TEXT
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertUser(name: String, lastname: String, age: Int, gender: String, phone: String, email: String): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_LASTNAME, lastname)
            put(COLUMN_AGE, age)
            put(COLUMN_GENDER, gender)
            put(COLUMN_PHONE, phone)
            put(COLUMN_EMAIL, email)
        }

        val result = db.insert(TABLE_NAME, null, values)
        db.close()
        return result != -1L
    }

    fun getAllUsers(): List<Map<String, String>> {
        val usersList = mutableListOf<Map<String, String>>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_NAME"
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val user = mutableMapOf<String, String>()
                user["id"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)).toString()
                user["name"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                user["lastname"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LASTNAME))
                user["age"] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AGE)).toString()
                user["gender"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENDER))
                user["phone"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE))
                user["email"] = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))

                usersList.add(user)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return usersList
    }
}

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabaseOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dbHelper = DatabaseOpenHelper(this)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AddUsers(dbHelper)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddUsers(dbHelper: DatabaseOpenHelper) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Masculino", "Femenino", "Otro")

    var users by remember { mutableStateOf(dbHelper.getAllUsers()) }

    Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = lastname,
            onValueChange = { lastname = it },
            label = { Text("Apellido") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = age,
            onValueChange = { age = it },
            label = { Text("Edad") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown para género
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text(text = "Género") },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Expandir"
                    )
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(text = selectionOption) },
                        onClick = {
                            gender = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (dbHelper.insertUser(name, lastname, age.toIntOrNull() ?: 0, gender, phone, email)) {
                    Toast.makeText(context, "Usuario agregado", Toast.LENGTH_SHORT).show()
                    users = dbHelper.getAllUsers()
                    name = ""
                    lastname = ""
                    age = ""
                    gender = ""
                    phone = ""
                    email = ""
                } else {
                    Toast.makeText(context, "Error al agregar el usuario", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Agregar Usuario")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lista de Usuarios",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(users) { user ->
                UserRow(user)
            }
        }
    }
}

@Composable
fun UserRow(user: Map<String, String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Nombre: ${user["name"]}")
            Text(text = "Apellido: ${user["lastname"]}")
            Text(text = "Edad: ${user["age"]}")
            Text(text = "Género: ${user["gender"]}")
            Text(text = "Teléfono: ${user["phone"]}")
            Text(text = "Email: ${user["email"]}")
        }
    }
}
