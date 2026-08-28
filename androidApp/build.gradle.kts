import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.compose.foundation)
    implementation(libs.koin.android)
}

fun localProperty(key: String): String {
    val file = rootProject.file("local.properties")
    if (!file.exists()) {
        return ""
    }
    val props = Properties()
    file.inputStream().use { stream -> props.load(stream) }
    return props.getProperty(key)?.trim().orEmpty()
}

val sampleAdmobAppId: String = "ca-app-pub-3940256099942544~3347511713"
val sampleAdmobBannerId: String = "ca-app-pub-3940256099942544/9214589741"

android {
    namespace = "com.hnexperts.cosmetics"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.hnexperts.cosmetics.scanner"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["admobAppId"] = sampleAdmobAppId
        manifestPlaceholders["admobBannerId"] = sampleAdmobBannerId
        manifestPlaceholders["catalogBaseUrl"] = localProperty("catalog.sync.url")
        manifestPlaceholders["reportsUrl"] = localProperty("reports.flush.url")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("debug") {
            manifestPlaceholders["admobAppId"] = sampleAdmobAppId
            manifestPlaceholders["admobBannerId"] = sampleAdmobBannerId
            manifestPlaceholders["catalogBaseUrl"] = localProperty("catalog.sync.url")
            manifestPlaceholders["reportsUrl"] = localProperty("reports.flush.url")
        }
        getByName("release") {
            isMinifyEnabled = false
            val releaseAppId: String = localProperty("admob.app.id")
            val releaseBannerId: String = localProperty("admob.banner.id")
            manifestPlaceholders["admobAppId"] =
                releaseAppId.ifEmpty { "ca-app-pub-0000000000000000~0000000000" }
            manifestPlaceholders["admobBannerId"] = releaseBannerId
            manifestPlaceholders["catalogBaseUrl"] = localProperty("catalog.sync.url")
            manifestPlaceholders["reportsUrl"] = localProperty("reports.flush.url")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
