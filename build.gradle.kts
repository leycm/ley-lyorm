plugins {
    `maven-publish`
    java
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.lombok) apply false
}

group = "de.leycm.lyorm"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "io.freefair.lombok")

    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                groupId = project.group.toString()
                artifactId = project.name
                version = project.version.toString()
            }
        }

        repositories {
            maven {
                name = "leycm-repo"
                val repoDir = rootProject.projectDir.parentFile.resolve("repository")
                url = uri(repoDir)
            }
        }
    }
}
