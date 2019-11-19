/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

/**
 * @author Stefan Kästle
 *
 *
 *         {"@type":"room","id":"hz_1","iconId":"icon_room_bedroom","name":"Bedroom"},
 *
 */
public class Room {

    @SerializedName("@type")
    String type;

    String id;
    String name;
}
