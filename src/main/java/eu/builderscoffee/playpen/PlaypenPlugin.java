package eu.builderscoffee.playpen;

import eu.builderscoffee.api.common.data.DataManager;
import eu.builderscoffee.api.common.redisson.Redis;
import eu.builderscoffee.api.common.redisson.RedisTopic;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.configuration.SettingsConfig;
import eu.builderscoffee.playpen.listeners.NetworkListener;
import eu.builderscoffee.playpen.listeners.RedissonActionListener;
import eu.builderscoffee.playpen.listeners.RedissonRequestListener;
import eu.builderscoffee.playpen.tasks.HeartbeartsTask;
import eu.builderscoffee.playpen.tasks.StaticServersTask;
import io.playpen.core.coordinator.CoordinatorMode;
import io.playpen.core.coordinator.PlayPen;
import io.playpen.core.coordinator.network.Network;
import io.playpen.core.plugin.AbstractPlugin;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;

import java.util.Timer;
import java.util.concurrent.TimeUnit;

import static eu.builderscoffee.api.common.configuration.Configuration.readOrCreateConfiguration;

@Getter
public class PlaypenPlugin extends AbstractPlugin {

    @Getter
    public static PlaypenPlugin instance;

    private SettingsConfig settingsConfig;

    @SneakyThrows
    @Override
    public boolean onStart() {
        if (PlayPen.get().getCoordinatorMode() != CoordinatorMode.NETWORK) {
            LogUtils.fatal("Only network coordinators are supported");
            return false;
        }

        // Instance
        instance = this;

        // Configuration playpen
        settingsConfig = readOrCreateConfiguration(getSchema().getId(), SettingsConfig.class);

        // Change reflections logger level
        //Configurator.setLevel("org.reflections", Level.ERROR);
        Configurator.setLevel("eu.builderscoffee", getSettingsConfig().isDebugConsole() ? Level.DEBUG : Level.INFO);

        // Initialisation of redisson
        LogUtils.debug("Starting redis channels");
        Redis.Initialize("playpen", getSettingsConfig().getRedisson(), 0, 0);
        Redis.subscribe(RedisTopic.PLAYPEN, new RedissonActionListener());
        Redis.subscribe(RedisTopic.PLAYPEN, new RedissonRequestListener());

        // Initialisation of MySQL
        DataManager.init(getSettingsConfig().getMysql().toHikari());

        // Tâche Heartbeats
        Network.get().getScheduler().scheduleAtFixedRate(new HeartbeartsTask(), 5, 5, TimeUnit.SECONDS);

        // Clear servers information
        Redis.getRedissonClient().getSortedSet("servers").clear();

        Network.get().getScheduler().scheduleAtFixedRate(StaticServersTask.getInstance(), 10, 30, TimeUnit.SECONDS);

        LogUtils.debug("Starting networks listeners");
        return Network.get().getEventManager().registerListener(new NetworkListener());
    }

    @Override
    public void onStop() {
        // Clear servers information
        Redis.getRedissonClient().getSortedSet("servers").clear();
        Redis.close();
    }
}