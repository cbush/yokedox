plugins {
  kotlin("jvm") version "1.4.10"

  id("com.github.johnrengelman.shadow") version "6.0.0"

}

java {
  sourceCompatibility = JavaVersion.VERSION_12
  targetCompatibility = JavaVersion.VERSION_12
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // JSON parsing https://github.com/cbeust/klaxon
    implementation("com.beust:klaxon:5.0.1")
}

tasks.build {
  // Build fat jar (all dependencies included) for use with javadoc
  val shadowJar = tasks.named("shadowJar")
  finalizedBy(shadowJar)
}

tasks {
  test {
    exclude("**/JavadocTest*")

    // Force kotlin build directory to front of classpath.
    // The IDEA coverage runner with Java tests in a Kotlin project
    // seems to produce classes for everything in the java/ directory,
    // which then do not get updated upon subsequent runs of "test".
    classpath = files("./build/classes/kotlin/test") + classpath
  }
}

tasks.withType<JavaCompile> {
  options.compilerArgs = listOf(
    "--add-exports", "jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED"
  )
}

tasks.register<Test>("testJavadoc") {
  jvmArgs(
    "--add-exports", "jdk.javadoc/jdk.javadoc.internal.tool=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED",
    "--add-exports", "jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED"
  )

  include("**/JavadocTest*")
}

tasks.register("testDoclet") {
  dependsOn(tasks.build)
  mustRunAfter(tasks.build)
  doLast {
    project.exec {
      commandLine = "javadoc -doclet com.yokedox.JsonDoclet -docletpath ./build/libs/yokedox-all.jar -sourcepath test/src/main/java/ --output-path tmp/com.yokedox.test.json -f com.yokedox.test".split(" ")
    }
  }
}
