/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.CameraTransformViewBobbingListener;
import net.wurstclient.events.RenderListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.ColorSetting;
import net.wurstclient.settings.EnumSetting;
import net.wurstclient.util.RenderUtils;
import net.wurstclient.util.RotationUtils;

@SearchTags({"item esp", "ItemTracers", "item tracers"})
public final class ItemEspHack extends Hack implements UpdateListener,
	CameraTransformViewBobbingListener, RenderListener
{
	private final EnumSetting<Style> style =
		new EnumSetting<>("Style", Style.values(), Style.BOXES);
	
	private final EnumSetting<BoxSize> boxSize = new EnumSetting<>("Box size",
		"\u00a7lAccurate\u00a7r mode shows the exact\n"
			+ "hitbox of each item.\n"
			+ "\u00a7lFancy\u00a7r mode shows larger boxes\n"
			+ "that look better.",
		BoxSize.values(), BoxSize.FANCY);
	
	private final ColorSetting color = new ColorSetting("Color",
		"Items will be highlighted in this color.", Color.YELLOW);
	
	private final CheckboxSetting names = new CheckboxSetting("Show item names",
		"Sorry, this is currently broken!\n"
			+ "19w39a changed how nameplates work\n"
			+ "and we haven't figured it out yet.",
		true);

	private final CheckboxSetting showMore = new CheckboxSetting("Show more \"Items\"",
		"Shows \"Experience orb\", too",
		true);

	private final ArrayList<ItemEntity> items = new ArrayList<>();
	private int itemBox;
	private final ArrayList<net.minecraft.entity.Entity> more = new ArrayList<>();
	
	public ItemEspHack()
	{
		super("ItemESP");
		setCategory(Category.RENDER);
		
		addSetting(style);
		addSetting(boxSize);
		addSetting(color);

		addSetting(names);
		addSetting(showMore);

	}
	
	@Override
	public void onEnable()
	{
		EVENTS.add(UpdateListener.class, this);
		EVENTS.add(CameraTransformViewBobbingListener.class, this);
		EVENTS.add(RenderListener.class, this);
	}
	
	@Override
	public void onDisable()
	{
		EVENTS.remove(UpdateListener.class, this);
		EVENTS.remove(CameraTransformViewBobbingListener.class, this);
		EVENTS.remove(RenderListener.class, this);
	}
	
	@Override
	public void onUpdate()
	{
		items.clear();
		more.clear();

		for(Entity entity : MC.world.getEntities()) {
			if(entity instanceof ItemEntity)
				items.add((ItemEntity)entity);

			if(showMore.isChecked() && (entity.getType() == net.minecraft.entity.EntityType.EXPERIENCE_ORB)) {
// crash, can not cast				items.add((ItemEntity)entity);
				more.add(entity);
			}
		}
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
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		
		matrixStack.push();
		RenderUtils.applyRegionalRenderOffset(matrixStack);
		
		BlockPos camPos = RenderUtils.getCameraBlockPos();
		int regionX = (camPos.getX() >> 9) * 512;
		int regionZ = (camPos.getZ() >> 9) * 512;
		
		renderBoxes(matrixStack, partialTicks, regionX, regionZ);
		
		if(style.getSelected().lines)
			renderTracers(matrixStack, partialTicks, regionX, regionZ);

		if(showMore.isChecked())
			renderBoxesMore(partialTicks);
		
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
		
		for(ItemEntity e : items)
		{
			matrixStack.push();
			
			matrixStack.translate(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ);
			
			if(style.getSelected().boxes)
			{
				matrixStack.push();
				matrixStack.scale(e.getWidth() + extraSize,
					e.getHeight() + extraSize, e.getWidth() + extraSize);
				
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				float[] colorF = color.getColorF();
				RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2],
					0.5F);
				RenderUtils.drawOutlinedBox(new Box(-0.5, 0, -0.5, 0.5, 1, 0.5),
					matrixStack);
				
				matrixStack.pop();
			}

			etyCustomName(e, e.getStack().isStackable(), e.getStack().getCount(), names.isChecked());
			/*if(names.isChecked()) {
				// ItemStack stack = e.getStack();
				// GameRenderer.renderFloatingText(MC.textRenderer,
				// stack.getCount() + "x "
				// + stack.getName().asFormattedString(),
				// 0, 1, 0, 0, MC.getEntityRenderManager().cameraYaw,
				// MC.getEntityRenderManager().cameraPitch, false);
				// GL11.glDisable(GL11.GL_LIGHTING);

				net.minecraft.item.ItemStack stack = e.getStack();
				

				String sName = stack.getName().asFormattedString();
				if(e.hasCustomName())
					sName = e.getCustomName().asFormattedString();

				String sJson = "{\"text\":\"";
				String sPat = "x ";
				boolean hasChanged = false;

				if(sName.contains(sPat)) {
					String sArr[] = sName.split(sPat);
					try {
						//int iCount = Integer.parseInt(sArr[0]);
						if(stack.getCount() != Integer.parseInt(sArr[0])) {
							hasChanged = true;
							sName = stack.getName().asFormattedString();
							sJson = sJson + Integer.toString(stack.getCount());
						}

					} catch (Exception ex) {
						new net.wurstclient.command.CmdSyntaxError("Invalid" + ex.getMessage());
					}
				} else {
					hasChanged = true;
					sJson = sJson + Integer.toString(stack.getCount());
				}

				sJson = sJson + sPat + sName + "\"}";
				if(hasChanged) {
					//net.minecraft.text.Text mcTxt = net.minecraft.text.Text.Serializer.fromJson(sJson);
					//e.setCustomName(mcTxt);
					e.setCustomName(net.minecraft.text.Text.Serializer.fromJson(sJson));
				}

				if(!e.isCustomNameVisible()) {
					e.setCustomNameVisible(true);
				}
			}*/
			
			matrixStack.pop();
		}
	}
	
	private void renderTracers(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		float[] colorF = color.getColorF();
		RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.5F);
		
		Matrix4f matrix = matrixStack.peek().getPositionMatrix();
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		RenderSystem.setShader(GameRenderer::getPositionShader);
		
		Vec3d start =
			RotationUtils.getClientLookVec().add(RenderUtils.getCameraPos());
		
		bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES,
			VertexFormats.POSITION);
		for(ItemEntity e : items)
		{
			Vec3d end = e.getBoundingBox().getCenter()
				.subtract(new Vec3d(e.getX(), e.getY(), e.getZ())
					.subtract(e.prevX, e.prevY, e.prevZ)
					.multiply(1 - partialTicks));
			
			bufferBuilder.vertex(matrix, (float)start.x - regionX,
				(float)start.y, (float)start.z - regionZ).next();
			bufferBuilder.vertex(matrix, (float)end.x - regionX, (float)end.y,
				(float)end.z - regionZ).next();
		}
		tessellator.draw();
	}

	private void renderBoxesMore(double partialTicks)
	{
		double extraSize = boxSize.getSelected().extraSize;
		
		for(Entity e : more)
		{
			GL11.glPushMatrix();
			
			GL11.glTranslated(e.prevX + (e.getX() - e.prevX) * partialTicks,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks);
			
			if(style.getSelected().boxes)
			{
				GL11.glPushMatrix();
				GL11.glScaled(e.getWidth() + extraSize,
					e.getHeight() + extraSize, e.getWidth() + extraSize);
				GL11.glCallList(itemBox);
				GL11.glPopMatrix();
			}

			boolean hasCount = false;
			int iCount = 0;
			if(e.getType() == net.minecraft.entity.EntityType.EXPERIENCE_ORB) {
				iCount = ((net.minecraft.entity.ExperienceOrbEntity)e).getExperienceAmount();
				hasCount = (iCount >= 1);
			}

			etyCustomName(e, hasCount, iCount, names.isChecked());
			/*if(names.isChecked()) {
				// ItemStack stack = e.getStack();
				// GameRenderer.renderFloatingText(MC.textRenderer,
				// stack.getCount() + "x "
				// + stack.getName().asFormattedString(),
				// 0, 1, 0, 0, MC.getEntityRenderManager().cameraYaw,
				// MC.getEntityRenderManager().cameraPitch, false);
				// GL11.glDisable(GL11.GL_LIGHTING);

				int expAmt = 0;//net.minecraft.item.ItemStack stack = e.getStack();
				String sName = e.getName().asFormattedString();
				if(e.hasCustomName())
					sName = e.getCustomName().asFormattedString();

				String sJson = "{\"text\":\"";
				String sPat = "x ";
				boolean hasChanged = false;

				if(e.getType() == net.minecraft.entity.EntityType.EXPERIENCE_ORB) {
					net.minecraft.entity.ExperienceOrbEntity expOrbEty = (net.minecraft.entity.ExperienceOrbEntity)e;
					expAmt = expOrbEty.getExperienceAmount();

					//net.minecraft.nbt.CompoundTag compTag = new net.minecraft.nbt.CompoundTag();
					//expOrbEty.toTag(compTag);
					//expOrbEty.saveSelfToTag(compTag);
					//expOrbEty.saveToTag(compTag);

					//expAmt = 0;
				}

				if(sName.contains(sPat)) {
					String sArr[] = sName.split(sPat);
					try {
						//int iCount = Integer.parseInt(sArr[0]);
						if(expAmt != Integer.parseInt(sArr[0])) {
							hasChanged = true;
							sName = e.getName().asFormattedString();
							sJson = sJson + Integer.toString(1);
						}

					} catch (Exception ex) {
						new net.wurstclient.command.CmdSyntaxError("Invalid" + ex.getMessage());
					}
				} else {
					hasChanged = true;
					sJson = sJson + Integer.toString(expAmt);
				}

				sJson = sJson + sPat + sName + "\"}";
				if(hasChanged) {
					net.minecraft.text.Text mcTxt = net.minecraft.text.Text.Serializer.fromJson(sJson);
					e.setCustomName(mcTxt);
				}

				if(!e.isCustomNameVisible()) {
					e.setCustomNameVisible(true);
				}
			}*/
			
			GL11.glPopMatrix();
		}
	}

	private void etyCustomName(Entity ety, boolean hasCount, int iCount, boolean visible) {
		//String sName = ety.getName().asFormattedString();
		String sName = ety.getName().toString();
		if(ety.hasCustomName())
			//sName = ety.getCustomName().asFormattedString();
			sName = ety.getCustomName().toString();

		String sJson = "{\"text\":\"";
		String sPat = "x ";
		//boolean hasChanged = false;

		if(!visible) {
			sJson = null;

		} else if(hasCount && sName.contains(sPat)) {
			String sArr[] = sName.split(sPat);
			try {
				//int iCount = Integer.parseInt(sArr[0]);
				if(iCount != Integer.parseInt(sArr[0])) {
					//hasChanged = true;
					sName = sArr[1];
					sJson = sJson + Integer.toString(iCount);
				} else {
					sJson = null;
				}

			} catch (Exception ex) {
				sJson = null;
				visible = false;
				new net.wurstclient.command.CmdSyntaxError("Invalid" + ex.getMessage());
			}
		} else if(hasCount) {
			//hasChanged = true;
			sJson = sJson + Integer.toString(iCount);
		}

		if(sJson != null) {
			if(hasCount)
				sJson = sJson + sPat;
			sJson = sJson + sName + "\"}";
			ety.setCustomName(net.minecraft.text.Text.Serializer.fromJson(sJson));
		}

		if(ety.isCustomNameVisible() != visible) {
			ety.setCustomNameVisible(visible);
		}
	}
	
	private enum Style
	{
		BOXES("Boxes only", true, false),
		LINES("Lines only", false, true),
		LINES_AND_BOXES("Lines and boxes", true, true);
		
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
}
