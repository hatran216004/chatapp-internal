package com.example.librarymanagement.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class Helpers {

    public static LocalDateTime dateToLoCalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
