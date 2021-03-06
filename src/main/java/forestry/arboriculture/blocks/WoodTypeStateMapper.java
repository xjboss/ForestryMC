package forestry.arboriculture.blocks;

import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.api.arboriculture.EnumWoodType;
import forestry.arboriculture.IWoodTyped;
import forestry.arboriculture.blocks.property.PropertyWoodType;

@SideOnly(Side.CLIENT)
class WoodTypeStateMapper extends StateMapperBase {

	@Nonnull
	private final IWoodTyped woodTyped;
	@Nonnull
	private final String blockPath;
	@Nullable
	private final PropertyWoodType propertyWoodType;
	@Nonnull
	private final List<IProperty> propertysToRemove = new ArrayList();

	public WoodTypeStateMapper(@Nonnull IWoodTyped woodTyped, @Nullable PropertyWoodType propertyWoodType) {
		this.woodTyped = woodTyped;
		this.blockPath = woodTyped.getBlockKind();
		this.propertyWoodType = propertyWoodType;
	}

	public WoodTypeStateMapper(@Nonnull IWoodTyped woodTyped, @Nonnull String blockPath, @Nullable PropertyWoodType propertyWoodType) {
		this.woodTyped = woodTyped;
		this.blockPath = blockPath;
		this.propertyWoodType = propertyWoodType;
	}
	
	public WoodTypeStateMapper addPropertyToRemove(IProperty property){
		this.propertysToRemove.add(property);
		return this;
	}

	@Override
	protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
		final Map<IProperty, Comparable> properties;
		if (propertyWoodType != null) {
			properties = Maps.newLinkedHashMap(state.getProperties());
			properties.remove(propertyWoodType);
		} else {
			properties = Maps.newLinkedHashMap(state.getProperties());
		}
		
		for(IProperty property : propertysToRemove){
			properties.remove(property);
		}

		Block block = state.getBlock();
		int meta = block.getMetaFromState(state);
		EnumWoodType woodType = woodTyped.getWoodType(meta);
		String resourceDomain = Block.blockRegistry.getNameForObject(block).getResourceDomain();
		String resourceLocation = "arboriculture/" + blockPath + '/' + woodType;
		String propertyString = this.getPropertyString(properties);
		return new ModelResourceLocation(resourceDomain + ':' + resourceLocation, propertyString);
	}

}
