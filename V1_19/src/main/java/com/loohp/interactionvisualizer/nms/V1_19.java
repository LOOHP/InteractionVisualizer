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
import net.minecraft.world.phys.Vec3D;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class V1_19 extends NMSWrapper {

    private final Field[] entityMetadataPacketFields;
    private final Field entityCountField;
    private final Field dataWatcherByteField;
    private final Field dataWatcherCustomNameField;
    private final Field dataWatcherCustomNameVisibleField;
    private final Field dataWatcherSilentField;
    private final Field dataWatcherNoGravityField;
    private final Field dataWatcherItemItemField;
    private final Field dataWatcherItemFrameItemField;
    private final Field dataWatcherItemFrameRotationField;
    private final Field[] entityTeleportPacketFields;

    public V1_19() {
        try {
            entityMetadataPacketFields = PacketPlayOutEntityMetadata.class.getDeclaredFields();
            entityCountField = net.minecraft.world.entity.Entity.class.getDeclaredField("c");
            dataWatcherByteField = net.minecraft.world.entity.Entity.class.getDeclaredField("Z");
            dataWatcherCustomNameField = net.minecraft.world.entity.Entity.class.getDeclaredField("aM");
            dataWatcherCustomNameVisibleField = net.minecraft.world.entity.Entity.class.getDeclaredField("aN");
            dataWatcherSilentField = net.minecraft.world.entity.Entity.class.getDeclaredField("aO");
            dataWatcherNoGravityField = net.minecraft.world.entity.Entity.class.getDeclaredField("aP");
            dataWatcherItemItemField = EntityItem.class.getDeclaredField("c");
            dataWatcherItemFrameItemField = EntityItemFrame.class.getDeclaredField("ao");
            dataWatcherItemFrameRotationField = EntityItemFrame.class.getDeclaredField("ap");
            entityTeleportPacketFields = PacketPlayOutEntityTeleport.class.getDeclaredFields();
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Particle getItemCrackParticle() {
        return Particle.ITEM_CRACK;
    }

    @Override
    public Enchantment getPowerEnchantment() {
        return Enchantment.ARROW_DAMAGE;
    }

    @Override
    public int getItemAge(Item item) {
        return ((EntityItem) ((CraftItem) item).getHandle()).ao;
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
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.x()));
    }

    @Override
    public PacketPlayOutEntityEquipment[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
        List<Pair<EnumItemSlot, net.minecraft.world.item.ItemStack>> nmsEquipments = new ArrayList<>();
        for (ValuePairs<EquipmentSlot, ItemStack> pair : equipments) {
            EnumItemSlot nmsSlot;
            switch (pair.getFirst()) {
                case CHEST:
                    nmsSlot = EnumItemSlot.e;
                    break;
                case FEET:
                    nmsSlot = EnumItemSlot.c;
                    break;
                case HEAD:
                    nmsSlot = EnumItemSlot.f;
                    break;
                case LEGS:
                    nmsSlot = EnumItemSlot.d;
                    break;
                case OFF_HAND:
                    nmsSlot = EnumItemSlot.b;
                    break;
                case HAND:
                default:
                    nmsSlot = EnumItemSlot.a;
                    break;
            }
            net.minecraft.world.item.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
            nmsEquipments.add(new Pair<>(nmsSlot, nmsItem));
        }
        return new PacketPlayOutEntityEquipment[] {new PacketPlayOutEntityEquipment(entityId, nmsEquipments)};
    }

    @Override
    public PacketPlayOutEntityDestroy[] createEntityDestroyPacket(int... entityIds) {
        return new PacketPlayOutEntityDestroy[] {new PacketPlayOutEntityDestroy(entityIds)};
    }

    @Override
    public PacketPlayOutEntityMetadata createEntityMetadataPacket(int entityId, List<?> dataWatchers) {
        try {
            PacketPlayOutEntityMetadata packet = (PacketPlayOutEntityMetadata) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityMetadata.class);
            entityMetadataPacketFields[0].setAccessible(true);
            entityMetadataPacketFields[0].setInt(packet, entityId);
            entityMetadataPacketFields[1].setAccessible(true);
            entityMetadataPacketFields[1].set(packet, dataWatchers);
            return packet;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        net.minecraft.core.BlockPosition blockpos = new net.minecraft.core.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
        WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
        VoxelShape shape = world.a_(blockpos).j(world, blockpos);
        return shape.d().stream().map(each -> new BoundingBox(each.a + pos.getX(), each.b + pos.getY(), each.c + pos.getZ(), each.d + pos.getX(), each.e + pos.getY(), each.f + pos.getZ())).collect(Collectors.toList());
    }

    @Override
    public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
        if (!chunk.isLoaded() && !load) {
            return null;
        }
        World world = chunk.getWorld();
        Chunk nmsChunk = ((CraftWorld) world).getHandle().getChunkIfLoaded(chunk.getChunkX(), chunk.getChunkZ());
        return new NMSTileEntitySet<>(nmsChunk.i, entry -> {
            net.minecraft.core.BlockPosition pos = entry.getKey();
            Material type = CraftMagicNumbers.getMaterial(entry.getValue().q().b());
            TileEntity.TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
            if (tileEntityType != null) {
                return new TileEntity(world, pos.u(), pos.v(), pos.w(), tileEntityType);
            } else {
                return null;
            }
        });
    }

    @SuppressWarnings("JavaReflectionMemberAccess")
    @Override
    public int getItemDespawnRate(Item item) {
        int despawnRate;
        try {
            Object spigotWorldConfig = net.minecraft.world.level.World.class.getField("spigotConfig").get(((CraftWorld) item.getWorld()).getHandle());
            despawnRate = spigotWorldConfig.getClass().getField("itemDespawnRate").getInt(spigotWorldConfig);
            try {
                despawnRate = (int) EntityItem.class.getMethod("getDespawnRate").invoke(((CraftItem) item).getHandle());
            } catch (Throwable ignore) {
            }
        } catch (Throwable e) {
            despawnRate = 6000;
        }
        return despawnRate;
    }

    @Override
    public String getBannerCustomName(Block block) {
        WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
        net.minecraft.world.level.block.entity.TileEntity tileEntity = worldServer.c_(new net.minecraft.core.BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (tileEntity == null) {
            return "";
        }
        return tileEntity.aa_().l("CustomName");
    }

    @Override
    public WrappedIterable<?, Entity> getEntities(World world) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        return new WrappedIterable<>(worldServer.P.d().a(), net.minecraft.world.entity.Entity::getBukkitEntity);
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
        String str = nmsItemStack.B().e.toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.c().a();
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
            return EntityType.DROPPED_ITEM;
        } else if (entity instanceof ItemFrame) {
            return EntityType.ITEM_FRAME;
        }
        throw new RuntimeException("Unknown VisualizerEntity class " + entity.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.Item<?>> getWatchableCollection(ArmorStand stand) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherSilentField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);

            byte bitmask = (byte) 0;
            bitmask = !stand.isVisible() ? (byte) (bitmask | 0x20) : bitmask;
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Component customName = stand.getCustomName();
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), stand.isCustomNameVisible()));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), stand.isSilent()));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !stand.hasGravity()));

            byte standbitmask = (byte) 0;
            standbitmask = stand.isSmall() ? (byte) (standbitmask | 0x01) : standbitmask;
            standbitmask = stand.hasArms() ? (byte) (standbitmask | 0x04) : standbitmask;
            standbitmask = !stand.hasBasePlate() ? (byte) (standbitmask | 0x08) : standbitmask;
            standbitmask = stand.isMarker() ? (byte) (standbitmask | 0x10) : standbitmask;

            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.bG, standbitmask));

            Vector3f headrotation = new Vector3f((float) Math.toDegrees(stand.getHeadPose().getX()), (float) Math.toDegrees(stand.getHeadPose().getY()), (float) Math.toDegrees(stand.getHeadPose().getZ()));
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.bH, headrotation));

            Vector3f rightarmrotation = new Vector3f((float) Math.toDegrees(stand.getRightArmPose().getX()), (float) Math.toDegrees(stand.getRightArmPose().getY()), (float) Math.toDegrees(stand.getRightArmPose().getZ()));
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.bK, rightarmrotation));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.Item<?>> getWatchableCollection(com.loohp.interactionvisualizer.entityholders.Item item) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherByteField.setAccessible(true);
            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);
            dataWatcherNoGravityField.setAccessible(true);
            dataWatcherItemItemField.setAccessible(true);

            byte bitmask = (byte) 0;
            bitmask = item.isGlowing() ? (byte) (bitmask | 0x40) : bitmask;
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Byte>) dataWatcherByteField.get(null), bitmask));

            Component customName = item.getCustomName();
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), item.isCustomNameVisible()));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !item.hasGravity()));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<net.minecraft.world.item.ItemStack>) dataWatcherItemItemField.get(null), CraftItemStack.asNMSCopy(item.getItemStack())));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.Item<?>> getWatchableCollection(ItemFrame frame) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherSilentField.setAccessible(true);
            dataWatcherItemFrameItemField.setAccessible(true);
            dataWatcherItemFrameRotationField.setAccessible(true);

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), frame.isSilent()));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<net.minecraft.world.item.ItemStack>) dataWatcherItemFrameItemField.get(null), CraftItemStack.asNMSCopy(frame.getItem())));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Integer>) dataWatcherItemFrameRotationField.get(null), frame.getFrameRotation()));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.Item<?>> createCustomNameWatchableCollection(Component name) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            boolean visible = name != null;

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name == null ? Optional.empty() : Optional.ofNullable(CraftChatMessage.fromJSON(GsonComponentSerializer.gson().serialize(name)))));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

            return dataWatcher;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DataWatcher.Item<?>> resetCustomNameWatchableCollection(Entity entity) {
        try {
            List<DataWatcher.Item<?>> dataWatcher = new ArrayList<>();

            dataWatcherCustomNameField.setAccessible(true);
            dataWatcherCustomNameVisibleField.setAccessible(true);

            net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.ai();

            Optional<IChatBaseComponent> name = watcher.a((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.a((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), visible));

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
            entityTeleportPacketFields[0].setAccessible(true);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);

            entityTeleportPacketFields[0].setInt(packet, entityId);
            entityTeleportPacketFields[1].setDouble(packet, location.getX());
            entityTeleportPacketFields[2].setDouble(packet, location.getY());
            entityTeleportPacketFields[3].setDouble(packet, location.getZ());
            entityTeleportPacketFields[4].setByte(packet, (byte) (int) (location.getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[5].setByte(packet, (byte) (int) (location.getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setBoolean(packet, false);
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

        List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
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
            entityTeleportPacketFields[0].setAccessible(true);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);

            entityTeleportPacketFields[0].setInt(packet1, entity.getEntityId());
            entityTeleportPacketFields[1].setDouble(packet1, entity.getLocation().getX());
            entityTeleportPacketFields[2].setDouble(packet1, entity.getLocation().getY());
            entityTeleportPacketFields[3].setDouble(packet1, entity.getLocation().getZ());
            entityTeleportPacketFields[4].setByte(packet1, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[5].setByte(packet1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setBoolean(packet1, false);

            List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
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
        List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
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

        EntityTypes<EntityItem> type = EntityTypes.T;
        Vec3D velocity = new Vec3D(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity, entity.getLocation().getYaw());

        List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
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
            List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
            PacketPlayOutEntityMetadata packet1 = createEntityMetadataPacket(entity.getEntityId(), watcher);

            PacketPlayOutEntityTeleport packet2 = (PacketPlayOutEntityTeleport) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutEntityTeleport.class);
            entityTeleportPacketFields[0].setAccessible(true);
            entityTeleportPacketFields[1].setAccessible(true);
            entityTeleportPacketFields[2].setAccessible(true);
            entityTeleportPacketFields[3].setAccessible(true);
            entityTeleportPacketFields[4].setAccessible(true);
            entityTeleportPacketFields[5].setAccessible(true);
            entityTeleportPacketFields[6].setAccessible(true);

            entityTeleportPacketFields[0].setInt(packet2, entity.getEntityId());
            entityTeleportPacketFields[1].setDouble(packet2, entity.getLocation().getX());
            entityTeleportPacketFields[2].setDouble(packet2, entity.getLocation().getY());
            entityTeleportPacketFields[3].setDouble(packet2, entity.getLocation().getZ());
            entityTeleportPacketFields[4].setByte(packet2, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
            entityTeleportPacketFields[5].setByte(packet2, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
            entityTeleportPacketFields[6].setBoolean(packet2, false);

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
        EntityTypes<EntityItemFrame> type = EntityTypes.U;
        Vec3D velocity = Vec3D.b;
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, getItemFrameData(entity), velocity, entity.getLocation().getYaw());

        List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
        PacketPlayOutEntityMetadata packet2 = createEntityMetadataPacket(entity.getEntityId(), watcher);

        for (Player player : players) {
            sendPacket(player, packet1);
            sendPacket(player, packet2);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateItemFrame(Collection<Player> players, ItemFrame entity) {
        List<DataWatcher.Item<?>> watcher = (List<DataWatcher.Item<?>>) entity.getDataWatchers();
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
        ((CraftPlayer) player).getHandle().b.a((Packet<?>) packet);
    }
}
