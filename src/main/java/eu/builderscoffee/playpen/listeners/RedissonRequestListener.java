package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.listeners.PubSubListener;
import eu.builderscoffee.api.common.redisson.packets.Packet;
import eu.builderscoffee.api.common.redisson.packets.types.RedissonPacket;
import eu.builderscoffee.api.common.redisson.packets.types.redisson.RedissonRequestPacket;
import eu.builderscoffee.api.common.redisson.packets.types.redisson.requests.RequestServersPacket;
import eu.builderscoffee.api.common.redisson.packets.types.redisson.responses.ResponseServersPacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;

import java.util.ArrayList;

public class RedissonRequestListener implements PubSubListener {

    @Override
    public void onMessage(String json) {
        val temp = Packet.deserialize(json);

        if (!(temp instanceof RedissonRequestPacket)) return;

        val packet = (RedissonPacket) temp;

        LogUtils.debug(String.format("Request packet received(%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));

        if (temp instanceof RequestServersPacket) {
            val rsp = (RequestServersPacket) packet;

            if (rsp.getServerName() == null) {
                LogUtils.error(String.format("Request for servers: Server name empty or too short (%s)", rsp.getServerName()));
                return;
            }

            val servers = new ArrayList<String>();
            Network.get().getCoordinators().values().forEach(c -> c.getServers().values().forEach(s -> servers.add(s.getName())));

            val response = new ResponseServersPacket((RedissonRequestPacket) packet);
            response.setDestinationServerName(rsp.getServerName());
            response.setServers(servers);

            Redis.publish(RedisTopic.REDISSON, response);
        }
    }
}
