import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "2.2.+"
    kotlin("plugin.serialization") version "2.2.+"
    id("fabric-loom") version "1.14.+"
    id("me.modmuss50.mod-publish-plugin") version "0.8.+"
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.let {
        load(it.inputStream())
    }
}

class ModData {
    val id = property("mod.id").toString()
    val name = property("mod.name").toString()
    val version = property("mod.version").toString()
    val group = property("mod.group").toString()
}

class ModDependencies {
    operator fun get(name: String) = property("deps.$name").toString()
}

val mod = ModData()
val deps = ModDependencies()
val mcVersion = stonecutter.current.version
val mcDep = property("mod.mc_dep").toString()

version = "${mod.version}+$mcVersion"
group = mod.group
base { archivesName.set(mod.id) }

repositories {
    maven("https://maven.quiltmc.org/repository/release")
    maven("https://maven.nucleoid.xyz/") { name = "Nucleoid" }
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${mcVersion}")
    mappings("net.fabricmc:yarn:${mcVersion}+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabric_loader"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${deps["kotlin_version"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps["fabric_api"]}")

    modImplementation("eu.pb4", "placeholder-api", deps["placeholder-api"])
    modImplementation(include("eu.pb4", "sgui", deps["sgui"]))
    modImplementation(include("me.lucko", "fabric-permissions-api", deps["permissions-api"]))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.10.1")
}

loom {
    if (mcVersion == "1.21.10") {
        accessWidenerPath = rootProject.file("src/main/resources/accesswideners/1.21.10.accesswidener")
    }

    decompilers {
        get("vineflower").apply {
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

java {
    val javaVersion = JavaVersion.VERSION_21
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
    withSourcesJar()
}

tasks.processResources {
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcDep)

    val accessWidenerEntry = if (mcVersion == "1.21.10") ",\n  \"accessWidener\": \"accesswideners/1.21.10.accesswidener\",\n  " else ",\n  "

    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcDep,
        "accessWidenerEntry" to accessWidenerEntry
    )

    filesMatching("fabric.mod.json") { expand(map) }
    
    if (mcVersion != "1.21.10") {
        exclude("accesswideners/**")
    }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}

publishMods {
    file = tasks.remapJar.get().archiveFile
    additionalFiles.from(tasks.named("sourcesJar").get().outputs.files)
    displayName = "${mod.name} ${mod.version}"
    version = mod.version
    changelog = rootProject.file("CHANGELOG.md").readText()
    type = STABLE
    modLoaders.add("fabric")
    val lower = """>=\s*([0-9.]+)""".toRegex().find(mcDep)?.groupValues?.get(1)
    val upper = """<=\s*([0-9.]+)""".toRegex().find(mcDep)?.groupValues?.get(1)

    modrinth {
        projectId = "WdbPWi13"
        accessToken = localProperties.getProperty("MODRINTH_TOKEN")
        minecraftVersionRange {
            start = lower ?: "latest"
            end = upper ?: "latest"
        }
        requires("fabric-api")
        requires("placeholder-api")
        requires("fabric-language-kotlin")
    }

    curseforge {
        projectId = "1150354"
        projectSlug = "holodisplays"
        accessToken = localProperties.getProperty("CURSEFORGE_TOKEN")
        serverRequired = true
        minecraftVersionRange {
            start = lower ?: "latest"
            end = upper ?: "latest"
        }
        requires("fabric-api")
        requires("text-placeholder-api")
        requires("fabric-language-kotlin")
    }
}