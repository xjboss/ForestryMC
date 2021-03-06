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
package forestry.core.render;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import forestry.apiculture.render.ModelAnalyzer;
import forestry.core.proxy.Proxies;
import forestry.core.tiles.TileAnalyzer;

public class RenderAnalyzer extends TileEntitySpecialRenderer<TileAnalyzer> {

	private final ModelAnalyzer model;
	private final EntityItem dummyEntityItem = new EntityItem(null);
	private long lastTick;

	public RenderAnalyzer(String baseTexture) {
		this.model = new ModelAnalyzer(baseTexture);
	}
	
	/**
	 * @param analyzer If it null its render the item else it render the tile entity.
	 */
	@Override
	public void renderTileEntityAt(TileAnalyzer analyzer, double x, double y, double z, float partialTicks, int destroyStage) {
		if (analyzer != null) {
			render(analyzer.getIndividualOnDisplay(), analyzer.getWorld(), analyzer.getOrientation(), x, y, z);
		} else {
			render(null, null, EnumFacing.WEST, x, y, z);
		}
	}

	private void render(ItemStack itemstack, World world, EnumFacing orientation, double x, double y, double z) {

		dummyEntityItem.worldObj = world;

		model.render(orientation, (float) x, (float) y, (float) z);
		if (itemstack == null) {
			return;
		}
		float renderScale = 1.0f;

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y, (float) z);
		GL11.glTranslatef(0.5f, 0.2f, 0.5f);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(itemstack);

		if (world.getTotalWorldTime() != lastTick) {
			lastTick = world.getTotalWorldTime();
			dummyEntityItem.onUpdate();
		}
		RenderManager rendermanager = Proxies.common.getClientInstance().getRenderManager();
		
		rendermanager.renderEntityWithPosYaw(dummyEntityItem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
		GL11.glPopMatrix();

	}

}
