plugins {
    id("java")
}

subprojects {
    apply<JavaPlugin>()

    repositories {
        mavenLocal()
        mavenCentral()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
    }
}

val archives_base_name: String by project
val mod_version: String by project

val buildAll = tasks.create("buildAll") {
    dependsOn(":v1_17:build")
    dependsOn(":v1_18:build")
    dependsOn(":v1_19:build")
    doLast {
        println("Copying files...")
        file("v1_19/build/libs/v1_19.jar").copyTo(file("build/${archives_base_name}-1.19-${mod_version}.jar"), true)
        file("v1_18/build/libs/v1_18.jar").copyTo(file("build/${archives_base_name}-1.18-${mod_version}.jar"), true)
        file("v1_17/build/libs/v1_17.jar").copyTo(file("build/${archives_base_name}-1.17-${mod_version}.jar"), true)
    }
}
