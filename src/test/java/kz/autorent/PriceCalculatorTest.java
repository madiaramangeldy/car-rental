package kz.autorent;

import kz.autorent.service.PriceCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceCalculatorTest {

    private static final BigDecimal PRICE = new BigDecimal("10000");

    @Test
    void shortRentalHasNoDiscount() {
        assertEquals(new BigDecimal("30000.00"), PriceCalculator.calculate(PRICE, 3));
    }

    @Test
    void weekRentalHas10PercentDiscount() {
        // 7 * 10000 = 70000, -10% = 63000
        assertEquals(new BigDecimal("63000.00"), PriceCalculator.calculate(PRICE, 7));
    }

    @Test
    void twoWeeksRentalHas15PercentDiscount() {
        // 14 * 10000 = 140000, -15% = 119000
        assertEquals(new BigDecimal("119000.00"), PriceCalculator.calculate(PRICE, 14));
    }

    @Test
    void depositIsTwoDays() {
        assertEquals(new BigDecimal("20000.00"), PriceCalculator.deposit(PRICE));
    }

    @Test
    void zeroDaysIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PriceCalculator.calculate(PRICE, 0));
    }
}
