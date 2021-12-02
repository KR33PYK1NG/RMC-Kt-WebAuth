val rmcRepoUser: String by project
val rmcRepoPass: String by project

val rmcGroup = "rmc.kt.plugins"
val rmcArtifact = "webauth"
val rmcVersion = "1.0.7"
val rmcBaseName = "RMC-Kt-WebAuth"

group = rmcGroup
version = rmcVersion

@Suppress("DEPRECATION")
base.archivesBaseName = rmcBaseName

plugins {
    `maven-publish`
    kotlin("jvm") version "1.6.0"
    kotlin("plugin.serialization") version "1.6.0"
}

java {
    withJavadocJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.matching("Eclipse Adoptium"))
    }
}

tasks.withType<ProcessResources> {
    expand("rmcBaseName" to rmcBaseName,
           "rmcVersion" to rmcVersion)
}

tasks.create<Copy>("dumpDeps") {
    from(configurations.runtimeClasspath)
    into("build/deps/")
}

configure<PublishingExtension> {
    repositories {
        maven {
            setUrl("https://repo.rus-minecraft.ru/maven/")
            credentials {
                username = rmcRepoUser
                password = rmcRepoPass
            }
        }
    }
    publications {
        create<MavenPublication>("release") {
            artifactId = rmcArtifact
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()
    maven {
        setUrl("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") // Spigot
    }
    maven {
        setUrl("https://repo.dmulloy2.net/repository/public/") // ProtocolLib
    }
    maven {
        setUrl("https://repo.essentialsx.net/snapshots/") // EssentialsX
    }
    maven {
        setUrl("https://repo.rus-minecraft.ru/maven/") // RMC-Kt-Core
        credentials {
            username = rmcRepoUser
            password = rmcRepoPass
        }
    }
}

dependencies {
    add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.1")
    add("compileOnly", "org.spigotmc:spigot-api:1.18-rc3-R0.1-SNAPSHOT")
    add("compileOnly", "com.comphenix.protocol:ProtocolLib:4.7.0")
    add("compileOnly", "net.essentialsx:EssentialsX:2.19.1-SNAPSHOT")
    add("compileOnly", "rmc.kt.plugins:core:1.2.0")
}
