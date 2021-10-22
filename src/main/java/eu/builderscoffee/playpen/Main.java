package eu.builderscoffee.playpen;

import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisCredentials;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.configuration.PlaypenConfig;
import eu.builderscoffee.playpen.configuration.PortsConfig;
import eu.builderscoffee.playpen.listeners.NetworkListener;
import eu.builderscoffee.playpen.listeners.RedissonActionListener;
import eu.builderscoffee.playpen.listeners.RedissonRequestListener;
import eu.builderscoffee.playpen.tasks.HeartbeartsTask;
import io.playpen.core.coordinator.CoordinatorMode;
import io.playpen.core.coordinator.PlayPen;
import io.playpen.core.coordinator.network.Network;
import io.playpen.core.plugin.AbstractPlugin;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Timer;

import static eu.builderscoffee.api.common.configuration.Configuration.readOrCreateConfiguration;

@Getter
public class Main extends AbstractPlugin {

    @Getter
    public static Main instance;

    private RedisCredentials redisCredentials;
    private PlaypenConfig playerpenConfig;
    private PortsConfig portsConfig;

    @Override
    public boolean onStart() {
        if (PlayPen.get().getCoordinatorMode() != CoordinatorMode.NETWORK) {
            LogUtils.fatal("Only network coordinators are supported");
            return false;
        }

        // Enregistre l'instance
        instance = this;

        // Configuration playpen
        playerpenConfig = readOrCreateConfiguration(getSchema().getId(), PlaypenConfig.class);

        // Change reflections logger level
        //Configurator.setLevel("org.reflections", Level.ERROR);
        Configurator.setLevel("eu.builderscoffee", getPlayerpenConfig().isDebugConsole() ? Level.DEBUG : Level.INFO);

        // Récupère la configuration redisson
        portsConfig = readOrCreateConfiguration(getSchema().getId(), PortsConfig.class);
        redisCredentials = readOrCreateConfiguration(getSchema().getId(), RedisCredentials.class);
        if (redisCredentials.getClientName() == null || redisCredentials.getIp() == null || redisCredentials.getPassword() == null) {
            System.out.println("");
            LogUtils.fatal("The redisson config file is not correctly completed");
            System.out.println("");
            return false;
        }

        // Initialisation de redisson
        LogUtils.debug("Starting redis channels");
        Redis.Initialize("playpen", redisCredentials, 0, 0);
        Redis.subscribe(RedisTopic.PLAYPEN, new RedissonActionListener());
        Redis.subscribe(RedisTopic.PLAYPEN, new RedissonRequestListener());

        // Tâche Heartbeats
        new Timer().scheduleAtFixedRate(new HeartbeartsTask(), 5 * 1000, 5 * 1000);

        // Clear servers information
        Redis.getRedissonClient().getSortedSet("servers").clear();


        LogUtils.debug("Starting networks listeners");
        return Network.get().getEventManager().registerListener(new NetworkListener());
    }

    @Override
    public void onStop() {
        Redis.close();
    }
}