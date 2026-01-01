plugins {
    id("io.github.patrick.remapper")
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.20.6-R0.1-SNAPSHOT:remapped-mojang")
    compileOnly(project(":core"))
}

tasks {
    remap {
        version.set("1.20.6")
        dependsOn(jar)
    }
    jar {
        finalizedBy("remap")
    }
}

// Make sure the remapped JAR is used as the outgoing artifact
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
