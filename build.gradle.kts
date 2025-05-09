plugins {
  `java-library`
  id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
  id("xyz.jpenilla.run-paper") version "2.2.3"
}

java {
  // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 8 installed for example.
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

repositories {
  mavenCentral()
  maven(url = "https://jitpack.io")
  maven(url = "https://maven.playpro.com")
  maven(url = "https://maven.enginehub.org/repo/")
  maven(url = "https://repo.papermc.io/repository/maven-public/")
  //maven(url = "https://repo.dmulloy2.net/repository/public/")
  maven(url = "https://repo.codemc.io/repository/maven-snapshots/")
}

dependencies {
  implementation("org.projectlombok:lombok:1.18.26")
    // Use the paperDevBundle for PaperMC development
  paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")

  // Additional dependencies
  //compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0")
  compileOnly("com.github.retrooper:packetevents-spigot:2.8.0-SNAPSHOT")
}

tasks {
  // Configure reobfJar to run when invoking the build task
  assemble {
    dependsOn(reobfJar)
  }

  compileJava {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything

    // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
    // See https://openjdk.java.net/jeps/247 for more information.
    options.release.set(21)
  }
  javadoc {
    options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
  }
//  processResources {
//    filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
//    val props = mapOf(
//      "name" to project.name,
//      "version" to project.version,
//      "description" to project.description,
//      "apiVersion" to "1.20"
//    )
//    inputs.properties(props)
//    filesMatching("plugin.yml") {
//      expand(props)
//    }
//  }
}

// Let's keep it simple for testing
tasks.register("copyPluginJar") {
  dependsOn("reobfJar")
  doLast {
    copy {
      from(tasks.reobfJar.get().outputs)
      into(file("server/plugins"))
    }
  }
}

// Add a new task to build and deploy to server
tasks.register("deployToServer") {
  dependsOn("build")
  doLast {
    copy {
      from("build/libs/Sectors-unspecified-reobf.jar")
      into("run/plugins")
      rename { "Sectors.jar" } // Use a consistent name
    }
    println("Plugin deployed to run/plugins directory")
  }
}
