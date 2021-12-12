// Top-level build file where you can add configuration options common to all sub-projects/modules.
apply(from ="./tools/config.gradle")

buildscript { 
    val kotlin_version = "1.4.32"


    apply(from = "./tools/config.gradle")
    repositories {
        mavenCentral()
        jcenter()
        mavenLocal()
        google()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath("com.leon.channel:plugin:2.0.1")
        classpath("com.tencent.bugly:tinker-support:1.1.5")
        //classpath libraries.greendaoPlugin // add plugin

        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.1.0-beta02")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlin_version}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.12")

        classpath("com.google.dagger:hilt-android-gradle-plugin:2.28-alpha")
    }
}
allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven( url = "https://jitpack.io" )
        mavenLocal()
    }
}
//task clean(type: Delete) {
//    print "before clean rootproject \n"
//    delete rootProject.buildDir
//}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}
