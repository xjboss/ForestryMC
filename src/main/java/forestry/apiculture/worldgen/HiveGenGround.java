/*******************************************************************************
 * Copyright (c) 2011-2014 SirSengir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-3.0.txt
 *
 * Various Contributors including, but not limited to:
 * SirSengir (original work), CovertJaguar, Player, Binnie, MysteriousAges
 ******************************************************************************/
package forestry.apiculture.worldgen;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import forestry.core.utils.BlockUtil;

public class HiveGenGround extends HiveGen {

	private final Set<Material> groundMaterials = new HashSet<>();

	public HiveGenGround(Block... groundBlocks) {
		for (Block block : groundBlocks) {
			groundMaterials.add(block.getMaterial());
		}
	}

	@Override
	public boolean isValidLocation(World world, BlockPos pos) {
		Block ground = BlockUtil.getBlock(world, pos.add(0, -1, 0));
		return groundMaterials.contains(ground.getMaterial());
	}

	@Override
	public int getYForHive(World world, int x, int z) {

		// get to the ground
		BlockPos pos = world.getHeight(new BlockPos(x, 0, z));
		while (pos.getY() >= 0 && (world.getBlockState(pos.down()).getBlock().isLeaves(world, pos.down()) || canReplace(world, pos.down()))) {
			pos = pos.add(0, -1, 0);
		}

		return pos.getY();
	}
}
