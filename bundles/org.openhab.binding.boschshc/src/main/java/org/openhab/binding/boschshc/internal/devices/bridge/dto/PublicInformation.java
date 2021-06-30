/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

/**
 * Public Information of the controller.
 *
 * Currently the ipAddress is used for discovery. More fields can be added on demand.
 *
 * Json example:
 * {
 * "apiVersions":["1.2","2.1"],
 * ...
 * "shcIpAddress":"192.168.1.2",
 * ...
 * }
 *
 * @author Gerd Zanker - Initial contribution
 */
public class PublicInformation {

    public String shcIpAddress;
}
