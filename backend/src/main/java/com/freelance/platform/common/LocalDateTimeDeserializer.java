package com.freelance.platform.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm:ss]");
    private static final DateTimeFormatter DATE_TIME_MS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim();
        if (value.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                String cleanValue = value;
                if (cleanValue.endsWith("Z")) {
                    cleanValue = cleanValue.substring(0, cleanValue.length() - 1);
                }
                if (cleanValue.contains(".")) {
                    return LocalDateTime.parse(cleanValue, DATE_TIME_MS_FORMATTER);
                }
                LocalDate date = LocalDate.parse(value, DATE_FORMATTER);
                return LocalDateTime.of(date, LocalTime.of(23, 59, 59));
            } catch (DateTimeParseException ex) {
                try {
                    return LocalDateTime.ofInstant(Instant.parse(value), ZoneId.systemDefault());
                } catch (DateTimeParseException isoEx) {
                    throw new IOException("Unable to parse date: " + value, ex);
                }
            }
        }
    }
}
