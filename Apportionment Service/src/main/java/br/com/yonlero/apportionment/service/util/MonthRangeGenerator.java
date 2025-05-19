package br.com.yonlero.apportionment.service.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MonthRangeGenerator {

    public static List<YearMonth> generateYearMonths(LocalDate startDate, LocalDate endDate) {
        List<YearMonth> months = new ArrayList<>();

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("StartDate cannot be after endDate.");
        }

        YearMonth start = YearMonth.from(startDate);
        YearMonth end = YearMonth.from(endDate);

        YearMonth current = start;

        while (!current.isAfter(end)) {
            months.add(current);
            current = current.plusMonths(1);
        }

        return months;
    }
}