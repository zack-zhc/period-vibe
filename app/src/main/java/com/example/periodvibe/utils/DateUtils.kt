package com.example.periodvibe.utils

import java.time.LocalDate
import java.time.temporal.ChronoUnit

object DateUtils {

    /**
     * 计算两个日期之间的天数差
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差（endDate - startDate）
     */
    @JvmStatic
    fun daysBetween(startDate: LocalDate, endDate: LocalDate): Int {
        return ChronoUnit.DAYS.between(startDate, endDate).toInt()
    }
}
