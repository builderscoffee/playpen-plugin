package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.listeners.PubSubListener;
import eu.builderscoffee.api.common.redisson.packets.Packet;
import eu.builderscoffee.api.common.redisson.packets.types.ActionPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.DeprovisionServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.ProvisionServerPacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;

import java.util.HashMap;

public class RedissonActionListener implements PubSubListener {

    @Override
    public void onMessage(String json) {
        val packet = Packet.deserialize(json);

        if (!(packet instanceof ActionPacket)) return;

        LogUtils.debug(String.format("Action packet received(%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));

        if (packet instanceof ProvisionServerPacket) {
            val psp = (ProvisionServerPacket) packet;

            val p3Package = Network.get().getPackageManager().resolve(psp.getNewServerPacketId(), "promoted");

            if (psp.getNewServerName() == null || psp.getNewServerName().length() < 4) {
                LogUtils.error(String.format("Server start action from %s canceled: Server name empty or too short (%s)", psp.getServerName(), psp.getNewServerName()));
                return;
            }

            if (p3Package == null) {
                LogUtils.error(String.format("Server start action from %s canceled: P3Package not found (%s)", psp.getServerName(), psp.getNewServerPacketId()));
                return;
            }

            if (Network.get().getCoordinators().values().stream().filter(c -> c.getServer(psp.getNewServerName()) == null).findFirst().isPresent()) {
                LogUtils.error(String.format("Server start action from %s canceled: Server with name %s already exist", psp.getServerName(), psp.getNewServerName()));
                return;
            }

            Network.get().provision(p3Package, psp.getNewServerName(), psp.getNewServerProperties() == null ? new HashMap<>() : psp.getNewServerProperties());
        } else if (packet instanceof DeprovisionServerPacket) {
            val dsp = (DeprovisionServerPacket) packet;

            if (!Network.get().getCoordinators().values().stream().filter(c -> c.getServer(dsp.getTargetServerName()) == null).findFirst().isPresent()) {
                LogUtils.error(String.format("Server stop action from %s canceled: Server with name %s doesn't exist", dsp.getServerName(), dsp.getTargetServerName()));
                return;
            }

            val coordinator = Network.get().getCoordinators().values().stream().filter(c -> c.getServer(dsp.getTargetServerName()) != null).findFirst().orElse(null);
            Network.get().deprovision(coordinator.getUuid(), coordinator.getServer(dsp.getTargetServerName()).getUuid());
        }
    }
}
