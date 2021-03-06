/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.system.listeners.velocity;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.db.Database;
import com.djrapitops.plan.db.access.transactions.events.GeoInfoStoreTransaction;
import com.djrapitops.plan.db.access.transactions.events.PlayerRegisterTransaction;
import com.djrapitops.plan.system.cache.GeolocationCache;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.info.server.ServerInfo;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.Processors;
import com.djrapitops.plan.system.settings.config.PlanConfig;
import com.djrapitops.plan.system.settings.paths.DataGatheringSettings;
import com.djrapitops.plan.system.webserver.cache.PageId;
import com.djrapitops.plan.system.webserver.cache.ResponseCache;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.util.UUID;

/**
 * Player Join listener for Velocity.
 * <p>
 * Based on the bungee version.
 *
 * @author MicleBrick
 */
@Singleton
public class PlayerOnlineListener {

    private final PlanConfig config;
    private final Processors processors;
    private final Processing processing;
    private final DBSystem dbSystem;
    private final GeolocationCache geolocationCache;
    private final SessionCache sessionCache;
    private final ServerInfo serverInfo;
    private final ErrorHandler errorHandler;

    @Inject
    public PlayerOnlineListener(
            PlanConfig config,
            Processing processing,
            Processors processors,
            DBSystem dbSystem,
            GeolocationCache geolocationCache,
            SessionCache sessionCache,
            ServerInfo serverInfo,
            ErrorHandler errorHandler
    ) {
        this.config = config;
        this.processing = processing;
        this.processors = processors;
        this.dbSystem = dbSystem;
        this.geolocationCache = geolocationCache;
        this.sessionCache = sessionCache;
        this.serverInfo = serverInfo;
        this.errorHandler = errorHandler;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        try {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            String name = player.getUsername();
            InetAddress address = player.getRemoteAddress().getAddress();
            long time = System.currentTimeMillis();

            sessionCache.cacheSession(playerUUID, new Session(playerUUID, serverInfo.getServerUUID(), time, null, null));

            Database database = dbSystem.getDatabase();

            boolean gatheringGeolocations = config.isTrue(DataGatheringSettings.GEOLOCATIONS);
            if (gatheringGeolocations) {
                database.executeTransaction(
                        new GeoInfoStoreTransaction(playerUUID, address, time, geolocationCache::getCountry)
                );
            }

            database.executeTransaction(new PlayerRegisterTransaction(playerUUID, () -> time, name));
            processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @Subscribe
    public void onLogout(DisconnectEvent event) {
        try {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();

            sessionCache.endSession(playerUUID, System.currentTimeMillis());
            processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
            ResponseCache.clearResponse(PageId.SERVER.of(serverInfo.getServerUUID()));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }

    @Subscribe
    public void onServerSwitch(ServerConnectedEvent event) {
        try {
            Player player = event.getPlayer();
            UUID playerUUID = player.getUniqueId();
            long time = System.currentTimeMillis();

            // Replaces the current session in the cache.
            sessionCache.cacheSession(playerUUID, new Session(playerUUID, serverInfo.getServerUUID(), time, null, null));

            processing.submit(processors.info().playerPageUpdateProcessor(playerUUID));
        } catch (Exception e) {
            errorHandler.log(L.WARN, this.getClass(), e);
        }
    }
}
