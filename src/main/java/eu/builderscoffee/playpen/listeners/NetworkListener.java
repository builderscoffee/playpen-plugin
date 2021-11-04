package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.packets.types.common.BungeecordPacket;
import eu.builderscoffee.playpen.tasks.StaticServersTask;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.INetworkListener;
import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Network;
import io.playpen.core.coordinator.network.Server;
import io.playpen.core.plugin.EventManager;
import io.playpen.core.plugin.IPlugin;
import lombok.val;

public class NetworkListener implements INetworkListener {
    @Override
    public void onPluginMessage(IPlugin plugin, String id, Object... args) {

    }

    @Override
    public void onNetworkStartup() {

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
    }

    @Override
    public void onCoordinatorCreated(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onCoordinatorSync(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onRequestProvision(LocalCoordinator localCoordinator, Server server) {

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
        }
    }

    @Override
    public void onRequestDeprovision(LocalCoordinator localCoordinator, Server server) {

    }

    @Override
    public void onServerShutdown(LocalCoordinator localCoordinator, Server server) {
        val packet = new BungeecordPacket();
        packet.setHostName(server.getName());
        packet.setHostAddress(server.getProperties().get("ip"));
        packet.setHostAddress(server.getProperties().get("port"));
        packet.setServerStatus(BungeecordPacket.ServerStatus.STOPPED);
        Redis.publish(RedisTopic.BUNGEECORD, packet);
        StaticServersTask.getInstance().run();
    }

    @Override
    public void onRequestShutdown(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onListenerRegistered(EventManager<INetworkListener> eventManager) {

    }

    @Override
    public void onListenerRemoved(EventManager<INetworkListener> eventManager) {

    }
}