plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.2")
}

application {
    mainClass.set("ui.Main")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
