// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.google.gms.google-services") version "4.4.2" apply false // Đảm bảo plugin Google Services đã được thêm
    id("com.android.application") version "8.9.1" apply false
}

buildscript {
    repositories {
        google()  // Đảm bảo bạn có Google repository
        mavenCentral()
    }
    dependencies {
        classpath("com.google.gms:google-services:4.4.2")  // Đảm bảo có dòng này để sử dụng plugin Google services
    }
}

allprojects {
    repositories {
        mavenCentral()
    }
}
