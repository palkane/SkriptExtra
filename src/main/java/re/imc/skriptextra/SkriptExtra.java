package re.imc.skriptextra;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.function.Parameter;
import ch.njol.skript.lang.function.SimpleJavaFunction;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.DefaultClasses;
import ch.njol.skript.util.Version;
import com.github.retrooper.packetevents.PacketEvents;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import io.papermc.paper.math.Position;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import re.imc.skriptextra.elements.ExtraClasses;
import re.imc.skriptextra.utils.placeholderAPI.PlaceholderPlugin;
import re.imc.skriptextra.utils.placeholderAPI.PlaceholderRegistry;
import re.imc.skriptextra.utils.BiomeSendingListener;
import re.imc.skriptextra.utils.FileHash;
import re.imc.skriptextra.utils.WorldEditUtils;
import re.imc.skriptextra.utils.LogUtils;
import re.imc.skriptextra.utils.MapVariablesFeature;
import re.imc.skriptextra.utils.MultipleConditionsFeature;
import re.imc.skriptextra.utils.PluginConfig;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class SkriptExtra extends JavaPlugin {
    
    private static SkriptExtra instance;
    private PlaceholderRegistry registry;
    private boolean packetEventsManaged;

    public static SkriptExtra getInstance() {
        return instance;
    }

    public PlaceholderRegistry getRegistry() {
        return registry;
    }

    @Override
    public void onLoad() {
        instance = this;
        saveDefaultConfig();
        getLogger().info("配置文件位置: " + new File(getDataFolder(), "config.yml").getAbsolutePath());
    }

    @Override
    public void onEnable() {
        instance = this;
        // 初始化日志 + PacketEvents
        LogUtils.init(this);
        LogUtils.info("启动中...");

        if (!PluginConfig.enabled("general.enabled")) {
            LogUtils.warn("插件已在 config.yml 中关闭");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 检查 Skript 版本
        if (Skript.getVersion().isSmallerThan(new Version(2, 7, 3))) {
            LogUtils.severe("需要 Skript 2.7.3 或更高版本！");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // 初始化 PlaceholderRegistry（如果有可用的placeholder插件）
        if (PluginConfig.enabled("features.placeholder-api.enabled") && !PlaceholderPlugin.getInstalledPlugins().isEmpty()) {
            registry = new PlaceholderRegistry(this);
            LogUtils.info("检测到 Placeholder 插件，已启用 Placeholder 功能");
        }

        registerPacketEvents();

        // 注册 Skript 语法 (Events, Expressions, Effects, Sections, Conditions, Structures)
        Skript.registerAddon(this);
        LogUtils.info("正在加载 Skript 语法...");
        if (PluginConfig.enabled("features.classes.interact-action")) {
            ExtraClasses.load();
        }
        MapVariablesFeature.register();
        MultipleConditionsFeature.register();
        loadElement("features.events.async-periodical.enabled", "re.imc.skriptextra.elements.events.EvtAsyncPeriodical");
        loadElement("features.events.raw-click.enabled", "re.imc.skriptextra.elements.events.EvtRawClick");
        loadElement("features.expressions.interact-type", "re.imc.skriptextra.elements.expressions.ExprInteractType");
        loadElement("features.expressions.interact-slot", "re.imc.skriptextra.elements.expressions.ExprInteractSlot");
        loadElement("features.expressions.nano-time", "re.imc.skriptextra.elements.expressions.ExprNanoTime");
        if (registry != null) {
            loadElement("features.expressions.placeholder-request", "re.imc.skriptextra.elements.expressions.ExprPlaceholder");
            loadElement("features.expressions.placeholder-value", "re.imc.skriptextra.elements.expressions.ExprPlaceholderValue");
            loadElement("features.expressions.placeholder-result", "re.imc.skriptextra.elements.expressions.ExprPlaceholderResult");
            loadElement("features.expressions.relational-placeholder-players", "re.imc.skriptextra.elements.expressions.ExprRelationalPlaceholderPlayers");
            loadElement("features.structures.custom-placeholder.enabled", "re.imc.skriptextra.elements.structures.StructCustomPlaceholder");
        }
        registerFastContains();

        // 注册 Skript Functions
        if (PluginConfig.enabled("features.functions.file-sha1.enabled")) {
            Functions.registerFunction(new SimpleJavaFunction<>("fileSha1",
            new Parameter[]{new Parameter<>("file", DefaultClasses.OBJECT, true, null)},
            DefaultClasses.STRING, true) {
            @Override
            public String @Nullable [] executeSimple(Object[][] objects) {
                Object input = objects[0][0];
                File file = input instanceof File ? (File) input : input instanceof String ? new File((String) input) : null;
                if (file != null) {
                    try {
                        return new String[]{FileHash.sha1(file)};
                    } catch (Exception e) {
                        LogUtils.severe("Error calculating SHA-1 hash: " + e.getMessage());
                    }
                }
                return null;
            }
            });
        }

        // WorldEdit 相关函数
        if (PluginConfig.enabled("features.functions.worldedit-schematics.enabled") && isWorldEditEnabled()) {
            if (PluginConfig.enabled("features.functions.worldedit-schematics.load")) {
                Functions.registerFunction(new SimpleJavaFunction<>("loadSchem", new Parameter[]{new Parameter<>("file", DefaultClasses.OBJECT, true, null), new Parameter<>("id", DefaultClasses.STRING, true, null)}, DefaultClasses.OBJECT, true) {
                @Override
                public Object @Nullable [] executeSimple(Object[][] objects) {
                    String id = objects[1][0].toString();
                    File file = null;
                    if (objects[0][0] instanceof File f) {
                        file = f;
                    }
                    if (objects[0][0] instanceof String str) {
                        file = new File(str);
                    }
                    return new Clipboard[]{WorldEditUtils.loadSchem(file, id)};
                }
                });
            }

            if (PluginConfig.enabled("features.functions.worldedit-schematics.place")) {
                Functions.registerFunction(new SimpleJavaFunction<>("placeSchem", new Parameter[]{
                    new Parameter<>("id", DefaultClasses.STRING, true, null),
                    new Parameter<>("loc", DefaultClasses.LOCATION, true, null),
                    new Parameter<>("direction", DefaultClasses.STRING, true, null),
                    new Parameter<>("ignoreair", DefaultClasses.BOOLEAN, true,
                            new SimpleLiteral<>(PluginConfig.bool("features.functions.worldedit-schematics.default-ignore-air", false), true)),
                    new Parameter<>("async", DefaultClasses.BOOLEAN, true,
                            new SimpleLiteral<>(PluginConfig.bool("features.functions.worldedit-schematics.default-async", true), true))

            }, DefaultClasses.OBJECT, true) {
                @Override
                public Object @Nullable [] executeSimple(Object[][] objects) {
                    String id = objects[0][0].toString();
                    Location loc = (Location) objects[1][0];
                    BlockFace face = BlockFace.valueOf(objects[2][0].toString().toUpperCase());
                    boolean ignoreAir = (boolean) objects[3][0];
                    boolean async = (boolean) objects[4][0];
                    WorldEditUtils.pasteSchem(loc, id, face, ignoreAir, async);
                    return null;
                }
                });
            }
        }

        // 注册 sendMultiBlockChange 更改函数
        if (PluginConfig.enabled("features.functions.send-multi-block-change.enabled")) {
            Functions.registerFunction(new SimpleJavaFunction<>("sendMultiBlockChange", new Parameter[]{
                new Parameter("players", DefaultClasses.PLAYER, false, null),
                new Parameter("blockdata", Classes.getExactClassInfo(BlockData.class), true, null),
                new Parameter("locations", DefaultClasses.LOCATION, false, null)
        }, DefaultClasses.OBJECT, true) {
            @Override
            public @Nullable Object[] executeSimple(Object[][] objects) {
                Player[] players = (Player[]) objects[0];
                BlockData blockData = (BlockData) objects[1][0];
                Location[] locations = (Location[]) objects[2];
                int maximumPlayers = PluginConfig.integer("features.functions.send-multi-block-change.maximum-players", 0);
                int maximumLocations = PluginConfig.integer("features.functions.send-multi-block-change.maximum-locations", 0);
                if (maximumPlayers > 0 && players.length > maximumPlayers) {
                    LogUtils.warn("sendMultiBlockChange 玩家数量超过配置限制: " + players.length);
                    return new Object[0];
                }
                if (maximumLocations > 0 && locations.length > maximumLocations) {
                    LogUtils.warn("sendMultiBlockChange 方块数量超过配置限制: " + locations.length);
                    return new Object[0];
                }
                Map<Position, BlockData> blocks = new HashMap<>();
                for (Location location : locations) {
                    blocks.put(Position.block(location), blockData);
                }
                for (Player player : players) {
                    player.sendMultiBlockChange(blocks);
                }
                return new Object[0];
            }
            });
        }
        LogUtils.info("已完成加载...");
    }

    private void loadElement(String configPath, String className) {
        if (!PluginConfig.enabled(configPath)) {
            return;
        }
        try {
            Class.forName(className, true, getClassLoader());
        } catch (Throwable throwable) {
            LogUtils.severe("无法加载功能 " + className + ": " + throwable.getMessage());
        }
    }

    private void registerFastContains() {
        if (!PluginConfig.enabled("features.conditions.fast-contains.enabled")) {
            return;
        }
        if (PluginConfig.bool("features.conditions.fast-contains.disable-when-vessel-present", true)
                && Bukkit.getPluginManager().isPluginEnabled("Vessel")) {
            LogUtils.info("检测到 Vessel 已启用，已禁用 fast contain 条件以避免语法冲突");
            return;
        }
        loadElement("features.conditions.fast-contains.enabled",
                "re.imc.skriptextra.elements.conditions.CondFastContains");
    }

    private boolean isWorldEditEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("WorldEdit")
                || Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit");
    }

    private void registerPacketEvents() {
        if (!PluginConfig.enabled("features.packet-events.enabled")
                || Bukkit.getPluginManager().getPlugin("PacketEvents") == null) {
            return;
        }
        boolean anyFeatureEnabled = PluginConfig.enabled("features.packet-events.view-distance.enabled")
                || PluginConfig.enabled("features.packet-events.simulation-distance.enabled")
                || PluginConfig.enabled("features.packet-events.entity-status-override.enabled")
                || PluginConfig.enabled("features.packet-events.biome-namespace-rewrite.enabled");
        if (!anyFeatureEnabled) {
            return;
        }
        PacketEvents.getAPI().getEventManager().registerListener(new BiomeSendingListener());
        packetEventsManaged = PluginConfig.bool("features.packet-events.manage-api-lifecycle", true);
        if (packetEventsManaged) {
            PacketEvents.getAPI().init();
        }
        LogUtils.info("已启用 PacketEvents 功能");
    }

    @Override
    public void onDisable() {
        LogUtils.info("正在卸载...");
        if (packetEventsManaged) {
            PacketEvents.getAPI().terminate();
        }
        instance = null;
        registry = null;
    }
}
