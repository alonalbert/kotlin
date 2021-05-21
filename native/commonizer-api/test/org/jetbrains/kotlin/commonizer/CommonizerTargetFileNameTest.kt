/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.jetbrains.kotlin.commonizer.HierarchicalCommonizerOutputLayout.fileName
import org.jetbrains.kotlin.commonizer.HierarchicalCommonizerOutputLayout.maxFileNameLength
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommonizerTargetFileNameTest {
    @Test
    fun `small targets will use identityString`() {
        val target = parseCommonizerTarget("((a, b), c)")
        assertEquals(target.identityString, target.fileName)
    }

    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun `large targets respect maximum filename length`() {
        val target = parseCommonizerTarget(
            buildString {
                append("(")
                append(
                    sequence {
                        var i = 0
                        while (true) {
                            yield(i.toString())
                            i++
                        }
                    }.take(maxFileNameLength).joinToString(", ")
                )
                append(")")
            }
        )

        assertTrue(
            target.identityString.length > maxFileNameLength,
            "Expected test target's identityString to exceed maxFileNameLength"
        )

        assertEquals(
            target.fileName.length, maxFileNameLength,
            "Expected test target's fileName to be exactly match the maximum"
        )
    }
}
