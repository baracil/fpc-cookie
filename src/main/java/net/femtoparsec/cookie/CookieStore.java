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

package net.femtoparsec.cookie;

import lombok.NonNull;
import net.femtoparsec.cookie.rfc6265.InMemoryCookieStore;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A store of cookie. Cookies can be added and clean up
 */
public interface CookieStore {

    /**
     * @return a RFC 6265 cookie store that stores cookie in memory
     */
    @NonNull
    static CookieStore inMemory() {
        return new InMemoryCookieStore();
    }

    /**
     * @return all the cookies in the store
     */
    @NonNull
    List<Cookie> getAllCookies();

    /**
     * Initialize the store with the provided cookies
     * @param cookies the cookies to use as initial cookies
     */
    void initialize(@NonNull Collection<Cookie> cookies);

    /**
     * Retrieve the cookies associate to a request
     * @param requestInfo the information about the request
     * @param now the current time
     * @return the list of cookie to put in the header of the user-agent response
     */
    @NonNull
    List<Cookie> getCookies(@NonNull RequestInfo requestInfo, @NonNull Instant now);

    /**
     * Remove expired cookie
     * @param now the current time used to check cookie expiration
     */
    void cleanUp(@NonNull Instant now);

    /**
     * Add a cookie to the store
     * @param requestInfo the information of the request providing the cookie
     * @param cookie the cookie to add
     * @param now the current time
     */
    void addCookie(@NonNull RequestInfo requestInfo, @NonNull Cookie cookie, @NonNull Instant now);


    /**
     * Remove all the cookie that would have been returned by {@link #getCookies(RequestInfo, Instant)} with the provided <code>requestInfo</code>
     * as parameter
     *
     * @param requestInfo the information about the request
     */
    void remove(@NonNull RequestInfo requestInfo);

    /**
     * Remove all cookies from the store
     */
    default void clean() {
        initialize(Collections.emptyList());
    }


}
