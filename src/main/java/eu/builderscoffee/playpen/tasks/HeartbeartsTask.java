package eu.builderscoffee.playpen.tasks;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.redisson.packets.types.common.HeartBeatPacket;
import eu.builderscoffee.api.common.redisson.serverinfos.ServerInfo;
import lombok.val;

import java.util.ArrayList;
import java.util.Date;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class HeartbeartsTask extends TimerTask {

    private int maxServerInfoAlive = 2;
    private int times = 0;

    @Override
    public void run() {
        if(times >= maxServerInfoAlive){
            times = 0;
            val now = new Date();
            val servers = Redis.redissonClient.getList("servers");
            val toRemove = new ArrayList<>();
            servers.stream()
                    .filter(o -> o instanceof ServerInfo)
                    .forEach(o -> {
                        val si = (ServerInfo) o;
                        long diffInMillies = Math.abs(now.getTime() - si.getLastHeartbeat().getTime());
                        long diff = TimeUnit.SECONDS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                        if (diff > maxServerInfoAlive * 5) {
                            toRemove.add(o);
                        }
                    });
            toRemove.forEach(servers::remove);
        }
        Redis.publish(RedisTopic.HEARTBEATS, new HeartBeatPacket());
        times++;
    }
}
