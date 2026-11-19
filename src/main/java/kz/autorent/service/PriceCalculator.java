package kz.autorent.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Жалдау құнын есептеу.
 * 7 тәуліктен бастап — 10% жеңілдік, 14 тәуліктен бастап — 15% жеңілдік.
 */
public final class PriceCalculator {

    private PriceCalculator() {
    }

    public static BigDecimal discountPercent(long days) {
        if (days >= 14) {
            return new BigDecimal("15");
        }
        if (days >= 7) {
            return new BigDecimal("10");
        }
        return BigDecimal.ZERO;
    }

    public static BigDecimal calculate(BigDecimal pricePerDay, long days) {
        if (pricePerDay == null || days <= 0) {
            throw new IllegalArgumentException("Баға мен тәулік саны оң болуы керек");
        }
        BigDecimal base = pricePerDay.multiply(BigDecimal.valueOf(days));
        BigDecimal discount = base.multiply(discountPercent(days)).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return base.subtract(discount).setScale(2, RoundingMode.HALF_UP);
    }

    /** Кепілдік — екі тәуліктің құны. */
    public static BigDecimal deposit(BigDecimal pricePerDay) {
        return pricePerDay.multiply(new BigDecimal("2")).setScale(2, RoundingMode.HALF_UP);
    }
}
