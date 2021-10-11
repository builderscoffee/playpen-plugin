package eu.builderscoffee.playpen.configuration;

import eu.builderscoffee.api.common.configuration.annotation.Configuration;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Configuration(value = "playpen")
public final class PlaypenConfig {

    boolean debugConsole = false;
}
