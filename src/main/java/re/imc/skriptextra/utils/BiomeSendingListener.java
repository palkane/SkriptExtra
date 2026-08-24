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
                WrapperPlayServerUpdateViewDistance wrapper = new WrapperPlayServerUpdateViewDistance(event);
                wrapper.setViewDistance(32);
                break;
            }
            case PacketType.Play.Server.UPDATE_SIMULATION_DISTANCE: {
                WrapperPlayServerUpdateSimulationDistance wrapper = new WrapperPlayServerUpdateSimulationDistance(event);
                wrapper.setSimulationDistance(32);
                break;
            }
            case PacketType.Play.Server.ENTITY_STATUS: {
                WrapperPlayServerEntityStatus wrapper = new WrapperPlayServerEntityStatus(event);
                Player player = event.getPlayer();
                if (player.hasPermission("sicilia.admin")) {
                    wrapper.setStatus(28);
                }
                break;
            }
            case PacketType.Configuration.Server.REGISTRY_DATA: {
                WrapperConfigServerRegistryData wrapper = new WrapperConfigServerRegistryData(event);
                @Nullable ResourceLocation registryKey = wrapper.getRegistryKey();
                if (registryKey != null && (registryKey.getNamespace().equals("minecraft") && registryKey.getKey().equals("worldgen/biome"))) {
                    List<WrapperConfigServerRegistryData.RegistryElement> elements = new ArrayList<>();
                    @Nullable List<WrapperConfigServerRegistryData.RegistryElement> wrapperElements = wrapper.getElements();
                    if (wrapperElements == null) {
                        return;
                    }
                    for (WrapperConfigServerRegistryData.RegistryElement wrapperElement : wrapperElements) {
                        if (!wrapperElement.getId().getNamespace().equals("minecraft")) {
                            String originalKey = wrapperElement.getId().getKey();
                            String[] keyParts = originalKey.split("/");
                            ResourceLocation newKey = new ResourceLocation("sicilia", keyParts[2]);
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