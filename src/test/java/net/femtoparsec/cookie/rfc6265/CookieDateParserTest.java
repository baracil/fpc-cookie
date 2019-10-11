/*
 * MIT License
 *
 * Copyright (c) 2019 Bastien Aracil
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
 */

package net.femtoparsec.cookie.rfc6265;

import lombok.NonNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookieDateParserTest {

    @DataProvider(name = "cookiedates")
    public static Object[][] cookiedates() throws IOException {
        final List<Object[]> lines = new ArrayList<>();
        final URL url = CookieDateParserTest.class.getResource("date_sample.txt");

        try (BufferedReader isr = new BufferedReader(new InputStreamReader(url.openStream()))) {
            do {
                final String line = isr.readLine();
                if (line == null) {
                    break;
                }
                lines.add(new Object[]{line});
            } while (true);
        }
        return lines.toArray(Object[][]::new);
    }

    @Test(dataProvider = "cookiedates")
    public void testDateParsing(@NonNull String cookieDate) {
        final Optional<Instant> date =  CookieDateParser.parse(cookieDate);
        Assert.assertTrue(date.isPresent());
    }
}
