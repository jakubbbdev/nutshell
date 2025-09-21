plugins {
    kotlin("jvm") version "2.2.0"
    kotlin("kapt") version "2.2.0"
    id("maven-publish")
}

group = "dev.jakub.nutshell"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // MongoDB Driver
    implementation("org.mongodb:mongodb-driver-sync:4.10.2")
    implementation("org.mongodb:mongodb-driver-core:4.10.2")
    implementation("org.mongodb:bson:4.10.2")
    
    // Kotlin Reflection
    implementation("org.jetbrains.kotlin:kotlin-reflect:2.2.0")
    
    // JSON Processing
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    
    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
    testImplementation("org.testcontainers:mongodb:1.19.3")
    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
}

tasks.test {
    useJUnitPlatform()
    enabled = true
}

kotlin {
    jvmToolchain(21)
}


publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/jakubbbdev/nutshell")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            
            pom {
                name.set("Nutshell MongoDB")
                description.set("A powerful MongoDB library for Kotlin with annotations and repository pattern")
                url.set("https://github.com/jakubbbdev/nutshell")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("jakubbbdev")
                        name.set("Jakub")
                        email.set("jakub@example.com")
                    }
                }
                
                scm {
                    connection.set("scm:git:git://github.com/jakubbbdev/nutshell.git")
                    developerConnection.set("scm:git:ssh://github.com/jakubbbdev/nutshell.git")
                    url.set("https://github.com/jakubbbdev/nutshell")
                }
            }
        }
    }
}