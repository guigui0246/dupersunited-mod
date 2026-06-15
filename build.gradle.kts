plugins {
    id("fabric-loom") version "1.15-SNAPSHOT"
    id("maven-publish")
}

base {
    archivesName = properties["archives_base_name"] as String
    version = "${properties["mod_version"] as String}+${properties["minecraft_version"] as String}"
    group = properties["maven_group"] as String
}

repositories {
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/releases/")
    }
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.properties["minecraft_version"] as String}")
    mappings("net.fabricmc:yarn:${project.properties["yarn_mappings"] as String}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.properties["loader_version"] as String}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.properties["fabric_version"] as String}")

    include(implementation("io.netty:netty-handler-proxy:${project.properties["netty_version"]}")!!)
    include(implementation("io.netty:netty-codec-socks:${project.properties["netty_version"]}")!!)

    modCompileOnly("com.terraformersmc:modmenu:${project.properties["modmenu_version"] as String}")
}

tasks {
    processResources {
        val properties = mapOf(
            "version" to project.version,
            "minecraft_version" to project.properties["minecraft_version"] as String,
            "loader_version" to project.properties["loader_version"] as String
        )

        filteringCharset = "UTF-8"

        inputs.properties(properties)
        filesMatching("fabric.mod.json") {
            expand(properties)
        }
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }

        withSourcesJar()
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = project.base.archivesName.get()
            from(components["java"])
        }
    }

    repositories {
        mavenLocal() // till release
    }
}
