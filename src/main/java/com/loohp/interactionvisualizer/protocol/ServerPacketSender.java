package com.loohp.interactionvisualizer.protocol;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.entityholders.ArmorStand;
import com.loohp.interactionvisualizer.entityholders.Item;
import com.loohp.interactionvisualizer.entityholders.ItemFrame;
import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.ValuePairs;
import com.loohp.interactionvisualizer.utils.MCVersion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ServerPacketSender {

    private static final Plugin plugin = InteractionVisualizer.plugin;
    private static final MCVersion version = InteractionVisualizer.version;
    private static final ProtocolManager protocolManager = InteractionVisualizer.protocolManager;
    private static final NMS nms = NMS.getInstance();

    public static void sendHandMovement(Collection<Player> players, Player entity) {
        if (!InteractionVisualizer.handMovementEnabled) {
            return;
        }

        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
        packet1.getIntegers().write(0, entity.getEntityId());
        packet1.getIntegers().write(1, 0);

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                protocolManager.sendServerPacket(entity, packet1);
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void teleportEntity(Player player, int entityId, Location location) {
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet1.getIntegers().write(0, entityId);
        packet1.getDoubles().write(0, location.getX());
        packet1.getDoubles().write(1, location.getY());
        packet1.getDoubles().write(2, location.getZ());
        packet1.getBytes().write(0, (byte) (int) (location.getYaw() * 256.0F / 360.0F));
        packet1.getBytes().write(1, (byte) (int) (location.getPitch() * 256.0F / 360.0F));

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                protocolManager.sendServerPacket(player, packet1);
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void spawnArmorStand(Collection<Player> players, ArmorStand entity) {
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet1.getIntegers().write(0, entity.getEntityId());
        if (packet1.getUUIDs().size() > 0) {
            packet1.getUUIDs().write(0, entity.getUniqueId());
        }
        packet1.getIntegers().write(1, version.isLegacy() ? 30 : 1);
        packet1.getIntegers().write(2, (int) (entity.getVelocity().getX() * 8000));
        packet1.getIntegers().write(3, (int) (entity.getVelocity().getY() * 8000));
        packet1.getIntegers().write(4, (int) (entity.getVelocity().getZ() * 8000));
        packet1.getDoubles().write(0, entity.getLocation().getX());
        packet1.getDoubles().write(1, entity.getLocation().getY());
        packet1.getDoubles().write(2, entity.getLocation().getZ());
        packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F)); //Yaw
        packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F)); //Pitch
        packet1.getBytes().write(2, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F)); //Head

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        List<ValuePairs<EquipmentSlot, ItemStack>> equipments = new ArrayList<>();
        equipments.add(new ValuePairs<>(EquipmentSlot.HAND, entity.getItemInMainHand()));
        equipments.add(new ValuePairs<>(EquipmentSlot.HEAD, entity.getHelmet()));
        PacketContainer[] packet3 = nms.createEntityEquipmentPacket(entity.getEntityId(), equipments);

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                    protocolManager.sendServerPacket(player, packet2);
                    for (PacketContainer packet : packet3) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void updateArmorStand(Collection<Player> players, ArmorStand entity) {
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet1.getIntegers().write(0, entity.getEntityId());
        packet1.getDoubles().write(0, entity.getLocation().getX());
        packet1.getDoubles().write(1, entity.getLocation().getY());
        packet1.getDoubles().write(2, entity.getLocation().getZ());
        packet1.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        packet1.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        List<ValuePairs<EquipmentSlot, ItemStack>> equipments = new ArrayList<>();
        equipments.add(new ValuePairs<>(EquipmentSlot.HAND, entity.getItemInMainHand()));
        equipments.add(new ValuePairs<>(EquipmentSlot.HEAD, entity.getHelmet()));
        PacketContainer[] packet3 = nms.createEntityEquipmentPacket(entity.getEntityId(), equipments);

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                    protocolManager.sendServerPacket(player, packet2);
                    for (PacketContainer packet : packet3) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void updateArmorStandOnlyMeta(Collection<Player> players, ArmorStand entity) {
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet1.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void removeArmorStand(Collection<Player> players, ArmorStand entity) {
        PacketContainer[] packets = NMS.getInstance().createEntityDestoryPacket(entity.getEntityId());

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void spawnItem(Collection<Player> players, Item entity) {
        if (entity.getItemStack().getType().equals(Material.AIR)) {
            return;
        }

        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        packet1.getIntegers().write(0, entity.getEntityId());
        if (packet1.getUUIDs().size() > 0) {
            packet1.getUUIDs().write(0, entity.getUniqueId());
        }
        packet1.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
        packet1.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
        packet1.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));
        packet1.getIntegers().write(4, (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));
        packet1.getIntegers().write(5, (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        if (version.isLegacy() || version.equals(MCVersion.V1_13) || version.equals(MCVersion.V1_13_1)) {
            packet1.getIntegers().write(6, 2);
            packet1.getIntegers().write(7, 1);
        } else {
            packet1.getEntityTypeModifier().write(0, entity.getType());
            packet1.getIntegers().write(6, 1);
        }
        packet1.getUUIDs().write(0, entity.getUniqueId());
        Location location = entity.getLocation();
        packet1.getDoubles().write(0, location.getX());
        packet1.getDoubles().write(1, location.getY());
        packet1.getDoubles().write(2, location.getZ());

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        PacketContainer packet3 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet3.getIntegers().write(0, entity.getEntityId());
        packet3.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
        packet3.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
        packet3.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                    protocolManager.sendServerPacket(player, packet2);
                    protocolManager.sendServerPacket(player, packet3);
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void updateItem(Collection<Player> players, Item entity) {
        if (entity.getItemStack().getType().equals(Material.AIR)) {
            return;
        }

        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet1.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet2.getIntegers().write(0, entity.getEntityId());
        packet2.getDoubles().write(0, entity.getLocation().getX());
        packet2.getDoubles().write(1, entity.getLocation().getY());
        packet2.getDoubles().write(2, entity.getLocation().getZ());
        packet2.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        packet2.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

        PacketContainer packet3 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet3.getIntegers().write(0, entity.getEntityId());
        packet3.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
        packet3.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
        packet3.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                    protocolManager.sendServerPacket(player, packet2);
                    protocolManager.sendServerPacket(player, packet3);
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void updateItemAsync(Collection<Player> players, Item entity) {
        if (entity.getItemStack().getType().equals(Material.AIR)) {
            return;
        }

        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet1.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
        packet2.getIntegers().write(0, entity.getEntityId());
        packet2.getDoubles().write(0, entity.getLocation().getX());
        packet2.getDoubles().write(1, entity.getLocation().getY());
        packet2.getDoubles().write(2, entity.getLocation().getZ());
        packet2.getBytes().write(0, (byte) (int) (entity.getLocation().getYaw() * 256.0F / 360.0F));
        packet2.getBytes().write(1, (byte) (int) (entity.getLocation().getPitch() * 256.0F / 360.0F));

        PacketContainer packet3 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
        packet3.getIntegers().write(0, entity.getEntityId());
        packet3.getIntegers().write(1, (int) (entity.getVelocity().getX() * 8000));
        packet3.getIntegers().write(2, (int) (entity.getVelocity().getY() * 8000));
        packet3.getIntegers().write(3, (int) (entity.getVelocity().getZ() * 8000));

        try {
            for (Player player : players) {
                protocolManager.sendServerPacket(player, packet1);
                protocolManager.sendServerPacket(player, packet2);
                protocolManager.sendServerPacket(player, packet3);
            }
        } catch (InvocationTargetException | IllegalArgumentException e) {
        }
    }

    public static void removeItem(Collection<Player> players, Item entity) {
        PacketContainer[] packets = NMS.getInstance().createEntityDestoryPacket(entity.getEntityId());

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void spawnItemFrame(Collection<Player> players, ItemFrame entity) {
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        packet1.getIntegers().write(0, entity.getEntityId());
        if (packet1.getUUIDs().size() > 0) {
            packet1.getUUIDs().write(0, entity.getUniqueId());
        }
        packet1.getIntegers().write(1, 0);
        packet1.getIntegers().write(2, 0);
        packet1.getIntegers().write(3, 0);
        packet1.getIntegers().write(4, (int) (entity.getPitch() * 256.0F / 360.0F));
        packet1.getIntegers().write(5, (int) (entity.getYaw() * 256.0F / 360.0F));
        if (version.isLegacy() || version.equals(MCVersion.V1_13) || version.equals(MCVersion.V1_13_1)) {
            packet1.getIntegers().write(6, 33);
            packet1.getIntegers().write(7, getItemFrameData(entity));
        } else {
            packet1.getEntityTypeModifier().write(0, entity.getType());
            packet1.getIntegers().write(6, getItemFrameData(entity));
        }
        packet1.getUUIDs().write(0, entity.getUniqueId());
        Location location = entity.getLocation();
        packet1.getDoubles().write(0, location.getX());
        packet1.getDoubles().write(1, location.getY());
        packet1.getDoubles().write(2, location.getZ());

        PacketContainer packet2 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet2.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet2.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                    protocolManager.sendServerPacket(player, packet2);
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static int getItemFrameData(ItemFrame frame) {
        switch (frame.getAttachedFace()) {
            case DOWN:
                return 0;
            case UP:
                return 1;
            case NORTH:
                return 2;
            case SOUTH:
                return 3;
            case WEST:
                return 4;
            case EAST:
                return 5;
            default:
                return 0;
        }
    }

    public static void updateItemFrame(Collection<Player> players, ItemFrame entity) {
        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        packet1.getIntegers().write(0, entity.getEntityId());
        WrappedDataWatcher wpw = entity.getWrappedDataWatcher();
        packet1.getWatchableCollectionModifier().write(0, wpw.getWatchableObjects());

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    protocolManager.sendServerPacket(player, packet1);
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }

    public static void removeItemFrame(Collection<Player> players, ItemFrame entity) {
        PacketContainer[] packets = NMS.getInstance().createEntityDestoryPacket(entity.getEntityId());

        if (!plugin.isEnabled()) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                for (Player player : players) {
                    for (PacketContainer packet : packets) {
                        protocolManager.sendServerPacket(player, packet);
                    }
                }
            } catch (InvocationTargetException | IllegalArgumentException e) {
            }
        });
    }
}
