import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

plugins {
    `maven-publish`
}

@Serializable
data class Publish(
    val modules: List<Module>
)

@Serializable
data class Module(
    val name: String,
    val artifactId: String,
    val version: String
)

var json = Json { ignoreUnknownKeys = true }
var publishConf = json.decodeFromString<Publish>(rootProject.file("publish.json").readText())

val publishMavenURL = project.findProperty("mavenURL") ?: "https://maven.pkg.github.com/yihuier/core-ng-project"
val publishMavenAccessToken: String? = project.findProperty("mavenAccessToken")?.toString() ?: System.getenv("MAVEN_ACCESS_TOKEN")

publishConf.modules.forEach { module ->
    project(module.name) {
        pluginManager.apply(MavenPublishPlugin::class.java)

        publishing {
            publications {
                register("apiInterface", MavenPublication::class) {
                    groupId = "xyz.yihuier"
                    artifactId = module.artifactId
                    version = module.version
                    from(components["java"])
                }
            }
            repositories {
                maven {
                    url = uri(publishMavenURL)
                    credentials {
                        username = "yihuier"
                        password = publishMavenAccessToken
                    }
                }
            }
        }
    }
}

