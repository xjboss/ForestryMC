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
package forestry.apiculture.multiblock;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.StatCollector;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import com.mojang.authlib.GameProfile;

import forestry.api.apiculture.BeeManager;
import forestry.api.apiculture.IBeeHousingInventory;
import forestry.api.apiculture.IBeeListener;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.IBeekeepingLogic;
import forestry.api.core.EnumHumidity;
import forestry.api.core.EnumTemperature;
import forestry.api.core.ForestryAPI;
import forestry.api.core.IClimateControlled;
import forestry.api.multiblock.IAlvearyComponent;
import forestry.api.multiblock.IMultiblockComponent;
import forestry.apiculture.AlvearyBeeModifier;
import forestry.apiculture.InventoryBeeHousing;
import forestry.core.access.EnumAccess;
import forestry.core.inventory.FakeInventoryAdapter;
import forestry.core.inventory.IInventoryAdapter;
import forestry.core.multiblock.IMultiblockControllerInternal;
import forestry.core.multiblock.MultiblockValidationException;
import forestry.core.multiblock.RectangularMultiblockControllerBase;
import forestry.core.network.DataInputStreamForestry;
import forestry.core.network.DataOutputStreamForestry;
import forestry.core.proxy.Proxies;
import forestry.core.utils.BlockUtil;

public class AlvearyController extends RectangularMultiblockControllerBase implements IAlvearyControllerInternal, IClimateControlled {

	private final InventoryBeeHousing inventory;
	private final IBeekeepingLogic beekeepingLogic;

	private BiomeGenBase cachedBiome;
	private float tempChange = 0.0f;
	private float humidChange = 0.0f;

	// PARTS
	private final Set<IBeeModifier> beeModifiers = new HashSet<>();
	private final Set<IBeeListener> beeListeners = new HashSet<>();
	private final Set<IAlvearyComponent.Climatiser> climatisers = new HashSet<>();
	private final Set<IAlvearyComponent.Active> activeComponents = new HashSet<>();

	// CLIENT
	private int breedingProgressPercent = 0;

	public AlvearyController(World world) {
		super(world, AlvearyMultiblockSizeLimits.instance);
		this.inventory = new InventoryBeeHousing(9, getAccessHandler());
		this.beekeepingLogic = BeeManager.beeRoot.createBeekeepingLogic(this);

		this.beeModifiers.add(new AlvearyBeeModifier());
	}

	@Override
	public IBeeHousingInventory getBeeInventory() {
		return inventory;
	}

	@Override
	public IBeekeepingLogic getBeekeepingLogic() {
		return beekeepingLogic;
	}

	@Override
	public IInventoryAdapter getInternalInventory() {
		if (isAssembled()) {
			return inventory;
		} else {
			return FakeInventoryAdapter.instance();
		}
	}

	@Override
	public Iterable<IBeeListener> getBeeListeners() {
		return beeListeners;
	}

	@Override
	public Iterable<IBeeModifier> getBeeModifiers() {
		return beeModifiers;
	}

	@Override
	public void onAttachedPartWithMultiblockData(IMultiblockComponent part, NBTTagCompound data) {
		this.readFromNBT(data);
	}

	@Override
	protected void onBlockAdded(IMultiblockComponent newPart) {
		if (newPart instanceof IAlvearyComponent) {
			if (newPart instanceof IAlvearyComponent.BeeModifier) {
				IAlvearyComponent.BeeModifier alvearyBeeModifier = (IAlvearyComponent.BeeModifier) newPart;
				IBeeModifier beeModifier = alvearyBeeModifier.getBeeModifier();
				beeModifiers.add(beeModifier);
			}

			if (newPart instanceof IAlvearyComponent.BeeListener) {
				IAlvearyComponent.BeeListener beeListenerSource = (IAlvearyComponent.BeeListener) newPart;
				IBeeListener beeListener = beeListenerSource.getBeeListener();
				beeListeners.add(beeListener);
			}

			if (newPart instanceof IAlvearyComponent.Climatiser) {
				climatisers.add((IAlvearyComponent.Climatiser) newPart);
			}

			if (newPart instanceof IAlvearyComponent.Active) {
				activeComponents.add((IAlvearyComponent.Active) newPart);
			}
		}
	}

	@Override
	protected void onBlockRemoved(IMultiblockComponent oldPart) {
		if (oldPart instanceof IAlvearyComponent) {
			if (oldPart instanceof IAlvearyComponent.BeeModifier) {
				IAlvearyComponent.BeeModifier alvearyBeeModifier = (IAlvearyComponent.BeeModifier) oldPart;
				IBeeModifier beeModifier = alvearyBeeModifier.getBeeModifier();
				beeModifiers.remove(beeModifier);
			}

			if (oldPart instanceof IAlvearyComponent.BeeListener) {
				IAlvearyComponent.BeeListener beeListenerSource = (IAlvearyComponent.BeeListener) oldPart;
				IBeeListener beeListener = beeListenerSource.getBeeListener();
				beeListeners.remove(beeListener);
			}

			if (oldPart instanceof IAlvearyComponent.Climatiser) {
				climatisers.remove(oldPart);
			}

			if (oldPart instanceof IAlvearyComponent.Active) {
				activeComponents.remove(oldPart);
			}
		}
	}

	@Override
	protected void isMachineWhole() throws MultiblockValidationException {
		super.isMachineWhole();

		final BlockPos maximumCoord = getMaximumCoord();
		final BlockPos minimumCoord = getMinimumCoord();

		// check that the top is covered in wood slabs

		final int slabY = maximumCoord.getY() + 1;
		for (int slabX = minimumCoord.getX(); slabX <= maximumCoord.getX(); slabX++) {
			for (int slabZ = minimumCoord.getZ(); slabZ <= maximumCoord.getZ(); slabZ++) {
				BlockPos pos = new BlockPos(slabX, slabY, slabZ);
				IBlockState state = worldObj.getBlockState(pos);
				Block block = state.getBlock();
				if (!BlockUtil.isWoodSlabBlock(block, worldObj, pos)) {
					throw new MultiblockValidationException(StatCollector.translateToLocal("for.multiblock.alveary.error.needSlabs"));
				}

				int meta = block.getMetaFromState(state);
				if ((meta & 8) != 0) {
					throw new MultiblockValidationException(StatCollector.translateToLocal("for.multiblock.alveary.error.needSlabs"));
				}
			}
		}

		// check that there is space all around the alveary entrances

		int airY = maximumCoord.getY();
		for (int airX = minimumCoord.getX() - 1; airX <= maximumCoord.getX() + 1; airX++) {
			for (int airZ = minimumCoord.getZ() - 1; airZ <= maximumCoord.getZ() + 1; airZ++) {
				if (isCoordInMultiblock(airX, airY, airZ)) {
					continue;
				}
				Block block = BlockUtil.getBlock(worldObj, new BlockPos(airX, airY, airZ));
				if (block.isOpaqueCube()) {
					throw new MultiblockValidationException(StatCollector.translateToLocal("for.multiblock.alveary.error.needSpace"));
				}
			}
		}
	}

	@Override
	protected void isGoodForExteriorLevel(IMultiblockComponent part, int level) throws MultiblockValidationException {
		if (level == 2 && !(part instanceof TileAlvearyPlain)) {
			throw new MultiblockValidationException(StatCollector.translateToLocal("for.multiblock.alveary.error.needPlainOnTop"));
		}
	}

	@Override
	protected void isGoodForInterior(IMultiblockComponent part) throws MultiblockValidationException {
		if (!(part instanceof TileAlvearyPlain)) {
			throw new MultiblockValidationException(StatCollector.translateToLocal("for.multiblock.alveary.error.needPlainInterior"));
		}
	}

	@Override
	protected void onAssimilate(IMultiblockControllerInternal assimilated) {

	}

	@Override
	public void onAssimilated(IMultiblockControllerInternal assimilator) {

	}

	@Override
	protected boolean updateServer(int tickCount) {
		for (IAlvearyComponent.Active activeComponent : activeComponents) {
			activeComponent.updateServer(tickCount);
		}

		final boolean canWork = beekeepingLogic.canWork();
		if (canWork) {
			beekeepingLogic.doWork();
		}

		for (IAlvearyComponent.Climatiser climatiser : climatisers) {
			climatiser.changeClimate(tickCount, this);
		}

		tempChange = equalizeChange(tempChange);
		humidChange = equalizeChange(humidChange);

		return canWork;
	}

	private static float equalizeChange(float change) {
		if (change == 0) {
			return 0;
		}

		change *= 0.95f;
		if (change <= 0.001f && change >= -0.001f) {
			change = 0;
		}
		return change;
	}

	@Override
	protected void updateClient(int tickCount) {
		for (IAlvearyComponent.Active activeComponent : activeComponents) {
			activeComponent.updateClient(tickCount);
		}

		if (beekeepingLogic.canDoBeeFX() && updateOnInterval(2)) {
			beekeepingLogic.doBeeFX();

			if (updateOnInterval(50)) {
				BlockPos center = getCenterCoord();
				float fxX = center.getX() + 0.5F;
				float fxY = center.getY() + 1.0F;
				float fxZ = center.getZ() + 0.5F;
				float distanceFromCenter = 1.6F;

				float leftRightSpreadFromCenter = distanceFromCenter * (worldObj.rand.nextFloat() - 0.5F);
				float upSpread = worldObj.rand.nextFloat() * 0.8F;
				fxY += upSpread;

				// display fx on all 4 sides
				Proxies.render.addEntityHoneyDustFX(worldObj, fxX - distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
				Proxies.render.addEntityHoneyDustFX(worldObj, fxX + distanceFromCenter, fxY, fxZ + leftRightSpreadFromCenter);
				Proxies.render.addEntityHoneyDustFX(worldObj, fxX + leftRightSpreadFromCenter, fxY, fxZ - distanceFromCenter);
				Proxies.render.addEntityHoneyDustFX(worldObj, fxX + leftRightSpreadFromCenter, fxY, fxZ + distanceFromCenter);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		data.setFloat("TempChange", tempChange);
		data.setFloat("HumidChange", humidChange);

		beekeepingLogic.writeToNBT(data);
		inventory.writeToNBT(data);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		tempChange = data.getFloat("TempChange");
		humidChange = data.getFloat("HumidChange");

		beekeepingLogic.readFromNBT(data);
		inventory.readFromNBT(data);
	}

	@Override
	public void formatDescriptionPacket(NBTTagCompound data) {
		writeToNBT(data);
		beekeepingLogic.syncToClient();
	}

	@Override
	public void decodeDescriptionPacket(NBTTagCompound data) {
		readFromNBT(data);
	}

	/* IActivatable */

	@Override
	public BlockPos getCoordinates() {
		BlockPos coord = getCenterCoord();
		return coord.add(0, +1, 0);
	}

	@Override
	public Vec3 getBeeFXCoordinates() {
		BlockPos coord = getCenterCoord();
		return new Vec3(coord.getX() + 0.5, coord.getY() + 1.5, coord.getZ() + 0.5);
	}

	@Override
	public void onSwitchAccess(EnumAccess oldAccess, EnumAccess newAccess) {
		if (oldAccess == EnumAccess.SHARED || newAccess == EnumAccess.SHARED) {
			// pipes connected to this need to update
			for (IMultiblockComponent part : connectedParts) {
				if (part instanceof TileEntity) {
					TileEntity tile = (TileEntity) part;
					worldObj.notifyBlockOfStateChange(tile.getPos(), tile.getBlockType());
				}
			}
			markDirty();
		}
	}

	@Override
	public float getExactTemperature() {
		BlockPos coords = getReferenceCoord();
		return ForestryAPI.climateManager.getTemperature(getWorld(), coords) + tempChange;
	}

	@Override
	public float getExactHumidity() {
		BlockPos coords = getReferenceCoord();
		return ForestryAPI.climateManager.getHumidity(getWorld(), coords) + humidChange;
	}

	@Override
	public EnumTemperature getTemperature() {
		IBeeModifier beeModifier = BeeManager.beeRoot.createBeeHousingModifier(this);
		if (beeModifier.isHellish() && tempChange >= 0) {
			return EnumTemperature.HELLISH;
		}

		return EnumTemperature.getFromValue(getExactTemperature());
	}

	@Override
	public EnumHumidity getHumidity() {
		return EnumHumidity.getFromValue(getExactHumidity());
	}

	@Override
	public GameProfile getOwner() {
		return getAccessHandler().getOwner();
	}

	@Override
	public World getWorld() {
		return worldObj;
	}

	@Override
	public BiomeGenBase getBiome() {
		if (cachedBiome == null) {
			BlockPos coords = getReferenceCoord();
			cachedBiome = worldObj.getBiomeGenForCoords(coords);
		}
		return cachedBiome;
	}

	@Override
	public int getBlockLightValue() {
		BlockPos topCenter = getTopCenterCoord();
		return worldObj.getLightFromNeighbors(topCenter.add(0, 1, 0));
	}

	@Override
	public boolean canBlockSeeTheSky() {
		BlockPos topCenter = getTopCenterCoord();
		return worldObj.canBlockSeeSky(topCenter.add(0, 2, 0));
	}

	@Override
	public void addTemperatureChange(float change, float boundaryDown, float boundaryUp) {
		BlockPos coordinates = getCoordinates();

		float temperature = ForestryAPI.climateManager.getTemperature(worldObj, getReferenceCoord());

		tempChange += change;
		tempChange = Math.max(boundaryDown - temperature, tempChange);
		tempChange = Math.min(boundaryUp - temperature, tempChange);
	}

	@Override
	public void addHumidityChange(float change, float boundaryDown, float boundaryUp) {
		float humidity = ForestryAPI.climateManager.getHumidity(worldObj, getReferenceCoord());

		humidChange += change;
		humidChange = Math.max(boundaryDown - humidity, humidChange);
		humidChange = Math.min(boundaryUp - humidity, humidChange);
	}

	/* GUI */
	@Override
	public int getHealthScaled(int i) {
		return breedingProgressPercent * i / 100;
	}

	@Override
	public void writeGuiData(DataOutputStreamForestry data) throws IOException {
		data.writeVarInt(beekeepingLogic.getBeeProgressPercent());
		data.writeVarInt(Math.round(tempChange * 100));
		data.writeVarInt(Math.round(humidChange * 100));
	}

	@Override
	public void readGuiData(DataInputStreamForestry data) throws IOException {
		breedingProgressPercent = data.readVarInt();
		tempChange = data.readVarInt() / 100.0F;
		humidChange = data.readVarInt() / 100.0F;
	}
}
