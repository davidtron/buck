/*
 * Copyright 2019-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.multitenant.service

import com.facebook.buck.core.model.AbstractRuleType
import com.facebook.buck.core.model.ImmutableCanonicalCellName
import com.facebook.buck.core.model.ImmutableUnconfiguredBuildTarget
import com.facebook.buck.core.model.RuleType
import com.facebook.buck.multitenant.fs.FsAgnosticPath
import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedSet
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Optional
import org.junit.Assert.assertEquals
import org.junit.Test

class BuildPackageTest {
    @Test fun canSerializeAndDeserializeBuildPackage() {
        val buildPackage = BuildPackage(
            buildFileDirectory = FsAgnosticPath.of("foo/bar"),
            rules = setOf(
                RawBuildRule(
                    targetNode = ServiceRawTargetNode(
                        buildTarget = ImmutableUnconfiguredBuildTarget.of(
                            ImmutableCanonicalCellName.of(Optional.of("cell")),
                            "//foo/bar",
                            "baz",
                            ImmutableSortedSet.of()
                        ),
                        ruleType = RuleType.of("java_library", AbstractRuleType.Kind.BUILD),
                        attributes = ImmutableMap.of("attr1", "va1", "attr2", "val2")
                    ),
                    deps = setOf(
                        ImmutableUnconfiguredBuildTarget.of(
                            ImmutableCanonicalCellName.of(
                                Optional.of("cell")),
                            "//foo/bar",
                                "baz_lib",
                                ImmutableSortedSet.of()
                        )
                    )
                )
            ),
            errors = listOf(BuildPackageParsingError(
                    message = "parsing error",
                    stacktrace = listOf("stack line 1", "stack line 2")
                ))
        )

        val stream = ByteArrayOutputStream()

        serializePackagesToStream(listOf(buildPackage), stream)

        val deserializedPackages = parsePackagesFromStream(
            ByteArrayInputStream(stream.toByteArray()), ::multitenantJsonToBuildPackageParser)

        assertEquals(1, deserializedPackages.size)
        assertEquals(buildPackage, deserializedPackages.first())
    }
}
