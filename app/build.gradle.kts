plugins {
    kotlin("js")
}

repositories {
    mavenCentral()
    maven { url = uri("https://dl.bintray.com/kotlin/kotlinx") }
}

dependencies {
    implementation(project(":htmlcanvas"))
    implementation(project(":keycommand"))
    implementation(project(":lifecycle"))
    implementation(project(":shape"))
    implementation(project(":monoboard"))
    implementation(project(":monobitmap"))
    implementation(project(":graphicsgeo"))
    implementation(project(":livedata"))
    implementation(project(":statemanager"))

    testImplementation(kotlin("test-js"))
}

kotlin {
    js(LEGACY) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }
}
