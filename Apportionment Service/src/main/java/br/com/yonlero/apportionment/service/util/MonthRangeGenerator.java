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
            throw new IllegalArgumentException("Data de início não pode ser depois da data de fim.");
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