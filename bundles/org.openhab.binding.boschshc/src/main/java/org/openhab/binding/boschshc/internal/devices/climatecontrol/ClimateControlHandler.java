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
package org.openhab.binding.boschshc.internal.devices.climatecontrol;

import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_SETPOINT_TEMPERATURE;
import static org.openhab.binding.boschshc.internal.BoschSHCBindingConstants.CHANNEL_TEMPERATURE;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.boschshc.internal.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlService;
import org.openhab.binding.boschshc.internal.services.roomclimatecontrol.RoomClimateControlServiceState;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelService;
import org.openhab.binding.boschshc.internal.services.temperaturelevel.TemperatureLevelServiceState;

import tec.uom.se.unit.Units;

/**
 * A virtual device which controls up to six Bosch Smart Home radiator thermostats in a room.
 * 
 * @author Christian Oeing (christian.oeing@slashgames.org)
 */
@NonNullByDefault
public final class ClimateControlHandler extends BoschSHCHandler {

    @NonNullByDefault({})
    private RoomClimateControlService roomClimateControlService;

    /**
     * Constructor.
     * 
     * @param thing The Bosch Smart Home device that should be handled.
     */
    public ClimateControlHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        super.createService(TemperatureLevelService.class, this::updateChannels, Arrays.asList(CHANNEL_TEMPERATURE));
        this.roomClimateControlService = super.createService(RoomClimateControlService.class, this::updateChannels,
                Arrays.asList(CHANNEL_SETPOINT_TEMPERATURE));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        switch (channelUID.getId()) {
            case CHANNEL_SETPOINT_TEMPERATURE:
                if (command instanceof QuantityType<?>) {
                    updateSetpointTemperature((QuantityType<?>) command);
                }
                break;
        }
    }

    /**
     * Updates the channels which are linked to the {@link TemperatureLevelService} of the device.
     * 
     * @param state Current state of {@link TemperatureLevelService}.
     */
    private void updateChannels(TemperatureLevelServiceState state) {
        super.updateState(CHANNEL_TEMPERATURE, state.getTemperatureState());
    }

    /**
     * Updates the channels which are linked to the {@link RoomClimateControlService} of the device.
     * 
     * @param state Current state of {@link RoomClimateControlService}.
     */
    private void updateChannels(RoomClimateControlServiceState state) {
        super.updateState(CHANNEL_SETPOINT_TEMPERATURE, state.getSetpointTemperatureState());
    }

    /**
     * Sets the desired temperature for the device.
     * 
     * @param quantityType Command which contains the new desired temperature.
     */
    private void updateSetpointTemperature(QuantityType<?> quantityType) {
        QuantityType<?> celsiusType = quantityType.toUnit(Units.CELSIUS);
        if (celsiusType == null) {
            logger.debug("Could not convert quantity command to celsius");
            return;
        }

        double setpointTemperature = celsiusType.doubleValue();
        this.roomClimateControlService.setState(new RoomClimateControlServiceState(setpointTemperature));
    }
}
