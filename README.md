# CRUD-Android-SQLite

App de ejemplo de **cómo guardar y consultar datos** en Android: un CRUD de
notas con persistencia local en SQLite, escrito con stack moderno.

## Stack

- **Kotlin**
- **Jetpack Compose** (Material 3) para la interfaz
- **Room** como capa de acceso a SQLite
- **ViewModel + Kotlin Flow/StateFlow** para el estado reactivo (un ViewModel
  por pantalla; el borrador del formulario vive en `SavedStateHandle`)
- **Navigation Compose** con rutas de tipos seguros (`@Serializable`)
- Una sola `Activity` (`MainActivity`) con splash screen (`core-splashscreen`)
- Gradle 9 + AGP 9 (Kotlin integrado), catálogo de versiones en
  `gradle/libs.versions.toml`

## Qué hace

- Lista de notas ordenadas por última modificación.
- Crear, editar y eliminar notas. Al borrar (deslizando la nota a la izquierda
  o con el botón del formulario) aparece un Snackbar para **deshacer**.
- Si sales del formulario con cambios sin guardar, pide confirmación.
- El borrador se conserva al rotar la pantalla o si Android cierra la app en
  segundo plano.
- Búsqueda en vivo por título o contenido (`%` y `_` se buscan como texto, no
  como comodines).
- Interfaz en español e inglés, con tema claro/oscuro y color dinámico.
- La lista se actualiza sola cuando cambian los datos (Room devuelve `Flow`).
- Incluye una **nota de ejemplo** que se inserta la primera vez que se abre la
  app (ver `SeedCallback` en `data/NoteDatabase.kt`).

## Cómo funciona (guardar y consultar)

El flujo de datos sigue una arquitectura por capas:

```
UI (Compose)  →  NoteListViewModel / NoteFormViewModel  →  NoteRepository  →  NoteDao (Room)  →  SQLite
   estado                      lógica                         abstracción        consultas
```

Archivos clave:

| Archivo | Rol |
|---|---|
| `data/Note.kt` | Entidad `@Entity`: una fila de la tabla `notes`. |
| `data/NoteDao.kt` | Consultas SQL (`@Query`, `@Insert`, `@Update`, `@Delete`). |
| `data/NoteDatabase.kt` | Base de datos Room (singleton). |
| `data/NoteRepository.kt` | Abstrae el origen de datos frente a la UI. |
| `ui/NoteListViewModel.kt` | Búsqueda, lista, borrado y deshacer. |
| `ui/NoteFormViewModel.kt` | Borrador, validación y guardado del formulario. |
| `ui/screens/` | Pantallas de lista y formulario en Compose. |
| `app/schemas/` | Esquema exportado de cada versión de la base (para migraciones). |

**Consultar:** los `@Query` que devuelven `Flow<List<Note>>` son reactivos; el
`ViewModel` los expone como `StateFlow` y Compose se recompone al cambiar.

**Guardar:** las operaciones de escritura son funciones `suspend` que se
ejecutan en corrutinas (fuera del hilo principal) desde el `ViewModel`.

**Cambiar la base de datos:** si modificas `Note`, sube `version` en
`NoteDatabase` y declara la migración (p. ej. `AutoMigration(from = 1, to = 2)`).
Room compara los JSON de `app/schemas/` para generarla; sin migración la app
fallaría al abrirse en dispositivos que ya tienen datos.

## Requisitos y ejecución

- Android Studio reciente (compatible con AGP 9.4).
- JDK 21 para ejecutar Gradle (Android Studio ya incluye uno).
- SDK de Android 37 para compilar (`compileSdk`); la app apunta a Android 16
  (`targetSdk 36`) y funciona desde Android 7 (`minSdk 24`).

Abre el proyecto en Android Studio, deja que sincronice Gradle y pulsa **Run**.

## Pruebas

- **Unitarias (JVM, rápidas)** en `app/src/test/`:
  `NoteRepositoryTest` valida la lógica del repositorio (insertar vs. actualizar,
  refresco de fecha, borrado, restaurar y búsqueda) usando un `FakeNoteDao` en
  memoria. `NoteListViewModelTest` y `NoteFormViewModelTest` prueban los
  ViewModels (búsqueda, deshacer, validación, doble guardado, borrador que
  sobrevive a la rotación). Se ejecutan con:

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
  (estado vacío, botón +, filtrado de búsqueda, deslizar para borrar y deshacer,
  validación de campos, guardado y aviso de cambios sin guardar) con Compose Test
  sobre ViewModels respaldados por Room en memoria. También se ejecutan con
  `connectedAndroidTest`.

## Integración continua

`.github/workflows/android.yml` ejecuta en GitHub Actions, en cada push a
`master` y en cada pull request: pruebas unitarias, lint y compilación debug y
release (con R8). Los reportes quedan como artefacto de la ejecución.
