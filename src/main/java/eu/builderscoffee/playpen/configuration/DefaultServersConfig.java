package eu.builderscoffee.playpen.configuration;

import eu.builderscoffee.api.common.configuration.annotation.Configuration;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
@Configuration(value = "static_servers")
public final class DefaultServersConfig {

    List<ServerConfig> servers = Arrays.asList(new ServerConfig("hub-dev-1", "hub", PackageVersion.DEVELOPMENT, "54.36.124.50", 25595),
            new ServerConfig("proxy-1", "proxy", PackageVersion.DEVELOPMENT, "54.36.124.50", 25594));

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
