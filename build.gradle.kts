plugins {
    id("java")
}

group = "org.kotkina"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks.jar {
    archiveFileName.set("sql-parser.jar")
    manifest {
        attributes["Main-Class"] = "org.kotkina.Main"
    }
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}