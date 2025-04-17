plugins {
    id("dev.kikugie.stonecutter")
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

stonecutter registerChiseled tasks.register("chiseledBuild", stonecutter.chiseled) {
    group = "project"
    ofTask("buildAndCollect")
}

stonecutter parameters {
    swap("mod_version", "\"${property("mod.version")}\";")
    const("release", property("mod.id") != "holodisplays")
    dependency("fapi", node!!.property("deps.fabric_api").toString())
}

stonecutter registerChiseled tasks.register("chiseledPublishMods", stonecutter.chiseled) {
    group = "project"
    ofTask("publishMods")
}
