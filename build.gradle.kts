import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    id("org.jetbrains.intellij.platform") version "2.2.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

group = "dev.maxblack"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3.2")
        bundledPlugin("com.intellij.java")
        instrumentationTools()
        testFramework(TestFrameworkType.Platform)
    }

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("io.mockk:mockk:1.13.12")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

intellijPlatform {
    pluginConfiguration {
        name = "LearnShortcuts"
        version = "0.1.0"

        description = """
            <![CDATA[
            <b>LearnShortcuts</b> — Master every IntelliJ IDEA keyboard shortcut through
            interactive, gamified practice sessions.<br/><br/>
            <ul>
              <li>🎯 Practice all IntelliJ IDEA shortcuts <b>in context</b> — see what each one actually does</li>
              <li>📊 Track progress with detailed <b>statistics</b> across sessions</li>
              <li>🔀 <b>Popularity-ordered</b> or <b>random</b> practice modes</li>
              <li>👁 <b>Reveal</b> button — peek the answer without losing your streak</li>
              <li>🔗 <b>Grouped shortcut sequences</b> (e.g. Extend → Shrink selection)</li>
              <li>💻 Works on <b>Mac, Linux, Windows</b>, and <b>Chrome OS</b></li>
            </ul>
            Shortcuts are loaded dynamically from your current active keymap —
            so every practised binding is exactly the one bound on your machine.
            ]]>
        """.trimIndent()

        changeNotes = """
            <![CDATA[
            <b>0.1.0</b> — Initial release<br/>
            <ul>
              <li>Core practice engine with session management</li>
              <li>Full shortcut catalog with popularity ranking</li>
              <li>Dynamic keymap extraction (works on all OS)</li>
              <li>Reveal-and-retry logic</li>
              <li>Statistics tracking across sessions</li>
              <li>Settings: popularity / random / category order</li>
            </ul>
            ]]>
        """.trimIndent()

        ideaVersion {
            sinceBuild = "233"
            untilBuild = "243.*"
        }

        vendor {
            name = "MaxBlack"
            url = "https://github.com/MaxBlack-dev"
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    buildSearchableOptions {
        enabled = false // Speed up dev builds; re-enable before publishing
    }
}

kotlin {
    jvmToolchain(17)
}
