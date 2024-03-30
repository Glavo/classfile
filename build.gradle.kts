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
version = "0.5.0" + "-SNAPSHOT"
description = "Java 21 Classfile API"

val sourceToolchainVersion = 17
val javadocToolchainVersion = 21
val testToolchainVersion = 21

sourceSets {
    test {
        java {
            srcDir("src/examples/java")
        }
    }
}

tasks.withType<JavaCompile> {
    options.forkOptions.jvmArgs = listOf("-Duser.language=en")
}

tasks.compileJava {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(sourceToolchainVersion))
    })

    options.release.set(sourceToolchainVersion)

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

tasks.compileTestJava {
    javaCompiler.set(javaToolchains.compilerFor {
        languageVersion.set(JavaLanguageVersion.of(testToolchainVersion))
    })
    options.compilerArgs.add("--enable-preview")

    options.release.set(testToolchainVersion)
}

tasks.javadoc {
    javadocTool.set(javaToolchains.javadocToolFor {
        languageVersion.set(JavaLanguageVersion.of(javadocToolchainVersion))
    })

    val options = this.options as StandardJavadocDocletOptions

    options.jFlags("-Duser.language=en")
    options.encoding = "UTF-8"

    options.tags("apiNote", "implNote", "implSpec", "jvms", "jls", "snippet-files")

    options.addStringOption("Xdoclint:none", "-quiet")
    options.addBooleanOption("-enable-preview", true)
    options.addStringOption("-source", "20")
    options.addStringOption("link", "https://docs.oracle.com/en/java/javase/17/docs/api/")

    options.addStringOption("-snippet-path", file("src/main/snippet-files").absolutePath)
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

repositories {
    mavenCentral()
}

dependencies {
    // https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
    val junitVersion = "5.10.2"

    // https://mvnrepository.com/artifact/org.ow2.asm/asm
    val asmVersion = "9.7"

    testImplementation("org.ow2.asm:asm:$asmVersion")
    testImplementation("org.ow2.asm:asm-tree:$asmVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks.test {
    javaLauncher.set(javaToolchains.launcherFor() {
        languageVersion.set(JavaLanguageVersion.of(testToolchainVersion))
    })

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