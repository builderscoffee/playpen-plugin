package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.listeners.PacketListener;
import eu.builderscoffee.api.common.redisson.listeners.ProcessPacket;
import eu.builderscoffee.api.common.redisson.packets.types.RequestPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.requests.ServersRequestPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.responses.ServersResponsePacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.Server;
import lombok.val;

import java.util.stream.Collectors;

public class RedissonRequestListener implements PacketListener {

    @ProcessPacket
    public void onRequestPacket(RequestPacket packet){
        LogUtils.debug(String.format("Request packet received (%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));
    }


    @ProcessPacket
    public void onServersRequestPacket(ServersRequestPacket srp){
        if (srp.getServerName() == null || srp.getServerName().isEmpty()) {
            LogUtils.error(String.format("Request for servers: Server name empty (%s)", srp.getServerName()));
            return;
        }

        val response = new ServersResponsePacket(srp);
        response.setTargetServerName(srp.getServerName());
        response.setServers(PlaypenUtils.getServers().stream()
                .map(Server::getName)
                .collect(Collectors.toList()));

        Redis.publish(RedisTopic.PLAYPEN, response);
    }
}
