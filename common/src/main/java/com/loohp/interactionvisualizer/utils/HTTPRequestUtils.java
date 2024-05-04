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

package com.loohp.interactionvisualizer.utils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class HTTPRequestUtils {

    public static JSONObject getJSONResponse(String link) {
        try {
            return (JSONObject) new JSONParser().parse(getTextResponse(link, true));
        } catch (ParseException e) {
            return null;
        }
    }

    public static String getTextResponse(String link) {
        return getTextResponse(link, false);
    }

    public static String getTextResponse(String link, boolean joinLines) {
        try {
            URL url = new URL(link);
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            connection.setDefaultUseCaches(false);
            connection.addRequestProperty("User-Agent", "Mozilla/5.0");
            connection.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
            connection.addRequestProperty("Pragma", "no-cache");
            Collector<CharSequence, ?, String> c = joinLines ? Collectors.joining() : Collectors.joining("\n");
            String reply = new BufferedReader(new InputStreamReader(connection.getInputStream())).lines().collect(c);
            return reply;
        } catch (IOException e) {
            return null;
        }
    }

    public static boolean download(File file, String link) {
        try {
            ReadableByteChannel rbc = Channels.newChannel(new URL(link).openStream());
            FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static byte[] download(String link) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            InputStream is = new URL(link).openStream();
            byte[] byteChunk = new byte[4096];
            int n;
            while ((n = is.read(byteChunk)) > 0) {
                baos.write(byteChunk, 0, n);
            }
            is.close();
            return baos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

}
