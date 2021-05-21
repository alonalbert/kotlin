/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.dsl

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.invocation.Gradle
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaToolchainSpec
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.utils.newInstance
import javax.inject.Inject

internal interface ToolchainSupport {
    fun applyToolchain(action: Action<JavaToolchainSpec>)

    companion object {
        internal fun createToolchain(
            objectFactory: ObjectFactory,
            gradle: Gradle,
            extensions: ExtensionContainer
        ): ToolchainSupport {
            val currentVersion = GradleVersion.version(gradle.gradleVersion)
            val gradleVersionWithToolchainSupport = GradleVersion.version("6.7")
            return when {
                currentVersion < gradleVersionWithToolchainSupport -> objectFactory.newInstance<NonExistingToolchainSupport>()
                else -> objectFactory.newInstance<DefaultToolchainSupport>(extensions)
            }
        }
    }
}

internal abstract class NonExistingToolchainSupport : ToolchainSupport {
    override fun applyToolchain(
        action: Action<JavaToolchainSpec>
    ) {
        throw GradleException("JavaToolchain support is only available from Gradle 6.7")
    }
}

internal abstract class DefaultToolchainSupport @Inject constructor(
    private val extensions: ExtensionContainer
) : ToolchainSupport {
    private val toolchainSpec: JavaToolchainSpec
        get() = extensions
            .getByType(JavaPluginExtension::class.java)
            .toolchain

    override fun applyToolchain(
        action: Action<JavaToolchainSpec>
    ) {
        action.execute(toolchainSpec)
    }
}
