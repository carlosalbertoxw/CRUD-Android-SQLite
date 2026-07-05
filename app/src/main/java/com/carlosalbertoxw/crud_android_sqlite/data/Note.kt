package com.carlosalbertoxw.crud_android_sqlite.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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
    // Marca de tiempo (epoch millis) para poder ordenar por "más reciente".
    val updatedAt: Long = System.currentTimeMillis()
)
