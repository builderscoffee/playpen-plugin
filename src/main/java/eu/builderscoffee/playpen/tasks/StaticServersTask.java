package eu.builderscoffee.playpen.tasks;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.infos.Server;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.Main;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import org.redisson.api.RSortedSet;

import java.util.HashMap;
import java.util.Objects;
import java.util.TimerTask;

public class StaticServersTask extends TimerTask {

    @Override
    public void run() {
        final RSortedSet<Server> servers = Redis.getRedissonClient().getSortedSet("servers");

        Main.getInstance().getDefaultServersConfig().getServers().stream()
                .filter(s -> !PlaypenUtils.existServer(s.getName()))
                .forEach(s -> {
                    try {
                        PlaypenUtils.provisionServer(s.getName(), s.getPackageName(), s.getPackageVersion().name(), s.getIp(), s.getPort(), new HashMap<>());
                    } catch (RuntimeException e) {
                        LogUtils.warn(e.getMessage());
                    }
                });
    }
}
