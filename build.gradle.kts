plugins {
    `maven-publish`
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization")
}

group = "io.github.jokoroukwu"
version = "0.1.1"

repositories {
    jcenter()
    mavenCentral()
}
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.snakeyaml:snakeyaml-engine:2.3")
    implementation("com.github.kittinunf.fuel:fuel:2.2.3")
    implementation("org.testng:testng:7.3.0")
    implementation("io.github.microutils:kotlin-logging:2.0.6")

    testImplementation("io.mockk:mockk:1.10.6")
    testImplementation("org.assertj:assertj-core:3.19.0")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.27.2")
    testImplementation("com.github.tomakehurst:wiremock-jre8:2.27.2")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}


publishing {
    publications {
        create<MavenPublication>("zephyr-api") {
            from(components["java"])
            pom {
                name.set("zephyr-api")
                description.set("A simple library for publishing test results to 'Zephyr for JIRA Server' test management tool")
                url.set("https://github.com/jokoroukwu/zephyr-api")
                licenses {
                    license {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                }
                developers {
                    developer {
                        id.set("jokoroukwu")
                        name.set("John Okoroukwu")
                        email.set("john.okoroukwu@gmail.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com//jokoroukwu/zephyr-api.git")
                    developerConnection.set("scm:git:ssh://jokoroukwu/zephyr-api.git")
                    url.set("https://github.com/jokoroukwu/zephyr-api")
                }
            }
        }
    }
    repositories {
        maven {
            name = "sonatype"
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = property("sonatypeUser") as String
                password = property("sonatypePassword") as String
            }
        }
    }
}
