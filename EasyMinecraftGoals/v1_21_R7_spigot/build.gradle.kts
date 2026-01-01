plugins {
    id("io.github.patrick.remapper")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.21.11-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly(project(":core"))
}

// Transform source from v1_21_R7_common, replacing package name AND directory structure
val transformSources by tasks.registering(Copy::class) {
    from("../v1_21_R7_common/src/main/java")
    into(layout.buildDirectory.dir("generated-sources/java"))
    // Replace package name in file contents
    filter { line ->
        line.replace("v1_21_R7_common", "v1_21_R7_spigot")
    }
    // Also rename the directory structure
    eachFile {
        path = path.replace("v1_21_R7_common", "v1_21_R7_spigot")
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

    remap {
        version.set("1.21.11")
        dependsOn(jar)  // Ensure jar is built before remapping
    }

    jar {
        finalizedBy("remap")
    }
}

// Make sure the remapped JAR is used as the outgoing artifact
// The remapper plugin outputs to build/libs/<name>-<version>.jar (replacing the original)
val remappedJar = tasks.named("remap").map {
    layout.buildDirectory.file("libs/${base.archivesName.get()}-${project.version}.jar").get().asFile
}

configurations {
    named("runtimeElements") {
        outgoing {
            artifacts.clear()
            artifact(remappedJar) {
                builtBy(tasks.named("remap"))
            }
        }
    }
    named("apiElements") {
        outgoing {
            artifacts.clear()
            artifact(remappedJar) {
                builtBy(tasks.named("remap"))
            }
        }
    }
}
