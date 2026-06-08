/*
 * Copyright (C) 2022 thepro1604
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package io.github.thepro1604.advancedchatcore.util;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class FindPair {

    FindType type;
    String string;

}
