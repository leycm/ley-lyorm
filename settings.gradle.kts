dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("libs.versions.toml"))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {}
}

rootProject.name = "ley-lyorm"

include("common", "api")

project(":api").projectDir = file("orm-api")
project(":common").projectDir = file("orm-common")
