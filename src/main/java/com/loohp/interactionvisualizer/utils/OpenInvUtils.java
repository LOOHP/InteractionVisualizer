package com.loohp.interactionvisualizer.utils;

import com.lishid.openinv.OpenInv;
import com.loohp.interactionvisualizer.InteractionVisualizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OpenInvUtils {

    private static final boolean openinvhook = InteractionVisualizer.openinv;

    private static OpenInv openInvInstance = null;

    private static OpenInv getOpenInvInstance() {
        if (openInvInstance == null) {
            openInvInstance = (OpenInv) Bukkit.getPluginManager().getPlugin("OpenInv");
        }
        return openInvInstance;
    }

    public static boolean isSlientChest(Player player) {
        if (!openinvhook) {
            return false;
        }
        OpenInv openinv = getOpenInvInstance();
        boolean isSilent = openinv.getPlayerSilentChestStatus(player);
        return isSilent;
    }

}
