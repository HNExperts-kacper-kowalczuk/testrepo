import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "com.hnexperts.cosmetics.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        androidResources {
            enable = true
        }
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
    }

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.appcompat)
            implementation(libs.koin.android)
            implementation(libs.sqldelight.android.driver)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.androidx.camera.mlkit)
            implementation(libs.mlkit.barcode)
            implementation(libs.mlkit.text)
            implementation(libs.kotlinx.coroutines.play.services)
            implementation(libs.play.services.ads)
            implementation(libs.user.messaging.platform)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqlite.driver)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.material.icons.core)

            implementation(libs.navigation.compose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}

sqldelight {
    databases {
        create("CatalogDatabase") {
            packageName.set("com.hnexperts.cosmetics.data.catalogdb")
            srcDirs.setFrom("src/commonMain/sqldelight/catalog")
        }
        create("UserDatabase") {
            packageName.set("com.hnexperts.cosmetics.data.userdb")
            srcDirs.setFrom("src/commonMain/sqldelight/user")
        }
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.hnexperts.cosmetics.resources"
}

afterEvaluate {
    val jvmMain = kotlin.targets.getByName("jvm").compilations.getByName("main")
    tasks.register<JavaExec>("exportCatalogSources") {
        group = "catalog"
        description = "Write CosIng/OBF source JSON, manifest, and catalog.sqlite.gz"
        dependsOn(jvmMain.compileTaskProvider)
        classpath = files(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
        mainClass.set("com.hnexperts.cosmetics.catalog.pipeline.ExportCatalogSourcesKt")
        args(rootProject.projectDir.absolutePath)
    }
    tasks.register<JavaExec>("packShippedCatalog") {
        group = "catalog"
        description = "Pack CosIng/OBF ingest (or fixture sources) into composeResources/files/catalog.sqlite.gz"
        dependsOn(jvmMain.compileTaskProvider)
        classpath = files(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
        mainClass.set("com.hnexperts.cosmetics.catalog.pipeline.PackShippedCatalogKt")
        args(rootProject.projectDir.absolutePath)
        (project.findProperty("maxProducts") as String?)?.let { extra -> args(extra) }
    }
    tasks.register<JavaExec>("ingestCatalogSources") {
        group = "catalog"
        description = "Fetch CosIng + Open Beauty Facts and write candidate dumps to catalog/ingest"
        dependsOn(jvmMain.compileTaskProvider)
        classpath = files(jvmMain.output.allOutputs, jvmMain.runtimeDependencyFiles)
        mainClass.set("com.hnexperts.cosmetics.catalog.pipeline.ingest.IngestCatalogSourcesKt")
        args(rootProject.projectDir.absolutePath)
        (project.findProperty("ingestArgs") as String?)?.split(' ')?.forEach { extra -> args(extra) }
    }
}
