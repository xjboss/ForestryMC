package forestry.greenhouse.blocks;

import java.util.Map;

import forestry.core.blocks.BlockRegistry;
import forestry.core.items.ItemBlockForestry;
import forestry.greenhouse.items.ItemBlockGreenhouseDoor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class BlockRegistryGreenhouse extends BlockRegistry{
	
	private final Map<BlockGreenhouseType, BlockGreenhouse> greenhouseBlockMap;
	
	public BlockRegistryGreenhouse() {
		greenhouseBlockMap = BlockGreenhouse.create();
		for (BlockGreenhouse block : greenhouseBlockMap.values()) {
			ItemBlock itemBlock = new ItemBlockForestry(block);
			if(block instanceof BlockGreenhouseDoor){
				itemBlock = new ItemBlockGreenhouseDoor(block);
			}
			registerBlock(block, itemBlock, "greenhouse." + block.getGreenhouseType());
		}
	}

	public ItemStack getGreenhouseBlock(BlockGreenhouseType type) {
		BlockGreenhouse greenhouseBlock = greenhouseBlockMap.get(type);
		return new ItemStack(greenhouseBlock);
	}
	
}
