package wtf.dupers.dupersunited.features;

import com.google.gson.*;
import wtf.dupers.dupersunited.MainClient;
import wtf.dupers.dupersunited.SharedVariables;
import wtf.dupers.dupersunited.features.screens.ClickGui;
import wtf.dupers.dupersunited.features.screens.hud.HudEditorScreen;
import wtf.dupers.dupersunited.features.screens.hud.HudElement;
import wtf.dupers.dupersunited.api.keybind.Keybind;
import wtf.dupers.dupersunited.keybinds.KeybindManager;
import wtf.dupers.dupersunited.api.module.Module;
import wtf.dupers.dupersunited.modules.render.BlockEspModule;
import wtf.dupers.dupersunited.modules.render.EspModule;
import wtf.dupers.dupersunited.modules.render.NoRenderModule;
import wtf.dupers.dupersunited.api.module.settings.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ConfigManager {

    private static final Path CONFIG_FILE = SharedVariables.DIRECTORY.resolve("config.json");

    private static final List<HudElement> HUD_ELEMENTS = List.of(
        HudEditorScreen.WATERMARK,
        //HudEditorScreen.MACRO,
        HudEditorScreen.SAVED_GUI,
        HudEditorScreen.TPS,
        HudEditorScreen.HUD_LIST
    );

    public static boolean autoReconnectEnabled = false;
    public static boolean rpBypassEnabled = false;
    public static boolean brandSpoofEnabled = false;
    public static boolean serverAlertsEnabled = true;
    public static String addonToken = "";
    public static String addonApiBaseUrl = "https://dupersunited-server.dupers.wtf";
    public static String addonWsBaseUrl = "wss://dupersunited-server.dupers.wtf/ws/private";

    public static boolean firstLaunch = true;

    public static void save() {
        AsyncConfigs.save(serialize(), CONFIG_FILE, "config");
    }

    public static void saveBlocking() {
        AsyncConfigs.saveSync(serialize(), CONFIG_FILE, "config");
    }

    private static JsonObject serialize() {
        JsonObject root = new JsonObject();

        root.addProperty("firstLaunch", firstLaunch);
        root.addProperty("autoReconnectEnabled", autoReconnectEnabled);
        root.addProperty("rpBypassEnabled", rpBypassEnabled);
        root.addProperty("brandSpoofEnabled", brandSpoofEnabled);
        root.addProperty("serverAlertsEnabled", serverAlertsEnabled);
        root.addProperty("addonToken", addonToken);
        root.addProperty("addonApiBaseUrl", addonApiBaseUrl);
        root.addProperty("addonWsBaseUrl", addonWsBaseUrl);

        JsonObject hudObj = new JsonObject();
        for (HudElement el : HUD_ELEMENTS) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", el.x);
            pos.addProperty("y", el.y);
            pos.addProperty("scale", el.scale);
            hudObj.add(el.id, pos);
        }
        root.add("hud", hudObj);

        for (Module module : MainClient.MODULE_MANAGER.modules()) {
            JsonObject modObj = new JsonObject();

            modObj.addProperty("keybind", module.getKeybind());
            modObj.addProperty("enabled", module.isEnabled());

            if (!module.getSettings().isEmpty()) {
                JsonObject settingsObj = new JsonObject();
                for (Setting<?> s : module.getSettings()) {
                    if (s instanceof FloatSetting fs) {
                        settingsObj.addProperty(s.getName(), fs.getValue());
                    } else if (s instanceof BooleanSetting bs) {
                        settingsObj.addProperty(s.getName(), bs.getValue());
                    } else if (s instanceof ModeSetting ms) {
                        settingsObj.addProperty(s.getName(), ms.getValue());
                    } else if (s instanceof EnumSetting<?> es) {
                        settingsObj.addProperty(s.getName(), es.getValue().toString());
                    } else if (s instanceof StringSetting ss) {
                        settingsObj.addProperty(s.getName(), ss.getValue());
                    } else if (s instanceof BindSetting bs) {
                        settingsObj.addProperty(s.getName(), bs.getValue());
                    }
                }
                modObj.add("settings", settingsObj);
            }

            root.add(module.getIdentifier(), modObj);
        }

        JsonObject keybindsObj = new JsonObject();
        for (Keybind kb : KeybindManager.getRegisteredKeybinds().values()) {
            keybindsObj.addProperty(kb.getName(), kb.getKeyCode());
        }
        root.add("keybinds", keybindsObj);

        JsonArray espBlocks = new JsonArray();
        for (Block block : BlockEspModule.selectedBlocks) espBlocks.add(Registries.BLOCK.getId(block).toString());
        root.add("blockEspBlocks", espBlocks);

        JsonArray noRenderEntities = new JsonArray();
        for (EntityType<?> type : NoRenderModule.selectedEntityIds) noRenderEntities.add(Registries.ENTITY_TYPE.getId(type).toString());
        root.add("noRenderEntities", noRenderEntities);

        JsonArray espEntities = new JsonArray();
        for (EntityType<?> type : EspModule.selectedEntityIds) espEntities.add(Registries.ENTITY_TYPE.getId(type).toString());
        root.add("espEntities", espEntities);

        JsonObject clickguiCategories = new JsonObject();
        ClickGui.customCategories.forEach(clickguiCategories::addProperty);
        root.add("clickguiCategories", clickguiCategories);

        JsonObject clickguiCategoryPositions = new JsonObject();
        ClickGui.categoryPositions.forEach((category, pos) -> {
            JsonObject position = new JsonObject();
            position.addProperty("x", pos.x());
            position.addProperty("y", pos.y());
            clickguiCategoryPositions.add(category, position);
        });
        root.add("clickguiCategoryPositions", clickguiCategoryPositions);

        JsonObject clickguiCategoryExpandedModules = new JsonObject();
        ClickGui.categoryExpandedModules.forEach((category, modules) -> {
            JsonArray expandedModules = new JsonArray();
            modules.forEach(expandedModules::add);
            clickguiCategoryExpandedModules.add(category, expandedModules);
        });
        root.add("clickguiCategoryExpandedModules", clickguiCategoryExpandedModules);

        JsonObject clickguiCategoryCollapsed = new JsonObject();
        ClickGui.categoryCollapsed.forEach(clickguiCategoryCollapsed::addProperty);
        root.add("clickguiCategoryCollapsed", clickguiCategoryCollapsed);

        if (ClickGui.hudPanelPosition != null) {
            JsonObject clickguiHudPanelPosition = new JsonObject();
            clickguiHudPanelPosition.addProperty("x", ClickGui.hudPanelPosition.x());
            clickguiHudPanelPosition.addProperty("y", ClickGui.hudPanelPosition.y());
            root.add("clickguiHudPanelPosition", clickguiHudPanelPosition);
        }

        return root;
    }

    public static CompletableFuture<Void> load() {
        return AsyncConfigs.load(JsonObject.class, CONFIG_FILE, "config").thenAccept(root -> {
            if (root.has("firstLaunch")) {
                firstLaunch = root.get("firstLaunch").getAsBoolean();
            }
            if (root.has("autoReconnectEnabled")) {
                autoReconnectEnabled = root.get("autoReconnectEnabled").getAsBoolean();
            }
            if (root.has("rpBypassEnabled")) {
                rpBypassEnabled = root.get("rpBypassEnabled").getAsBoolean();
            }
            if (root.has("brandSpoofEnabled")) {
                brandSpoofEnabled = root.get("brandSpoofEnabled").getAsBoolean();
            }
            if (root.has("serverAlertsEnabled ")) {
                serverAlertsEnabled = root.get("serverAlertsEnabled ").getAsBoolean();
            }
            if (root.has("addonToken")) {
                addonToken = root.get("addonToken").getAsString().trim();
            }
            if (root.has("addonApiBaseUrl")) {
                addonApiBaseUrl = root.get("addonApiBaseUrl").getAsString().trim();
            }
            if (root.has("addonWsBaseUrl")) {
                addonWsBaseUrl = root.get("addonWsBaseUrl").getAsString().trim();
            }

            if (root.has("hud")) {
                JsonObject hudObj = root.getAsJsonObject("hud");
                for (HudElement el : HUD_ELEMENTS) {
                    if (!hudObj.has(el.id)) continue;
                    JsonObject pos = hudObj.getAsJsonObject(el.id);
                    if (pos.has("x")) el.x = pos.get("x").getAsInt();
                    if (pos.has("y")) el.y = pos.get("y").getAsInt();
                    if (pos.has("scale")) el.scale = pos.get("scale").getAsFloat();
                }
            }

            for (Module module : MainClient.MODULE_MANAGER.modules()) {
                boolean useIdentifier = root.has(module.getIdentifier());
                if (!useIdentifier && !root.has(module.getName())) continue;

                JsonObject modObj = root.getAsJsonObject(useIdentifier ? module.getIdentifier() : module.getName());

                if (modObj.has("keybind")) {
                    module.setKeybind(modObj.get("keybind").getAsInt());
                }
                if (modObj.has("enabled")) {
                    module.setEnabled(modObj.get("enabled").getAsBoolean());
                }
                if (modObj.has("settings")) {
                    JsonObject settingsObj = modObj.getAsJsonObject("settings");
                    for (Setting<?> s : module.getSettings()) {
                        if (!settingsObj.has(s.getName())) continue;
                        JsonElement el = settingsObj.get(s.getName());
                        try {
                            if (s instanceof FloatSetting fs) {
                                fs.setValue(el.getAsFloat());
                            } else if (s instanceof BooleanSetting bs) {
                                bs.setValue(el.getAsBoolean());
                            } else if (s instanceof ModeSetting ms) {
                                ms.setValue(el.getAsString());
                            } else if (s instanceof EnumSetting<?> es) {
                                es.setValue(el.getAsString());
                            } else if (s instanceof StringSetting ss) {
                                ss.setValue(el.getAsString());
                            } else if (s instanceof BindSetting bs) {
                                bs.setKeyCode(el.getAsInt());
                            }
                        } catch (Exception e) {
                            MainClient.LOGGER.error("Bad value for {}.{}: '{}'", module.getName(), s.getName(), el, e);
                        }
                    }
                }
            }

            if (root.has("keybinds")) {
                JsonObject keybindsObj = root.getAsJsonObject("keybinds");
                for (Keybind kb : KeybindManager.getRegisteredKeybinds().values()) {
                    if (keybindsObj.has(kb.getName())) {
                        kb.setKeyCode(keybindsObj.get(kb.getName()).getAsInt());
                    }
                }
            }

            if (root.has("blockEspBlocks")) {
                BlockEspModule.selectedBlocks.clear();
                for (JsonElement el : root.getAsJsonArray("blockEspBlocks")) {
                    Registries.BLOCK.getEntry(Identifier.tryParse(el.getAsString())).ifPresent(entry -> BlockEspModule.selectedBlocks.add(entry.value()));
                }
            }

            if (root.has("noRenderEntities")) {
                NoRenderModule.selectedEntityIds.clear();
                for (JsonElement el : root.getAsJsonArray("noRenderEntities")) {
                    Registries.ENTITY_TYPE.getEntry(Identifier.tryParse(el.getAsString())).ifPresent(entry -> NoRenderModule.selectedEntityIds.add(entry.value()));
                }
            }

            if (root.has("espEntities")) {
                EspModule.selectedEntityIds.clear();
                for (JsonElement el : root.getAsJsonArray("espEntities")) {
                    Registries.ENTITY_TYPE.getEntry(Identifier.tryParse(el.getAsString())).ifPresent(entry -> EspModule.selectedEntityIds.add(entry.value()));
                }
            }

            if (root.has("clickguiCategories")) {
                root.getAsJsonObject("clickguiCategories").asMap().forEach((module, category) -> {
                    ClickGui.customCategories.put(module, category.getAsString());
                });
            }

            if (root.has("clickguiCategoryPositions")) {
                root.getAsJsonObject("clickguiCategoryPositions").asMap().forEach((category, position) -> {
                    JsonObject positionObject = position.getAsJsonObject();
                    ClickGui.categoryPositions.computeIfAbsent(category, k -> new Vector2i())
                        .set(positionObject.get("x").getAsInt(), positionObject.get("y").getAsInt());
                });
            }

            if (root.has("clickguiCategoryExpandedModules")) {
                root.getAsJsonObject("clickguiCategoryExpandedModules").asMap().forEach((category, expandedModules) -> {
                    Set<String> set = ClickGui.categoryExpandedModules.computeIfAbsent(category, k -> new ObjectOpenHashSet<>());
                    expandedModules.getAsJsonArray().asList().forEach(el -> set.add(el.getAsString()));
                });
            }

            if (root.has("clickguiCategoryCollapsed")) {
                root.getAsJsonObject("clickguiCategoryCollapsed").asMap().forEach((category, collapsed) -> {
                    ClickGui.categoryCollapsed.put(category, collapsed.getAsBoolean());
                });
            }

            if (root.has("clickguiHudPanelPosition")) {
                JsonObject position = root.getAsJsonObject("clickguiHudPanelPosition");
                ClickGui.hudPanelPosition = new Vector2i(position.get("x").getAsInt(), position.get("y").getAsInt());
            }
        });
    }
}