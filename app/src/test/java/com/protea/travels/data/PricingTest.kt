package com.protea.travels.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PricingTest {

    // ---------------- totalPrice ----------------
    @Test fun `return trip for two economy adults doubles the fare twice`() =
        assertEquals(9800, totalPrice(2450, 2, "Economy", "Return")) // 2450 x 2 people x 2 legs

    @Test fun `one-way for one economy adult is just the fare`() =
        assertEquals(2450, totalPrice(2450, 1, "Economy", "One-way"))

    @Test fun `business class applies the 2_5 multiplier`() =
        assertEquals(4725, totalPrice(1890, 1, "Business", "One-way"))

    @Test fun `premium economy applies the 1_6 multiplier without rounding errors`() =
        assertEquals(15680, totalPrice(2450, 2, "Premium Economy", "Return"))

    @Test fun `an unknown cabin falls back to the economy price`() =
        assertEquals(2450, totalPrice(2450, 1, "Mystery", "One-way"))

    @Test fun `price scales linearly with passengers`() {
        val one = totalPrice(2000, 1, "Economy", "One-way")
        assertEquals(one * 4, totalPrice(2000, 4, "Economy", "One-way"))
    }

    @Test fun `cabin multipliers are as designed`() {
        assertEquals(1.0, cabinMultiplier["Economy"]!!, 0.0)
        assertEquals(1.6, cabinMultiplier["Premium Economy"]!!, 0.0)
        assertEquals(2.5, cabinMultiplier["Business"]!!, 0.0)
    }

    // ---------------- money formatting ----------------
    @Test fun `rand amounts use a comma thousands separator`() {
        assertEquals("R2,450", money(2450, "ZAR"))
        assertEquals("R9,800", money(9800, "ZAR"))
        assertEquals("R999", money(999, "ZAR"))
        assertEquals("R1,234,567", money(1234567, "ZAR"))
    }

    @Test fun `dollar amounts convert at the app's approximate rate`() {
        assertEquals("US$132", money(2450, "USD"))  // 2450 / 18.5 = 132.4 -> 132
        assertEquals("US$529", money(9800, "USD"))
    }

    @Test fun `any other currency code is shown in rand`() =
        assertEquals("R2,450", money(2450, "EUR"))
}
