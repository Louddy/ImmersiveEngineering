package blusunrize.immersiveengineering.common.blocks.plant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class BlockIECrop<E extends Enum<E> & BlockIEBase.IBlockEnum> extends BlockBush implements IGrowable, IIEMetaBlock
{
	protected static IProperty[] tempProperties;
	protected static EnumWorldBlockLayer currentRenderLayer;

	public final String name;
	public final PropertyEnum<E> property;
	public final E[] enumValues;

	public BlockIECrop(String name, PropertyEnum<E> mainProperty)
	{
		super(setTempProperties(Material.plants, mainProperty));
		this.name = name;
		this.property = mainProperty;
		this.enumValues = mainProperty.getValueClass().getEnumConstants();
		this.setDefaultState(getInitDefaultState());
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setTickRandomly(true);
		this.setBlockBounds(0,0,0, 1,.25f,1);
		this.setCreativeTab((CreativeTabs)null);
		this.setHardness(0.0F);
		this.setStepSound(soundTypeGrass);
		this.disableStats();
		GameRegistry.registerBlock(this, name);
		IEContent.registeredIEBlocks.add(this);
	}

	@Override
	public String getIEBlockName()
	{
		return this.name;
	}
	@Override
	public Enum[] getMetaEnums()
	{
		return enumValues;
	}
	@Override
	public IBlockState getInventoryState(int meta)
	{
		IBlockState state = this.blockState.getBaseState().withProperty(this.property, enumValues[meta]);
		return state;
	}
	@Override
	public IProperty getMetaProperty()
	{
		return this.property;
	}
	@Override
	public boolean useCustomStateMapper()
	{
		return false;
	}
	@Override
	public String getCustomStateMapping(int meta)
	{
		return null;
	}

	protected static Material setTempProperties(Material material, PropertyEnum<?> property)
	{
		tempProperties=new IProperty[1];
		tempProperties[0] = property;
		return material;
	}

	protected BlockState createNotTempBlockState()
	{
		IProperty[] array = new IProperty[1];
		array[0] = this.property;
		return new BlockState(this, array);
	}
	protected IBlockState getInitDefaultState()
	{
		IBlockState state = this.blockState.getBaseState().withProperty(this.property, enumValues[0]);
		return state;
	}
	
	public void onIEBlockPlacedBy(World world, BlockPos pos, IBlockState state, EnumFacing side, float hitX, float hitY, float hitZ, EntityLivingBase placer, ItemStack stack)
	{
	}

	@Override
	protected BlockState createBlockState()
	{
		return this.property!=null?createNotTempBlockState(): new BlockState(this, tempProperties);
	}
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
	}
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(this.property).getMeta();
	}
	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return state;
	}
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState state = this.getDefaultState().withProperty(this.property, fromMeta(meta));
		return state;
	}
	protected E fromMeta(int meta)
	{
		if(meta<0||meta>=enumValues.length)
			meta = 0;
		return enumValues[meta];
	}

	public int getMinMeta(int meta)
	{
		return meta<=4?0:5;
	}
	public int getMaxMeta(int meta)
	{
		return meta<=4?4:5;
	}
	@Override
	public boolean canBlockStay(World world, BlockPos pos, IBlockState state)
	{
		boolean b = super.canBlockStay(world, pos, state);
		if(this.getMetaFromState(state)==5)
		{
			IBlockState stateBelow = world.getBlockState(pos.add(0,-1,0));
			b = stateBelow.getBlock().equals(this)&&this.getMetaFromState(stateBelow)==getMaxMeta(0);
		}
		return b;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		int meta = this.getMetaFromState(world.getBlockState(pos));
		this.setBlockBounds(0,0,0,1,meta==0?.375f: meta==1?.625f: meta==2?.875f: 1,1);
	}
	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		this.setBlockBoundsBasedOnState(world,pos);
		return super.getSelectedBoundingBox(world, pos);
	}
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		int meta = this.getMetaFromState(state);
		if(meta>=4)
		{
	        Random rand = world instanceof World ? ((World)world).rand : RANDOM;
			for (int i=0; i<3+fortune; ++i)
				if(rand.nextInt(8) <= meta)
					ret.add(new ItemStack(IEContent.itemMaterial,1,4));
			ret.add(new ItemStack(IEContent.itemSeeds,1,0));
		}

		return ret;
	}
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
	{
		super.onNeighborBlockChange(world, pos, state, block);
		if(this.getMetaFromState(state)<getMaxMeta(0))
			world.notifyBlockOfStateChange(pos.add(0,1,0), this);
	}


	@Override
	public void updateTick (World world, BlockPos pos, IBlockState state, Random random)
	{
		this.checkAndDropBlock(world, pos, state);
		int light = world.getLight(pos);
		if(light >= 12)
		{
			int meta = this.getMetaFromState(state);
			if(meta>4)
				return;
			float growth = this.getGrowthSpeed(world, pos, state, light);
			if(random.nextInt((int)(50F/growth)+1) == 0)
			{
				if(this.getMaxMeta(meta) != meta)
				{
					meta++;
					world.setBlockState(pos, this.getStateFromMeta(meta));
				}
				if(meta>3 && world.isAirBlock(pos.add(0,1,0)))
					world.setBlockState(pos.add(0,1,0), this.getStateFromMeta(meta+1));
			}
		}
	}
	float getGrowthSpeed(World world, BlockPos pos, IBlockState sate, int light)
	{
		float growth = 0.125f * (light - 11);
		if(world.canBlockSeeSky(pos))
			growth += 2f;
		IBlockState soil = world.getBlockState(pos.add(0,-1,0));
		if(soil.getBlock().isFertile(world,pos.add(0,-1,0)))
			growth *= 1.5f;
		return 1f + growth;
	}

	@Override
	protected boolean canPlaceBlockOn(Block block)
	{
		return block!=null && (block==this || block.equals(Blocks.farmland) || block instanceof BlockFarmland);
	}

	//isNotGrown
	@Override
	public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient)
	{
		int meta = this.getMetaFromState(state);
		if(meta<getMaxMeta(meta))
			return true;
		else 
			return meta==4 && !world.getBlockState(pos.add(0,1,0)).getBlock().equals(this);
	}
	//canBonemeal
	@Override
	public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state)
	{
		int meta = this.getMetaFromState(state);
		if(meta<getMaxMeta(meta))
			return true;
		else 
			return meta==4 && !world.getBlockState(pos.add(0,1,0)).getBlock().equals(this);
	}
	@Override
	public void grow(World world, Random rand, BlockPos pos, IBlockState state)
	{
		int meta = this.getMetaFromState(state);
		if(meta<getMaxMeta(meta))
		{
			int span = getMaxMeta(meta)-meta;
			int newMeta = meta+rand.nextInt(span)+1;
			if(newMeta!=meta)
				world.setBlockState(pos, this.getStateFromMeta(newMeta));
			meta = newMeta;
		}
		if(meta==4 && world.isAirBlock(pos.add(0,1,0)))
			world.setBlockState(pos.add(0,1,0), this.getStateFromMeta(meta+1));
	}
}