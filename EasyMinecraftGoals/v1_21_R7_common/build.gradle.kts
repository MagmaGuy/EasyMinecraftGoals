plugins {
    java
}

// This module contains shared source code for R7
// Source is copied and transformed by v1_21_R7_spigot and v1_21_R7_paper
// This module should NOT compile - it's source-only

dependencies {
    // No dependencies - this module is source-only
    compileOnly(project(":core"))
}

// Disable ALL compilation - this is source-only module
tasks.compileJava {
    enabled = false
}

tasks.processResources {
    enabled = false
}

tasks.classes {
    enabled = false
}

// Don't produce a standalone jar - source is used by spigot/paper modules
tasks.jar {
    enabled = false
}

// Don't publish this module - it's just shared source
tasks.withType<PublishToMavenLocal> {
    enabled = false
}
tasks.withType<PublishToMavenRepository> {
    enabled = false
}
