// Archivo raíz: solo declara los plugins que usarán los módulos.
plugins {
    alias(libs.plugins.android.application) apply false
    // AGP 9 compila Kotlin por sí mismo; declarar el plugin aquí (sin aplicarlo)
    // solo sirve para usar una versión de Kotlin más reciente que la de AGP.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ktlint) apply false
}
