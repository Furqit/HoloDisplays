import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.*

plugins {
    kotlin("jvm") version "2.3.+"
    kotlin("plugin.serialization") version "2.3.+"
    id("dev.kikugie.loom-back-compat")
    id("me.modmuss50.mod-publish-plugin") version "2.0.0-beta.1"
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.let {
        load(it.inputStream())
    }
}

val modId: String = sc.properties["mod.id"]
val modVersion: String = sc.properties["mod.version"]
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")?.asList().orEmpty().map { it.toString() }

version = "${modVersion}+${compatibleVersions.first()}${if (compatibleVersions.size > 1) "-${compatibleVersions.last()}" else ""}"
group = sc.properties["mod.group"] as String
base.archivesName = modId

val mcVersion = stonecutter.current.version
val mcDep: String = property("mod.mc_dep") as String

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    maven("https://maven.nucleoid.xyz")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${mcVersion}")
    loomx.applyMojangMappings()
    modImplementation("net.fabricmc:fabric-loader:${property("deps.fabric_loader")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${property("deps.kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fabric_api")}")

    modImplementation("eu.pb4", "placeholder-api", property("deps.placeholder-api") as String)
    modImplementation(include("eu.pb4", "sgui", property("deps.sgui") as String))
    modImplementation(include("me.lucko", "fabric-permissions-api", property("deps.permissions-api") as String))

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.10.1")
}

loom {
    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1")
    }

    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        runDir = "../../run"
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(when (requiredJava) {
            JavaVersion.VERSION_25 -> JvmTarget.JVM_25
            JavaVersion.VERSION_21 -> JvmTarget.JVM_21
            JavaVersion.VERSION_17 -> JvmTarget.JVM_17
            else -> JvmTarget.JVM_1_8
        })
    }
}

java {
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(requiredJava.majorVersion))
    }
}

tasks.processResources {
    val props = mapOf(
        "id" to modId,
        "name" to modName,
        "version" to modVersion,
        "mcdep" to mcDep
    )

    filesMatching("fabric.mod.json") { expand(props) }
}

val modName: String = sc.properties["mod.name"]

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(loomx.modJar.map { it.archiveFile })
    into(rootProject.layout.buildDirectory.file("libs/${modVersion}"))
    dependsOn("build")
}

publishMods {
    file.set(loomx.modJar.flatMap { it.archiveFile })
    displayName = "$modName $modVersion"
    version = modVersion
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