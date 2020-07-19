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
 * {
 * "result": [
 * {
 * "path": "/devices/hdm:ZigBee:000d6f0004b95a62/services/LatestMotion",
 * "@type": "DeviceServiceData",
 * "id": "LatestMotion",
 * "state": {
 * "latestMotionDetected": "2020-04-03T19:02:19.054Z",
 * "@type": "latestMotionState"
 * },
 * "deviceId": "hdm:ZigBee:000d6f0004b95a62"
 * }
 * ],
 * "jsonrpc": "2.0"
 * }
 *
 */
public class LatestMotionState {

    @SerializedName("@type")
    String type;

    String latestMotionDetected;
}
