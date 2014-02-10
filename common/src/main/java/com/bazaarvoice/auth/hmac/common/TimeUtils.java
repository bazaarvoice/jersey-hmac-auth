package com.bazaarvoice.auth.hmac.common;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

public class TimeUtils {

    public static String getCurrentTimestamp() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        return formatter.print(nowInUTC());
    }

    public static DateTime parse(String timestamp) {
        return DateTime.parse(timestamp, ISODateTimeFormat.dateTimeParser());
    }

    public static DateTime nowInUTC() {
        return new DateTime(DateTimeZone.UTC);
    }
}
