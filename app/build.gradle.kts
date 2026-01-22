import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.ilseon.drift"
    compileSdk = 36 // Fix syntax - was using invalid block

    sourceSets {
        getByName("androidTest") {
            assets.srcDirs(file("src/androidTest/assets"))
        }
    }

    signingConfigs {
        create("release") {
            val signingStoreFilePath = System.getenv("SIGNING_STORE_FILE_PATH")
            if (signingStoreFilePath != null) {
                storeFile = file(signingStoreFilePath)
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            } else {
                val keystorePropertiesFile = rootProject.file("keystore.properties")
                if (keystorePropertiesFile.exists()) {
                    val keystoreProperties = Properties()
                    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                    keyAlias = keystoreProperties["keyAlias"] as String
                    keyPassword = keystoreProperties["keyPassword"] as String
                    storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                    storePassword = keystoreProperties["storePassword"] as String
                }
            }
        }
    }

    defaultConfig {
        applicationId = "com.ilseon.drift"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Exclude all JUnit license files to prevent packaging conflicts
            excludes += "META-INF/LICENSE*"
        }
    }
    testOptions {
        unitTests.all {
            it.forkEvery = 1
            it.jvmArgs("-Xmx1g")
        }
    }
}

kotlin {
    jvmToolchain(17)
}

ksp {
    arg("room.schemaDirectory", "$projectDir/schemas")
}

tasks.register("printVersionCodeAndName") {
    doLast {
        println("VERSION_CODE=${android.defaultConfig.versionCode}")
        println("VERSION_NAME=${android.defaultConfig.versionName}")
    }
}

dependencies {
    implementation("androidx.camera:camera-core:1.5.2")
    implementation("androidx.camera:camera-camera2:1.5.2")
    implementation("androidx.camera:camera-lifecycle:1.5.2")
    implementation("androidx.camera:camera-view:1.5.2")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.3.0")
    implementation("com.google.guava:guava:33.5.0-android")
    implementation("androidx.health.connect:connect-client:1.1.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    androidTestImplementation(libs.androidx.rules)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.compose.markdown)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // Pager
    implementation("com.google.accompanist:accompanist-pager:0.28.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.28.0")

    // ** Hilt (Dependency Injection) **
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)


    // Room (Database Persistence) **
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // Kotlin Extensions and Coroutines support for Room
    implementation(libs.androidx.room.ktx)

    // ** 2. LIFECYCLE & COROUTINES **
    // For ViewModel and Coroutine Scope
    implementation(libs.androidx.lifecycle.viewmodel.compose) // Updated to a more recent stable version
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.kotlinx.coroutines.core)

    // ** 3. NAVIGATION **
    implementation(libs.androidx.navigation.compose)

    // ** 4. KOTLINX SERIALIZATION **
    implementation(libs.kotlinx.serialization.json)

    // In-app reviews
    implementation(libs.play.review)

    // ** Test Dependencies **
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.core.testing)

    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockk.android)
}
