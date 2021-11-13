package eu.builderscoffee.playpen.utils;

import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Network;
import io.playpen.core.coordinator.network.Server;
import lombok.experimental.UtilityClass;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public static List<LocalCoordinator> getLocalCoordinators() {
        return Network.get().getCoordinators().values().stream().collect(Collectors.toList());
    }

    public static void provisionServer(String name, String packageName, String version, String ip, int port, Map<String, String> properties) throws RuntimeException {
        val p3Package = Network.get().getPackageManager().resolve(packageName, version);

        if (name.isEmpty() || name.length() < 4)
            throw new RuntimeException(String.format("Server name empty or too short (%s)", name));

        if (p3Package == null)
            throw new RuntimeException(String.format("P3Package %s not found with version %s", packageName, version));

        if (PlaypenUtils.existServer(name))
            throw new RuntimeException(String.format("Server with name %s already exist", name));

        if(!PortUtils.available(port))
            throw new RuntimeException(String.format("Cannot find a free port (%s)", name));

        properties.put("name", name);
        properties.put("ip", ip);
        properties.put("port", String.valueOf(port));

        Network.get().provision(p3Package, name, properties);
    }
}
