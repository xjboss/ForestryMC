package forestry.farming.blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IStringSerializable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import forestry.core.render.TextureManager;

public enum EnumFarmBlockType implements IStringSerializable {
	PLAIN,
	BAND,
	GEARBOX,
	HATCH,
	VALVE,
	CONTROL;
	
	public static final EnumFarmBlockType[] VALUES = values();
	
	private static final int TYPE_PLAIN = 0;
	private static final int TYPE_REVERSE = 1;
	private static final int TYPE_TOP = 2;
	private static final int TYPE_BAND = 3;
	private static final int TYPE_GEARS = 4;
	private static final int TYPE_HATCH = 5;
	private static final int TYPE_VALVE = 6;
	private static final int TYPE_CONTROL = 7;

	@SideOnly(Side.CLIENT)
	private static List<TextureAtlasSprite> sprites;

	@SideOnly(Side.CLIENT)
	public static void registerSprites() {
		sprites = Arrays.asList(
				TextureManager.registerSprite("blocks/farm/plain"),
				TextureManager.registerSprite("blocks/farm/reverse"),
				TextureManager.registerSprite("blocks/farm/top"),
				TextureManager.registerSprite("blocks/farm/band"),
				TextureManager.registerSprite("blocks/farm/gears"),
				TextureManager.registerSprite("blocks/farm/hatch"),
				TextureManager.registerSprite("blocks/farm/valve"),
				TextureManager.registerSprite("blocks/farm/control")
		);
	}
	
	/**
	 * @return The texture sprite from the type of the farm block
	 */
	@SideOnly(Side.CLIENT)
	public static TextureAtlasSprite getSprite(EnumFarmBlockType type, int side) {
		switch (type) {
			case PLAIN: {
				if (side == 2) {
					return sprites.get(TYPE_REVERSE);
				} else if (side == 0 || side == 1) {
					return sprites.get(TYPE_TOP);
				} else {
					return sprites.get(TYPE_PLAIN);
				}
			}
			case BAND:
				return sprites.get(TYPE_BAND);
			case GEARBOX:
				return sprites.get(TYPE_GEARS);
			case HATCH:
				return sprites.get(TYPE_HATCH);
			case VALVE:
				return sprites.get(TYPE_VALVE);
			case CONTROL:
				return sprites.get(TYPE_CONTROL);
			default:
				return sprites.get(TYPE_PLAIN);
		}
	}
	
	@Override
	public String getName() {
		return name().toLowerCase(Locale.ENGLISH);
	}
	
}
