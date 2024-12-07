pluginManagement {
    repositories {
//        google()
        maven {url=uri("https://maven.aliyun.com/repository/central")}
        maven {url=uri("https://maven.aliyun.com/repository/jcenter")}
        maven {url=uri("https://maven.aliyun.com/repository/google")}
        maven {url=uri("https://maven.aliyun.com/repository/gradle-plugin")}
        maven {url=uri("https://maven.aliyun.com/repository/public")}
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        google()
        maven {url=uri("https://maven.aliyun.com/repository/central")}
        maven {url=uri("https://maven.aliyun.com/repository/jcenter")}
        maven {url=uri("https://maven.aliyun.com/repository/google")}
        maven {url=uri("https://maven.aliyun.com/repository/gradle-plugin")}
        maven {url=uri("https://maven.aliyun.com/repository/public")}
        mavenCentral()
    }
}

rootProject.name = "RedsysA90ProKeyPOS"
include(":app")
include (":msgdialog")
 