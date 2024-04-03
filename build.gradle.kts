import de.undercouch.gradle.tasks.download.Download

plugins {
    embeddedKotlin("jvm")
    id("io.ktor.plugin") version "2.3.7"
    kotlin("plugin.serialization") version "1.9.22"
    id("de.undercouch.download") version "5.6.0"
}

group = "com.example"
version = "0.0.1"

application {
    mainClass = "com.example.ApplicationKt"

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

repositories {
    mavenCentral()
}

val ktorVersion = "2.3.9"
//val kotlinVersion = "1.9.22"
val logbackVersion = "1.5.3"
val lakeFsVersion = "1.15.0"


dependencies {
    implementation("net.pwall.json:json-kotlin-schema:0.47")
    // https://mvnrepository.com/artifact/io.lakefs/sdk
    implementation("io.lakefs:sdk:$lakeFsVersion")

    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-http:$ktorVersion")
    //    runtimeOnly("io.ktor:ktor-http:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm")
    //    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")

    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.2")
}

tasks {

    val downloadLakeFS by registering(Download::class) {
        src("https://github.com/treeverse/lakeFS/releases/download/v$lakeFsVersion/lakeFS_${lakeFsVersion}_Linux_x86_64.tar.gz")
        dest(layout.buildDirectory.file("lakeFS_$lakeFsVersion.tar.gz"))
    }

    val downloadAndExtractLakeFS by registering(Copy::class) {
        dependsOn(downloadLakeFS)
        from(tarTree(downloadLakeFS.get().dest))
        into(layout.buildDirectory.file("lakeFS_$lakeFsVersion"))
    }

    test {
        dependsOn(downloadAndExtractLakeFS)
        useJUnitPlatform()
    }
}