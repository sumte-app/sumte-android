plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id ("kotlin-parcelize")
    id("org.jetbrains.kotlin.kapt")

}

android {
    namespace = "com.example.sumte"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sumte"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.kizitonwose.calendar:view:2.0.3")
    implementation("com.google.android.material:material:1.12.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.places)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    //0703추가
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("com.kizitonwose.calendar:view:2.0.3")

    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlin Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // OkHttp 로깅 (디버깅 시 유용)
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Glide (이미지 로딩용)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    kapt("com.github.bumptech.glide:compiler:4.15.1")

    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")


}