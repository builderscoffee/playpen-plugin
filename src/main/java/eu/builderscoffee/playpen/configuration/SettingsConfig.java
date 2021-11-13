package eu.builderscoffee.playpen.configuration;

import eu.builderscoffee.api.common.configuration.annotation.Configuration;
import eu.builderscoffee.api.common.data.MySQLCredentials;
import eu.builderscoffee.api.common.redisson.RedisCredentials;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Data
@Configuration("settings")
public class SettingsConfig {

    boolean debugConsole = false;
    List<Integer> ports = Collections.emptyList();
    List<ServerConfig> servers = Arrays.asList(new ServerConfig("hub-dev-1", "hub", PackageVersion.DEVELOPMENT, "54.36.124.50", 25595),
            new ServerConfig("proxy-1", "proxy", PackageVersion.DEVELOPMENT, "54.36.124.50", 25594));
    MySQLCredentials mysql = new MySQLCredentials();
    RedisCredentials redisson = new RedisCredentials();

    @Data
    public static class ServerConfig{
        String name;
        String packageName;
        PackageVersion packageVersion;
        String ip;
        int port;

        private ServerConfig() {

        }

        public ServerConfig(String name, String packageName, PackageVersion packageVersion, String ip, int port) {
            this.name = name;
            this.packageName = packageName;
            this.packageVersion = packageVersion;
            this.ip = ip;
            this.port = port;
        }
    }

    public enum PackageVersion{
        PRODUCTION,
        DEVELOPMENT
    }
}
