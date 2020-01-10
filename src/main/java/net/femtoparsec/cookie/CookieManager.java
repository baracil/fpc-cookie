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

package net.femtoparsec.cookie;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.femtoparsec.cookie.rfc6265.CookieParser;
import net.femtoparsec.cookie.rfc6265.InMemoryCookieStore;

import java.net.CookieHandler;
import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieManager extends CookieHandler {

    /**
     * Create a new cookie manager using the provided cookie store
     * @param cookieStore the cookie store to use
     * @return a {@link CookieHandler} that can be used with {@link CookieHandler#setDefault(CookieHandler)}
     */
    @NonNull
    public static CookieManager create(@NonNull CookieStore cookieStore) {
        return new CookieManager(cookieStore, null);
    }

    /**
     * Create a new cookie manager using the provided cookie store
     * @param cookieStore the cookie store to use
     * @param publicSuffixTester a predicate to test if domain of a cookie is a public suffix
     * @return a {@link CookieHandler} that can be used with {@link CookieHandler#setDefault(CookieHandler)}
     */
    @NonNull
    public static CookieManager create(@NonNull CookieStore cookieStore, @NonNull Predicate<String> publicSuffixTester) {
        return new CookieManager(cookieStore,publicSuffixTester);
    }

    /**
     * Create a new cookie manager using the default cookie store
     * @return a {@link CookieHandler} that can be used with {@link CookieHandler#setDefault(CookieHandler)}
     */
    @NonNull
    public static CookieManager create() {
        return create(new InMemoryCookieStore());
    }

    /**
     * Create a new cookie manager using the default cookie store
     * @param publicSuffixTester a predicate to test if domain of a cookie is a public suffix
     * @return a {@link CookieHandler} that can be used with {@link CookieHandler#setDefault(CookieHandler)}
     */
    @NonNull
    public static CookieManager create(@NonNull Predicate<String> publicSuffixTester) {
        return create(new InMemoryCookieStore(),publicSuffixTester);
    }


    @NonNull
    private final CookieStore cookieStore;

    private final Predicate<String> publicSuffixTester;


    @Override
    public Map<String,List<String>> get(URI uri, Map<String,List<String>> requestHeaders) {
        if (uri == null) {
            return Map.of();
        }

        final RequestInfo requestInfo = RequestInfo.create(uri);
        final Instant now = Instant.now();

        final List<Cookie> cookies = cookieStore.getCookies(requestInfo,now);

        if (cookies.isEmpty()) {
            return Map.of();
        }

        final String cookieHeader = cookies.stream()
                                           .sorted(Cookie.PATH_COMPARATOR)
                                           .map(Cookie::formHeaderString)
                                           .collect(Collectors.joining("; "));

        return Map.of("Cookie", List.of(cookieHeader));
    }

    @Override
    public void put(URI uri, Map<String,List<String>> responseHeaders) {
        if (uri == null || responseHeaders == null) {
            return;
        }

        final RequestInfo requestInfo = RequestInfo.create(uri);
        final Instant now = Instant.now();
        final CookieParser parser = createCookieParserForRequest(requestInfo);

        responseHeaders.entrySet().stream()
                       .filter(e -> "Set-Cookie".equalsIgnoreCase(e.getKey()))
                       .map(Map.Entry::getValue)
                       .flatMap(Collection::stream)
                       .map(parser::parse)
                       .flatMap(Optional::stream)
                       .forEach(c -> cookieStore.addCookie(requestInfo,c,now));

    }

    @NonNull
    private CookieParser createCookieParserForRequest(@NonNull RequestInfo requestInfo) {
        return publicSuffixTester==null?CookieParser.create(requestInfo):CookieParser.create(requestInfo,publicSuffixTester);
    }
}
