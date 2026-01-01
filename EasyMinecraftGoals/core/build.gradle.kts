plugins {
    java
}

repositories {
    maven("https://repo.opencollab.dev/main/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    // Bedrock detection - optional runtime dependencies
    compileOnly("org.geysermc.floodgate:api:2.2.3-SNAPSHOT")
    compileOnly("org.geysermc.geyser:api:2.4.2-SNAPSHOT")
}
