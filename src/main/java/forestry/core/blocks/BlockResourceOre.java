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
package forestry.core.blocks;

import java.util.ArrayList;
import java.util.List;
import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import forestry.core.CreativeTabForestry;
import forestry.core.PluginCore;

public class BlockResourceOre extends Block implements IItemModelRegister {
	public static final PropertyEnum<EnumResourceType> ORE_RESOURCES = PropertyEnum.create("resource", EnumResourceType.class, new Predicate<EnumResourceType>(){
		@Override
		public boolean apply(EnumResourceType input) {
			return input.hasOre();
		}
	});
	
	public BlockResourceOre() {
		super(Material.rock);
		setHardness(3F);
		setResistance(5F);
		setCreativeTab(CreativeTabForestry.tabForestry);
		setDefaultState(this.blockState.getBaseState().withProperty(ORE_RESOURCES, EnumResourceType.APATITE));
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, ORE_RESOURCES);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ORE_RESOURCES).getMeta();
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ORE_RESOURCES, EnumResourceType.VALUES[meta]);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
		super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
		
		if (state.getValue(ORE_RESOURCES) == EnumResourceType.APATITE) {
			this.dropXpOnBlockBreak(world, pos, MathHelper.getRandomIntegerInRange(world.rand, 1, 4));
		}
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList();
		EnumResourceType metadata = state.getValue(ORE_RESOURCES);

		if (metadata == EnumResourceType.APATITE) {
			int fortuneModifier = RANDOM.nextInt(fortune + 2) - 1;
			if (fortuneModifier < 0) {
				fortuneModifier = 0;
			}

			int amount = (2 + RANDOM.nextInt(5)) * (fortuneModifier + 1);
			if (amount > 0) {
				drops.add(PluginCore.items.apatite.getItemStack(amount));
			}
		} else {
			drops.add(new ItemStack(this, 1, metadata.getMeta()));
		}

		return drops;
	}

	@Override
	public int getDamageValue(World world, BlockPos pos) {
		return getMetaFromState(world.getBlockState(pos));
	}
	
	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs par2CreativeTabs, List<ItemStack> itemList) {
		for (EnumResourceType resourceType : ORE_RESOURCES.getAllowedValues()) {
			itemList.add(get(resourceType, 1));
		}
	}

	/* MODLES */
	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel(Item item, IModelManager manager) {
		manager.registerItemModel(item, 0, "ores/apatite");
		manager.registerItemModel(item, 1, "ores/copper");
		manager.registerItemModel(item, 2, "ores/tin");
	}

	public ItemStack get(EnumResourceType type, int amount) {
		return new ItemStack(this, amount, type.getMeta());
	}
}
