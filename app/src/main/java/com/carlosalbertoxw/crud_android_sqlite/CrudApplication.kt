package com.carlosalbertoxw.crud_android_sqlite

import android.app.Application
import com.carlosalbertoxw.crud_android_sqlite.data.NoteDatabase
import com.carlosalbertoxw.crud_android_sqlite.data.NoteRepository

/**
 * Application propia para crear una sola vez la base de datos y el repositorio
 * y compartirlos con toda la app. `by lazy` los inicializa la primera vez que
 * se usan (inyección de dependencias "manual", sin librerías extra).
 */
class CrudApplication : Application() {
    private val database by lazy { NoteDatabase.getInstance(this) }
    val repository by lazy { NoteRepository(database.noteDao()) }
}
