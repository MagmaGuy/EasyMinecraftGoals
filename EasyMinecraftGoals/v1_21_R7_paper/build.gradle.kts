plugins {
    id("io.papermc.paperweight.userdev")
}

// Use Spigot mappings (REOBF_PRODUCTION) for the output artifact.
// This ensures uniform mapping with older modules. Paper's plugin remapper
// converts to Mojang mappings at runtime. (Fixed in PR #13388)
paperweight.reobfArtifactConfiguration = io.papermc.paperweight.userdev.ReobfArtifactConfiguration.REOBF_PRODUCTION

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
    compileOnly(project(":core"))
}

// Transform source from v1_21_R7_common, replacing package name AND directory structure
val transformSources by tasks.registering(Copy::class) {
    from("../v1_21_R7_common/src/main/java")
    into(layout.buildDirectory.dir("generated-sources/java"))
    // Replace package name in file contents
    filter { line ->
        line.replace("v1_21_R7_common", "v1_21_R7_paper")
    }
    // Also rename the directory structure
    eachFile {
        path = path.replace("v1_21_R7_common", "v1_21_R7_paper")
    }
    includeEmptyDirs = false
}

sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-sources/java"))
        }
        resources {
            srcDir("../v1_21_R7_common/src/main/resources")
        }
    }
}

tasks {
    compileJava {
        dependsOn(transformSources)
    }

    // Ensure reobfJar runs during build (required for REOBF_PRODUCTION)
    assemble {
        dependsOn(reobfJar)
    }
}

// Override the default artifact to use reobfJar instead of the dev jar
// This ensures shadow plugin gets the Spigot-mapped jar
afterEvaluate {
    val reobfJar = tasks.named<io.papermc.paperweight.tasks.RemapJar>("reobfJar")

    configurations.named("runtimeElements") {
        outgoing.artifacts.clear()
        outgoing.artifact(reobfJar.flatMap { it.outputJar }) {
            builtBy(reobfJar)
        }
    }
    configurations.named("apiElements") {
        outgoing.artifacts.clear()
        outgoing.artifact(reobfJar.flatMap { it.outputJar }) {
            builtBy(reobfJar)
        }
    }
}
