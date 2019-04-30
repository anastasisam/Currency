package com.anastasisam;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class UrlsConnectedWithCurrensyCourse {
    @Value("${currency.course.access.key}")
    private String accessKey;
    @Value("${currency.api}")
    private String currencyApi;
    @Value("${currency.latest.postfix}")
    private String latestPostfix;
    @Value("${currency.historical.postfix}")
    private String historicalPostfix;
    @Value("${currency.historical.date.format}")
    private String historicalDateFormat;

    public String getHistoricalRequestUrl(Long date) {
        return currencyApi + String.format(historicalPostfix,
                new SimpleDateFormat(historicalDateFormat).format(new Date(date)), accessKey);
    }

    public String getLatestRequestUrl() {
        return currencyApi + String.format(latestPostfix, accessKey);
    }
}
