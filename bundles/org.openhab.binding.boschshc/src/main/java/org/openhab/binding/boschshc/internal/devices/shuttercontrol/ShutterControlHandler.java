/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.shuttercontrol;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_LEVEL;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.OperationState;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.ShutterControlService;
import org.openhab.binding.boschshc.internal.services.shuttercontrol.dto.ShutterControlServiceState;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;

/**
 * Handler for a shutter control device
 * 
 * @author Christian Oeing - Initial contribution
 */
@NonNullByDefault
public class ShutterControlHandler extends BoschSHCHandler {
    /**
     * Utility functions to convert data between Bosch things and openHAB items
     */
    static final class DataConversion {
        public static int levelToOpenPercentage(double level) {
            return (int) Math.round((1 - level) * 100);
        }

        public static double openPercentageToLevel(double openPercentage) {
            return (100 - openPercentage) / 100.0;
        }
    }

    private ShutterControlService shutterControlService;

    public ShutterControlHandler(Thing thing) {
        super(thing);
        this.shutterControlService = new ShutterControlService();
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        this.registerService(this.shutterControlService, this::updateChannels, Arrays.asList(CHANNEL_LEVEL));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (command instanceof UpDownType) {
            // Set full close/open as target state
            UpDownType upDownType = (UpDownType) command;
            ShutterControlServiceState state = new ShutterControlServiceState();
            if (upDownType == UpDownType.UP) {
                state.level = 1.0;
            } else if (upDownType == UpDownType.DOWN) {
                state.level = 0.0;
            } else {
                logger.warn("Received unknown UpDownType command: {}", upDownType);
                return;
            }
            this.shutterControlService.setState(state);
        } else if (command instanceof StopMoveType) {
            // Set STOPPED operation state
            ShutterControlServiceState state = new ShutterControlServiceState();
            state.operationState = OperationState.STOPPED;
            this.shutterControlService.setState(state);
        } else if (command instanceof PercentType) {
            // Set specific level
            PercentType percentType = (PercentType) command;
            double level = DataConversion.openPercentageToLevel(percentType.doubleValue());
            this.shutterControlService.setState(new ShutterControlServiceState(level));
        }
    }

    private void updateChannels(ShutterControlServiceState state) {
        if (state.level != null) {
            // Convert level to open ratio
            int openPercentage = DataConversion.levelToOpenPercentage(state.level);
            updateState(CHANNEL_LEVEL, new PercentType(openPercentage));
        }
    }
}
