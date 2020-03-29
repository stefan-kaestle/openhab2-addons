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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BoschSHCBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschSHCBindingConstants {

    private static final String BINDING_ID = "boschshc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHC = new ThingTypeUID(BINDING_ID, "shc");

    public static final ThingTypeUID THING_TYPE_INWALL_SWITCH = new ThingTypeUID(BINDING_ID, "in-wall-switch");
    public static final ThingTypeUID THING_TYPE_TWINGUARD = new ThingTypeUID(BINDING_ID, "twinguard");

    // List of all Channel ids
    public static final String CHANNEL_POWER_SWITCH = "power-switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
}
