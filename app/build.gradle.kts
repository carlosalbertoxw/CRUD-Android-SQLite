import com.android.build.api.dsl.ManagedVirtualDevice
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.carlosalbertoxw.crud_android_sqlite"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.carlosalbertoxw.crud_android_sqlite"
        minSdk = 24
        targetSdk = 37
        versionCode = 3
        versionName = "2.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // R8: reduce, optimiza y ofusca el código; además elimina recursos sin usar.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
    }

    lint {
        // Cualquier advertencia de lint hace fallar el build (también en CI).
        warningsAsErrors = true
        // Las versiones nuevas de dependencias las propone Dependabot; si lint
        // también las reportara, el build fallaría cada vez que sale una.
        disable += setOf("GradleDependency", "AndroidGradlePluginVersion", "NewerVersionAvailable")
    }

    testOptions {
        // Emulador gestionado por Gradle para las pruebas instrumentadas:
        // ./gradlew pixel6Api34DebugAndroidTest lo descarga, lo arranca, corre
        // las pruebas y lo apaga. Es lo que usa el CI.
        managedDevices {
            allDevices {
                create<ManagedVirtualDevice>("pixel6Api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    // Imagen "ATD": optimizada para pruebas automatizadas.
                    systemImageSource = "aosp-atd"
                }
            }
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

room {
    // Guarda el esquema de cada versión de la base en app/schemas (se versiona
    // en git). Room lo usa para generar las migraciones automáticas y para
    // probarlas con MigrationTestHelper.
    schemaDirectory("$projectDir/schemas")
}

ktlint {
    // El estilo (android_studio) y las excepciones están en .editorconfig.
    version = libs.versions.ktlint
}

dependencies {
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Corrutinas declaradas explícitamente: sin esto la app hereda una versión
    // más vieja de AndroidX y no coincide con kotlinx-coroutines-test en las
    // pruebas instrumentadas (NoSuchMethodError en runTest).
    implementation(libs.kotlinx.coroutines.android)

    // Compose (versiones gestionadas por el BOM)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.core)

    // Room (persistencia sobre SQLite)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Pruebas unitarias locales (JVM)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Pruebas instrumentadas (requieren emulador o dispositivo)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
