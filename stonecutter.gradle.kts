plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

stonecutter configureEach {
    swap("mod_version", "\"${property("mod.version")}\";")
    const("release", property("mod.id") != "vinyls")
    dependency("fapi", project.property("deps.fabric_api").toString())
}