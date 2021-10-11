package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.listeners.PubSubListener;
import eu.builderscoffee.api.common.redisson.packets.Packet;
import eu.builderscoffee.api.common.redisson.packets.types.RequestPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.requests.ServersRequestPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.responses.ServersResponsePacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.Server;
import lombok.val;

import java.util.stream.Collectors;

public class RedissonRequestListener implements PubSubListener {

    @Override
    public void onMessage(String json) {
        val packet = Packet.deserialize(json);

        if (!(packet instanceof RequestPacket)) return;

        LogUtils.debug(String.format("Request packet received (%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));

        if (packet instanceof ServersRequestPacket) {
            val srp = (ServersRequestPacket) packet;

            if (srp.getServerName() == null || srp.getServerName().isEmpty()) {
                LogUtils.error(String.format("Request for servers: Server name empty (%s)", srp.getServerName()));
                return;
            }

            val response = new ServersResponsePacket((RequestPacket) packet);
            response.setTargetServerName(srp.getServerName());
            response.setServers(PlaypenUtils.getServers().stream()
                    .map(Server::getName)
                    .collect(Collectors.toList()));

            Redis.publish(RedisTopic.PLAYPEN, response);
        }
    }
}
