plugins {
    id("fabric-loom").version("1.6-SNAPSHOT")
    id("maven-publish")
}

//subprojects {
//apply<JavaPlugin>()

val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val malilib_version: String by project
val litematica_projectid: String by project
val litematica_fileid: String by project

val archives_base_name: String by project
val mod_version: String by project

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
//}

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://masa.dy.fi/maven")
    maven("https://www.cursemaven.com")
}

dependencies {
//    implementation(project(":common"))
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")

    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("fi.dy.masa.malilib:malilib-fabric-${malilib_version}")
    modImplementation("curse.maven:litematica-${litematica_projectid}:${litematica_fileid}")
}

tasks.withType<ProcessResources> {
    inputs.property("version", mod_version)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to mod_version))
    }
}

tasks.register("copyJar") {
    // Specify that this task runs after the 'build' task
    dependsOn("build")

    // Specify the task's action
    doLast {
        val destination = file("build/${archives_base_name}-${minecraft_version}-${mod_version}.jar")
        file("build/libs/litematica-printer.jar").copyTo(destination, true)
        println("Copied output to ${destination.absolutePath}")
    }
}

tasks.build {
    finalizedBy("copyJar")
}