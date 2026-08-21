package com.kieronquinn.app.smartspacer.plugin.travel.resources

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.regex.Pattern

/**
 * Ensures every plugin ships complete English and Chinese string resources: every key present in
 * `values/strings.xml` must exist in `values-zh/strings.xml` and vice versa.
 */
class StringResourcesConsistencyTest {

    private val plugins = listOf(
        "plugin-travel-suggestion",
        "plugin-parcel",
        "plugin-check-in",
        "plugin-water",
        "plugin-medication",
        "plugin-food"
    )

    private fun keysOf(file: File): Set<String> {
        val pattern = Pattern.compile("<string name=\"([^\"]+)\"")
        val matcher = pattern.matcher(file.readText(Charsets.UTF_8))
        val keys = mutableSetOf<String>()
        while (matcher.find()) {
            keys.add(matcher.group(1))
        }
        return keys
    }

    @Test
    fun `every plugin ships complete en and zh string resources`() {
        for (plugin in plugins) {
            val en = File("../$plugin/src/main/res/values/strings.xml")
            val zh = File("../$plugin/src/main/res/values-zh/strings.xml")
            assertTrue("$plugin: values/strings.xml must exist", en.exists())
            assertTrue("$plugin: values-zh/strings.xml must exist", zh.exists())

            val enKeys = keysOf(en)
            val zhKeys = keysOf(zh)

            assertEquals(
                "$plugin: keys missing in values-zh/strings.xml",
                emptySet<String>(),
                enKeys - zhKeys
            )
            assertEquals(
                "$plugin: keys missing in values/strings.xml",
                emptySet<String>(),
                zhKeys - enKeys
            )
        }
    }

    @Test
    fun `new user-visible strings are non-empty in both locales`() {
        for (plugin in plugins) {
            val en = File("../$plugin/src/main/res/values/strings.xml")
            val zh = File("../$plugin/src/main/res/values-zh/strings.xml")
            val enText = en.readText(Charsets.UTF_8)
            val zhText = zh.readText(Charsets.UTF_8)
            for (key in keysOf(en)) {
                val enValue = Pattern.compile("<string name=\"$key\">([^<]*)</string>").matcher(enText)
                val zhValue = Pattern.compile("<string name=\"$key\">([^<]*)</string>").matcher(zhText)
                assertTrue("$plugin: key $key present in en", enValue.find())
                assertTrue("$plugin: key $key present in zh", zhValue.find())
                assertTrue("$plugin: en value for $key must not be empty", enValue.group(1).isNotBlank())
                assertTrue("$plugin: zh value for $key must not be empty", zhValue.group(1).isNotBlank())
            }
        }
    }
}
