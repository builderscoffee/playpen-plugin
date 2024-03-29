package eu.builderscoffee.playpen.listeners;

import io.playpen.core.coordinator.network.INetworkListener;
import io.playpen.core.coordinator.network.LocalCoordinator;
import io.playpen.core.coordinator.network.Server;
import io.playpen.core.plugin.EventManager;
import io.playpen.core.plugin.IPlugin;
import lombok.extern.log4j.Log4j2;

public class NetworkListener implements INetworkListener {
    @Override
    public void onPluginMessage(IPlugin plugin, String id, Object... args) {
        /*if("command".equalsIgnoreCase(id)) {
            if(args.length < 1)
                return;

            if("balance".equalsIgnoreCase(args[0].toString())) {
                if(args.length != 1) {
                    Network.get().pluginMessage(Main.getInstance(), "log", "balance usage:\npass balance");
                    return;
                }

                Network.get().pluginMessage(Main.getInstance(), "log", "Triggering balance!");
                log.info("Triggering balance from command");
                Balancer.balance();
            }
        }*/
    }

    @Override
    public void onNetworkStartup() {

    }

    @Override
    public void onNetworkShutdown() {

    }

    @Override
    public void onCoordinatorCreated(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onCoordinatorSync(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onRequestProvision(LocalCoordinator localCoordinator, Server server) {

    }

    @Override
    public void onProvisionResponse(LocalCoordinator localCoordinator, Server server, boolean b) {

    }

    @Override
    public void onRequestDeprovision(LocalCoordinator localCoordinator, Server server) {

    }

    @Override
    public void onServerShutdown(LocalCoordinator localCoordinator, Server server) {

    }

    @Override
    public void onRequestShutdown(LocalCoordinator localCoordinator) {

    }

    @Override
    public void onListenerRegistered(EventManager<INetworkListener> eventManager) {

    }

    @Override
    public void onListenerRemoved(EventManager<INetworkListener> eventManager) {

    }
}