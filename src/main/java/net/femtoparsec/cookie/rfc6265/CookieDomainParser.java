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

import java.util.Optional;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieDomainParser {

    @NonNull
    public static Optional<String> parse(@NonNull String value) {
        return new CookieDomainParser(value.strip()).parse();
    }

    @NonNull
    private final String domain;

    @NonNull
    private Optional<String> parse() {
        if (domain.isEmpty()) {
            return Optional.empty();
        }
        if (domain.startsWith(".")) {
            return Optional.of(domain.substring(1).toLowerCase());
        }
        return Optional.of(domain.toLowerCase());

    }
}
