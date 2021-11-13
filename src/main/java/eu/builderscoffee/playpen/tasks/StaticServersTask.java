package eu.builderscoffee.playpen.tasks;

import eu.builderscoffee.api.common.utils.LogUtils;
import eu.builderscoffee.playpen.PlaypenPlugin;
import eu.builderscoffee.playpen.utils.PlaypenUtils;

import java.util.HashMap;
import java.util.TimerTask;

public class StaticServersTask extends TimerTask {

    private StaticServersTask(){}

    private static class StaticServersTaskHolder{
        private static final StaticServersTask INSTANCE = new StaticServersTask();
    }

    public static StaticServersTask getInstance(){
        return StaticServersTaskHolder.INSTANCE;
    }

    @Override
    public void run() {
        if(PlaypenUtils.getLocalCoordinators().size() == 0)
            return;

        PlaypenPlugin.getInstance().getSettingsConfig().getServers().stream()
                .filter(s -> !PlaypenUtils.existServer(s.getName()))
                .forEach(s -> {
                    try {
                        PlaypenUtils.provisionServer(s.getName(), s.getPackageName(), s.getPackageVersion().name(), s.getIp(), s.getPort(), new HashMap<>());
                    } catch (RuntimeException e) {
                        LogUtils.warn(e.getMessage());
                    }
                });
    }
}
