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

val archivesBaseName = "${archives_base_name}-${minecraft_version}"

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
		val sourceStart = this.project.projectDir.absolutePath + "/src/main/java/me/aleksilassila/litematica/printer/v1_19"
		val sourceDir = file(sourceStart)
		for (sourceFile in sourceDir.listFiles()) {
			if (sourceFile.name.equals("interfaces") || sourceFile.name.equals("mixin")) continue

			sourceFile.walk()
				.filter { it.isFile }
				.forEach { copyFile(it, "v1_19", "v1_18"); copyFile(it, "v1_19", "v1_17"); }
		}
	}
}

/*
plugins {
	id 'fabric-loom' version '1.0-SNAPSHOT'
	id 'maven-publish'
}


sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17


archivesBaseName = "${project.archives_base_name}-${project.minecraft_version}"
version = project.mod_version
//group = project.maven_group

repositories {
    maven { url 'https://masa.dy.fi/maven' }
    maven { url = "https://www.cursemaven.com" }
}

dependencies {
//	implementation(project(":testmod"))
	implementation(project(":common"))
	//to change the versions see the gradle.properties file
	minecraft "com.mojang:minecraft:${project.minecraft_version}"
	mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"

	modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"
	modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
	modImplementation "fi.dy.masa.malilib:malilib-fabric-${project.malilib_version}"
	modImplementation "curse.maven:litematica-${project.litematica_projectid}:${project.litematica_fileid}"
}

processResources {
	inputs.property "version", project.version

	filesMatching("fabric.mod.json") {
		expand "version": project.version
	}
}

tasks.withType(JavaCompile).configureEach {
	// Minecraft 1.18 (1.18-pre2) upwards uses Java 17.
	it.options.release = 17
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()
}

jar {
	from("LICENSE") {
		rename { "${it}_${project.archivesBaseName}"}
	}
}

// configure the maven publication
publishing {
	publications {
		mavenJava(MavenPublication) {
			from components.java
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}

 */
