/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gl.VertexBuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.clickgui.ClickGui;
import net.wurstclient.clickgui.Component;
import net.wurstclient.clickgui.Window;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.hud.IngameHUD;
import net.wurstclient.settings.BlockListSetting;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.settings.ItemListSetting;
import net.wurstclient.settings.MobListSetting;
import net.wurstclient.settings.Setting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"mob esp", "MobTracers", "mob tracers"})
public final class MobEspHack extends Hack implements UpdateListener,
	CameraTransformViewBobbingListener, RenderListener
{
	private final EnumSetting<Style> style =
		new EnumSetting<>("Style", Style.values(), Style.BOXES);
	
	private final EnumSetting<BoxSize> boxSize = new EnumSetting<>("Box size",
		"\u00a7lAccurate\u00a7r mode shows the exact\n"
			+ "hitbox of each mob.\n"
			+ "\u00a7lFancy\u00a7r mode shows slightly larger\n"
			+ "boxes that look better.",
		BoxSize.values(), BoxSize.FANCY);
	
	private final CheckboxSetting filterInvisible = new CheckboxSetting(
		"Filter invisible", "Won't show invisible mobs.", false);
	
	private final CheckboxSetting filterMobs = new CheckboxSetting(
		"Filter Mobs", "Won't show other mobs.", false);

	private final EnumSetting<OnlyMob_> onlyMob =
		new EnumSetting<>("OnlyMob", OnlyMob_.values(), OnlyMob_.zombie_villager);
	private final MobListSetting onlyMob_0 = new MobListSetting("Ores", "",
		"minecraft:ancient_debris", "minecraft:anvil", "minecraft:beacon",
		"minecraft:bone_block", "minecraft:bookshelf",
		"minecraft:brewing_stand", "minecraft:chain_command_block",
		"minecraft:chest", "minecraft:clay", "minecraft:coal_block",
		"minecraft:coal_ore", "minecraft:command_block", "minecraft:copper_ore",
		"minecraft:crafting_table", "minecraft:deepslate_coal_ore",
		"minecraft:deepslate_copper_ore", "minecraft:deepslate_diamond_ore",
		"minecraft:deepslate_gold_ore", "minecraft:deepslate_iron_ore",
		"minecraft:deepslate_lapis_ore", "minecraft:deepslate_redstone_ore",
		"minecraft:diamond_block", "minecraft:diamond_ore",
		"minecraft:dispenser", "minecraft:dropper", "minecraft:emerald_block",
		"minecraft:emerald_ore", "minecraft:enchanting_table",
		"minecraft:end_portal", "minecraft:end_portal_frame",
		"minecraft:ender_chest", "minecraft:furnace", "minecraft:glowstone",
		"minecraft:gold_block", "minecraft:gold_ore", "minecraft:hopper",
		"minecraft:iron_block", "minecraft:iron_ore", "minecraft:ladder",
		"minecraft:lapis_block", "minecraft:lapis_ore", "minecraft:lava",
		"minecraft:lodestone", "minecraft:mossy_cobblestone",
		"minecraft:nether_gold_ore", "minecraft:nether_portal",
		"minecraft:nether_quartz_ore", "minecraft:raw_copper_block",
		"minecraft:raw_gold_block", "minecraft:raw_iron_block",
		"minecraft:redstone_block", "minecraft:redstone_ore",
		"minecraft:repeating_command_block", "minecraft:spawner",
		"minecraft:tnt", "minecraft:torch", "minecraft:trapped_chest",
		"minecraft:water");

	private final CheckboxSetting showMobsNames = new CheckboxSetting(
		"Show Mobs Names", "show mobs names.", false);

	private final ArrayList<MobEntity> mobs = new ArrayList<>();
	private VertexBuffer mobBox;
	
	public MobEspHack()
	{
		super("MobESP");
		setCategory(Category.RENDER);
		addSetting(style);
		addSetting(boxSize);
		addSetting(filterInvisible);

		addSetting(filterMobs);
		addSetting(onlyMob);
		addSetting(onlyMob_0);
//		addSetting(showMobsNames);
	}
	
	@Override
	public void onEnable()
	{
		if(filterMobs.isChecked()) {
//			filterMobs.lock(filterMobs);
			
			Component onlyMobCom = onlyMob.getComponent();
			if(onlyMobCom != null) {
				Window w_cgui_wdw = onlyMob.getComponent().getParent();
				if(w_cgui_wdw != null) {
					int i_tmp = onlyMob.getComponent().getParent().countChildren();
					onlyMobCom = onlyMob.getComponent().getParent().getChild(0);

					//onlyMob.getComponent().getParent().setInvisible(true);
					//onlyMob.getComponent().getParent().setInvisible(filterMobs.isChecked());
				}
			}

			ClickGui gui = this.WURST.getGui();
			if (null != gui) {
				gui.getTxtColor();

				/*Window win = new Window("test");
				win.validate();
				gui.addWindow(win);;*/
			}

			
						
//			filterMobs.unlock();
		} else {
			onlyMob.getComponent().setHeight(1);
		}

		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(CameraTransformViewBobbingListener.class, this);
		EVENTS.add(RenderListener.class, this);
		
		mobBox = new VertexBuffer();
		Box bb = new Box(-0.5, 0, -0.5, 0.5, 1, 0.5);
		RenderUtils.drawOutlinedBox(bb, mobBox);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(CameraTransformViewBobbingListener.class, this);
		EVENTS.remove(RenderListener.class, this);
		
		if(mobBox != null)
			mobBox.close();
	}
	
	@Override
	public void onUpdate()
	{
		mobs.clear();
		
		Stream<MobEntity> stream =
			StreamSupport.stream(MC.world.getEntities().spliterator(), false)
				.filter(e -> e instanceof MobEntity).map(e -> (MobEntity)e)
				.filter(e -> !e.isRemoved() && e.getHealth() > 0);
		
		if(filterInvisible.isChecked())
			stream = stream.filter(e -> !e.isInvisible());

		if(filterMobs.isChecked()) {
			stream = stream.filter(e -> (e.getType() == onlyMob.getSelected().getType()));

			//if((onlyMob.getSelected().getDescription() != null) && (!onlyMob.getSelected().getDescription().isEmpty())) {
			if(onlyMob.getSelected().getItem() != null) {
				//String sName[] = onlyMob.getSelected().toString().split(",");
				String sItemId = onlyMob.getSelected().getDescription().toLowerCase();//"trident";

				//stream = stream.filter(e -> e.toString().contains(sName[0]));
				try {
					//net.minecraft.item.Item mcItem = net.minecraft.util.registry.Registry.ITEM.get(new net.minecraft.util.Identifier(sItemId));
					stream = stream.filter(e -> e.isHolding(onlyMob.getSelected().getItem()));

				} catch(net.minecraft.util.InvalidIdentifierException e) {
					/*throw */new net.wurstclient.command.CmdSyntaxError("Invalid item: " + sItemId);
				}

			}/* else {
				stream = stream.filter(e -> e.toString().contains(onlyMob.getSelected().toString()));
			}*/
		}
		
		mobs.addAll(stream.collect(Collectors.toList()));
	}
	
	@Override
	public void onCameraTransformViewBobbing(
		CameraTransformViewBobbingEvent event)
	{
		if(style.getSelected().lines)
			event.cancel();
	}
	
	@Override
	public void onRender(MatrixStack matrixStack, float partialTicks)
	{
		// GL settings
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		
		if(style.getSelected().boxes)
			renderBoxes(matrixStack, partialTicks, regionX, regionZ);
		
		if(style.getSelected().lines)
			renderTracers(matrixStack, partialTicks, regionX, regionZ);

		if(/*true || */showMobsNames.isChecked())
			renderNames(partialTicks);
		
		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
	}
	
	private void renderBoxes(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		float extraSize = boxSize.getSelected().extraSize;
		RenderSystem.setShader(GameRenderer::getPositionShader);
		
		for(MobEntity e : mobs)
		{
			matrixStack.push();
			
			matrixStack.translate(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ);
			
			matrixStack.scale(e.getWidth() + extraSize,
				e.getHeight() + extraSize, e.getWidth() + extraSize);
			
			float f = MC.player.distanceTo(e) / 20F;
			RenderSystem.setShaderColor(2 - f, f, 0, 0.5F);
			
			Shader shader = RenderSystem.getShader();
			Matrix4f matrix4f = RenderSystem.getProjectionMatrix();
			mobBox.bind();
			mobBox.draw(matrixStack.peek().getPositionMatrix(), matrix4f,
				shader);
			VertexBuffer.unbind();
			
			matrixStack.pop();
		}
	}
	
	private void renderTracers(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		RenderSystem.setShaderColor(1, 1, 1, 1);
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
			VertexFormats.POSITION_COLOR);
		
		Vec3d start = RotationUtils.getClientLookVec()
			.add(RenderUtils.getCameraPos()).subtract(regionX, 0, regionZ);
		
		for(MobEntity e : mobs)
		{
			Vec3d interpolationOffset = new Vec3d(e.getX(), e.getY(), e.getZ())
				.subtract(e.prevX, e.prevY, e.prevZ).multiply(1 - partialTicks);
			
			Vec3d end = e.getBoundingBox().getCenter()
				.subtract(interpolationOffset).subtract(regionX, 0, regionZ);
			
			float f = MC.player.distanceTo(e) / 20F;
			float r = MathHelper.clamp(2 - f, 0, 1);
			float g = MathHelper.clamp(f, 0, 1);
			
			bufferBuilder
				.vertex(matrix, (float)start.x, (float)start.y, (float)start.z)
				.color(r, g, 0, 0.5F).next();
			
			bufferBuilder
				.vertex(matrix, (float)end.x, (float)end.y, (float)end.z)
				.color(r, g, 0, 0.5F).next();
		}
		
		tessellator.draw();
		
	}

	private void renderNames(double partialTicks) {// throws net.wurstclient.command.CmdSyntaxError
		for(MobEntity mob : mobs) {
			net.minecraft.item.ItemStack stack;
			boolean isValid;
			net.minecraft.item.Item.Settings setts;
			setts = new net.minecraft.item.Item.Settings();
			net.minecraft.item.TridentItem trident = new net.minecraft.item.TridentItem(setts);
			String sId = "trident";
			net.minecraft.item.Item mcItem = null;

			try {
				mcItem = net.minecraft.util.registry.Registry.ITEM.get(new net.minecraft.util.Identifier(sId));
			}catch(net.minecraft.util.InvalidIdentifierException e) {
				/*throw */new net.wurstclient.command.CmdSyntaxError("Invalid item: " + sId);
			}
			

			if((onlyMob.getSelected().getDescription() != null) && (!onlyMob.getSelected().getDescription().isEmpty())) {
				String sAry[] = onlyMob.getSelected().toString().split(",");
				if(sAry.length >= 2) {
					//MobEntity mEty;
					//mEty.getActiveHand().MAIN_HAND.
					//java.lang.Iterable<net.minecraft.item.ItemStack> stacks = mEty.getItemsEquipped();
					//stacks = mEty.getItemsHand();
					//mEty.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);
					//ArrayList<net.minecraft.item.Item> mcItems = new ArrayList<>();
					//MobEntity.canEquipmentSlotContain(net.minecraft.entity.EquipmentSlot.MAINHAND, net.minecraft.item.ItemStack);
					//net.minecraft.item.Item mcItem = mEty.getEquipmentForSlot(net.minecraft.entity.EquipmentSlot.MAINHAND, 0);

					isValid = mob.toString().contains(sAry[0]);
					isValid = mob.isUsingItem();
					isValid = mob.isHolding(trident);
					if(mcItem != null)
						isValid = mob.isHolding(mcItem);

					if(mob.getActiveItem() != null)
						isValid = mob.getActiveItem().getItem() instanceof net.minecraft.item.TridentItem;
					
					stack = mob.getEquippedStack(net.minecraft.entity.EquipmentSlot.MAINHAND);//"1 trident"
					isValid = mob.isHolding(stack.getItem());
					isValid = stack.getItem() instanceof net.minecraft.item.TridentItem;
					stack = mob.getEquippedStack(net.minecraft.entity.EquipmentSlot.OFFHAND);//"1 nautilus_shell"
					isValid = mob.isHolding(stack.getItem());
					isValid = stack.getItem() instanceof net.minecraft.item.TridentItem;

					isValid = mob.hasStackEquipped(net.minecraft.entity.EquipmentSlot.MAINHAND);
					isValid = mob.hasStackEquipped(net.minecraft.entity.EquipmentSlot.OFFHAND);
					isValid = isValid ? true : false;
					//stream = stream.filter(e -> e.toString().contains(sAry[0])).filter(e -> e.isUsingItem()).filter(e -> e.isHolding(trident));
				}
			}

			if(trident != null)
				trident.isDamageable();
		}
	}
	
	private enum Style
	{
		BOXES("Boxes only", true, false),
		LINES("Lines only", false, true),
		LINES_AND_BOXES("Lines and boxes", true, true),
		BOXES_AND_NAMES("boxes and names", false, false);
		
		private final String name;
		private final boolean boxes;
		private final boolean lines;
		
		private Style(String name, boolean boxes, boolean lines)
		{
			this.name = name;
			this.boxes = boxes;
			this.lines = lines;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}
	
	private enum BoxSize
	{
		ACCURATE("Accurate", 0),
		FANCY("Fancy", 0.1F);
		
		private final String name;
		private final float extraSize;
		
		private BoxSize(String name, float extraSize)
		{
			this.name = name;
			this.extraSize = extraSize;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
	}

	private enum OnlyMob_ {
		drowned("Drowned", "Trident", net.minecraft.entity.EntityType.DROWNED, net.minecraft.item.Items.TRIDENT),
		axolotl("Axolotl", net.minecraft.entity.EntityType.AXOLOTL),
		bee("Bee", net.minecraft.entity.EntityType.BEE),
		enderman("Enderman", net.minecraft.entity.EntityType.ENDERMAN),
		ender_dragon("Ender Dragon", net.minecraft.entity.EntityType.ENDER_DRAGON),
		evoker("Evoker", net.minecraft.entity.EntityType.EVOKER),
		experience_orb("Experience orb", net.minecraft.entity.EntityType.EXPERIENCE_ORB),
		eye_of_ender("Eye of Ender", net.minecraft.entity.EntityType.EYE_OF_ENDER),
		glow_squid("Glow Squid", net.minecraft.entity.EntityType.GLOW_SQUID),
		magma_cube("Magma cube", net.minecraft.entity.EntityType.MAGMA_CUBE),
		pillager("Pillager", net.minecraft.entity.EntityType.PILLAGER),
		shulker("Shulker", net.minecraft.entity.EntityType.SHULKER),
		skeleton_hourse(net.minecraft.entity.mob.SkeletonHorseEntity.ID_KEY, net.minecraft.entity.EntityType.SKELETON_HORSE),
		slime("Slime", net.minecraft.entity.EntityType.SLIME),
		squid("Squid", net.minecraft.entity.EntityType.SQUID),
		warden("Warden", net.minecraft.entity.EntityType.WARDEN),
		wither_skeleton("Wither skeleton", net.minecraft.entity.EntityType.WITHER_SKELETON),
		wither_skull("Wither skeleton skull", net.minecraft.entity.EntityType.WITHER_SKULL),
		wolf("Wolf", net.minecraft.entity.EntityType.WOLF),
		zombie_villager("Zombie Villager", net.minecraft.entity.EntityType.ZOMBIE_VILLAGER);
		
		private final String name;
		private String description;
		net.minecraft.entity.EntityType<?> type;
		net.minecraft.item.Item item;
		
		
		private OnlyMob_(String name, net.minecraft.entity.EntityType<?> type) {
			this.name = name;
			this.type = type;
		}

		private OnlyMob_(String name, String description, net.minecraft.entity.EntityType<?> type, net.minecraft.item.Item item) {
			this.name = name + ", with " + description;
			this.description = description;
			this.type = type;
			this.item = item;
		}

		@Override
		public String toString() {
			return this.name;
		}

		public net.minecraft.entity.EntityType<?> getType() {
			return this.type;
		}

		public net.minecraft.item.Item getItem() {
			return this.item;
		}

		public String getDescription() {
			return this.description;
		}
	}
}
