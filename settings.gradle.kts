pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
//        maven("https://mirrors.tools.huawei.com/maven")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
//        maven("https://mirrors.tools.huawei.com/maven")
    }

}

rootProject.name = "LTSDemo"
include(":app")

//include(":lts-android-sdk")
//project(":lts-android-sdk").projectDir=file("../android-sdk-lts/LTS/lts-android-sdk")
