/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2024. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2024. Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.loohp.interactionvisualizer.nms;

import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.ItemFrame;
import com.loohp.interactionvisualizer.entityholders.VisualizerEntity;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;
import com.loohp.interactionvisualizer.objectholders.ChunkPosition;
import com.loohp.interactionvisualizer.objectholders.NMSTileEntitySet;
import com.loohp.interactionvisualizer.objectholders.TileEntity;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;
import com.loohp.interactionvisualizer.objectholders.WrappedIterable;
import com.loohp.interactionvisualizer.utils.ReflectionUtils;
import com.loohp.interactionvisualizer.utils.UnsafeAccessor;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.Vector3f;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutAnimation;
import net.minecraft.network.protocol.game.PacketPlayOutEntityDestroy;
import net.minecraft.network.protocol.game.PacketPlayOutEntityEquipment;
import net.minecraft.network.protocol.game.PacketPlayOutEntityMetadata;
import net.minecraft.network.protocol.game.PacketPlayOutEntityTeleport;
import net.minecraft.network.protocol.game.PacketPlayOutEntityVelocity;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntity;
import net.minecraft.network.syncher.DataWatcher;
import net.minecraft.network.syncher.DataWatcherObject;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EnumItemSlot;
import net.minecraft.world.entity.decoration.EntityArmorStand;
import net.minecraft.world.entity.decoration.EntityItemFrame;
import net.minecraft.world.entity.item.EntityItem;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_21_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_21_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_21_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_21_R1.util.CraftMagicNumbers;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V1_21 extends NMSWrapper {

    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;
    private final Field dataWatcherItemItemField;
    private final Field[] entityTeleportPacketFields;

    //spigot specific
    private Field spigotWorldConfigField;
    private Field spigotItemDespawnRateField;

    //paper
    private Field paperItemDespawnRateField;
    private Method worldServerEntityLookup;

    public V1_21() {
        try {
            entityCountField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, AtomicInteger.class, "ENTITY_COUNTER", "c");
            dataWatcherByteField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_SHARED_FLAGS_ID", "ap");
            dataWatcherCustomNameField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_CUSTOM_NAME", "aQ");
            dataWatcherCustomNameVisibleField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_CUSTOM_NAME_VISIBLE", "aR");
            dataWatcherSilentField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_SILENT", "aS");
            dataWatcherNoGravityField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, DataWatcherObject.class, "DATA_NO_GRAVITY", "aT");
            dataWatcherItemItemField = ReflectionUtils.findDeclaredField(EntityItem.class, DataWatcherObject.class, "DATA_ITEM", "d");
            entityTeleportPacketFields = PacketPlayOutEntityTeleport.class.getDeclaredFields();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            //spigot specific
            //noinspection JavaReflectionMemberAccess
            spigotWorldConfigField = net.minecraft.world.level.World.class.getField("spigotConfig");
            spigotItemDespawnRateField = spigotWorldConfigField.getType().getField("itemDespawnRate");
        } catch (NoSuchFieldException ignore) {
        }

        try {
            //paper
            //noinspection JavaReflectionMemberAccess
            paperItemDespawnRateField = EntityItem.class.getDeclaredField("despawnRate");
            //noinspection JavaReflectionMemberAccess
            worldServerEntityLookup = WorldServer.class.getMethod("moonrise$getEntityLookup");
        } catch (NoSuchMethodException | NoSuchFieldException ignore) {
        }
    }

    @Override
    public Enchantment getPowerEnchantment() {
        return Enchantment.POWER;
    }

    @Override
    public Particle getItemCrackParticle() {
        return Particle.ITEM;
    }

    @Override
    public int getItemAge(Item item) {
        return ((CraftItem) item).getHandle().i;
    }

    @Override
    public int getItemDamage(ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof Damageable) {
            return ((Damageable) itemMeta).getDamage();
        }
        return 0;
    }

    @Override
    public Component getItemHoverName(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.w()));
    }

    @Override
    public PacketPlayOutEntityEquipment[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = new ArrayList<>();
        for (ValuePairs<EquipmentSlot, ItemStack> pair : equipments) {
            EnumItemSlot nmsSlot = CraftEquipmentSlot.getNMS(pair.getFirst());
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
            nmsEquipments.add(new Pair<>(nmsSlot, nmsItem));
        }
        return new PacketPlayOutEntityEquipment[] {new PacketPlayOutEntityEquipment(entityId, nmsEquipments)};
    }

    @Override
    public PacketPlayOutEntityDestroy[] createEntityDestroyPacket(int... entityIds) {
        return new PacketPlayOutEntityDestroy[] {new PacketPlayOutEntityDestroy(entityIds)};
    }

    @SuppressWarnings("unchecked")
    @Override
    public PacketPlayOutEntityMetadata createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        return new PacketPlayOutEntityMetadata(entityId, (List<DataWatcher.c<?>>) dataWatchers);
    }

    @Override
    public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        net.minecraft.core.BlockPosition blockpos = new net.minecraft.core.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
        WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
        VoxelShape shape = world.a_(blockpos).j(world, blockpos);
        return shape.e().stream().map(each -> new BoundingBox(each.a + pos.getX(), each.b + pos.getY(), each.c + pos.getZ(), each.d + pos.getX(), each.e + pos.getY(), each.f + pos.getZ())).collect(Collectors.toList());
    }

    @Override
    public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
        if (!chunk.isLoaded() && !load) {
            return null;
        }
        World world = chunk.getWorld();
        Chunk nmsChunk = ((CraftWorld) world).getHandle().getChunkIfLoaded(chunk.getChunkX(), chunk.getChunkZ());
        return new NMSTileEntitySet<>(nmsChunk.k, entry -> {
            net.minecraft.core.BlockPosition pos = entry.getKey();
            Material type = CraftMagicNumbers.getMaterial(entry.getValue().n().b());
            TileEntity.TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
            if (tileEntityType != null) {
                return new TileEntity(world, pos.u(), pos.v(), pos.w(), tileEntityType);
            } else {
                return null;
            }
        });
    }

    @Override
    public int getItemDespawnRate(Item item) {
        try {
            if (paperItemDespawnRateField != null) {
                paperItemDespawnRateField.setAccessible(true);
                return paperItemDespawnRateField.getInt(((CraftItem) item).getHandle());
            }
            if (spigotWorldConfigField != null && spigotItemDespawnRateField != null) {
                Object spigotWorldConfig = spigotWorldConfigField.get(((CraftWorld) item.getWorld()).getHandle());
                return spigotItemDespawnRateField.getInt(spigotWorldConfig);
            }
        } catch (Throwable ignore) {
        }
        return 6000;
    }

    @Override
    public String getBannerCustomName(Block block) {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        net.minecraft.world.level.block.entity.TileEntity tileEntity = worldServer.c_(new net.minecraft.core.BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (tileEntity == null) {
            return "";
        }
        return tileEntity.a(worldServer.H_()).l("CustomName");
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedIterable<?, Entity> getEntities(World world) {
        try {
            WorldServer worldServer = ((CraftWorld) world).getHandle();
            LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter;
            if (worldServerEntityLookup == null) {
                levelEntityGetter = worldServer.N.d();
            } else {
                levelEntityGetter = (LevelEntityGetter<net.minecraft.world.entity.Entity>) worldServerEntityLookup.invoke(worldServer);
            }
            return new WrappedIterable<>(levelEntityGetter.a(), net.minecraft.world.entity.Entity::getBukkitEntity);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Future<Integer> getNextEntityId() {
        try {
            entityCountField.setAccessible(true);
            AtomicInteger counter = (AtomicInteger) entityCountField.get(null);
            return CompletableFuture.completedFuture(counter.incrementAndGet());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChatColor getRarityColor(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.y().a().toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.g().a();
    }

    @Override
    public String getEnchantmentTranslationKey(Enchantment enchantment) {
        NamespacedKey namespacedKey = enchantment.getKey();
        return "enchantment." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }

    @Override
    public String getEffectTranslationKey(PotionEffectType type) {
        NamespacedKey namespacedKey = type.getKey();
        return "effect." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }

    @Override
    public EntityType getEntityType(VisualizerEntity entity) {
        if (entity instanceof ArmorStand) {
            return EntityType.ARMOR_STAND;
        } else if (entity instanceof com.loohp.interactionvisualizer.entityholders.Item) {
            return EntityType.ITEM;
        } else if (entity instanceof ItemFrame) {
            return EntityType.ITEM_FRAME;
        }
        throw new RuntimeException("Unknown VisualizerEntity class " + entity.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.c<?>> getWatchableCollection(ArmorStand stand) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = (byte) 0;
            bitmask = !stand.isVisible() ? (byte) (bitmask | 0x20) : bitmask;
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Component customName = stand.getCustomName();
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), stand.isCustomNameVisible()));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), stand.isSilent()));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !stand.hasGravity()));

            byte standbitmask = (byte) 0;
            standbitmask = stand.isSmall() ? (byte) (standbitmask | 0x01) : standbitmask;
            standbitmask = stand.hasArms() ? (byte) (standbitmask | 0x04) : standbitmask;
            standbitmask = !stand.hasBasePlate() ? (byte) (standbitmask | 0x08) : standbitmask;
            standbitmask = stand.isMarker() ? (byte) (standbitmask | 0x10) : standbitmask;

            dataWatcher.add(DataWatcher.c.a(EntityArmorStand.bH, standbitmask));

            Vector3f headrotation = new Vector3f((float) Math.toDegrees(stand.getHeadPose().getX()), (float) Math.toDegrees(stand.getHeadPose().getY()), (float) Math.toDegrees(stand.getHeadPose().getZ()));
            dataWatcher.add(DataWatcher.c.a(EntityArmorStand.bI, headrotation));

            Vector3f rightarmrotation = new Vector3f((float) Math.toDegrees(stand.getRightArmPose().getX()), (float) Math.toDegrees(stand.getRightArmPose().getY()), (float) Math.toDegrees(stand.getRightArmPose().getZ()));
            dataWatcher.add(DataWatcher.c.a(EntityArmorStand.bL, rightarmrotation));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.c<?>> getWatchableCollection(com.loohp.interactionvisualizer.entityholders.Item item) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);
            dataWatcherItemItemField.setAccessible(true);

            byte bitmask = (byte) 0;
            bitmask = item.isGlowing() ? (byte) (bitmask | 0x40) : bitmask;
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Component customName = item.getCustomName();
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), item.isCustomNameVisible()));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !item.hasGravity()));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<net.minecraft.world.item.ItemStack>) dataWatcherItemItemField.get(null), CraftItemStack.asNMSCopy(item.getItemStack())));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.c<?>> getWatchableCollection(ItemFrame frame) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherSilentField.setAccessible(true);

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), frame.isSilent()));

            dataWatcher.add(DataWatcher.c.a(EntityItemFrame.f, CraftItemStack.asNMSCopy(frame.getItem())));
            dataWatcher.add(DataWatcher.c.a(EntityItemFrame.g, frame.getFrameRotation()));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.c<?>> createCustomNameWatchableCollection(Component name) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            boolean visible = name != null;

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(name)))));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.c<?>> resetCustomNameWatchableCollection(Entity entity) {
        try {
            List<DataWatcher.c<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.ar();

            Optional<IChatBaseComponent> name = watcher.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null));

            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(DataWatcher.c.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendHandMovement(Collection<Player> players, Player entity) {
        EntityPlayer entityPlayer = ((CraftPlayer) entity).getHandle();
        PacketPlayOutAnimation packet = new PacketPlayOutAnimation(entityPlayer, 0);
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    @Override
    public void teleportEntity(Player player, int entityId, Location location) {
        try {
            PacketPlayOutEntityTeleport packet = (PacketPlayOutEntityTeleport) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityTeleport.class);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);
            entityTeleportPacketFields[7].setAccessible(true);

            entityTeleportPacketFields[1].setInt(packet, entityId);
            entityTeleportPacketFields[2].setDouble(packet, location.getX());
            entityTeleportPacketFields[3].setDouble(packet, location.getY());
            entityTeleportPacketFields[4].setDouble(packet, location.getZ());
            entityTeleportPacketFields[5].setByte(packet, (byte) (int) (location.getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setByte(packet, (byte) (int) (location.getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[7].setBoolean(packet, false);
            sendPacket(player, packet);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnArmorStand(Collection<Player> players, ArmorStand entity) {
        EntityTypes<EntityArmorStand> type = EntityTypes.d;
        Vec3D velocity = new Vec3D(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

        List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
        PacketPlayOutEntityMetadata packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        List<ValuePairs<EquipmentSlot, ItemStack>> equipments = new ArrayList<>();
        equipments.add(new ValuePairs<>(EquipmentSlot.HAND, entity.getItemInMainHand()));
        equipments.add(new ValuePairs<>(EquipmentSlot.HEAD, entity.getHelmet()));
        PacketPlayOutEntityEquipment[] packet3 = createEntityEquipmentPacket(entity.getEntityId(), equipments);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
            for (PacketPlayOutEntityEquipment packet : packet3) {
                sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateArmorStand(Collection<Player> players, ArmorStand entity) {
        try {
            PacketPlayOutEntityTeleport packet1 = (PacketPlayOutEntityTeleport) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityTeleport.class);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);
            entityTeleportPacketFields[7].setAccessible(true);

            entityTeleportPacketFields[1].setInt(packet1, entity.getEntityId());
            entityTeleportPacketFields[2].setDouble(packet1, entity.getLocation().getX());
            entityTeleportPacketFields[3].setDouble(packet1, entity.getLocation().getY());
            entityTeleportPacketFields[4].setDouble(packet1, entity.getLocation().getZ());
            entityTeleportPacketFields[5].setByte(packet1, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setByte(packet1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[7].setBoolean(packet1, false);

            List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
            PacketPlayOutEntityMetadata packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

            List<ValuePairs<EquipmentSlot, ItemStack>> equipments = new ArrayList<>();
            equipments.add(new ValuePairs<>(EquipmentSlot.HAND, entity.getItemInMainHand()));
            equipments.add(new ValuePairs<>(EquipmentSlot.HEAD, entity.getHelmet()));
            PacketPlayOutEntityEquipment[] packet3 = createEntityEquipmentPacket(entity.getEntityId(), equipments);

            for (Player player : players) {
                sendPacket(player, packet1);
                sendPacket(player, packet2);
                for (PacketPlayOutEntityEquipment packet : packet3) {
                    sendPacket(player, packet);
                }
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateArmorStandOnlyMeta(Collection<Player> players, ArmorStand entity) {
        List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
        PacketPlayOutEntityMetadata packet = createEntityMetadataPacket(entity.getEntityId(), watcher);
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    @Override
    public void removeArmorStand(Collection<Player> players, ArmorStand entity) {
        PacketPlayOutEntityDestroy[] packets = createEntityDestroyPacket(entity.getEntityId());
        for (Player player : players) {
            for (PacketPlayOutEntityDestroy packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity) {
        if (entity.getItemStack().getType().equals(Material.AIR)) {
            return;
        }

        EntityTypes<EntityItem> type = EntityTypes.ag;
        Vec3D velocity = new Vec3D(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

        List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
        PacketPlayOutEntityMetadata packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity) {
        try {
            List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
            PacketPlayOutEntityMetadata packet1 = createEntityMetadataPacket(entity.getEntityId(), watcher);

            PacketPlayOutEntityTeleport packet2 = (PacketPlayOutEntityTeleport) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityTeleport.class);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);
            entityTeleportPacketFields[7].setAccessible(true);

            entityTeleportPacketFields[1].setInt(packet2, entity.getEntityId());
            entityTeleportPacketFields[2].setDouble(packet2, entity.getLocation().getX());
            entityTeleportPacketFields[3].setDouble(packet2, entity.getLocation().getY());
            entityTeleportPacketFields[4].setDouble(packet2, entity.getLocation().getZ());
            entityTeleportPacketFields[5].setByte(packet2, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setByte(packet2, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[7].setBoolean(packet2, false);

            Vec3D velocity = new Vec3D(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
            PacketPlayOutEntityVelocity packet3 = new PacketPlayOutEntityVelocity(entity.getEntityId(), velocity);

            for (Player player : players) {
                sendPacket(player, packet1);
                sendPacket(player, packet2);
                sendPacket(player, packet3);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void removeItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity) {
        PacketPlayOutEntityDestroy[] packets = createEntityDestroyPacket(entity.getEntityId());
        for (Player player : players) {
            for (PacketPlayOutEntityDestroy packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnItemFrame(Collection<Player> players, ItemFrame entity) {
        EntityTypes<EntityItemFrame> type = EntityTypes.ai;
        Vec3D velocity = Vec3D.b;
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, getItemFrameData(entity), velocity, entity.getLocation().getYaw());

        List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
        PacketPlayOutEntityMetadata packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateItemFrame(Collection<Player> players, ItemFrame entity) {
        List<DataWatcher.c<?>> watcher = (List<DataWatcher.c<?>>) entity.getDataWatchers();
        PacketPlayOutEntityMetadata packet = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    @Override
    public void removeItemFrame(Collection<Player> players, ItemFrame entity) {
        PacketPlayOutEntityDestroy[] packets = createEntityDestroyPacket(entity.getEntityId());
        for (Player player : players) {
            for (PacketPlayOutEntityDestroy packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().c.sendPacket((Packet<?>) packet);
    }
}
