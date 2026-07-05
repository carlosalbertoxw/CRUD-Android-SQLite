package com.carlosalbertoxw.crud_android_sqlite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Punto de entrada a la base de datos SQLite gestionada por Room.
 *
 * Se expone como singleton: crear una instancia de Room es costoso, así que la
 * app comparte una sola para todo su ciclo de vida.
 */
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {

    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "notes.db"
                )
                    // Inserta una nota de ejemplo la PRIMERA vez que se crea la base
                    // (no en cada arranque). Se hace con SQL directo porque el DAO
                    // todavía no está disponible dentro de este callback.
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }

        private val SeedCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO notes (title, text, updatedAt) VALUES (?, ?, ?)",
                    arrayOf<Any>(
                        "Bienvenido a Notas",
                        "Esta es una nota de ejemplo. Tócala para editarla, usa el " +
                            "botón + para crear otra y desliza por la lista para verlas " +
                            "todas. También puedes buscarlas por título o contenido.",
                        System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
