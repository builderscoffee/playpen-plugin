package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.listeners.PubSubListener;
import eu.builderscoffee.api.common.redisson.packets.Packet;
import eu.builderscoffee.api.common.redisson.packets.types.ActionPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.DeprovisionServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.FreezeServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.ProvisionServerPacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;

import java.util.HashMap;

public class RedissonActionListener implements PubSubListener {

    @Override
    public void onMessage(String json) {
        val packet = Packet.deserialize(json);

        if (!(packet instanceof ActionPacket)) return;

        LogUtils.debug(String.format("Action packet received (%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));

        if (packet instanceof ProvisionServerPacket) {
            val psp = (ProvisionServerPacket) packet;

            val p3Package = Network.get().getPackageManager().resolve(psp.getNewServerPacketId(), "promoted");

            if (psp.getNewServerName() == null || psp.getNewServerName().length() < 4) {
                LogUtils.error(String.format("Provision from %s canceled: Server name empty or too short (%s)", psp.getServerName(), psp.getNewServerName()));
                return;
            }

            if (p3Package == null) {
                LogUtils.error(String.format("Provision from %s canceled: P3Package not found (%s)", psp.getServerName(), psp.getNewServerPacketId()));
                return;
            }

            if (PlaypenUtils.existServer(psp.getNewServerName())) {
                LogUtils.error(String.format("Provision from %s canceled: Server with name %s already exist", psp.getServerName(), psp.getNewServerName()));
                return;
            }

            Network.get().provision(p3Package, psp.getNewServerName(), psp.getNewServerProperties() == null ? new HashMap<>() : psp.getNewServerProperties());
        }
        else if (packet instanceof DeprovisionServerPacket) {
            val dsp = (DeprovisionServerPacket) packet;

            if (!PlaypenUtils.existServer(dsp.getTargetServerName())) {
                LogUtils.error(String.format("Deprovision from %s canceled: Server with name %s doesn't exist", dsp.getServerName(), dsp.getTargetServerName()));
                return;
            }

            val server = PlaypenUtils.getServer(dsp.getTargetServerName());
            Network.get().deprovision(server.getCoordinator().getUuid(), server.getUuid());
        }
        else if (packet instanceof FreezeServerPacket) {
            val dsp = (FreezeServerPacket) packet;

            if (!PlaypenUtils.existServer(dsp.getTargetServerName())) {
                LogUtils.error(String.format("Deprovision from %s canceled: Server with name %s doesn't exist", dsp.getServerName(), dsp.getTargetServerName()));
                return;
            }

            val server = PlaypenUtils.getServer(dsp.getTargetServerName());
            Network.get().freezeServer(server.getCoordinator().getUuid(), server.getUuid());
        }
    }
}
