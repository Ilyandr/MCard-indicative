buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:8.0.0-alpha03")
        classpath("com.google.gms:google-services:4.3.14")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("org.jetbrains.kotlin:kotlin-noarg:1.7.10")
        classpath("com.google.android.libraries.mapsplatform.secrets-gradle-plugin:secrets-gradle-plugin:2.0.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.5.2")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://jitpack.io")
        }
    }
}

tasks.create<Delete>("cleanBuildDir") {
    delete = setOf(
        rootProject.buildDir
    )
}
