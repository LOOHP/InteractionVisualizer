package com.loohp.interactionvisualizer.objectholders;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.blocks.EnchantmentTableDisplay;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.managers.PacketManager;
import com.loohp.interactionvisualizer.managers.SoundManager;
import com.loohp.interactionvisualizer.utils.ComponentFont;
import com.loohp.interactionvisualizer.utils.CustomStringUtils;
import com.loohp.interactionvisualizer.utils.RomanNumberUtils;
import com.loohp.interactionvisualizer.utils.TranslationUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class EnchantmentTableAnimation {

    public static final EntryKey KEY = new EntryKey("enchantment_table");

    public static final int SET_ITEM = 0;
    public static final int PLAY_ENCHANTMENT = 1;
    public static final int PLAY_PICKUP = 2;
    public static final int CLOSE_TABLE = 3;

    private static final Map<Block, EnchantmentTableAnimation> tables = new ConcurrentHashMap<>();

    public static EnchantmentTableAnimation getTableAnimation(Block block, Player player) {
        EnchantmentTableAnimation animation = tables.get(block);
        if (animation == null) {
            animation = new EnchantmentTableAnimation(block, player);
            tables.put(block, animation);
            return animation;
        } else if (animation.getEnchanter().equals(player)) {
            return animation;
        } else {
            return null;
        }
    }
    private final Plugin plugin;
    private final Block block;
    private final Location location;
    private final Player enchanter;
    private final Queue<Supplier<CompletableFuture<Integer>>> taskQueue;
    private final AtomicBoolean enchanting;
    private Optional<Item> item;

    private EnchantmentTableAnimation(Block block, Player enchanter) {
        this.plugin = InteractionVisualizer.plugin;
        this.block = block;
        this.enchanter = enchanter;
        this.location = block.getLocation().clone();
        this.item = Optional.empty();
        this.enchanting = new AtomicBoolean(false);
        this.taskQueue = new ConcurrentLinkedQueue<>();
        tick();
    }

    private void tick() {
        InteractionVisualizer.asyncExecutorManager.runTaskLaterAsynchronously(() -> {
            run();
        }, 1);
    }

    private void run() {
        Supplier<CompletableFuture<Integer>> task = taskQueue.poll();
        if (task != null) {
            int result = -1;
            try {
                CompletableFuture<Integer> future = task.get();
                if (future != null) {
                    result = future.get();
                } else {
                    run();
                    return;
                }
            } catch (Throwable e) {
            }
            if (result != CLOSE_TABLE) {
                tick();
            } else {
                tables.remove(block);
            }
        } else {
            tick();
        }
    }

    @SuppressWarnings("deprecation")
    private CompletableFuture<Integer> playEnchantAnimation(Map<Enchantment, Integer> enchantsToAdd, Integer expCost, ItemStack itemstack) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        if (item.isPresent() && item.get().isLocked()) {
            future.complete(PLAY_ENCHANTMENT);
            return future;
        }

        this.enchanting.set(true);

        if (!this.item.isPresent()) {
            this.item = Optional.of(new Item(location.clone().add(0.5, 1.3, 0.5)));
            PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item.get());
        }

        Item item = this.item.get();

        item.setItemStack(itemstack);
        item.setGravity(false);
        item.setLocked(true);
        item.setVelocity(new Vector(0.0, 0.05, 0.0));
        PacketManager.updateItem(item);
        for (Player each : InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY)) {
            each.spawnParticle(Particle.PORTAL, location.clone().add(0.5, 2.6, 0.5), 200);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            item.teleport(location.clone().add(0.5, 2.3, 0.5));
            item.setVelocity(new Vector(0, 0, 0));
            PacketManager.updateItem(item);
        }, 20);

        List<ArmorStand> stands = new LinkedList<>();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Location standloc = item.getLocation().add(0.0, 0.5, 0.0);
            for (Entry<Enchantment, Integer> entry : enchantsToAdd.entrySet()) {
                Enchantment ench = entry.getKey();
                int level = entry.getValue();
                String str = TranslationUtils.getEnchantment(ench);
                if (!EnchantmentTableDisplay.getTranslatableEnchantments().contains(str)) {
                    str = null;
                }
                Component enchantmentName = (str == null || str.equals("")) ? ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(CustomStringUtils.capitalize(ench.getName().toLowerCase().replace("_", " ")))) : Component.translatable(str);
                ArmorStand stand = new ArmorStand(standloc);
                if (ench.getMaxLevel() != 1 || level != 1) {
                    enchantmentName = enchantmentName.append(ComponentFont.parseFont(LegacyComponentSerializer.legacySection().deserialize(" " + ChatColor.AQUA + RomanNumberUtils.toRoman(entry.getValue()))));
                }
                if (ench.isCursed()) {
                    enchantmentName = enchantmentName.color(NamedTextColor.RED);
                } else {
                    enchantmentName = enchantmentName.color(NamedTextColor.AQUA);
                }
                stand.setCustomName(enchantmentName);
                stand.setCustomNameVisible(true);
                setStand(stand);
                PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), stand);
                stands.add(stand);
                standloc.add(0.0, 0.3, 0.0);
            }

            ArmorStand stand = new ArmorStand(standloc);
            TranslatableComponent levelTrans = Component.translatable(TranslationUtils.getLevel(expCost));
            if (expCost != 1) {
                levelTrans = levelTrans.args(Component.text(expCost));
            }
            levelTrans = levelTrans.color(NamedTextColor.GREEN);
            stand.setCustomName(levelTrans);
            stand.setCustomNameVisible(true);
            setStand(stand);
            PacketManager.sendArmorStandSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.HOLOGRAM, KEY), stand);
            stands.add(stand);

            PacketManager.updateItem(item);
        }, 50);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            while (!stands.isEmpty()) {
                ArmorStand stand = stands.remove(0);
                PacketManager.removeArmorStand(InteractionVisualizerAPI.getPlayers(), stand);
            }
            item.setGravity(true);
            PacketManager.updateItem(item);
        }, 90);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            item.teleport(location.clone().add(0.5, 1.3, 0.5));
            item.setGravity(false);
            PacketManager.updateItem(item);
            item.setLocked(false);
            future.complete(PLAY_ENCHANTMENT);

            this.enchanting.set(false);
        }, 98);
        return future;
    }

    private CompletableFuture<Integer> playPickUpAnimation(ItemStack itemstack) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        if (!item.isPresent()) {
            future.complete(PLAY_PICKUP);
            return future;
        }
        Item item = this.item.get();
        item.setLocked(true);
        item.setItemStack(itemstack);
        if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
            future.complete(PLAY_PICKUP);
            return future;
        }

        Vector lift = new Vector(0.0, 0.15, 0.0);
        Vector pickup = enchanter.getEyeLocation().add(0.0, -0.5, 0.0).add(0.0, InteractionVisualizer.playerPickupYOffset, 0.0).toVector().subtract(location.clone().add(0.5, 1.2, 0.5).toVector()).multiply(0.15).add(lift);
        item.setVelocity(pickup);
        item.setGravity(true);
        item.setPickupDelay(32767);
        PacketManager.updateItem(item);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            SoundManager.playItemPickup(item.getLocation(), InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY));
            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item);
            this.item = Optional.empty();
            future.complete(PLAY_PICKUP);
        }, 8);
        return future;
    }

    private CompletableFuture<Integer> close() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (this.item.isPresent()) {
                PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item.get());
            }
            future.complete(CLOSE_TABLE);
        });
        return future;
    }

    private CompletableFuture<Integer> setItemStack(ItemStack itemstack) {
        CompletableFuture<Integer> future = new CompletableFuture<>();

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (itemstack == null || itemstack.getType().equals(Material.AIR)) {
                clearItemStack();
                future.complete(SET_ITEM);
                return;
            }
            if (this.item.isPresent()) {
                this.item.get().setItemStack(itemstack);
                PacketManager.updateItem(item.get());
            } else {
                this.item = Optional.of(new Item(location.clone().add(0.5, 1.3, 0.5)));
                this.item.get().setItemStack(itemstack);
                PacketManager.sendItemSpawn(InteractionVisualizerAPI.getPlayerModuleList(Modules.ITEMDROP, KEY), item.get());
                PacketManager.updateItem(item.get());
            }
            future.complete(SET_ITEM);
        });

        return future;
    }

    private void clearItemStack() {
        if (this.item.isPresent()) {
            PacketManager.removeItem(InteractionVisualizerAPI.getPlayers(), item.get());
            this.item = Optional.empty();
        }
    }

    private void setStand(ArmorStand stand) {
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setVisible(true);
        stand.setInvulnerable(true);
        stand.setBasePlate(false);
        stand.setVisible(false);
    }

    public ItemStack getItemStack() {
        return item.isPresent() ? item.get().getItemStack() : null;
    }

    public Player getEnchanter() {
        return enchanter;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isEnchanting() {
        return enchanting.get();
    }

    public void queueSetItem(ItemStack itemstack, Predicate<EnchantmentTableAnimation> condition) {
        taskQueue.add(() -> {
            if (condition.test(this)) {
                return setItemStack(itemstack == null ? null : itemstack.clone());
            } else {
                return null;
            }
        });
    }

    public void queueEnchant(Map<Enchantment, Integer> enchantsToAdd, int expCost, ItemStack itemstack, Predicate<EnchantmentTableAnimation> condition) {
        taskQueue.add(() -> {
            if (condition.test(this)) {
                return playEnchantAnimation(enchantsToAdd, expCost, itemstack == null ? null : itemstack.clone());
            } else {
                return null;
            }
        });
    }

    public void queuePickupAnimation(ItemStack itemstack, Predicate<EnchantmentTableAnimation> condition) {
        taskQueue.add(() -> {
            if (condition.test(this)) {
                return playPickUpAnimation(itemstack == null ? null : itemstack.clone());
            } else {
                return null;
            }
        });
    }

    public void queueClose(Predicate<EnchantmentTableAnimation> condition) {
        taskQueue.add(() -> {
            if (condition.test(this)) {
                return close();
            } else {
                return null;
            }
        });
    }

}
