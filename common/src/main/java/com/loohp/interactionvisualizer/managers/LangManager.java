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
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LangManager {

    private static final File DataFolder = InteractionVisualizer.plugin.getDataFolder();
    private static final File LangFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Language_Repo");
    private static final File TempFolder = new File(InteractionVisualizer.plugin.getDataFolder(), "Temp");
    private static final int BUFFER_SIZE = 4096;

    public static void generate() {
        try {
            removeFolder(TempFolder);

            //https://github.com/LOOHP/InteractionVisualizerLanguages/archive/master.zip
            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractionVisualizer] Downloading and extracting latest Language files...");
            TempFolder.mkdirs();

            File zip = downloadFile(new File(TempFolder, "Lang.zip"), new URL("https://github.com/LOOHP/InteractionVisualizerLanguages/archive/master.zip"));
            if (zip == null) {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to connect to github. Could be an internet issue.");
                try {
                    removeFolder(TempFolder);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            removeFolder(LangFolder);
            extract(new ZipInputStream(new FileInputStream(zip)), DataFolder);
            new File(DataFolder, "InteractionVisualizerLanguages-master").renameTo(new File(DataFolder, "Language_Repo"));

            removeFolder(TempFolder);

            Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[InteractionVisualizer] Sucessfully downloaded the latest Language files!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeFolder(File folder) {
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    removeFolder(file);
                } else {
                    file.delete();
                }
            }
            folder.delete();
        }
    }

    public static File downloadFile(File output, URL download) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(download.openStream());

            FileOutputStream fos = new FileOutputStream(output);

            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            fos.close();

            return output;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void extract(ZipInputStream zip, File target) throws IOException {
        try {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                File file = new File(target, entry.getName());

                if (!file.toPath().normalize().startsWith(target.toPath())) {
                    throw new IOException("Bad zip entry");
                }

                if (entry.isDirectory()) {
                    file.mkdirs();
                    continue;
                }

                byte[] buffer = new byte[BUFFER_SIZE];
                file.getParentFile().mkdirs();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                int count;

                while ((count = zip.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }

                out.close();
            }
        } finally {
            zip.close();
        }
    }

}
