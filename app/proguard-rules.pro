# Reglas de R8 propias de la app. Room, Compose, Navigation y
# kotlinx.serialization ya incluyen las suyas dentro de sus librerías, así que
# aquí solo hace falta lo específico del proyecto.

# Conserva archivo y número de línea en las trazas de error de la versión
# release, para que los reportes de fallos sigan siendo legibles.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
