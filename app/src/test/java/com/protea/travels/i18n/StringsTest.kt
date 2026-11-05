package com.protea.travels.i18n

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Modifier

class StringsTest {

    private fun allValues(strings: AppStrings): Map<String, String> =
        AppStrings::class.java.declaredFields
            .filter { !Modifier.isStatic(it.modifiers) && it.type == String::class.java }
            .associate { f -> f.isAccessible = true; f.name to (f.get(strings) as String) }

    @Test fun `there are at least two South African languages besides English`() {
        val codes = Language.entries.map { it.code }
        assertTrue("en" in codes)
        assertTrue("zu" in codes) // isiZulu
        assertTrue("af" in codes) // Afrikaans
    }

    @Test fun `every language fills in every text`() {
        for (lang in Language.entries) {
            val values = allValues(stringsFor(lang))
            assertTrue("AppStrings should have many fields", values.size > 50)
            values.forEach { (field, text) -> assertTrue("${lang.displayName}: '$field' is blank", text.isNotBlank()) }
        }
    }

    @Test fun `stringsFor returns the right table for each language`() {
        assertSame(English, stringsFor(Language.ENGLISH))
        assertSame(Zulu, stringsFor(Language.ZULU))
        assertSame(Afrikaans, stringsFor(Language.AFRIKAANS))
    }

    @Test fun `translations really differ from English`() {
        for (other in listOf(Zulu, Afrikaans)) {
            assertNotEquals(English.signIn, other.signIn)
            assertNotEquals(English.settings, other.settings)
            assertNotEquals(English.searchFlights, other.searchFlights)
            assertNotEquals(English.welcomeBack, other.welcomeBack)
        }
    }

    @Test fun `known translations are correct`() {
        assertEquals("Sign In", English.signIn)
        assertEquals("Ngena", Zulu.signIn)
        assertEquals("Meld Aan", Afrikaans.signIn)
        assertEquals("Instellings", Afrikaans.settings)
    }

    @Test fun `language codes are the ones saved to the profile`() {
        assertEquals("en", Language.ENGLISH.code)
        assertEquals("zu", Language.ZULU.code)
        assertEquals("af", Language.AFRIKAANS.code)
    }

    @Test fun `every language can be found again from its saved code`() {
        for (lang in Language.entries) assertSame(lang, Language.entries.first { it.code == lang.code })
    }
}
