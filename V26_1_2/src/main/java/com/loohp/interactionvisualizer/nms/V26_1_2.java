/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2025. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2025. Contributors
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
import com.mojang.datafixers.util.Pair;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftEquipmentSlot;
import org.bukkit.craftbukkit.CraftRegistry;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
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

@SuppressWarnings("unused")
public class V26_1_2 extends NMSWrapper {

    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;
    private final Field dataWatcherItemItemField;

    //spigot specific
    private Field spigotWorldConfigField;
    private Field spigotItemDespawnRateField;

    //paper
    private Field paperItemDespawnRateField;
    private Method worldServerEntityLookup;

    public V26_1_2() {
        try {
            entityCountField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, AtomicInteger.class, "ENTITY_COUNTER");
            dataWatcherByteField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_SHARED_FLAGS_ID");
            dataWatcherCustomNameField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_CUSTOM_NAME");
            dataWatcherCustomNameVisibleField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_CUSTOM_NAME_VISIBLE");
            dataWatcherSilentField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_SILENT");
            dataWatcherNoGravityField = ReflectionUtils.findDeclaredField(net.minecraft.world.entity.Entity.class, EntityDataAccessor.class, "DATA_NO_GRAVITY");
            dataWatcherItemItemField = ReflectionUtils.findDeclaredField(ItemEntity.class, EntityDataAccessor.class, "DATA_ITEM");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        try {
            //spigot specific
            //noinspection JavaReflectionMemberAccess
            spigotWorldConfigField = Level.class.getField("spigotConfig");
            spigotItemDespawnRateField = spigotWorldConfigField.getType().getField("itemDespawnRate");
        } catch (NoSuchFieldException ignore) {
        }

        try {
            //paper
            //noinspection JavaReflectionMemberAccess
            paperItemDespawnRateField = ItemEntity.class.getDeclaredField("despawnRate");
            //noinspection JavaReflectionMemberAccess
            worldServerEntityLookup = ServerLevel.class.getMethod("moonrise$getEntityLookup");
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
        return ((CraftItem) item).getHandle().age;
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
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.getStyledHoverName()));
    }

    @Override
    public ClientboundSetEquipmentPacket[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
        List<Pair<net.minecraft.world.entity.EquipmentSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = new ArrayList<>();
        for (ValuePairs<EquipmentSlot, ItemStack> pair : equipments) {
            net.minecraft.world.entity.EquipmentSlot nmsSlot = CraftEquipmentSlot.getNMS(pair.getFirst());
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
            nmsEquipments.add(new Pair<>(nmsSlot, nmsItem));
        }
        return new ClientboundSetEquipmentPacket[] {new ClientboundSetEquipmentPacket(entityId, nmsEquipments)};
    }

    @Override
    public ClientboundRemoveEntitiesPacket[] createEntityDestroyPacket(int... entityIds) {
        return new ClientboundRemoveEntitiesPacket[] {new ClientboundRemoveEntitiesPacket(entityIds)};
    }

    @SuppressWarnings("unchecked")
    @Override
    public ClientboundSetEntityDataPacket createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        return new ClientboundSetEntityDataPacket(entityId, (List<SynchedEntityData.DataValue<?>>) dataWatchers);
    }

    @Override
    public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        BlockPos blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        ServerLevel world = ((CraftWorld) pos.getWorld()).getHandle();
        VoxelShape shape = world.getBlockState(blockpos).getShape(world, blockpos);
        return shape.toAabbs().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
    }

    @Override
    public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
        if (!chunk.isLoaded() && !load) {
            return null;
        }
        World world = chunk.getWorld();
        LevelChunk nmsChunk = ((CraftWorld) world).getHandle().getChunkIfLoaded(chunk.getChunkX(), chunk.getChunkZ());
        return new NMSTileEntitySet<>(nmsChunk.blockEntities, entry -> {
            BlockPos pos = entry.getKey();
            Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlockState().getBlock());
            TileEntity.TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
            if (tileEntityType != null) {
                return new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType);
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
        ServerLevel worldServer = ((CraftWorld) block.getWorld()).getHandle();
        BlockEntity tileEntity = worldServer.getBlockEntity(new BlockPos(block.getX(), block.getY(), block.getZ()));
        if (tileEntity == null) {
            return "";
        }
        Tag nbtbase = tileEntity.getUpdateTag(CraftRegistry.getMinecraftRegistry()).get("CustomName");
        if (nbtbase == null) {
            return "";
        }
        Optional<net.minecraft.network.chat.Component> optChat = ComponentSerialization.CODEC.parse(CraftRegistry.getMinecraftRegistry().createSerializationContext(NbtOps.INSTANCE), nbtbase).resultOrPartial();
        if (!optChat.isPresent()) {
            return "";
        }
        return CraftChatMessage.toJSON(optChat.get());
    }

    @SuppressWarnings("unchecked")
    @Override
    public WrappedIterable<?, Entity> getEntities(World world) {
        try {
            ServerLevel worldServer = ((CraftWorld) world).getHandle();
            LevelEntityGetter<net.minecraft.world.entity.Entity> levelEntityGetter;
            if (worldServerEntityLookup == null) {
                levelEntityGetter = worldServer.entityManager.getEntityGetter();
            } else {
                levelEntityGetter = (LevelEntityGetter<net.minecraft.world.entity.Entity>) worldServerEntityLookup.invoke(worldServer);
            }
            return new WrappedIterable<>(levelEntityGetter.getAll(), net.minecraft.world.entity.Entity::getBukkitEntity);
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
        String str = nmsItemStack.getRarity().color().toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().getDescriptionId();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEnchantmentTranslationKey(Enchantment enchantment) {
        NamespacedKey namespacedKey = enchantment.getKey();
        return "enchantment." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }

    @SuppressWarnings("deprecation")
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
    public List<SynchedEntityData.DataValue<?>> getWatchableCollection(ArmorStand stand) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = (byte) 0;
            bitmask = !stand.isVisible() ? (byte) (bitmask | 0x20) : bitmask;
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Byte>) dataWatcherByteField.get(null), bitmask));

            Component customName = stand.getCustomName();
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), stand.isCustomNameVisible()));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherSilentField.get(null), stand.isSilent()));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherNoGravityField.get(null), !stand.hasGravity()));

            byte standbitmask = (byte) 0;
            standbitmask = stand.isSmall() ? (byte) (standbitmask | 0x01) : standbitmask;
            standbitmask = stand.hasArms() ? (byte) (standbitmask | 0x04) : standbitmask;
            standbitmask = !stand.hasBasePlate() ? (byte) (standbitmask | 0x08) : standbitmask;
            standbitmask = stand.isMarker() ? (byte) (standbitmask | 0x10) : standbitmask;

            dataWatcher.add(SynchedEntityData.DataValue.create(net.minecraft.world.entity.decoration.ArmorStand.DATA_CLIENT_FLAGS, standbitmask));

            Rotations headrotation = new Rotations((float) Math.toDegrees(stand.getHeadPose().getX()), (float) Math.toDegrees(stand.getHeadPose().getY()), (float) Math.toDegrees(stand.getHeadPose().getZ()));
            dataWatcher.add(SynchedEntityData.DataValue.create(net.minecraft.world.entity.decoration.ArmorStand.DATA_HEAD_POSE, headrotation));

            Rotations rightarmrotation = new Rotations((float) Math.toDegrees(stand.getRightArmPose().getX()), (float) Math.toDegrees(stand.getRightArmPose().getY()), (float) Math.toDegrees(stand.getRightArmPose().getZ()));
            dataWatcher.add(SynchedEntityData.DataValue.create(net.minecraft.world.entity.decoration.ArmorStand.DATA_RIGHT_ARM_POSE, rightarmrotation));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SynchedEntityData.DataValue<?>> getWatchableCollection(com.loohp.interactionvisualizer.entityholders.Item item) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);
            dataWatcherItemItemField.setAccessible(true);

            byte bitmask = (byte) 0;
            bitmask = item.isGlowing() ? (byte) (bitmask | 0x40) : bitmask;
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Byte>) dataWatcherByteField.get(null), bitmask));

            Component customName = item.getCustomName();
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), item.isCustomNameVisible()));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherNoGravityField.get(null), !item.hasGravity()));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<net.minecraft.world.item.ItemStack>) dataWatcherItemItemField.get(null), CraftItemStack.asNMSCopy(item.getItemStack())));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SynchedEntityData.DataValue<?>> getWatchableCollection(ItemFrame frame) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherSilentField.setAccessible(true);

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherSilentField.get(null), frame.isSilent()));

            dataWatcher.add(SynchedEntityData.DataValue.create(net.minecraft.world.entity.decoration.ItemFrame.DATA_ITEM, CraftItemStack.asNMSCopy(frame.getItem())));
            dataWatcher.add(SynchedEntityData.DataValue.create(net.minecraft.world.entity.decoration.ItemFrame.DATA_ROTATION, frame.getFrameRotation()));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SynchedEntityData.DataValue<?>> createCustomNameWatchableCollection(Component name) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            boolean visible = name != null;

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(name)))));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<SynchedEntityData.DataValue<?>> resetCustomNameWatchableCollection(Entity entity) {
        try {
            List<SynchedEntityData.DataValue<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            SynchedEntityData watcher = nmsEntity.getEntityData();

            Optional<net.minecraft.network.chat.Component> name = watcher.get((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.get((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null));

            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Optional<net.minecraft.network.chat.Component>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(SynchedEntityData.DataValue.create((EntityDataAccessor<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendHandMovement(Collection<Player> players, Player entity) {
        ServerPlayer entityPlayer = ((CraftPlayer) entity).getHandle();
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(entityPlayer, 0);
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    @Override
    public void teleportEntity(Player player, int entityId, Location location) {
        ClientboundEntityPositionSyncPacket packet = new ClientboundEntityPositionSyncPacket(entityId, new PositionMoveRotation(new Vec3(location.getX(), location.getY(), location.getZ()), Vec3.ZERO, location.getYaw(), location.getPitch()), false);
        sendPacket(player, packet);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnArmorStand(Collection<Player> players, ArmorStand entity) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.decoration.ArmorStand> type = net.minecraft.world.entity.EntityType.ARMOR_STAND;
        Vec3 velocity = new Vec3(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        ClientboundAddEntityPacket packet1 = new ClientboundAddEntityPacket(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        List<ValuePairs<EquipmentSlot, ItemStack>> equipments = new ArrayList<>();
        equipments.add(new ValuePairs<>(EquipmentSlot.HAND, entity.getItemInMainHand()));
        equipments.add(new ValuePairs<>(EquipmentSlot.HEAD, entity.getHelmet()));
        ClientboundSetEquipmentPacket[] packet3 = createEntityEquipmentPacket(entity.getEntityId(), equipments);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
            for (ClientboundSetEquipmentPacket packet : packet3) {
                sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateArmorStand(Collection<Player> players, ArmorStand entity) {
        ClientboundEntityPositionSyncPacket packet1 = new ClientboundEntityPositionSyncPacket(entity.getEntityId(), new PositionMoveRotation(new Vec3(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ()), Vec3.ZERO, entity.getLocation().getYaw(), entity.getLocation().getPitch()), false);

        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        List<ValuePairs<EquipmentSlot, ItemStack>> equipments = new ArrayList<>();
        equipments.add(new ValuePairs<>(EquipmentSlot.HAND, entity.getItemInMainHand()));
        equipments.add(new ValuePairs<>(EquipmentSlot.HEAD, entity.getHelmet()));
        ClientboundSetEquipmentPacket[] packet3 = createEntityEquipmentPacket(entity.getEntityId(), equipments);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
            for (ClientboundSetEquipmentPacket packet : packet3) {
                sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateArmorStandOnlyMeta(Collection<Player> players, ArmorStand entity) {
        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet = createEntityMetadataPacket(entity.getEntityId(), watcher);
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    @Override
    public void removeArmorStand(Collection<Player> players, ArmorStand entity) {
        ClientboundRemoveEntitiesPacket[] packets = createEntityDestroyPacket(entity.getEntityId());
        for (Player player : players) {
            for (ClientboundRemoveEntitiesPacket packet : packets) {
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

        net.minecraft.world.entity.EntityType<ItemEntity> type = net.minecraft.world.entity.EntityType.ITEM;
        Vec3 velocity = new Vec3(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        ClientboundAddEntityPacket packet1 = new ClientboundAddEntityPacket(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity) {
        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet1 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        ClientboundEntityPositionSyncPacket packet2 = new ClientboundEntityPositionSyncPacket(entity.getEntityId(), new PositionMoveRotation(new Vec3(entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ()), Vec3.ZERO, entity.getLocation().getYaw(), entity.getLocation().getPitch()), false);

        Vec3 velocity = new Vec3(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        ClientboundSetEntityMotionPacket packet3 = new ClientboundSetEntityMotionPacket(entity.getEntityId(), velocity);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
            sendPacket(player, packet3);
        }
    }

    @Override
    public void removeItem(Collection<Player> players, com.loohp.interactionvisualizer.entityholders.Item entity) {
        ClientboundRemoveEntitiesPacket[] packets = createEntityDestroyPacket(entity.getEntityId());
        for (Player player : players) {
            for (ClientboundRemoveEntitiesPacket packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnItemFrame(Collection<Player> players, ItemFrame entity) {
        net.minecraft.world.entity.EntityType<net.minecraft.world.entity.decoration.ItemFrame> type = net.minecraft.world.entity.EntityType.ITEM_FRAME;
        Vec3 velocity = Vec3.ZERO;
        ClientboundAddEntityPacket packet1 = new ClientboundAddEntityPacket(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, getItemFrameData(entity), velocity, entity.getLocation().getYaw());

        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateItemFrame(Collection<Player> players, ItemFrame entity) {
        List<SynchedEntityData.DataValue<?>> watcher = (List<SynchedEntityData.DataValue<?>>) entity.getDataWatchers();
        ClientboundSetEntityDataPacket packet = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    @Override
    public void removeItemFrame(Collection<Player> players, ItemFrame entity) {
        ClientboundRemoveEntitiesPacket[] packets = createEntityDestroyPacket(entity.getEntityId());
        for (Player player : players) {
            for (ClientboundRemoveEntitiesPacket packet : packets) {
                sendPacket(player, packet);
            }
        }
    }

    @Override
    public void sendPacket(Player player, Object packet) {
        ((CraftPlayer) player).getHandle().connection.send((Packet<?>) packet);
    }
}
