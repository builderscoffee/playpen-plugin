package eu.builderscoffee.playpen.utils;

import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Network;
import io.playpen.core.coordinator.network.Server;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PlaypenUtils {

    public static List<Server> getServers() {
        return Network.get().getCoordinators().values().stream()
                .map(lc -> lc.getServers().values())
                .collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }

    public static Server getServer(String serverName) {
        return getServers().stream()
                .filter(server -> server.getName().equals(serverName))
                .findFirst().orElse(null);
    }

    public static boolean existServer(String serverName) {
        return getServer(serverName) != null;
    }

    public static LocalCoordinator getLocalCoordinatorFromServerName(String serverName) {
        return getServer(serverName).getCoordinator();
    }
}
