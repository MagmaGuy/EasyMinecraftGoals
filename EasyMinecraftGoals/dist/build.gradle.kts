plugins {
    id("com.gradleup.shadow")
}

// Set the artifact ID to match what Maven was publishing
base {
    archivesName.set("EasyMinecraftGoals-dist")
}

// Disable Gradle module metadata (causes issues with shadow jar variants)
tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

// Configure publishing to use the shadow jar instead of regular jar
afterEvaluate {
    publishing {
        publications {
            named<MavenPublication>("maven") {
                artifactId = "EasyMinecraftGoals-dist"
                // Clear the default java component and use shadow jar
                artifacts.clear()
                artifact(tasks.shadowJar)

                // Remove all dependencies from POM since this is a fat jar
                pom.withXml {
                    val dependenciesNode = asNode().get("dependencies")
                    if (dependenciesNode is groovy.util.NodeList && dependenciesNode.isNotEmpty()) {
                        asNode().remove(dependenciesNode.first() as groovy.util.Node)
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation(project(":v1_19_R3"))
    implementation(project(":v1_20_R1"))
    implementation(project(":v1_20_R2"))
    implementation(project(":v1_20_R3"))
    implementation(project(":v1_20_R4"))
    implementation(project(":v1_21_R1"))
    implementation(project(":v1_21_R2"))
    implementation(project(":v1_21_R3"))
    implementation(project(":v1_21_R4"))
    implementation(project(":v1_21_R5"))
    implementation(project(":v1_21_R6"))
    implementation(project(":v1_21_R7_spigot"))
    // Don't include v1_21_R7_paper via normal dependency - we'll add the reobfJar directly
}

// Get the reobfJar output from v1_21_R7_paper after projects are evaluated
gradle.projectsEvaluated {
    val reobfJar = project(":v1_21_R7_paper").tasks.named<io.papermc.paperweight.tasks.RemapJar>("reobfJar")

    tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
        dependsOn(reobfJar)
        // Include the reobfJar output directly
        from(zipTree(reobfJar.flatMap { it.outputJar }))
    }
}

tasks {
    shadowJar {
        archiveBaseName.set("EasyMinecraftGoals")
        archiveClassifier.set("")
        archiveVersion.set(project.version.toString())

        // Don't set paperweight-mappings-namespace - let Paper's plugin remapper
        // handle the Spigot-mapped modules (R1-R6, R7_spigot)

        exclude("**/package-info.class")
        exclude("META-INF/MANIFEST.MF")

        destinationDirectory.set(file("output"))
    }

    build {
        dependsOn(shadowJar)
    }

    jar {
        enabled = false
    }
}
