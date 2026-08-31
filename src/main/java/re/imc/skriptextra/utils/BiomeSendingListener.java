package re.imc.skriptextra.utils;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.resources.ResourceLocation;
import com.github.retrooper.packetevents.wrapper.configuration.server.WrapperConfigServerRegistryData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateSimulationDistance;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerUpdateViewDistance;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BiomeSendingListener extends PacketListenerAbstract {

    public BiomeSendingListener() {
        super(PacketListenerPriority.NORMAL);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        switch (event.getPacketType()) {
            case PacketType.Play.Server.UPDATE_VIEW_DISTANCE: {
                if (!PluginConfig.enabled("features.packet-events.view-distance.enabled")) {
                    break;
                }
                WrapperPlayServerUpdateViewDistance wrapper = new WrapperPlayServerUpdateViewDistance(event);
                wrapper.setViewDistance(PluginConfig.integer("features.packet-events.view-distance.value", 32));
                break;
            }
            case PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE: {
                if (!PluginConfig.enabled("features.packet-events.simulation-distance.enabled")) {
                    break;
                }
                WrapperPlayServerUpdateSimulationDistance wrapper = new WrapperPlayServerUpdateSimulationDistance(event);
                wrapper.setSimulationDistance(PluginConfig.integer("features.packet-events.simulation-distance.value", 32));
                break;
            }
            case PacketType.Play.Server.ENTITY_STATUS: {
                if (!PluginConfig.enabled("features.packet-events.entity-status-override.enabled")) {
                    break;
                }
                WrapperPlayServerEntityStatus wrapper = new WrapperPlayServerEntityStatus(event);
                Player player = event.getPlayer();
                String permission = PluginConfig.string("features.packet-events.entity-status-override.permission", "sicilia.admin");
                if (permission.isBlank() || player.hasPermission(permission)) {
                    wrapper.setStatus(PluginConfig.integer("features.packet-events.entity-status-override.status", 28));
                }
                break;
            }
            case PacketType.Configuration.Server.REGISTRY_DATA: {
                if (!PluginConfig.enabled("features.packet-events.biome-namespace-rewrite.enabled")) {
                    break;
                }
                WrapperConfigServerRegistryData wrapper = new WrapperConfigServerRegistryData(event);
                @Nullable ResourceLocation registryKey = wrapper.getRegistryKey();
                String configuredRegistry = PluginConfig.string("features.packet-events.biome-namespace-rewrite.registry", "minecraft:worldgen/biome");
                if (registryKey != null && registryKey.toString().equals(configuredRegistry)) {
                    List<WrapperConfigServerRegistryData.RegistryElement> elements = new ArrayList<>();
                    @Nullable List<WrapperConfigServerRegistryData.RegistryElement> wrapperElements = wrapper.getElements();
                    if (wrapperElements == null) {
                        return;
                    }
                    String sourceNamespace = PluginConfig.string(
                            "features.packet-events.biome-namespace-rewrite.source-namespace", "terra");
                    String sourcePathPrefix = PluginConfig.string(
                            "features.packet-events.biome-namespace-rewrite.source-path-prefix", "sicilia/sicilia/");
                    String targetNamespace = PluginConfig.string(
                            "features.packet-events.biome-namespace-rewrite.target-namespace", "sicilia");
                    for (WrapperConfigServerRegistryData.RegistryElement wrapperElement : wrapperElements) {
                        ResourceLocation originalId = wrapperElement.getId();
                        String originalPath = originalId.getKey();
                        if (originalId.getNamespace().equals(sourceNamespace) && originalPath.startsWith(sourcePathPrefix)) {
                            String biomePath = originalPath.substring(sourcePathPrefix.length());
                            if (biomePath.isEmpty()) {
                                elements.add(wrapperElement);
                                continue;
                            }
                            ResourceLocation newKey = new ResourceLocation(targetNamespace, biomePath);
                            WrapperConfigServerRegistryData.RegistryElement newElement = new WrapperConfigServerRegistryData.RegistryElement(newKey, wrapperElement.getData());
                            elements.add(newElement);
                        } else {
                            elements.add(wrapperElement);
                        }
                    }
                    wrapper.setElements(elements);
                }
                break;
            }
            default:
                break;
        }
    }
}
