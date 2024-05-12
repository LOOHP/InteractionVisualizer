/*
 * This file is part of InteractionVisualizer.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
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

package com.loohp.interactionvisualizer.placeholderAPI;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI;
import com.loohp.interactionvisualizer.api.InteractionVisualizerAPI.Modules;
import com.loohp.interactionvisualizer.objectholders.EntryKey;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public String getAuthor() {
        return String.join(", ", InteractionVisualizer.plugin.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "interactionvisualizer";
    }

    @Override
    public String getVersion() {
        return "2.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String getRequiredPlugin() {
        return InteractionVisualizer.plugin.getName();
    }

    @Override
    public String onRequest(OfflinePlayer offlineplayer, String identifier) {

        if (identifier.startsWith("all")) {
            String entryType = identifier.substring("all_".length());
            if (entryType.equals("all")) {
                if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId())) {
                    return "enabled";
                } else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId())) {
                    return "partly enabled";
                }
                return "disabled";
            } else {
                EntryKey entry = new EntryKey(entryType);
                if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
                    if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), entry)) {
                        return "enabled";
                    } else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), entry)) {
                        return "partly enabled";
                    }
                    return "disabled";
                }
            }
            return "invalid";
        }

        if (identifier.startsWith("itemstand_")) {
            String entryType = identifier.substring("itemstand_".length());
            if (entryType.equals("all")) {
                if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMSTAND)) {
                    return "enabled";
                } else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMSTAND)) {
                    return "partly enabled";
                }
                return "disabled";
            } else {
                EntryKey entry = new EntryKey(entryType);
                if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
                    if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.ITEMSTAND, entry)) {
                        return "enabled";
                    }
                    return "disabled";
                }
            }
            return "invalid";
        }

        if (identifier.startsWith("itemdrop_")) {
            String entryType = identifier.substring("itemdrop_".length());
            if (entryType.equals("all")) {
                if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMDROP)) {
                    return "enabled";
                } else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), Modules.ITEMDROP)) {
                    return "partly enabled";
                }
                return "disabled";
            } else {
                EntryKey entry = new EntryKey(entryType);
                if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
                    if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.ITEMDROP, entry)) {
                        return "enabled";
                    }
                    return "disabled";
                }
            }
            return "invalid";
        }

        if (identifier.startsWith("hologram_")) {
            String entryType = identifier.substring("hologram_".length());
            if (entryType.equals("all")) {
                if (InteractionVisualizerAPI.hasAllPreferenceEnabled(offlineplayer.getUniqueId(), Modules.HOLOGRAM)) {
                    return "enabled";
                } else if (InteractionVisualizerAPI.hasAnyPreferenceEnabled(offlineplayer.getUniqueId(), Modules.HOLOGRAM)) {
                    return "partly enabled";
                }
                return "disabled";
            } else {
                EntryKey entry = new EntryKey(entryType);
                if (InteractionVisualizerAPI.isRegisteredEntry(entry)) {
                    if (InteractionVisualizerAPI.hasPlayerEnabledModule(offlineplayer.getUniqueId(), Modules.HOLOGRAM, entry)) {
                        return "enabled";
                    }
                    return "disabled";
                }
            }
            return "invalid";
        }

        return null;
    }

}
