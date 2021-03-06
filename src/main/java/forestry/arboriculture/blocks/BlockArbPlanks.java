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
package forestry.arboriculture.blocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.arboriculture.EnumWoodType;
import forestry.api.arboriculture.TreeManager;
import forestry.api.core.IItemModelRegister;
import forestry.api.core.IModelManager;
import forestry.api.core.IStateMapperRegister;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.WoodHelper;
import forestry.arboriculture.blocks.property.PropertyWoodType;
import forestry.core.proxy.Proxies;

public abstract class BlockArbPlanks extends Block implements IWoodTyped, IStateMapperRegister, IItemModelRegister {
	private static final int VARIANTS_PER_BLOCK = 16;
	private static final int VARIANTS_META_MASK = VARIANTS_PER_BLOCK - 1;

	public static List<BlockArbPlanks> create(boolean fireproof) {
		List<BlockArbPlanks> planks = new ArrayList<>();
		final int blockCount = PropertyWoodType.getBlockCount(VARIANTS_PER_BLOCK);
		for (int blockNumber = 0; blockNumber < blockCount; blockNumber++) {
			final PropertyWoodType variant = PropertyWoodType.create("variant", blockNumber, VARIANTS_PER_BLOCK);
			BlockArbPlanks block = new BlockArbPlanks(fireproof, blockNumber) {
				@Nonnull
				@Override
				protected PropertyWoodType getVariant() {
					return variant;
				}
			};
			planks.add(block);
		}
		return planks;
	}

	private final boolean fireproof;
	private final int blockNumber;

	private BlockArbPlanks(boolean fireproof, int blockNumber) {
		super(Material.wood);
		this.fireproof = fireproof;
		this.blockNumber = blockNumber;

		PropertyWoodType variant = getVariant();
		setDefaultState(this.blockState.getBaseState().withProperty(variant, variant.getFirstType()));

		setResistance(5.0F);
		setHarvestLevel("axe", 0);
	}

	@Override
	public boolean isFireproof() {
		return fireproof;
	}

	@Nonnull
	protected abstract PropertyWoodType getVariant();

	public int getBlockNumber() {
		return blockNumber;
	}

	@Nonnull
	@Override
	public String getBlockKind() {
		return "planks";
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumWoodType woodType = getWoodType(meta);
		return getDefaultState().withProperty(getVariant(), woodType);
	}

	@Nonnull
	@Override
	public EnumWoodType getWoodType(int meta) {
		int variantMeta = (meta & VARIANTS_META_MASK) + blockNumber * VARIANTS_PER_BLOCK;
		return EnumWoodType.byMetadata(variantMeta);
	}

	@Nonnull
	@Override
	public Collection<EnumWoodType> getWoodTypes() {
		return getVariant().getAllowedValues();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return damageDropped(state);
	}

	@Override
	protected BlockState createBlockState() {
		return new BlockState(this, getVariant());
	}

	@Override
	public int damageDropped(IBlockState state) {
		return state.getValue(getVariant()).getMetadata() - blockNumber * VARIANTS_PER_BLOCK;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		EnumWoodType woodType = getWoodType(meta);
		return getDefaultState().withProperty(getVariant(), woodType);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
		for (EnumWoodType woodType : getVariant().getAllowedValues()) {
			list.add(TreeManager.woodItemAccess.getPlanks(woodType, fireproof));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerStateMapper() {
		Proxies.render.registerStateMapper(this, new WoodTypeStateMapper(this, getVariant()));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModel(Item item, IModelManager manager) {
		manager.registerVariant(item, WoodHelper.getResourceLocations(this));
		manager.registerItemModel(item, new WoodHelper.WoodMeshDefinition(this));
	}

	@Override
	public float getBlockHardness(World world, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);
		int meta = getMetaFromState(blockState);
		EnumWoodType woodType = getWoodType(meta);
		return woodType.getHardness();
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, EnumFacing face) {
		if (fireproof) {
			return 0;
		}
		return 20;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, EnumFacing face) {
		if (fireproof) {
			return 0;
		}
		return 5;
	}
}
