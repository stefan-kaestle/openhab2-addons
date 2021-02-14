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

import java.net.InetAddress;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link BridgeDiscoveryParticipant} is responsible discovering the
 * Bosch Smart Home Controller as a Bridge with the mDNS services.
 *
 * @author Gerd Zanker - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "discovery.boschsmarthomebridge")
public class BridgeDiscoveryParticipant implements MDNSDiscoveryParticipant {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(BoschSHCBindingConstants.THING_TYPE_SHC);

    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryParticipant.class);
    private final HttpClient httpClient;

    public BridgeDiscoveryParticipant() {
        // create http client upfront to later get public information from SHC
        SslContextFactory sslContextFactory = new SslContextFactory.Client.Client(true); // Accept all certificates
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);
        httpClient = new HttpClient(sslContextFactory);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo serviceInfo) {
        logger.debug("Bridge Discovery started for {}", serviceInfo);

        @Nullable
        final ThingUID uid = getThingUID(serviceInfo);
        if (uid == null) {
            return null;
        }

        String ipAddress = uid.getId().replace('-', '.');
        logger.info("Discovered Bosch Smart Home Controller at {}", ipAddress);

        return DiscoveryResultBuilder.create(uid).withProperty("ipAddress", ipAddress)
                .withLabel("Bosch Smart Home Controller").withTTL(60 * 60 * 24).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        @Nullable
        String ipAddress = getBridgeAddress(serviceInfo);
        if (ipAddress != null) {
            return new ThingUID(BoschSHCBindingConstants.THING_TYPE_SHC, ipAddress.replace('.', '-'));
        }
        return null;
    }

    private @Nullable String getBridgeAddress(ServiceInfo serviceInfo) {
        logger.trace("Discovering serviceInfo {}", serviceInfo);

        for (InetAddress address : serviceInfo.getInetAddresses()) {
            logger.trace("Discovering InetAddress {}", address);
            @Nullable
            String bridgeAddress = getBridgeAddress(address.getHostAddress());
            if (bridgeAddress != null) {
                return bridgeAddress;
            }
        }

        return null;
    }

    private @Nullable String getBridgeAddress(String ipAddress) {

        String url = String.format("https://%s:8446/smarthome/public/information", ipAddress);
        logger.trace("Discovering ipAddress {}", url);
        try {
            httpClient.start();
            ContentResponse contentResponse = httpClient.newRequest(url).method(HttpMethod.GET).send();
            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Discovering failed with status code: {}", contentResponse.getStatus());
                return null;
            }

            String content = contentResponse.getContentAsString();
            logger.debug("Discovered SHC - public info {}", content);
            @Nullable
            PublicInformation versionInfo = gson.fromJson(content, PublicInformation.class);
            if (versionInfo != null) {
                return versionInfo.shcIpAddress;
            }
            return null;

        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("Discovering failed with exception", e);
            return null;
        } catch (Exception e) {
            logger.trace("Discovering failed in http client start", e);
            return null;
        }
    }
}
