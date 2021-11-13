package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.data.DataManager;
import eu.builderscoffee.api.common.data.tables.ServerActivityEntity;
import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.packets.types.common.BungeecordPacket;
import eu.builderscoffee.playpen.tasks.StaticServersTask;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.INetworkListener;
import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Server;
import io.playpen.core.plugin.EventManager;
import io.playpen.core.plugin.IPlugin;
import lombok.val;
import org.redisson.api.RSortedSet;

import java.util.ArrayList;
import java.util.Objects;

public class NetworkListener implements INetworkListener {
    @Override
    public void onPluginMessage(IPlugin plugin, String id, Object... args) {
        // Nothing to do here
    }

    @Override
    public void onNetworkStartup() {
        val entity = new ServerActivityEntity();
        entity.setServerName("playpen");
        entity.setMessage("Playpen started");
        DataManager.getServerActivityStore().insert(entity);
    }

    @Override
    public void onNetworkShutdown() {
        PlaypenUtils.getServers().forEach(server -> {
            val packet = new BungeecordPacket();
            packet.setHostName(server.getName());
            packet.setHostAddress(server.getProperties().get("ip"));
            packet.setHostAddress(server.getProperties().get("port"));
            packet.setServerStatus(BungeecordPacket.ServerStatus.STOPPED);
            Redis.publish(RedisTopic.BUNGEECORD, packet);
        });

        val entity = new ServerActivityEntity();
        entity.setServerName("playpen");
        entity.setMessage("Playpen stopped");
        DataManager.getServerActivityStore().insert(entity);
    }

    @Override
    public void onCoordinatorCreated(LocalCoordinator localCoordinator) {
        // Nothing to do here
    }

    @Override
    public void onCoordinatorSync(LocalCoordinator localCoordinator) {
        // Nothing to do here
    }

    @Override
    public void onRequestProvision(LocalCoordinator localCoordinator, Server server) {
        // Nothing to do here
    }

    @Override
    public void onProvisionResponse(LocalCoordinator localCoordinator, Server server, boolean success) {
        if(success){
            val packet = new BungeecordPacket();
            packet.setHostName(server.getName());
            packet.setHostAddress(server.getProperties().get("ip"));
            packet.setHostPort(Integer.parseInt(server.getProperties().get("port")));
            packet.setServerStatus(BungeecordPacket.ServerStatus.STARTED);
            Redis.publish(RedisTopic.BUNGEECORD, packet);

            final RSortedSet<eu.builderscoffee.api.common.redisson.infos.Server> servers = Redis.getRedissonClient().getSortedSet("servers");

            if(Objects.nonNull(servers) && servers.size() > 0 && !servers.stream().anyMatch(s -> server.getName().equalsIgnoreCase(s.getHostName()))) {
                val entity = new ServerActivityEntity();
                entity.setServerName(server.getName());
                entity.setMessage("Server " + server.getName() + " started");
                DataManager.getServerActivityStore().insert(entity);
            }
        }
    }

    @Override
    public void onRequestDeprovision(LocalCoordinator localCoordinator, Server server) {
        // Nothing to do here
    }

    @Override
    public void onServerShutdown(LocalCoordinator localCoordinator, Server server) {
        val packet = new BungeecordPacket();
        packet.setHostName(server.getName());
        packet.setHostAddress(server.getProperties().get("ip"));
        packet.setHostAddress(server.getProperties().get("port"));
        packet.setServerStatus(BungeecordPacket.ServerStatus.STOPPED);
        Redis.publish(RedisTopic.BUNGEECORD, packet);

        final RSortedSet<eu.builderscoffee.api.common.redisson.infos.Server> servers = Redis.getRedissonClient().getSortedSet("servers");
        val toRemove = new ArrayList<eu.builderscoffee.api.common.redisson.infos.Server>();

        if(Objects.nonNull(servers) && servers.size() > 0){
            servers.stream()
                    .filter(s -> s.getHostName().equalsIgnoreCase(server.getName()))
                    .forEach(toRemove::add);
            toRemove.forEach(servers::remove);
        }

        val entity = new ServerActivityEntity();
        entity.setServerName(server.getName());
        entity.setMessage("Server " + server.getName() + " stopped");
        DataManager.getServerActivityStore().insert(entity);

        StaticServersTask.getInstance().run();
    }

    @Override
    public void onRequestShutdown(LocalCoordinator localCoordinator) {
        // Nothing to do here
    }

    @Override
    public void onListenerRegistered(EventManager<INetworkListener> eventManager) {
        // Nothing to do here
    }

    @Override
    public void onListenerRemoved(EventManager<INetworkListener> eventManager) {
        // Nothing to do here
    }
}