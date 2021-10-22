package eu.builderscoffee.playpen.listeners;

import eu.builderscoffee.api.common.redisson.listeners.PacketListener;
import eu.builderscoffee.api.common.redisson.listeners.ProcessPacket;
import eu.builderscoffee.api.common.redisson.packets.types.ActionPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.DeprovisionServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.FreezeServerPacket;
import eu.builderscoffee.api.common.redisson.packets.types.playpen.actions.ProvisionServerPacket;
import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.utils.PlaypenUtils;
import io.playpen.core.coordinator.network.Network;
import lombok.val;

import java.util.HashMap;

public class RedissonActionListener implements PacketListener {

    @ProcessPacket
    public void onActionPacket(ActionPacket packet){
        LogUtils.debug(String.format("Action packet received (%s) from %s", packet.getClass().getSimpleName(), packet.getServerName()));
    }

    @ProcessPacket
    public void onProvisionServerPacket(ProvisionServerPacket psp){
        val version = Network.get().getPackageManager()
                .getPackageList().stream()
                .anyMatch(pkg->pkg.getId().equals(psp.getNewServerPacketId())
                        && pkg.getVersion().equals(psp.getNewServerVersion()))? psp.getNewServerVersion() : "promoted";
        val p3Package = Network.get().getPackageManager().resolve(psp.getNewServerPacketId(), version);

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

    @ProcessPacket
    public void onFreezeServerPacket(FreezeServerPacket fsp){
        if (!PlaypenUtils.existServer(fsp.getTargetServerName())) {
            LogUtils.error(String.format("Deprovision from %s canceled: Server with name %s doesn't exist", fsp.getServerName(), fsp.getTargetServerName()));
            return;
        }

        val server = PlaypenUtils.getServer(fsp.getTargetServerName());
        Network.get().freezeServer(server.getCoordinator().getUuid(), server.getUuid());
    }

    @ProcessPacket
    public void onDeprovisionServerPacket(DeprovisionServerPacket dsp){
        if (!PlaypenUtils.existServer(dsp.getTargetServerName())) {
            LogUtils.error(String.format("Deprovision from %s canceled: Server with name %s doesn't exist", dsp.getServerName(), dsp.getTargetServerName()));
            return;
        }

        val server = PlaypenUtils.getServer(dsp.getTargetServerName());
        Network.get().deprovision(server.getCoordinator().getUuid(), server.getUuid());
    }
}
