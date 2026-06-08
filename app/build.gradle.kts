import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

val localProperties = Properties().apply {
    val localFile = rootProject.file("local.properties")
    if (localFile.isFile) {
        localFile.inputStream().use(::load)
    }
}

val keystoreProperties = Properties().apply {
    val keystoreFile = rootProject.file("keystore.properties")
    if (keystoreFile.isFile) {
        keystoreFile.inputStream().use(::load)
    }
}

fun releaseSigningValue(envName: String, propertyName: String): String? =
    providers.environmentVariable(envName).orNull
        ?: keystoreProperties.getProperty(propertyName)
        ?: localProperties.getProperty(propertyName)

val releaseStoreFilePath = releaseSigningValue("CENTURION_RELEASE_STORE_FILE", "release.storeFile")
val releaseStorePassword = releaseSigningValue("CENTURION_RELEASE_STORE_PASSWORD", "release.storePassword")
val releaseKeyAlias = releaseSigningValue("CENTURION_RELEASE_KEY_ALIAS", "release.keyAlias")
val releaseKeyPassword = releaseSigningValue("CENTURION_RELEASE_KEY_PASSWORD", "release.keyPassword")
val hasReleaseSigningConfig = listOf(
    releaseStoreFilePath,
    releaseStorePassword,
    releaseKeyAlias,
    releaseKeyPassword
).all { !it.isNullOrBlank() }

android {
    namespace = "com.century.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.century.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 6
        versionName = "1.0.6"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseStoreFilePath))
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

gradle.taskGraph.whenReady {
    val releaseBuildRequested = allTasks.any { task ->
        task.project == project && task.name in setOf(
            "assembleRelease",
            "bundleRelease",
            "packageRelease",
            "installRelease"
        )
    }
    if (releaseBuildRequested && !hasReleaseSigningConfig) {
        throw GradleException(
            "Release signing is not configured. Set CENTURION_RELEASE_STORE_FILE, " +
                "CENTURION_RELEASE_STORE_PASSWORD, CENTURION_RELEASE_KEY_ALIAS, and " +
                "CENTURION_RELEASE_KEY_PASSWORD, or define release.storeFile, " +
                "release.storePassword, release.keyAlias, and release.keyPassword in " +
                "untracked keystore.properties or local.properties."
        )
    }
}

dependencies {
    // Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.01.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3:1.2.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coil
    implementation("io.coil-kt:coil-compose:2.5.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Vico Charts
    implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.01.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
