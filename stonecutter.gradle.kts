plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

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