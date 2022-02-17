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

import com.loohp.interactionvisualizer.nms.NMS;
import com.loohp.interactionvisualizer.objectholders.BlockPosition;
import com.loohp.interactionvisualizer.objectholders.BoundingBox;

import java.util.List;

public class BoundingBoxUtils {

    private static final NMS nmsImplementation = NMS.getInstance();

    public static List<BoundingBox> getBoundingBoxes(BlockPosition pos) {
        if (nmsImplementation != null) {
            return nmsImplementation.getBoundingBoxes(pos);
        } else {
            throw new RuntimeException("No NMS implementation found for BoundingBoxUtils.getBoundingBoxes(BlockPosition)");
        }
    }

}
