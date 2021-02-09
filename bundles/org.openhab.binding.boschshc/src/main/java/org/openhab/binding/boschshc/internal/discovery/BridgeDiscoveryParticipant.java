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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BoschSHCBindingConstants.THING_TYPE_SHC).collect(Collectors.toSet()));

    private final Gson gson = new Gson();
    private final Logger logger = LoggerFactory.getLogger(BridgeDiscoveryParticipant.class);

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
        logger.trace("ServiceInfo: {}", serviceInfo);

        final ThingUID uid = getThingUID(serviceInfo);
        if (uid == null) {
            return null;
        }

        Map<String, Object> properties = new HashMap<>(1);
        properties.put("ipAddress", uid.getId().replace('-', '.'));

        return DiscoveryResultBuilder.create(uid).withLabel("Smart Home Controller").withProperties(properties)
                .withTTL(60 * 60 * 24).build();
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo serviceInfo) {
        String ipAddress = getBridgeAddress(serviceInfo);
        if (ipAddress.isEmpty()) {
            return null;
        }
        return new ThingUID(BoschSHCBindingConstants.THING_TYPE_SHC, ipAddress.replace('.', '-'));
    }

    private String getBridgeAddress(ServiceInfo serviceInfo) {
        InetAddress[] addresses = serviceInfo.getInetAddresses();
        if (addresses.length == 0) {
            return "";
        }

        // Try to get public SHC info from address
        String ipAddress = addresses[0].getHostAddress();

        SslContextFactory sslContextFactory = new SslContextFactory.Client.Client(true); // Accept all certificates
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setValidateCerts(false);
        sslContextFactory.setValidatePeerCerts(false);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        HttpClient httpClient = new HttpClient(sslContextFactory);
        String url = String.format("https://%s:8446/smarthome/public/information", ipAddress);
        logger.trace("Discovering {}", url);
        try {
            httpClient.start();
            ContentResponse contentResponse = httpClient.newRequest(url).method(HttpMethod.GET).send();
            // check HTTP status code
            if (!HttpStatus.getCode(contentResponse.getStatus()).isSuccess()) {
                logger.debug("Discovering failed with status code: {}", contentResponse.getStatus());
                return "";
            }

            String content = contentResponse.getContentAsString();
            logger.info("Discovered SHC at address {}, public info {}", ipAddress, content);
            PublicInformation versionInfo = gson.fromJson(content, PublicInformation.class);
            return versionInfo.shcIpAddress;

        } catch (NullPointerException | InterruptedException | TimeoutException | ExecutionException e) {
            logger.trace("Discovering failed with exception: {}", e);
            return "";
        } catch (Exception e) {
            logger.trace("Discovering failed in http client start: {}", e);
            return "";
        }
    }
}
