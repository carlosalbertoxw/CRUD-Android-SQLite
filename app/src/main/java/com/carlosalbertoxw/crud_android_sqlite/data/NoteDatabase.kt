package com.carlosalbertoxw.crud_android_sqlite.data

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.carlosalbertoxw.crud_android_sqlite.R

/**
 * Punto de entrada a la base de datos SQLite gestionada por Room.
 *
 * El esquema de cada versión se exporta a `app/schemas/`; Room compara esos
 * JSON para generar las migraciones automáticas. Si se modifica [Note], hay
 * que subir `version` y declarar la migración: sin ella, la app fallaría al
 * abrir la base de los usuarios que ya tienen datos.
 *
 * Historial:
 * - v1: tabla `notes` (id, title, text, updatedAt).
 * - v2: columna `searchKey` para buscar sin distinguir mayúsculas ni acentos.
 */
@Database(
    entities = [Note::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = NoteDatabase.FillSearchKey::class)
    ]
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    /**
     * Room agrega la columna `searchKey` vacía; después de migrar se rellena
     * para las notas que ya existían, con la misma regla que usa la app.
     */
    class FillSearchKey : AutoMigrationSpec {
        override fun onPostMigrate(db: SupportSQLiteDatabase) {
            db.query("SELECT id, title, text FROM notes").use { cursor ->
                while (cursor.moveToNext()) {
                    db.execSQL(
                        "UPDATE notes SET searchKey = ? WHERE id = ?",
                        arrayOf<Any>(
                            searchKeyOf(cursor.getString(1), cursor.getString(2)),
                            cursor.getLong(0)
                        )
                    )
                }
            }
        }
    }

    companion object {
        /**
         * Crea la base. Hacerlo es costoso, así que la app guarda una sola
         * instancia (ver CrudApplication).
         */
        fun create(context: Context): NoteDatabase {
            val appContext = context.applicationContext
            return Room
                .databaseBuilder(appContext, NoteDatabase::class.java, "notes.db")
                .addCallback(SeedCallback(appContext))
                .build()
        }
    }

    /**
     * Inserta una nota de ejemplo, en el idioma del teléfono, la PRIMERA vez
     * que se crea la base (no en cada arranque). Se hace con SQL directo porque
     * el DAO todavía no está disponible dentro de este callback.
     */
    private class SeedCallback(private val context: Context) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            val title = context.getString(R.string.seed_title)
            val text = context.getString(R.string.seed_text)
            db.execSQL(
                "INSERT INTO notes (title, text, updatedAt, searchKey) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(title, text, System.currentTimeMillis(), searchKeyOf(title, text))
            )
        }
    }
}
