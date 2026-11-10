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
- Inyección de dependencias manual (`CrudApplication`), sin librerías extra
- Íconos de Material como vector drawables en `res/drawable/`
- Gradle 9 + AGP 9 (Kotlin integrado), catálogo de versiones en
  `gradle/libs.versions.toml`

## Qué hace

- Lista de notas ordenadas por última modificación.
- Crear, editar y eliminar notas. Al borrar (deslizando la nota a la izquierda
  o con el botón del formulario) aparece un Snackbar para **deshacer**.
- Si sales del formulario con cambios sin guardar, pide confirmación.
- El borrador se conserva al rotar la pantalla o si Android cierra la app en
  segundo plano.
- Búsqueda en vivo por título o contenido que ignora mayúsculas y acentos
  ("reunion" encuentra "REUNIÓN"; `%` y `_` se buscan como texto, no como
  comodines).
- Límite de 200 caracteres para el título y 20 000 para el contenido, con un
  contador que aparece al acercarse al máximo.
- Si falla una escritura en la base, se muestra un aviso en lugar de cerrarse.
- Interfaz en español e inglés, con tema claro/oscuro y color dinámico.
- La lista se actualiza sola cuando cambian los datos (Room devuelve `Flow`).
- Incluye una **nota de ejemplo**, en el idioma del teléfono, que se inserta la
  primera vez que se abre la app (ver `SeedCallback` en `data/NoteDatabase.kt`).

## Cómo funciona (guardar y consultar)

El flujo de datos sigue una arquitectura por capas:

```
UI (Compose)  →  NoteListViewModel / NoteFormViewModel  →  NoteRepository  →  NoteDao (Room)  →  SQLite
   estado                      lógica                         abstracción        consultas
```

Archivos clave:

| Archivo | Rol |
|---|---|
| `CrudApplication.kt` | Crea una sola vez la base y el repositorio para toda la app. |
| `MainActivity.kt` | Navegación entre la lista y el formulario. |
| `data/Note.kt` | Entidad `@Entity` (una fila de `notes`), vista previa para la lista y normalización para buscar. |
| `data/NoteDao.kt` | Consultas SQL (`@Query`, `@Insert`, `@Update`, `@Delete`). |
| `data/NoteDatabase.kt` | Base de datos Room, migraciones y nota de ejemplo. |
| `data/NoteRepository.kt` | Abstrae el origen de datos frente a la UI. |
| `ui/NoteListViewModel.kt` | Búsqueda, lista, borrado y deshacer. |
| `ui/NoteFormViewModel.kt` | Borrador, validación y guardado del formulario. |
| `ui/Routes.kt` | Rutas de navegación con tipos seguros. |
| `ui/screens/` | Pantallas de lista y formulario en Compose. |
| `ui/theme/` | Tema Material 3 (color dinámico con paleta de respaldo). |
| `app/schemas/` | Esquema exportado de cada versión de la base (para migraciones). |

**Consultar:** los `@Query` que devuelven `Flow<List<NotePreview>>` son
reactivos; el `ViewModel` los expone como `StateFlow` y Compose se recompone al
cambiar. La lista solo lee los primeros 200 caracteres del contenido; el texto
completo se lee al abrir la nota.

**Buscar:** `LIKE` de SQLite solo ignora mayúsculas en letras sin acento, así
que cada nota guarda en la columna `searchKey` su título y contenido en
minúsculas y sin acentos, y la búsqueda se normaliza igual antes de consultar.

**Guardar:** las operaciones de escritura son funciones `suspend` que se
ejecutan en corrutinas (fuera del hilo principal) desde el `ViewModel`.

**Cambiar la base de datos:** si modificas `Note`, sube `version` en
`NoteDatabase` y declara la migración. Room compara los JSON de `app/schemas/`
para generarla; sin migración la app fallaría al abrirse en dispositivos que ya
tienen datos. Ejemplo real: la versión 2 agregó `searchKey` con
`AutoMigration(from = 1, to = 2)` y un `AutoMigrationSpec` que la rellena para
las notas existentes; `NoteDatabaseMigrationTest` lo comprueba.

## Requisitos y ejecución

- Android Studio reciente (compatible con AGP 9.4).
- JDK 21 para ejecutar Gradle. Si no está instalado, Gradle lo descarga solo
  (ver `gradle/gradle-daemon-jvm.properties`).
- SDK de Android 37 para compilar (`compileSdk`); la app apunta a Android 17
  (`targetSdk 37`) y funciona desde Android 7 (`minSdk 24`).

Abre el proyecto en Android Studio, deja que sincronice Gradle y pulsa **Run**.

## Pruebas

- **Unitarias (JVM, rápidas)** en `app/src/test/`:
  `NoteRepositoryTest` valida la lógica del repositorio (insertar vs. actualizar,
  fecha con un reloj fijo, borrado, restaurar, vista previa y búsqueda sin
  acentos) usando un `FakeNoteDao` en memoria que imita las reglas de Room.
  `NoteListViewModelTest` y `NoteFormViewModelTest` prueban los ViewModels
  (búsqueda con espera al escribir, deshacer, validación, límites de longitud,
  errores al guardar, doble guardado, borrador que sobrevive a la rotación). Se
  ejecutan con:

  ```
  ./gradlew test
  ```

- **Instrumentadas (emulador/dispositivo)** en `app/src/androidTest/`:
  `NoteDaoTest` prueba el DAO contra una base Room real en memoria (inserción,
  orden, vista previa, actualización, borrado y búsqueda) y
  `NoteDatabaseMigrationTest` prueba la migración de la versión 1 a la 2. Con un
  emulador abierto o un teléfono conectado:

  ```
  ./gradlew connectedAndroidTest
  ```

  O sin abrir nada, con el emulador que Gradle descarga y gestiona (el mismo que
  usa el CI):

  ```
  ./gradlew pixel6Api34DebugAndroidTest
  ```

- **UI (Compose, emulador/dispositivo)** en `app/src/androidTest/.../ui/`:
  `NoteListScreenTest` y `NoteFormScreenTest` verifican la interfaz real
  (estado vacío, botón +, filtrado de búsqueda, deslizar para borrar y deshacer,
  validación de campos, guardado y aviso de cambios sin guardar) con Compose Test
  sobre ViewModels respaldados por Room en memoria. `NotesAppTest` arranca la
  app completa y recorre crear, abrir, borrar y deshacer (deja la base como
  estaba). También se ejecutan con los comandos anteriores.

## Estilo de código

El formato lo revisa [ktlint](https://pinterest.github.io/ktlint/) con el estilo
de Android Studio (configurado en `.editorconfig`):

```
./gradlew ktlintCheck    # revisa
./gradlew ktlintFormat   # corrige lo que pueda automáticamente
```

Android lint trata las advertencias como errores (`lint { warningsAsErrors }`).

## Integración continua

`.github/workflows/android.yml` ejecuta en GitHub Actions, en cada push a
`master` y en cada pull request, dos trabajos en paralelo:

- formato (ktlint), pruebas unitarias, lint y compilación debug y release (con
  R8);
- pruebas instrumentadas en un emulador gestionado por Gradle.

Los reportes quedan como artefactos de la ejecución. Además:

- `.github/workflows/codeql.yml` analiza el código con CodeQL (seguridad).
- `.github/dependabot.yml` propone cada semana actualizaciones de las
  dependencias de Gradle y de las acciones de GitHub.

## Licencia

[MIT](LICENSE).
