package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.listeners.PacketListener;
import eu.builderscoffee.api.common.redisson.listeners.ProcessPacket;
import eu.builderscoffee.api.common.redisson.packets.types.ActionPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.DeprovisionServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.FreezeServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.ProvisionServerPacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.PlaypenPlugin;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import eu.builderscoffee.playpen.utils.PortUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;

public class RedissonActionListener implements PacketListener {

    @ProcessPacket
    public void onActionPacket(ActionPacket packet) {
        LogUtils.debug(String.format("Action packet received (%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));
    }

    @ProcessPacket
    public void onProvisionServerPacket(ProvisionServerPacket psp) {
        val port = PlaypenPlugin.getInstance().getSettingsConfig().getPorts().parallelStream()
                .filter(PortUtils::available)
                .findFirst()
                .orElse(-1);

        if (port == -1) {
            LogUtils.error(String.format("Provision from %s canceled: Cannot find a free port (%s)", psp.getServerName(), psp.getNewServerName()));
            return;
        }

        try {
            PlaypenUtils.provisionServer(psp.getNewServerName(), psp.getNewServerPacketId(), psp.getNewServerVersion(), "54.36.124.50", port, psp.getNewServerProperties());
        } catch (RuntimeException e) {
            LogUtils.error(String.format("Provision from %s canceled: " + e.getMessage(), psp.getServerName()));
        }
    }

    @ProcessPacket
    public void onFreezeServerPacket(FreezeServerPacket fsp) {
        if (!PlaypenUtils.existServer(fsp.getTargetServerName())) {
            LogUtils.error(String.format("Deprovision from %s canceled: Server with name %s doesn't exist", fsp.getServerName(), fsp.getTargetServerName()));
            return;
        }

        val server = PlaypenUtils.getServer(fsp.getTargetServerName());
        Network.get().freezeServer(server.getCoordinator().getUuid(), server.getUuid());
    }

    @ProcessPacket
    public void onDeprovisionServerPacket(DeprovisionServerPacket dsp) {
        if (!PlaypenUtils.existServer(dsp.getServerToStop())) {
            LogUtils.error(String.format("Deprovision from %s canceled: Server with name %s doesn't exist", dsp.getServerName(), dsp.getTargetServerName()));
            return;
        }

        val server = PlaypenUtils.getServer(dsp.getServerToStop());
        Network.get().deprovision(server.getCoordinator().getUuid(), server.getUuid());
    }
}
