/*
 * Copyright (c) 2016. Trevor Jones
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.trevjonez.avdgp

import com.trevjonez.avdgp.dsl.AvdExtension
import com.trevjonez.avdgp.dsl.NamedConfigurationGroup
import com.trevjonez.avdgp.tasks.InstallSystemImageTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import java.io.File
import java.util.Properties
import kotlin.reflect.KClass

class AvdPlugin : Plugin<Project> {
    companion object {
        const val GROUP = "Android Virtual Device Plugin"
    }

    lateinit var extension: AvdExtension

    private lateinit var logger: Logger

    override fun apply(project: Project) {
        extension = project.extensions.create("AVD", AvdExtension::class.java, project)

        logger = project.logger
        project.afterEvaluate {
            extension.configs
                    .fold(mutableMapOf<String, NamedConfigurationGroup>()) { set, config ->
                        set.apply { put(config.systemImageKey(), config) }
                    }
                    .forEach { (_, config) ->
                        project.createTask(
                                type = InstallSystemImageTask::class,
                                name = config.installTaskName(),
                                description = "Install/Update system image").apply {
                            sdkPath = File(lookupSdkPath(project))
                            api = config.avdConfig.api
                            abi = config.avdConfig.abi
                            type = config.avdConfig.type
                            acceptSdkLicense = extension.acceptAndroidSdkLicense
                            acceptSdkPreviewLicense = extension.acceptAndroidSdkPreviewLicense
                            autoUpdate = extension.autoUpdate
                        }
                    }
        }
    }

    private fun lookupSdkPath(project: Project): String {
        val localPropFile = File(project.projectDir, "local.properties")
        if (localPropFile.exists()) {

            val localProperties = Properties().apply {
                load(localPropFile.inputStream())
            }
            val sdkDir = localProperties.getProperty("sdk.dir")
            if (sdkDir != null && File(sdkDir).exists()) {
                logger.info("Using sdk.dir path for avd plugin: $sdkDir")
                return sdkDir
            }
        } else {
            logger.info("local.properties doesn't exist at ${localPropFile.absolutePath}")
        }

        val androidHome = System.getenv("ANDROID_HOME")
        if (androidHome != null && File(androidHome).exists()) {
            logger.info("Using android home path for avd plugin: $androidHome")
            return androidHome
        }

        throw IllegalStateException("Unable to find android sdk. Specify ANDROID_HOME env variable or sdk.dir in local.properties")
    }

    private fun <T : DefaultTask> Project.createTask(type: KClass<T>,
                                                     name: String,
                                                     group: String = GROUP,
                                                     description: String? = null,
                                                     dependsOn: List<Task>? = null): T {
        return type.java.cast(project.tasks.create(LinkedHashMap<String, Any>().apply {
            put("name", name)
            put("type", type.java)
            put("group", group)
            description?.let { put("description", it) }
            dependsOn?.let { put("dependsOn", it) }
        }))
    }
}