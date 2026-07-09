import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

fun loadLocalEnv(): Properties {
    val properties = Properties()
    val envFile = rootProject.file(".env")

    if (envFile.exists()) {
        envFile.readLines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                val separatorIndex = trimmedLine.indexOf("=")
                if (separatorIndex > 0) {
                    val key = trimmedLine.substring(0, separatorIndex).trim()
                    val value = trimmedLine.substring(separatorIndex + 1)
                        .trim()
                        .removeSurrounding("\"")
                        .removeSurrounding("'")
                    properties.setProperty(key, value)
                }
            }
        }
    }

    return properties
}

val localEnv = loadLocalEnv()

fun envValue(name: String, defaultValue: String = ""): String {
    return localEnv.getProperty(name) ?: System.getenv(name) ?: defaultValue
}

fun String.asBuildConfigString(): String {
    return "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""
}

android {
    namespace = "com.example.healthplanner"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.example.healthplanner"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "AI_CALORIE_API_BASE_URL",
            envValue("AI_CALORIE_API_BASE_URL", "https://api.groq.com/openai/v1/chat/completions").asBuildConfigString()
        )
        buildConfigField(
            "String",
            "AI_CALORIE_MODEL",
            envValue("AI_CALORIE_MODEL", "llama-3.1-8b-instant").asBuildConfigString()
        )
        buildConfigField(
            "String",
            "AI_CALORIE_API_KEY",
            envValue("AI_CALORIE_API_KEY").asBuildConfigString()
        )
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.androidx.work.runtime)
    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}
