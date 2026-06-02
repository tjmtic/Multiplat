package com.abyxcz.composeforms.migration

import kotlin.test.Test
import kotlin.test.assertEquals

class ExtractJsonTest {
    @Test
    fun stripsCodeFencesAndProse() {
        val raw = "Sure! Here you go:\n```json\n{\"renames\":{\"a\":\"b\"}}\n```\nHope that helps."
        assertEquals("{\"renames\":{\"a\":\"b\"}}", extractJson(raw))
    }

    @Test
    fun handlesNestedBraces() {
        val raw = "{\"constants\":{\"x\":1}} trailing"
        assertEquals("{\"constants\":{\"x\":1}}", extractJson(raw))
    }
}
