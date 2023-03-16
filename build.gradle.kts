import org.aya.gradle.BuildUtil

buildscript {
    repositories { mavenCentral() }
    dependencies { classpath("org.aya-prover.upstream:build-util:0.0.11") }
}

plugins {
    id("java-library")
    id("jacoco")
    id("maven-publish")
    id("signing")
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("org.glavo.load-maven-publish-properties") version "0.1.0"
}

group = "org.glavo"
version = "0.3.0" + "-SNAPSHOT"
description = "Java 21 Classfile API"

tasks.withType<JavaCompile> {
    exclude("**/snippet-files/*")

    options.release.set(17)
    options.compilerArgs.add("--enable-preview")

    val root = destinationDirectory.asFile.get()
    doLast {
        val tree = fileTree(root)
        tree.include("**/*.class")
        tree.include("module-info.class")
        tree.forEach {
            BuildUtil.stripPreview(
                root.toPath(), it.toPath(),
                true, false,
                "java/lang/AssertionError",
            )
        }
    }
}

val sourcesJar = tasks.create<Jar>("sourcesJar") {
    group = "build"
    archiveClassifier.set("sources")

    from(sourceSets.main.get().allSource)
}

val javadocJar = tasks.create<Jar>("javadocJar") {
    group = "build"
    archiveClassifier.set("javadoc")
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

repositories {
    mavenCentral()
}

dependencies {
    val junitVersion = "5.9.2"
    val asmVersion = "9.4"

    // https://mvnrepository.com/artifact/org.ow2.asm/asm
    testImplementation("org.ow2.asm:asm:$asmVersion")
    testImplementation("org.ow2.asm:asm-tree:$asmVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(false)
    }
}

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            version = project.version.toString()
            artifactId = project.name
            from(components["java"])
            artifact(javadocJar)
            artifact(sourcesJar)

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/Glavo/classfile")

                licenses {
                    license {
                        name.set("GNU General Public License, version 2, with the Classpath Exception")
                        url.set("https://openjdk.org/legal/gplv2+ce.html")
                    }
                }

                developers {
                    developer {
                        id.set("asotona")
                        name.set("Adam Sotona")
                        email.set("asotona@openjdk.org")
                    }
                }

                scm {
                    url.set("https://github.com/Glavo/classfile")
                }
            }
        }
    }
}

if (rootProject.ext.has("signing.key")) {
    signing {
        useInMemoryPgpKeys(
            rootProject.ext["signing.keyId"].toString(),
            rootProject.ext["signing.key"].toString(),
            rootProject.ext["signing.password"].toString(),
        )
        sign(publishing.publications["maven"])
    }
}

// ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
nexusPublishing {
    repositories {
        sonatype {
            stagingProfileId.set(rootProject.ext["sonatypeStagingProfileId"].toString())
            username.set(rootProject.ext["sonatypeUsername"].toString())
            password.set(rootProject.ext["sonatypePassword"].toString())
        }
    }
}