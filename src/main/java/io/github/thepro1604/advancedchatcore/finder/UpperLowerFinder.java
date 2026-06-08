/*
 * Copyright (C) 2021 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.finder;

import java.util.regex.Pattern;

public class UpperLowerFinder extends PatternFinder {
    @Override
    public Pattern getPattern(String toMatch) {
        return Pattern.compile(Pattern.quote(toMatch), Pattern.CASE_INSENSITIVE);
    }
}
