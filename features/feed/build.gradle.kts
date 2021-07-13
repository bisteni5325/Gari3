plugins {
    id(Plugins.androidLibrary)
    id(Plugins.daggerHiltPlugin)
    id(Plugins.kotlinAndroid)
    id(Plugins.kotlinKapt)
    id(Plugins.kotlinAndroidExtensions)
}

android {

    compileSdkVersion(AppConfig.compileSdk)

    defaultConfig {
        //applicationId = "${AppConfig.applicationIdPrefix}.dashboard"
        versionCode = AppConfig.versionCode
        versionName = AppConfig.versionName
        minSdkVersion(AppConfig.minSdk)
        targetSdkVersion(AppConfig.targetSdk)
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            debuggable(false)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            manifestPlaceholders(
                mapOf(
                    "crashlyticsCollectionEnabled" to true,
                    "hereMapsLicenceKey" to AppConfig.HERE_LICENCE_KEY_RELEASE,
                    "hereMapsAppID" to AppConfig.HERE_APP_ID_RELEASE,
                    "hereMapsAppToken" to AppConfig.HERE_APP_TOKEN_RELEASE
                )
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
            debuggable(true)
            manifestPlaceholders(
                mapOf(
                    "crashlyticsCollectionEnabled" to false,
                    "hereMapsLicenceKey" to AppConfig.HERE_LICENCE_KEY_DEBUG,
                    "hereMapsAppID" to AppConfig.HERE_APP_ID_DEBUG,
                    "hereMapsAppToken" to AppConfig.HERE_APP_TOKEN_DEBUG
                )
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(AppDependencies.appLibraries)

    implementation(project(Modules.domain))
    implementation(project(Modules.data))
    implementation(project(Modules.content))

    implementation(AppDependencies.navigateLibraries)

    implementation(AppDependencies.daggerHiltLibraries)
    kapt(AppDependencies.daggerHiltCompiler)
    kapt(AppDependencies.daggerHiltAndroidXCompiler)

    implementation(AppDependencies.swipeToRefresh)
    implementation(project(Modules.HERE_SDK))
}
