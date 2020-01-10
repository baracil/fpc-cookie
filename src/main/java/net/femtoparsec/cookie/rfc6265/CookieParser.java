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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.femtoparsec.cookie.Cookie;
import net.femtoparsec.cookie.RequestInfo;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Predicate;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieParser {

    @NonNull
    public static CookieParser create(@NonNull RequestInfo request) {
        return new CookieParser(request, null);
    }

    @NonNull
    public static CookieParser create(@NonNull RequestInfo request, @NonNull Predicate<String> publicSuffixTester) {
        return new CookieParser(request,publicSuffixTester);
    }

    @NonNull
    private final RequestInfo requestInfo;

    private final Predicate<String> publicSuffixTester;

    private final Instant creationDate = Instant.now();

    @NonNull
    public Optional<Cookie> parse(@NonNull String setCookieString) {
        return SetCookieStringParser.parse(setCookieString).flatMap(this::finalizeCookie);
    }

    @NonNull
    private Optional<Cookie> finalizeCookie(@NonNull CookieData info) {
        final Cookie.Builder builder = Cookie.builder();

        builder.name(info.name())
               .value(info.value())
               .creationTime(creationDate)
               .lastAccessTime(creationDate)
               .securedOnly(info.secured())
               .httpOnly(info.httpOnly());

        if (info.maxAge() != null) {
            builder.expiryTime(creationDate.plusSeconds(info.maxAge()));
        } else if (info.expires() != null) {
            builder.expiryTime(info.expires());
        }

        String domain = computeDomain(info.domain());
        if (domain == null) {
            return Optional.empty();
        }
        if (domain.isEmpty()) {
            builder.hostOnly(true);
            builder.domain(requestInfo.hostName());
        }
        else {
            builder.hostOnly(false);
            builder.domain(domain);
        }

        if (info.path() == null) {
            builder.path(requestInfo.defaultPath());
        } else {
            builder.path(info.path());
        }

        if (info.httpOnly() && !requestInfo.http()) {
            return Optional.empty();
        }

        return Optional.of(builder.build());

    }

    private String computeDomain(String parsedDomain) {
        final String domain;
        if (parsedDomain != null) {
            if (publicSuffixTester != null && publicSuffixTester.test(parsedDomain)) {
                if (parsedDomain.equals(requestInfo.hostName())) {
                    domain = "";
                } else {
                    return null;
                }
            } else {
                domain = parsedDomain;
            }
        } else {
            domain = "";
        }

        if (!domain.isEmpty() &&  !requestInfo.doesDomainMatch(domain)) {
            return null;
        }
        return domain;
    }


}
