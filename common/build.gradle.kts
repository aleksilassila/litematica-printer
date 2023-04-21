plugins {
    id("fabric-loom").version("1.0-SNAPSHOT")
    id("maven-publish")
//    id("java")
}

group = "me.aleksilassila.litematica.printer.common"
version = "unspecified"

repositories {
//    mavenCentral()
    maven("https://masa.dy.fi/maven")
    maven("https://www.cursemaven.com")
}

val archives_base_name: String by project
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val malilib_version: String by project
val litematica_projectid: String by project
val litematica_fileid: String by project

dependencies {
//    implementation("com.google.code.gson:gson:2.8.9")

    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings("net.fabricmc:yarn:${yarn_mappings}:v2")

    modImplementation("net.fabricmc:fabric-loader:${loader_version}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${fabric_version}")
    modImplementation("fi.dy.masa.malilib:malilib-fabric-${malilib_version}")
    modImplementation("curse.maven:litematica-${litematica_projectid}:${litematica_fileid}")
}

//tasks.test {
//    useJUnitPlatform()
//}