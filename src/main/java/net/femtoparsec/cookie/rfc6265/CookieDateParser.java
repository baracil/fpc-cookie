/*
 * MIT License
 *
 * Copyright (c) 2020 Bastien Aracil
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package net.femtoparsec.cookie.rfc6265;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieDateParser {

    @NonNull
    public static Optional<Instant> parse(@NonNull String cookieDate) {
        return new CookieDateParser(cookieDate).parse();
    }

    private static final Pattern DELIMITER = Pattern.compile("[\\x09\\x20-\\x2F\\x3B-\\x40\\x5B-\\x60\\x7B-\\x7E]");

    private static final Pattern TIME = Pattern.compile("(\\d{1,2}):(\\d{1,2}):(\\d{1,2})");
    private static final Pattern DAY_OF_MONTH = Pattern.compile("\\d{1,2}");
    private static final Pattern YEAR = Pattern.compile("\\d{2,4}");

    private static final Map<String,Month> MONTHS;

    static {
        MONTHS = Map.ofEntries(Map.entry("jan", Month.JANUARY), Map.entry("feb", Month.FEBRUARY),
                               Map.entry("mar", Month.MARCH), Map.entry("apr", Month.APRIL),
                               Map.entry("may", Month.MAY), Map.entry("jun", Month.JUNE),
                               Map.entry("jul", Month.JULY), Map.entry("aug", Month.AUGUST),
                               Map.entry("sep", Month.SEPTEMBER), Map.entry("oct", Month.OCTOBER),
                               Map.entry("nov", Month.NOVEMBER), Map.entry("dec", Month.DECEMBER));
    }

    private final String cookieDate;

    private Integer hour = null;
    private Integer minute = null;
    private Integer second = null;

    private Integer dayOfMonth = null;
    private Month month = null;
    private Integer year = null;

    private boolean invalid = false;

    @NonNull
    private Optional<Instant> parse() {
        final String[] dateTokens = DELIMITER.split(cookieDate);
        for (String dateToken : dateTokens) {
            if (invalid) {
                break;
            }
            parseOnToken(dateToken.strip());
        }

        if (invalid) {
            return Optional.empty();
        }
        if (hour == null || minute == null || second == null || dayOfMonth == null || month == null ||year == null) {
            return Optional.empty();
        }

        return Optional.of(
                ZonedDateTime.of(year, month.getValue(), dayOfMonth, hour, minute, second, 0, ZoneOffset.UTC).toInstant()
        );
    }

    private void parseOnToken(String token) {
        if (tryWithTime(token)) {
            return;
        }
        if (tryWithDayOfMonth(token)) {
            return;
        }
        if (tryWithMonth(token)) {
            return;
        }
        tryWithYear(token);
    }

    private boolean tryWithTime(String token) {
        if (hour != null) {
            return false;
        }
        final Matcher matcher = TIME.matcher(token);
        if (!matcher.matches()) {
            return false;
        }
        this.hour = Integer.parseInt(matcher.group(1));
        this.minute = Integer.parseInt(matcher.group(2));
        this.second = Integer.parseInt(matcher.group(3));

        if (hour>23 || minute>59 || second >59) {
            invalid = true;
        }

        return true;
    }

    private boolean tryWithDayOfMonth(String token) {
        if (dayOfMonth != null) {
            return false;
        }
        final Matcher matcher = DAY_OF_MONTH.matcher(token);
        if (!matcher.matches()) {
            return false;
        }
        this.dayOfMonth = Integer.parseInt(token);
        if (dayOfMonth < 1 || dayOfMonth > 31) {
            invalid = true;
        }
        return true;
    }

    private boolean tryWithMonth(String token) {
        if (month != null) {
            return false;
        }
        this.month = MONTHS.get(token.toLowerCase());
        return month != null;
    }

    private void tryWithYear(String token) {
        if (year != null) {
            return;
        }
        final Matcher matcher = YEAR.matcher(token);
        if (!matcher.matches()) {
            return;
        }
        this.year = Integer.parseInt(token);
        if (this.year >= 70 && this.year<=99) {
            this.year+=1900;
        }
        if (this.year>=0 && this.year <= 69) {
            this.year+=2000;
        }

        if (this.year < 1601) {
            invalid = true;
        }
    }

}
