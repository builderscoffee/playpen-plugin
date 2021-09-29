package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.listeners.PubSubListener;
import eu.builderscoffee.api.common.redisson.packets.Packet;
import eu.builderscoffee.api.common.redisson.packets.types.RedissonPacket;
import eu.builderscoffee.api.common.redisson.packets.types.redisson.RedissonActionPacket;
import eu.builderscoffee.api.common.redisson.packets.types.redisson.actions.StartServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.redisson.actions.StopServerPacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;

import java.util.HashMap;

public class RedissonActionListener implements PubSubListener {

    @Override
    public void onMessage(String json) {
        val temp = Packet.deserialize(json);

        if (!(temp instanceof RedissonActionPacket)) return;

        val packet = (RedissonPacket) temp;

        LogUtils.debug(String.format("Action packet received(%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));

        if (temp instanceof StartServerPacket) {
            val ssp = (StartServerPacket) temp;

            val p3Package = Network.get().getPackageManager().resolve(ssp.getNewServerPacketId(), "promoted");

            if (ssp.getNewServerName() == null || ssp.getNewServerName().length() < 4) {
                LogUtils.error(String.format("Server start action from %s canceled: Server name empty or too short (%s)", ssp.getServerName(), ssp.getNewServerName()));
                return;
            }

            if (p3Package == null) {
                LogUtils.error(String.format("Server start action from %s canceled: P3Package not found (%s)", ssp.getServerName(), ssp.getNewServerPacketId()));
                return;
            }

            if (Network.get().getCoordinators().values().stream().filter(c -> c.getServer(ssp.getNewServerName()) == null).findFirst().isPresent()) {
                LogUtils.error(String.format("Server start action from %s canceled: Server with name %s already exist", ssp.getServerName(), ssp.getNewServerName()));
                return;
            }

            Network.get().provision(p3Package, ssp.getNewServerName(), ssp.getNewServerProperties() == null ? new HashMap<>() : ssp.getNewServerProperties());
        } else if (temp instanceof StopServerPacket) {
            val ssp = (StopServerPacket) temp;

            if (!Network.get().getCoordinators().values().stream().filter(c -> c.getServer(ssp.getNewServerName()) == null).findFirst().isPresent()) {
                LogUtils.error(String.format("Server stop action from %s canceled: Server with name %s doesn't exist", ssp.getServerName(), ssp.getNewServerName()));
                return;
            }

            val coordinator = Network.get().getCoordinators().values().stream().filter(c -> c.getServer(ssp.getNewServerName()) != null).findFirst().orElse(null);
            Network.get().deprovision(coordinator.getUuid(), coordinator.getServer(ssp.getNewServerName()).getUuid());
        }
    }
}
