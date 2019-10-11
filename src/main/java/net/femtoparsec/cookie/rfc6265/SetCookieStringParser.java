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
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class SetCookieStringParser {

    @NonNull
    public static Optional<CookieData> parse(@NonNull String setCookieString) {
        final CookieData info = new SetCookieStringParser(setCookieString.split(";")).parse();
        return Optional.ofNullable(info);
    }

    @NonNull
    private final String[] tokens;

    private final Map<String,String> attributes = new HashMap<>();

    private boolean skipped;


    private final CookieData info = new CookieData();

    private CookieData parse() {
        if (tokens.length == 0) {
            return null;
        }
        this.parseNameAndValue(tokens[0]);
        this.splitAllAttributes();
        for (Map.Entry<String,String> entry : attributes.entrySet()) {
            if (skipped) {
                break;
            }
            this.handleOneAttribute(entry.getKey(),entry.getValue());
        }

        return skipped?null:info;
    }

    private void parseNameAndValue(String nameValue) {
        final int equalIndex = nameValue.indexOf("=");
        if (equalIndex<=0) {
            skipped = true;
            return;
        }
        final String name = nameValue.substring(0,equalIndex).strip();
        final String value = nameValue.substring(equalIndex+1).strip();
        if (name.isEmpty()) {
            skipped = true;
            return;
        }
        info.name(name).value(value);
    }

    private void splitAllAttributes() {
        this.attributes.clear();
        for (int i = 1; i < tokens.length; i++) {
            this.splitOneAttribute(tokens[i]);
        }
    }

    private void splitOneAttribute(String token) {
        final int equalIndex = token.indexOf("=");
        final String name;
        final String value;
        if (equalIndex < 0) {
            name = token;
            value = "";
        } else {
            name = token.substring(0,equalIndex).strip();
            value = token.substring(equalIndex+1).strip();
        }
        attributes.put(name.toLowerCase(),value);
    }

    private void handleOneAttribute(String key, String value) {
        CookieAttribute.find(key)
                       .ifPresent(attribute -> attribute.handleValue(info, value));

    }



}
