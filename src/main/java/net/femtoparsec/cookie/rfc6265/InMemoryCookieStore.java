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

import lombok.NonNull;
import lombok.Synchronized;
import net.femtoparsec.cookie.Cookie;
import net.femtoparsec.cookie.CookieStore;
import net.femtoparsec.cookie.RequestInfo;

import java.time.Instant;
import java.util.*;

public class InMemoryCookieStore implements CookieStore {

    @NonNull
    private final Map<Cookie,Cookie> cookies = new HashMap<>();

    @NonNull
    @Override
    @Synchronized
    public List<Cookie> getAllCookies() {
        return new ArrayList<>(cookies.values());
    }

    @Override
    @Synchronized
    public void initialize(@NonNull Collection<Cookie> cookies) {
        this.cookies.clear();
        cookies.forEach(c -> this.cookies.put(c,c));
    }

    @Override
    @Synchronized
    public void cleanUp(@NonNull Instant now) {
        cookies.entrySet().removeIf(e -> e.getValue().isExpired(now));
    }

    @Override
    @Synchronized
    public void clean() {
        this.cookies.clear();
    }

    @Override
    @Synchronized
    public void remove(@NonNull RequestInfo requestInfo) {
        cookies.values().removeIf(requestInfo::isMyCookie);
    }

    @Override
    @Synchronized
    public @NonNull List<Cookie> getCookies(@NonNull RequestInfo requestInfo, @NonNull Instant now) {
        final List<Cookie> result = new ArrayList<>();
        final Iterator<Cookie> itr = cookies.values().iterator();
        while (itr.hasNext()) {
            final Cookie cookie = itr.next();
            if (cookie.isExpired(now)) {
                itr.remove();
            } else if (requestInfo.isMyCookie(cookie)) {
                final Cookie updated = cookie.withLastAccessTime(now);
                result.add(cookie);
            }
        }
        return result;
    }

    @Override
    @Synchronized
    public void addCookie(@NonNull RequestInfo requestInfo, @NonNull Cookie cookie, @NonNull Instant now) {
        if (cookie.isExpired(now)) {
            cookies.remove(cookie);
            return;
        }

        final Cookie oldCookie = cookies.get(cookie);
        if (oldCookie == null) {
            cookies.put(cookie,cookie);
        } else {
            if (oldCookie.httpOnly() && !requestInfo.http()) {
                return;
            }
            final Cookie newCookie = cookie.withCreationTime(oldCookie.creationTime());
            if (!newCookie.isExpired(now)) {
                cookies.put(cookie,newCookie);
            }
        }
    }

}
