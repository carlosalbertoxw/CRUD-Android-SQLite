package com.carlosalbertoxw.crud_android_sqlite.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprueba que las migraciones de la base conservan los datos. Usa los
 * esquemas exportados en `app/schemas/` para crear una base de la versión
 * vieja, migrarla y validar que el resultado coincide con la versión nueva.
 */
@RunWith(AndroidJUnit4::class)
class NoteDatabaseMigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        NoteDatabase::class.java
    )

    @Test
    fun migrate1To2_rellenaLaClaveDeBusqueda() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                "INSERT INTO notes (id, title, text, updatedAt) VALUES (?, ?, ?, ?)",
                arrayOf<Any>(7L, "Canción", "AÑO nuevo", 1_000L)
            )
        }

        val db = helper.runMigrationsAndValidate(DB_NAME, 2, true)

        db.query("SELECT title, updatedAt, searchKey FROM notes WHERE id = 7").use { cursor ->
            cursor.moveToFirst()
            assertEquals("Canción", cursor.getString(0))
            assertEquals(1_000L, cursor.getLong(1))
            assertEquals("cancion\nano nuevo", cursor.getString(2))
        }
        db.close()
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
