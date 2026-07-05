# CRUD-Android-SQLite

App de ejemplo de **cómo guardar y consultar datos** en Android: un CRUD de
notas con persistencia local en SQLite, escrito con stack moderno.

## Stack

- **Kotlin**
- **Jetpack Compose** (Material 3) para la interfaz
- **Room** como capa de acceso a SQLite
- **ViewModel + Kotlin Flow/StateFlow** para el estado reactivo
- **Navigation Compose** para moverse entre pantallas
- Una sola `Activity` (`MainActivity`)

## Qué hace

- Lista de notas ordenadas por última modificación.
- Crear, editar y eliminar notas (con diálogo de confirmación al borrar).
- Búsqueda en vivo por título o contenido.
- La lista se actualiza sola cuando cambian los datos (Room devuelve `Flow`).
- Incluye una **nota de ejemplo** que se inserta la primera vez que se abre la
  app (ver `SeedCallback` en `data/NoteDatabase.kt`).

## Cómo funciona (guardar y consultar)

El flujo de datos sigue una arquitectura por capas:

```
UI (Compose)  →  NotesViewModel  →  NoteRepository  →  NoteDao (Room)  →  SQLite
   estado           lógica            abstracción        consultas
```

Archivos clave:

| Archivo | Rol |
|---|---|
| `data/Note.kt` | Entidad `@Entity`: una fila de la tabla `notes`. |
| `data/NoteDao.kt` | Consultas SQL (`@Query`, `@Insert`, `@Update`, `@Delete`). |
| `data/NoteDatabase.kt` | Base de datos Room (singleton). |
| `data/NoteRepository.kt` | Abstrae el origen de datos frente a la UI. |
| `ui/NotesViewModel.kt` | Estado de pantalla y acciones. |
| `ui/screens/` | Pantallas de lista y formulario en Compose. |

**Consultar:** los `@Query` que devuelven `Flow<List<Note>>` son reactivos; el
`ViewModel` los expone como `StateFlow` y Compose se recompone al cambiar.

**Guardar:** las operaciones de escritura son funciones `suspend` que se
ejecutan en corrutinas (fuera del hilo principal) desde el `ViewModel`.

## Requisitos y ejecución

- Android Studio reciente (con JDK 17 incluido).
- SDK de Android 36.

Abre el proyecto en Android Studio, deja que sincronice Gradle y pulsa **Run**.

## Pruebas

- **Unitarias (JVM, rápidas)** en `app/src/test/`:
  `NoteRepositoryTest` valida la lógica del repositorio (insertar vs. actualizar,
  refresco de fecha, borrado y búsqueda) usando un `FakeNoteDao` en memoria.
  Se ejecutan con:

  ```
  ./gradlew test
  ```

- **Instrumentadas (emulador/dispositivo)** en `app/src/androidTest/`:
  `NoteDaoTest` prueba el DAO contra una base Room real en memoria (inserción,
  orden, actualización, borrado y búsqueda). Se ejecutan con:

  ```
  ./gradlew connectedAndroidTest
  ```

  (o desde Android Studio con un emulador abierto).

- **UI (Compose, emulador/dispositivo)** en `app/src/androidTest/.../ui/`:
  `NoteListScreenTest` y `NoteFormScreenTest` verifican la interfaz real
  (estado vacío, botón +, filtrado de búsqueda, validación de campos y guardado)
  con Compose Test sobre el `ViewModel` respaldado por Room en memoria. También
  se ejecutan con `connectedAndroidTest`.
