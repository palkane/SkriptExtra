plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.2.2"
}

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven {
        url = uri("https://repo.skriptlang.org/releases")
    }

    maven {
        url = uri("https://repo.destroystokyo.com/content/repositories/snapshots/")
    }

    maven {
        url = uri("https://repo.infernalsuite.com/repository/maven-snapshots/")
    }

    maven {
        url = uri("https://repo.codemc.org/repository/maven-public")
    }

    // Skript-PlaceholderAPI
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots")
    }

    maven {
        url = uri("https://oss.sonatype.org/content/repositories/snapshots")
    }

    maven {
        url = uri("https://jitpack.io" )
    }

    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/" )
    }

}
dependencies {
    compileOnly(libs.adventure.api)
    compileOnly(libs.paper.api)
    compileOnly(libs.skript)
    compileOnly(libs.fawe.core)
    compileOnly(libs.fawe.bukkit)
    compileOnly("com.github.retrooper:packetevents-spigot:2.10.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.github.SkriptLang:Skript-reflect:2.6.1")
}

group = "re.imc"
version = "1.0.4"
description = "SkriptExtra"

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    build.configure {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveFileName = "${project.name}-${project.version}.${archiveExtension.get()}"
        exclude("META-INF/**") // Avoid to include META-INF/maven in Jar
    }
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}
