package com.carlosalbertoxw.crud_android_sqlite.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.Normalizer
import java.util.Locale

/** Longitud máxima del título de una nota. */
const val MAX_TITLE_LENGTH = 200

/**
 * Longitud máxima del contenido. Evita que una nota enorme supere el límite de
 * unos 2 MB por fila que Android puede leer de SQLite (CursorWindow) o el de
 * 1 MB del estado guardado de la pantalla (SavedStateHandle).
 */
const val MAX_TEXT_LENGTH = 20_000

/** Caracteres del contenido que se leen para la vista previa de la lista. */
const val PREVIEW_LENGTH = 200

/**
 * Una nota. Cada instancia de esta clase se corresponde con una fila de la
 * tabla "notes". Room usa las anotaciones para crear la tabla y mapear
 * columnas <-> propiedades automáticamente.
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val text: String,
    // Marca de tiempo (epoch millis) para ordenar por "más reciente". La
    // asigna NoteRepository.save() al guardar.
    val updatedAt: Long = 0,
    // Título y contenido en minúsculas y sin acentos (ver searchKeyOf). Solo se
    // usa para buscar, porque LIKE de SQLite no ignora mayúsculas en "Á" o "Ñ".
    @ColumnInfo(defaultValue = "")
    val searchKey: String = ""
)

/**
 * Lo que muestra la lista: el contenido llega recortado a [PREVIEW_LENGTH]
 * caracteres para no cargar en memoria el texto completo de cada nota.
 */
data class NotePreview(val id: Long, val title: String, val preview: String, val updatedAt: Long)

/** Clave de búsqueda de una nota: título y contenido normalizados. */
fun searchKeyOf(title: String, text: String): String = "$title\n$text".normalizeForSearch()

private val COMBINING_MARKS = Regex("\\p{Mn}+")

/**
 * Pasa a minúsculas y quita los acentos ("Canción" -> "cancion"), para que la
 * búsqueda encuentre la palabra se escriba como se escriba. También convierte
 * la "ñ" en "n".
 */
fun String.normalizeForSearch(): String = Normalizer
    .normalize(lowercase(Locale.ROOT), Normalizer.Form.NFD)
    .replace(COMBINING_MARKS, "")
