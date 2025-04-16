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

package com.loohp.interactionvisualizer.managers;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.config.Config;
import com.loohp.yamlconfiguration.YamlConfiguration;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;

public class MusicManager {

    public static final String MUSIC_CONFIG_ID = "music";

    public static void setup() {
        if (!InteractionVisualizer.plugin.getDataFolder().exists()) {
            InteractionVisualizer.plugin.getDataFolder().mkdir();
        }
        try {
            Config.loadConfig(MUSIC_CONFIG_ID, new File(InteractionVisualizer.plugin.getDataFolder(), "music.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml"), InteractionVisualizer.plugin.getClass().getClassLoader().getResourceAsStream("music.yml"), true);
        } catch (IOException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(InteractionVisualizer.plugin);
            return;
        }
    }

    public static YamlConfiguration getMusicConfig() {
        return Config.getConfig(MUSIC_CONFIG_ID).getConfiguration();
    }

    public static void saveConfig() {
        Config.getConfig(MUSIC_CONFIG_ID).save();
    }

    public static void reloadConfig() {
        Config.getConfig(MUSIC_CONFIG_ID).reload();
    }

}
