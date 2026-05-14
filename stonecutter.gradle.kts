plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "26.1" /* [SC] DO NOT EDIT */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "holodisplays"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
}

tasks.register("chiseledBuild") {
    group = "build"
    dependsOn(stonecutter.tasks.named("buildAndCollect"))
}

tasks.register("chiseledRun") {
    group = "build"
    dependsOn(stonecutter.tasks.named("runServer"))
}

tasks.register("publishAll") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishMods"))
}

tasks.register("publishModrinthAll") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishModrinth"))
}

tasks.register("publishCurseforgeAll") {
    group = "publishing"
    dependsOn(stonecutter.tasks.named("publishCurseforge"))
}

stonecutter.tasks {
    order("publishMods")
}