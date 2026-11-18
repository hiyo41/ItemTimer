package me.hiyo41.itemtimer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ItemTracker implements Listener {
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private final ItemTimer plugin;
    private final ConfigManager config;
    private BukkitTask updateTask;

    private final NamespacedKey timeKey;
    private final NamespacedKey maxTimeKey;
    private final Map<Material, Component> nameCache = new EnumMap<>(Material.class);
    private String[] formatParts;
    private TextColor pulsingColor;
    private TextColor progressFullColor;
    private TextColor progressEmptyColor;
    private Sound lowTimeSound;
    private String lowTimeSoundName;
    private int trackedCount = 0;

    public ItemTracker(ItemTimer plugin, ConfigManager config) {
        this.plugin = plugin;
        this.config = config;
        this.timeKey = new NamespacedKey(plugin, "timer");
        this.maxTimeKey = new NamespacedKey(plugin, "max_timer");
        reload();
        startTask();
    }

    private void startTask() {
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Set<Block> playedSounds = new HashSet<>();
            int countTemp = 0;
            
            for (World world : Bukkit.getWorlds()) {
                for (Item item : world.getEntitiesByClass(Item.class)) {
                    if (!item.isValid() || item.isDead()) continue;
                    
                    PersistentDataContainer pdc = item.getPersistentDataContainer();
                    
                    int remaining;
                    int max;
                    
                    if (pdc.has(timeKey, PersistentDataType.INTEGER)) {
                        remaining = pdc.get(timeKey, PersistentDataType.INTEGER);
                        max = pdc.getOrDefault(maxTimeKey, PersistentDataType.INTEGER, remaining);
                    } else {
                        // Not tracked previously
                        if (!shouldTrack(item)) continue;
                        remaining = getInitialTime(item);
                        max = remaining;
                        pdc.set(maxTimeKey, PersistentDataType.INTEGER, max);
                    }
                    
                    remaining--; // giam thoi gian tung giay
                    
                    if (remaining <= 0) {
                        if (config.isItemRemovalEnabled()) {
                            item.remove();
                        } else {
                            pdc.remove(timeKey);
                            pdc.remove(maxTimeKey);
                            item.setCustomNameVisible(false);
                        }
                        continue;
                    }
                    
                    countTemp++;
                    pdc.set(timeKey, PersistentDataType.INTEGER, remaining);
                    
                    if (config.isDisplayNameTags()) {
                        updateItemDisplay(item, remaining, max);
                    }
                    
                    // Sound when time is low
                    if (config.isLowTimeSoundEnabled() && remaining <= config.getLowTimeSoundTime()) {
                        Block b = item.getLocation().getBlock();
                        Sound sound = getLowTimeSound();
                        if (sound != null && !playedSounds.contains(b)) {
                            world.playSound(item.getLocation(), sound, config.getLowTimeSoundVolume(), config.getLowTimeSoundPitch());
                            playedSounds.add(b);
                        }
                    }
                }
            }
            this.trackedCount = countTemp;
        }, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        if (!shouldTrack(item)) return;

        int time = getInitialTime(item);
        PersistentDataContainer pdc = item.getPersistentDataContainer();
        pdc.set(timeKey, PersistentDataType.INTEGER, time);
        pdc.set(maxTimeKey, PersistentDataType.INTEGER, time);
        
        if (config.isDisplayNameTags()) {
            updateItemDisplay(item, time, time);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemMerge(ItemMergeEvent event) {
        Item target = event.getTarget();
        Item source = event.getEntity();
        
        PersistentDataContainer pdcT = target.getPersistentDataContainer();
        PersistentDataContainer pdcS = source.getPersistentDataContainer();

        if (pdcT.has(timeKey, PersistentDataType.INTEGER) && pdcS.has(timeKey, PersistentDataType.INTEGER)) {
            int timeT = pdcT.get(timeKey, PersistentDataType.INTEGER);
            int timeS = pdcS.get(timeKey, PersistentDataType.INTEGER);
            int maxT = pdcT.getOrDefault(maxTimeKey, PersistentDataType.INTEGER, timeT);
            int maxS = pdcS.getOrDefault(maxTimeKey, PersistentDataType.INTEGER, timeS);
            
            int newTime = Math.max(timeT, timeS);
            int newMax = Math.max(maxT, maxS);
            
            pdcT.set(timeKey, PersistentDataType.INTEGER, newTime);
            pdcT.set(maxTimeKey, PersistentDataType.INTEGER, newMax);
            
            if (config.isDisplayNameTags()) {
                Bukkit.getScheduler().runTask(plugin, () -> updateItemDisplay(target, newTime, newMax));
            }
        }
    }

    private boolean shouldTrack(Item item) {
        String world = item.getWorld().getName();
        if (!config.getEnabledWorlds().isEmpty() && !config.getEnabledWorlds().contains(world)) return false;
        if (config.getDisabledWorlds().contains(world)) return false;

        String type = item.getItemStack().getType().name();
        if (config.getExcludedItems().contains(type)) return false;
        if (!config.getWhitelistItems().isEmpty() && !config.getWhitelistItems().contains(type)) return false;

        return true;
    }

    private int getInitialTime(Item item) {
        String type = item.getItemStack().getType().name();
        Integer custom = config.getCustomTimer(type);
        return (custom != null) ? custom : config.getDefaultTimer();
    }

    private void updateItemDisplay(Item item, int seconds, int max) {
        ItemStack stack = item.getItemStack();
        ItemMeta meta = stack.getItemMeta();
        Component nameComponent;

        if (config.isUseCustomName() && meta != null && meta.hasDisplayName()) {
            nameComponent = meta.displayName();
        } else if (config.isTranslateItemNames()) {
            nameComponent = nameCache.computeIfAbsent(stack.getType(),
                    type -> Component.translatable(type.translationKey()));
        } else {
            nameComponent = Component.text(capitalize(stack.getType().name().replace("_", " ")));
        }

        boolean pulsing = config.isPulsingEnabled()
                && seconds <= config.getPulsingStartAt()
                && seconds % 2 == 0;
        Component timeComp = Component.text(String.valueOf(seconds)).color(pulsing ? pulsingColor : NamedTextColor.RED);

        Component progressComp = config.isProgressBarEnabled()
                ? getProgressBarComponent(seconds, max)
                : Component.empty();
        Component amountComp = Component.text(stack.getAmount()).color(NamedTextColor.GREEN);

        Component finalDisplay = parseFormat(timeComp, nameComponent, amountComp, progressComp);
        item.customName(finalDisplay);
        item.setCustomNameVisible(true);
    }

    private Component parseFormat(Component time, Component name, Component amount, Component progress) {
        Component result = Component.empty();
        if (formatParts == null) return result;

        for (String part : formatParts) {
            switch (part) {
                case "{time}":
                    result = result.append(time);
                    break;
                case "{name}":
                    result = result.append(name);
                    break;
                case "{amount}":
                    result = result.append(amount);
                    break;
                case "{progress}":
                    result = result.append(progress);
                    break;
                default:
                    result = result.append(LEGACY.deserialize(part.replace("{", "").replace("}", "")));
                    break;
            }
        }
        return result;
    }

    private Component getProgressBarComponent(int current, int max) {
        int size = config.getProgressSize();
        if (max <= 0) max = 1;
        int fullCount = (int) Math.round(((double) current / max) * size);
        fullCount = Math.min(size, Math.max(0, fullCount));

        String symbol = config.getProgressSymbol();
        Component fullPart = Component.text(symbol.repeat(fullCount)).color(progressFullColor);
        Component emptyPart = Component.text(symbol.repeat(size - fullCount)).color(progressEmptyColor);
        return fullPart.append(emptyPart);
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.toLowerCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (w.length() > 0) {
                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public void reload() {
        nameCache.clear();
        formatParts = config.getDisplayFormat().split("(?=\\{)|(?<=\\})");
        pulsingColor = colorOr(config.getPulsingColor(), NamedTextColor.DARK_RED);
        progressFullColor = colorOr(config.getProgressFullColor(), NamedTextColor.GREEN);
        progressEmptyColor = colorOr(config.getProgressEmptyColor(), NamedTextColor.GRAY);
        lowTimeSound = null;
        lowTimeSoundName = null;
    }

    private TextColor colorOr(String legacy, TextColor fallback) {
        if (legacy == null) return fallback;
        TextColor color = LEGACY.deserialize(legacy).color();
        return color != null ? color : fallback;
    }

    private Sound getLowTimeSound() {
        String name = config.getLowTimeSoundValue();
        if (!name.equals(lowTimeSoundName)) {
            lowTimeSoundName = name;
            try {
                lowTimeSound = Sound.valueOf(name);
            } catch (IllegalArgumentException e) {
                lowTimeSound = null;
            }
        }
        return lowTimeSound;
    }

    public void cleanup() {
        if (updateTask != null) updateTask.cancel();
    }

    public int getTrackedCount() {
        return trackedCount;
    }
}
