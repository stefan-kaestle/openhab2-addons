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
package org.openhab.binding.boschshc.internal.discovery;

import java.util.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingDiscoveryService} is responsible discover Bosch Smart Home things.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
public class ThingDiscoveryService extends AbstractDiscoveryService implements DiscoveryService, ThingHandlerService {
    private static final int SEARCH_TIME = 10;

    private final Logger logger = LoggerFactory.getLogger(ThingDiscoveryService.class);
    private @Nullable BridgeHandler bridgeHandler;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(
            BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH, BoschSHCBindingConstants.THING_TYPE_TWINGUARD,
            BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT, BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR,
            BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL, BoschSHCBindingConstants.THING_TYPE_THERMOSTAT,
            BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL, BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT);

    // @formatter:off
    private static final Map<String, String> DEVICEMODEL_TO_THING_MAP = Map.ofEntries(
            new AbstractMap.SimpleEntry<>("BBL", BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL.getId()),
            new AbstractMap.SimpleEntry<>("TWINGUARD", BoschSHCBindingConstants.THING_TYPE_TWINGUARD.getId()),
            new AbstractMap.SimpleEntry<>("PSM", BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH.getId()),
            new AbstractMap.SimpleEntry<>("PLUG_COMPACT", BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH.getId()),
            new AbstractMap.SimpleEntry<>("ROOM_CLIMATE_CONTROL", BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL.getId()),
            new AbstractMap.SimpleEntry<>("BWTH", BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT.getId())

// FIXME: map all supported openhab Binding Things to the unknown Bosch SHC names
//            new AbstractMap.SimpleEntry<>("???", BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT.getId())
//            new AbstractMap.SimpleEntry<>("???", BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR.getId())
//            new AbstractMap.SimpleEntry<>("???", BoschSHCBindingConstants.THING_TYPE_THERMOSTAT.getId())


// FUTURE IMPLEMENTATION: map all Bosch SHC Names to currently not implemented Openhab Binding Things

//            new AbstractMap.SimpleEntry<>("CAMERA_EYES", ?), // Eyes 360 outdoor camera
//            new AbstractMap.SimpleEntry<>("SD", ?), // smoke detector

//            new AbstractMap.SimpleEntry<>("VENTILATION_SERVICE", ?),
//            new AbstractMap.SimpleEntry<>("SMOKE_DETECTION_SYSTEM", ?),
//            new AbstractMap.SimpleEntry<>("PRESENCE_SIMULATION_SERVICE", ?),

//            new AbstractMap.SimpleEntry<>("HUE_BRIDGE", ?)
//            new AbstractMap.SimpleEntry<>("HUE_BRIDGE_MANAGER", ?)
//            new AbstractMap.SimpleEntry<>("HUE_LIGHT", ?)
//            new AbstractMap.SimpleEntry<>("HUE_LIGHT_ROOM_CONTROL", ?)

            );
    // @formatter:on

    public ThingDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Override
    public void activate() {
        logger.trace("activate");
        // TODO: Preparation for ongoing discovery while bridge is online
        // final BridgeHandler handler = this.bridgeHandler;
        // if (handler != null) {
        // handler.registerDiscoveryListener(this);
        // }
    }

    @Override
    public void deactivate() {
        logger.trace("DEactivate");
        // TODO: Preparation for ongoing discovery while bridge is online
        // final BridgeHandler handler = this.bridgeHandler;
        // if (handler != null) {
        // removeOlderResults(new Date().getTime(), handler.getThing().getUID());
        // handler.unregisterDiscoveryListener();
        // } else {
        // logger.debug("DEactivate called, but can't removeOlderResults because no UID available!!!");
        // }
    }

    @Override
    protected void startScan() {
        // use shcBridgeHandler to getDevices()
        try {
            final BridgeHandler bridgeHandler;
            if (this.bridgeHandler != null) {
                bridgeHandler = this.bridgeHandler;
                @Nullable
                ArrayList<Device> devices = bridgeHandler.getDevices();
                if (devices != null) {
                    for (Device device : devices) {
                        addDevice(device);
                    }
                }
            }
        } catch (InterruptedException e) {
            logger.trace("scan was interrupted");
        }
    }

    private void addDevice(Device device) {
        logger.debug("Discovering device {}", device.name);
        logger.trace("   Details for device {}:\n" + "- id: {}\n" + "- manufacturer: {}\n" + "- roomId: {}\n"
                + "- deviceModel: {}\n" + "- serial: {}\n" + "- name: {}\n" + "- status: {}\n" + "- childDeviceIds: {}",
                device.name, device.id, device.manufacturer, device.roomId, device.deviceModel, device.serial,
                device.name, device.status, device.childDeviceIds);

        final BridgeHandler bridgeHandler;
        if (this.bridgeHandler != null) {
            bridgeHandler = this.bridgeHandler;
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            @Nullable
            ThingTypeUID thingTypeUID = getThingTypeUID(device);
            if (thingTypeUID == null)
                return;

            ThingUID thingUID = getThingUID(device, bridgeUID, thingTypeUID);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperty("id", device.id).withBridge(bridgeUID)
                    // TODO add openhab "Location" based on the SHC "rooms",
                    // look necessary via the SHC id for rooms like "hz_2"
                    // .withRepresentationProperty(UNIQUE_ID)
                    .withLabel(device.name).build();

            logger.debug("Discovered device '{}' with thingUID={}, thingTypeUID={}, id={}", device.name, thingUID,
                    thingTypeUID, device.id);

            thingDiscovered(discoveryResult);
        }
    }

    private ThingUID getThingUID(Device device, ThingUID bridgeUid, ThingTypeUID thingTypeUID) {
        logger.trace("got getThingUID {} for device {}", device.id, device);
        return new ThingUID(thingTypeUID, bridgeUid, device.id.replace(':', '_'));
    }

    private @Nullable ThingTypeUID getThingTypeUID(Device device) {
        String thingTypeId = DEVICEMODEL_TO_THING_MAP.get(device.deviceModel);
        if (thingTypeId != null) {
            logger.trace("got thingTypeID {} for deviceModel {}", thingTypeId, device.deviceModel);
            return new ThingTypeUID(BoschSHCBindingConstants.BINDING_ID, thingTypeId);
        }
        logger.info(
                "The Bosch Smart Home binding doesn't support this deviceModel {}. Please request support for it in github.",
                device.deviceModel);
        return null;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof BridgeHandler) {
            logger.trace("Set bridge handler {}", handler);
            bridgeHandler = (BridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }
}
