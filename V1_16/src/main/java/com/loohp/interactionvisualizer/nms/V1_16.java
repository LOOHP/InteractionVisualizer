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
import com.loohp.interactionvisualizer.utils.UnsafeAccessor;
import com.mojang.datafixers.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R1.Chunk;
import net.minecraft.server.v1_16_R1.DataWatcher;
import net.minecraft.server.v1_16_R1.DataWatcherObject;
import net.minecraft.server.v1_16_R1.EntityArmorStand;
import net.minecraft.server.v1_16_R1.EntityItem;
import net.minecraft.server.v1_16_R1.EntityItemFrame;
import net.minecraft.server.v1_16_R1.EntityPlayer;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EnumItemSlot;
import net.minecraft.server.v1_16_R1.IChatBaseComponent;
import net.minecraft.server.v1_16_R1.IRegistry;
import net.minecraft.server.v1_16_R1.MathHelper;
import net.minecraft.server.v1_16_R1.MobEffectList;
import net.minecraft.server.v1_16_R1.Packet;
import net.minecraft.server.v1_16_R1.PacketPlayOutAnimation;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_16_R1.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_16_R1.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R1.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_16_R1.Vec3D;
import net.minecraft.server.v1_16_R1.Vector3f;
import net.minecraft.server.v1_16_R1.VoxelShape;
import net.minecraft.server.v1_16_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftChatMessage;
import org.bukkit.craftbukkit.v1_16_R1.util.CraftMagicNumbers;
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
public class V1_16 extends NMSWrapper {

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
    private final Field[] spawnEntityLivingPacketFields;
    private final Field[] entityTeleportPacketFields;

    public V1_16() {
        try {
            entityMetadataPacketFields = PacketPlayOutEntityMetadata.class.getDeclaredFields();
            entityCountField = net.minecraft.server.v1_16_R1.Entity.class.getDeclaredField("entityCount");
            dataWatcherByteField = net.minecraft.server.v1_16_R1.Entity.class.getDeclaredField("T");
            dataWatcherCustomNameField = net.minecraft.server.v1_16_R1.Entity.class.getDeclaredField("ax");
            dataWatcherCustomNameVisibleField = net.minecraft.server.v1_16_R1.Entity.class.getDeclaredField("ay");
            dataWatcherSilentField = net.minecraft.server.v1_16_R1.Entity.class.getDeclaredField("az");
            dataWatcherNoGravityField = net.minecraft.server.v1_16_R1.Entity.class.getDeclaredField("aA");
            dataWatcherItemItemField = EntityItem.class.getDeclaredField("ITEM");
            dataWatcherItemFrameItemField = EntityItemFrame.class.getDeclaredField("ITEM");
            dataWatcherItemFrameRotationField = EntityItemFrame.class.getDeclaredField("g");
            spawnEntityLivingPacketFields = PacketPlayOutSpawnEntityLiving.class.getDeclaredFields();
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
        return ((EntityItem) ((CraftItem) item).getHandle()).age;
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
        net.minecraft.server.v1_16_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return GsonComponentSerializer.gson().deserialize(CraftChatMessage.toJSON(nmsItemStack.getName()));
    }

    @Override
    public PacketPlayOutEntityEquipment[] createEntityEquipmentPacket(int entityId, List<ValuePairs<EquipmentSlot, ItemStack>> equipments) {
        List<Pair<EnumItemSlot, net.minecraft.server.v1_16_R1.ItemStack>> nmsEquipments = new ArrayList<>();
        for (ValuePairs<EquipmentSlot, ItemStack> pair : equipments) {
            EnumItemSlot nmsSlot;
            switch (pair.getFirst()) {
                case CHEST:
                    nmsSlot = EnumItemSlot.CHEST;
                    break;
                case FEET:
                    nmsSlot = EnumItemSlot.FEET;
                    break;
                case HEAD:
                    nmsSlot = EnumItemSlot.HEAD;
                    break;
                case LEGS:
                    nmsSlot = EnumItemSlot.LEGS;
                    break;
                case OFF_HAND:
                    nmsSlot = EnumItemSlot.OFFHAND;
                    break;
                case HAND:
                default:
                    nmsSlot = EnumItemSlot.MAINHAND;
                    break;
            }
            net.minecraft.server.v1_16_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(pair.getSecond());
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
            PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata();
            entityMetadataPacketFields[0].setAccessible(true);
            entityMetadataPacketFields[0].setInt(packet, entityId);
            entityMetadataPacketFields[1].setAccessible(true);
            entityMetadataPacketFields[1].set(packet, dataWatchers);
            return packet;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        net.minecraft.server.v1_16_R1.BlockPosition blockpos = new net.minecraft.server.v1_16_R1.BlockPosition(pos.getX(), pos.getY(), pos.getZ());
        WorldServer world = ((CraftWorld) pos.getWorld()).getHandle();
        VoxelShape shape = world.getType(blockpos).getShape(world, blockpos);
        return shape.d().stream().map(each -> new BoundingBox(each.minX + pos.getX(), each.minY + pos.getY(), each.minZ + pos.getZ(), each.maxX + pos.getX(), each.maxY + pos.getY(), each.maxZ + pos.getZ())).collect(Collectors.toList());
    }

    @Override
    public NMSTileEntitySet<?, ?> getTileEntities(ChunkPosition chunk, boolean load) {
        if (!chunk.isLoaded() && !load) {
            return null;
        }
        World world = chunk.getWorld();
        Chunk nmsChunk = ((CraftWorld) world).getHandle().getChunkIfLoaded(chunk.getChunkX(), chunk.getChunkZ());
        return new NMSTileEntitySet<>(nmsChunk.getTileEntities(), entry -> {
            net.minecraft.server.v1_16_R1.BlockPosition pos = entry.getKey();
            Material type = CraftMagicNumbers.getMaterial(entry.getValue().getBlock().getBlock());
            TileEntity.TileEntityType tileEntityType = TileEntity.getTileEntityType(type);
            if (tileEntityType != null) {
                return new TileEntity(world, pos.getX(), pos.getY(), pos.getZ(), tileEntityType);
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
            Object spigotWorldConfig = net.minecraft.server.v1_16_R1.World.class.getField("spigotConfig").get(((CraftWorld) item.getWorld()).getHandle());
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
        net.minecraft.server.v1_16_R1.TileEntity tileEntity = worldServer.getTileEntity(new net.minecraft.server.v1_16_R1.BlockPosition(block.getX(), block.getY(), block.getZ()));
        if (tileEntity == null) {
            return "";
        }
        return tileEntity.b().getString("CustomName");
    }

    @Override
    public WrappedIterable<?, Entity> getEntities(World world) {
        WorldServer worldServer = ((CraftWorld) world).getHandle();
        return new WrappedIterable<>(worldServer.entitiesById.values(), net.minecraft.server.v1_16_R1.Entity::getBukkitEntity);
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
        net.minecraft.server.v1_16_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        String str = nmsItemStack.v().e.toString();
        return ChatColor.getByChar(str.charAt(str.length() - 1));
    }

    @Override
    public String getTranslationKey(ItemStack itemStack) {
        net.minecraft.server.v1_16_R1.ItemStack nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        return nmsItemStack.getItem().getName();
    }

    @Override
    public String getEnchantmentTranslationKey(Enchantment enchantment) {
        NamespacedKey namespacedKey = enchantment.getKey();
        return "enchantment." + namespacedKey.getNamespace() + "." + namespacedKey.getKey();
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getEffectTranslationKey(PotionEffectType type) {
        int id = type.getId();
        MobEffectList effectList = MobEffectList.fromId(id);
        if (effectList != null) {
            return effectList.c();
        } else {
            return "";
        }
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
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), stand.isCustomNameVisible()));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherSilentField.get(null), stand.isSilent()));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !stand.hasGravity()));

            byte standbitmask = (byte) 0;
            standbitmask = stand.isSmall() ? (byte) (standbitmask | 0x01) : standbitmask;
            standbitmask = stand.hasArms() ? (byte) (standbitmask | 0x04) : standbitmask;
            standbitmask = !stand.hasBasePlate() ? (byte) (standbitmask | 0x08) : standbitmask;
            standbitmask = stand.isMarker() ? (byte) (standbitmask | 0x10) : standbitmask;

            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.b, standbitmask));

            Vector3f headrotation = new Vector3f((float) Math.toDegrees(stand.getHeadPose().getX()), (float) Math.toDegrees(stand.getHeadPose().getY()), (float) Math.toDegrees(stand.getHeadPose().getZ()));
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.c, headrotation));

            Vector3f rightarmrotation = new Vector3f((float) Math.toDegrees(stand.getRightArmPose().getX()), (float) Math.toDegrees(stand.getRightArmPose().getY()), (float) Math.toDegrees(stand.getRightArmPose().getZ()));
            dataWatcher.add(new DataWatcher.Item<>(EntityArmorStand.f, rightarmrotation));

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
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), customName == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(customName)))));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null), item.isCustomNameVisible()));
            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Boolean>) dataWatcherNoGravityField.get(null), !item.hasGravity()));

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<net.minecraft.server.v1_16_R1.ItemStack>) dataWatcherItemItemField.get(null), CraftItemStack.asNMSCopy(item.getItemStack())));

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

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<net.minecraft.server.v1_16_R1.ItemStack>) dataWatcherItemFrameItemField.get(null), CraftItemStack.asNMSCopy(frame.getItem())));
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

            dataWatcher.add(new DataWatcher.Item<>((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null), name == null ? Optional.empty() : Optional.ofNullable(IChatBaseComponent.ChatSerializer.a(GsonComponentSerializer.gson().serialize(name)))));
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

            net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            DataWatcher watcher = nmsEntity.getDataWatcher();

            Optional<IChatBaseComponent> name = watcher.get((DataWatcherObject<Optional<IChatBaseComponent>>) dataWatcherCustomNameField.get(null));
            boolean visible = watcher.get((DataWatcherObject<Boolean>) dataWatcherCustomNameVisibleField.get(null));

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
            PacketPlayOutEntityTeleport packet = new PacketPlayOutEntityTeleport();
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
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void spawnArmorStand(Collection<Player> players, ArmorStand entity) {
        try {
            EntityTypes<EntityArmorStand> type = EntityTypes.ARMOR_STAND;
            PacketPlayOutSpawnEntityLiving packet1 = (PacketPlayOutSpawnEntityLiving) UnsafeAccessor.getUnsafe().allocateInstance(PacketPlayOutSpawnEntityLiving.class);
            spawnEntityLivingPacketFields[0].setAccessible(true);
            spawnEntityLivingPacketFields[1].setAccessible(true);
            spawnEntityLivingPacketFields[2].setAccessible(true);
            spawnEntityLivingPacketFields[3].setAccessible(true);
            spawnEntityLivingPacketFields[4].setAccessible(true);
            spawnEntityLivingPacketFields[5].setAccessible(true);
            spawnEntityLivingPacketFields[6].setAccessible(true);
            spawnEntityLivingPacketFields[7].setAccessible(true);
            spawnEntityLivingPacketFields[8].setAccessible(true);
            spawnEntityLivingPacketFields[9].setAccessible(true);
            spawnEntityLivingPacketFields[10].setAccessible(true);
            spawnEntityLivingPacketFields[11].setAccessible(true);

            spawnEntityLivingPacketFields[0].setInt(packet1, entity.getEntityId());
            spawnEntityLivingPacketFields[1].set(packet1, entity.getUniqueId());
            spawnEntityLivingPacketFields[2].setInt(packet1, IRegistry.ENTITY_TYPE.a(type));
            spawnEntityLivingPacketFields[3].setDouble(packet1, entity.getLocation().getX());
            spawnEntityLivingPacketFields[4].setDouble(packet1, entity.getLocation().getY());
            spawnEntityLivingPacketFields[5].setDouble(packet1, entity.getLocation().getZ());
            spawnEntityLivingPacketFields[6].setInt(packet1, (int) (MathHelper.a(entity.getVelocity().getX(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[7].setInt(packet1, (int) (MathHelper.a(entity.getVelocity().getY(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[8].setInt(packet1, (int) (MathHelper.a(entity.getVelocity().getZ(), -3.9, 3.9) * 8000.0));
            spawnEntityLivingPacketFields[9].setByte(packet1, (byte) ((int) (entity.getLocation().getYaw() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[10].setByte(packet1, (byte) ((int) (entity.getLocation().getPitch() * 256.0F / 360.0F)));
            spawnEntityLivingPacketFields[11].setByte(packet1, (byte) ((int) (entity.getLocation().getYaw() * 256.0F / 360.0F)));

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
    public void updateArmorStand(Collection<Player> players, ArmorStand entity) {
        try {
            PacketPlayOutEntityTeleport packet1 = new PacketPlayOutEntityTeleport();
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
        } catch (IllegalAccessException e) {
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

        EntityTypes<EntityItem> type = EntityTypes.ITEM;
        Vec3D velocity = new Vec3D(entity.getVelocity().getX(), entity.getVelocity().getY(), entity.getVelocity().getZ());
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, 0, velocity);

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

            PacketPlayOutEntityTeleport packet2 = new PacketPlayOutEntityTeleport();
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
        } catch (IllegalAccessException e) {
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
        EntityTypes<EntityItemFrame> type = EntityTypes.ITEM_FRAME;
        Vec3D velocity = Vec3D.a;
        PacketPlayOutSpawnEntity packet1 = new PacketPlayOutSpawnEntity(entity.getEntityId(), entity.getUniqueId(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ(), entity.getLocation().getPitch(), entity.getLocation().getYaw(), type, getItemFrameData(entity), velocity);

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
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket((Packet<?>) packet);
    }
}
