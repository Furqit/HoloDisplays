import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.+"
    id("fabric-loom") version "1.8.+"
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
    fun fapi(vararg modules: String) {
        modules.forEach { fabricApi.module(it, deps["fapi"]) }
    }

    minecraft("com.mojang:minecraft:${mcVersion}")
    mappings("net.fabricmc:yarn:${mcVersion}+build.${deps["yarn_build"]}:v2")
    modImplementation("net.fabricmc:fabric-loader:${deps["fabric_loader"]}")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.12.3+kotlin.2.0.21")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${deps["fabric_api"]}")

    modImplementation("eu.pb4:placeholder-api:${deps["placeholder-api"]}")
    modImplementation(include("eu.pb4","sgui","1.6.0+1.21"))
    api(include("org.quiltmc.parsers", "json", "0.3.0"))
    api(include("org.quiltmc.parsers", "gson", "0.3.0"))

    vineflowerDecompilerClasspath("org.vineflower:vineflower:1.10.1")
}

loom {
    decompilers {
        get("vineflower").apply {
            options.put("mark-corresponding-synthetics", "1")
        }
    }

    runConfigs.all {
        ideConfigGenerated(stonecutter.current.isActive)
        vmArgs("-Dmixin.debug.export=true")
        runDir = "../../run"
    }
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(
            if (stonecutter.compare(mcVersion, "1.20.6") >= 0)
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
            else
                org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
        )
    }
}

java {
    withSourcesJar()
    val javaVersion =
        if (stonecutter.compare(mcVersion, "1.20.6") >= 0) JavaVersion.VERSION_21 else JavaVersion.VERSION_17
    targetCompatibility = javaVersion
    sourceCompatibility = javaVersion
}
tasks.processResources {
    inputs.property("id", mod.id)
    inputs.property("name", mod.name)
    inputs.property("version", mod.version)
    inputs.property("mcdep", mcDep)

    val map = mapOf(
        "id" to mod.id,
        "name" to mod.name,
        "version" to mod.version,
        "mcdep" to mcDep
    )

    filesMatching("fabric.mod.json") { expand(map) }
}

tasks.register<Copy>("buildAndCollect") {
    group = "build"
    from(tasks.remapJar.get().archiveFile)
    into(rootProject.layout.buildDirectory.file("libs/${mod.version}"))
    dependsOn("build")
}