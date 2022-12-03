plugins {
    id("fabric-loom").version("1.0-SNAPSHOT")
    id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

val archives_base_name: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val malilib_version: String by project
val litematica_projectid: String by project
val litematica_fileid: String by project

dependencies {
//    implementation(project(":common"))
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")

    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("fi.dy.masa.malilib:malilib-fabric-${malilib_version}")
    modImplementation("curse.maven:litematica-${litematica_projectid}:${litematica_fileid}")
}

repositories {
    maven("https://masa.dy.fi/maven")
    maven("https://www.cursemaven.com")
}

fun copyFile(source: File, sourceVersion: String, targetVersion: String) {
    val destination = file(source.absolutePath.replace(sourceVersion, targetVersion))
    println("Copying ${source.absolutePath} to ${destination.absolutePath}")
    destination.parentFile.mkdirs()
    source.copyTo(destination, true)
    destination.writeText(destination.readText().replace(sourceVersion, targetVersion))
}

val setupServer = tasks.create("syncImplementations") {
    doFirst {
        val sourceStart =
            this.project.projectDir.absolutePath + "/src/main/java/me/aleksilassila/litematica/printer/v1_19"
        val sourceDir = file(sourceStart)
        for (sourceFile in sourceDir.listFiles()) {
            if (sourceFile.name.equals("implementation")) continue

            sourceFile.walk()
                .filter { it.isFile }
                .forEach { copyFile(it, "v1_19", "v1_18"); copyFile(it, "v1_19", "v1_17"); }
        }
    }
}
