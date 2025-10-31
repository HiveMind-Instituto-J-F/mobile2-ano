plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.aula.mobile_hivemind"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aula.mobile_hivemind"
        minSdk = 30
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.firebase.ai)
    implementation (libs.play.services.auth)
    implementation(libs.material.calendarview)
    implementation(libs.activity)
    implementation(libs.play.services.base)
    implementation(libs.firebase.auth)

    // Dependências do Gráfico
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(libs.firebase.firestore)

    // Dependências do JUnit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.0-M1") // Versão mais recente
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.0-M1") // Versão mais recente
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.0-M1") // Opcional, para testes parametrizados

    // Para rodar testes JUnit 4 e 5 juntos
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.0-M1") // Versão mais recente

    testImplementation(libs.junit) // Mantém a dependência do JUnit 4 para compatibilidade
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-firestore")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.cloudinary:cloudinary-android:3.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")
}