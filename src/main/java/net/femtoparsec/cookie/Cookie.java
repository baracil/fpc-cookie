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

import lombok.*;

import java.time.Instant;
import java.util.Comparator;

@RequiredArgsConstructor
@Getter
@Builder(builderClassName = "Builder",toBuilder = true)
@EqualsAndHashCode(of = {"name","domain","path"})
public class Cookie {

    public static final Comparator<Cookie> PATH_COMPARATOR = Comparator.comparingInt(Cookie::pathLength)
                                                                       .thenComparing(Cookie::creationTime);

    @NonNull
    private final Instant creationTime;

    @NonNull
    private final Instant lastAccessTime;

    @NonNull
    private final String name;

    @NonNull
    private final String domain;

    @NonNull
    private final String path;

    @NonNull
    private final String value;

    private final Instant expiryTime;

    private final boolean securedOnly;

    private final boolean httpOnly;

    private final boolean hostOnly;

    public int pathLength() {
        return path.length();
    }

    public boolean isPersistent() {
        return expiryTime != null;
    }

    public boolean isExpired(@NonNull Instant now) {
        return expiryTime != null && now.isAfter(expiryTime);
    }

    /**
     * @param creationTime the new creation time
     * @return a new Cookie with the exact same properties as this cookie except for the creation time that is equal to the provided one
     */
    @NonNull
    public Cookie withCreationTime(@NonNull Instant creationTime) {
        if (creationTime.equals(this.creationTime)) {
            return this;
        }
        return toBuilder().creationTime(creationTime).build();
    }

    /**
     * @param lastAccessTime the new last access time
     * @return a new Cookie with the exact same properties as this cookie except for the last access time that is equal to the provided one
     */
    @NonNull
    public Cookie withLastAccessTime(@NonNull Instant lastAccessTime) {
        if (lastAccessTime.equals(this.lastAccessTime)) {
            return this;
        }
        return toBuilder().lastAccessTime(lastAccessTime).build();
    }

    @NonNull
    public String formHeaderString() {
        return name+"="+value;
    }
}
