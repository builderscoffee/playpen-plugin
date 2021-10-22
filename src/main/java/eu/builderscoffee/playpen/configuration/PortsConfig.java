package eu.builderscoffee.playpen.configuration;

import eu.builderscoffee.api.common.configuration.annotation.Configuration;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@Configuration(value = "ports")
public final class PortsConfig {

    List<Integer> ports = Collections.emptyList();
}
