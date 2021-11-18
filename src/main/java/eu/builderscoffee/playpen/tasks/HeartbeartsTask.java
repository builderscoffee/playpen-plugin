package eu.builderscoffee.playpen.tasks;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.infos.Server;
import eu.builderscoffee.api.common.redisson.packets.types.common.HeartBeatPacket;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;
import org.redisson.api.RSortedSet;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HeartbeartsTask extends TimerTask {

    private int maxServerInfoAlive = 2;
    private int times = 0;

    @Override
    public void run() {
        final RSortedSet<Server> servers = Redis.getRedissonClient().getSortedSet("servers");
        if (times >= maxServerInfoAlive) {
            times = 0;
            val now = new Date();
            val toRemove = new ArrayList<Server>();
            servers.stream()
                    .forEach(si -> {
                        long diffInMillies = Math.abs(now.getTime() - si.getLastHeartbeat().getTime());
                        long diff = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        if (diff >= maxServerInfoAlive * 2) {
                            toRemove.add(si);
                        }
                    });
            toRemove.forEach(servers::remove);
        }

        PlaypenUtils.getServers().stream()
                .filter(server -> !servers.stream().anyMatch(serverInfo -> server.getName().equals(serverInfo.getHostName())))
                .forEach(server -> {
                    val serverInfo = new Server();
                    serverInfo.setHostName(server.getName());
                    serverInfo.setServerStatus(Server.ServerStatus.STARTING);
                    serverInfo.setStartingMethod(Server.ServerStartingMethod.DYNAMIC);
                    serverInfo.setLastHeartbeat(null);
                    servers.add(serverInfo);
                });

        Redis.publish(RedisTopic.HEARTBEATS, new HeartBeatPacket());
        times++;
    }
}
