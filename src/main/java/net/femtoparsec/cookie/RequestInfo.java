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

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import net.femtoparsec.cookie.rfc6265.CookieOwnershipTester;

import java.net.URI;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestInfo {

    private final String hostName;

    private final boolean http;

    private final boolean secured;

    private final String defaultPath;

    private final CookieOwnershipTester ownershipTester = new CookieOwnershipTester(this);

    public boolean doesDomainMatch(@NonNull String domain) {
        final String lowerDomain = domain.toLowerCase();
        //TODO check hostname is an IP
        if (hostName.equals(lowerDomain)) {
            return true;
        }
        return hostName.endsWith(lowerDomain) && hostName.charAt(hostName.length()-lowerDomain.length()-1) == '.';
    }

    public boolean isMyCookie(@NonNull Cookie cookie) {
        return ownershipTester.isMyCookie(cookie);
    }


    @NonNull
    public static RequestInfo create(@NonNull URI uri) {
        final String host = uri.getHost().toLowerCase();
        final String scheme = uri.getScheme();
        final boolean secured = "https".equalsIgnoreCase(scheme) || "javascripts".equalsIgnoreCase(scheme);
        final boolean http = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);


        final String path = uri.getPath();
        final String defaultPath = (path == null || path.isEmpty())?"/":path.toLowerCase();
        final String hostName = host.startsWith("www.")?host.substring("www.".length()):host;

        return new RequestInfo(hostName,http,secured, defaultPath);
    }


}
