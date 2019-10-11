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

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum CookieAttribute {
    EXPIRES("expires", CookieDateParser::parse, CookieData::expires),
    MAX_AGE("max-age", CookieMaxAgeParser::parse, CookieData::maxAge),
    DOMAIN("domain", CookieDomainParser::parse, CookieData::domain),
    PATH("path", CookiePathParser::parse, CookieData::path),
    SECURE("secure", b -> b.secured(true)),
    HTTP_ONLY("httponly", b -> b.httpOnly(true)),
    ;

    @NonNull
    private final String attributeName;

    private final BiConsumer<CookieData, String> setter;

    CookieAttribute(@NonNull String attributeName,
            Consumer<CookieData> setter) {
        this.attributeName = attributeName;
        this.setter = (b,s) -> setter.accept(b);
    }

    <T> CookieAttribute(@NonNull String attributeName, @NonNull Function<? super String,? extends Optional<? extends T>> transformer, @NonNull
            BiConsumer<? super CookieData,? super T> setter) {
        this.attributeName = attributeName;
        this.setter = (b,s) -> {
            final Optional<? extends T> result = transformer.apply(s);
            result.ifPresent(v -> setter.accept(b,v));
        };
    }

    @NonNull
    public static Optional<CookieAttribute> find(@NonNull String name) {
        return Optional.ofNullable(Holder.ATTRIBUTE_BY_NAME.get(name.toLowerCase()));
    }

    public void handleValue(CookieData info, String value) {
        setter.accept(info, value);
    }

    private static class Holder {

        private static final Map<String,CookieAttribute> ATTRIBUTE_BY_NAME = Arrays.stream(values()).collect(
                Collectors.toMap(a -> a.attributeName,a -> a));

    }
}
