/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright 2020 Urs Joss.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.kordamp.gradle.plugin.base.ProjectsExtension

val sonarToken = System.getenv("SONAR_TOKEN") ?: "n.a."

config {
    release = rootProject.findProperty("release").toString().toBoolean()

    info {
        name = "Kordamp Kotlin Project"
        vendor = "none"
        description = "sample project to validate functionality with kotlin & gradle kotlin DSL configuration"

        links {
            website = "https://github.com/ursjoss/kordamp-gradle-test-suite"
            issueTracker = "https://github.com/ursjoss/kordamp-gradle-test-suite/issues"
            scm = "https://github.com/ursjoss/kordamp-gradle-test-suite.git"
        }

        people {
            person {
                id = "ursjoss"
                name = "Urs Joss"
                roles = listOf("developer")
            }
        }
    }

    licensing {
        enabled = true
        licenses {
            license {
                id = "Apache-2.0"
            }
        }
    }

    coverage {
        jacoco {
            includeProjectDependencies = true
        }
    }

    quality {
        detekt {
            ignoreFailures = false
            parallel = false
            buildUponDefaultConfig = false
        }

        sonar {
            hostUrl = "https://sonarcloud.io"
            login = sonarToken
            organization = "ursjoss-github"
            projectKey = "ursjoss_${project.name}"
        }
    }
}

val junitVersion: String by project

configure<ProjectsExtension> {
    all {
        path("*") {
            repositories {
                mavenLocal()
                mavenCentral()
                jcenter()
            }
        }
        path(":") {
            tasks {
                val aggregateDetekt by existing {
                    dependsOn(subprojects.map { it.tasks.getByName("detekt") })
                }
                named("sonarqube").configure {
                    dependsOn(aggregateDetekt)
                }
            }
        }
        dir("subprojects") {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
            }
            java {
                sourceCompatibility = JavaVersion.VERSION_11
                targetCompatibility = JavaVersion.VERSION_11
            }
            tasks {
                withType<KotlinCompile>().configureEach {
                    kotlinOptions.jvmTarget = JavaVersion.VERSION_11.majorVersion
                }
                withType<Test> {
                    useJUnitPlatform()
                    testLogging {
                        events("passed", "skipped", "failed")
                    }
                }
            }
        }
    }
}
