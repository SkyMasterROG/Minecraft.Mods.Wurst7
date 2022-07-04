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

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
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

	private final CheckboxSetting showMore = new CheckboxSetting("Show more \"Items\"",
		"Shows \"Experience orb\", too",
		true);

	private final CheckboxSetting names = new CheckboxSetting("Show item names",
		"create a custom nametag",
		true);

	private final ArrayList<ItemEntity> items = new ArrayList<>();
	private final ArrayList<Entity> more = new ArrayList<>();
	
	public ItemEspHack()
	{
		super("ItemESP");
		setCategory(Category.RENDER);
		
		addSetting(style);
		addSetting(boxSize);
		addSetting(color);

		addSetting(showMore);
		addSetting(names);
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

		if(names.isChecked()) {
			showNames(matrixStack, partialTicks, regionX, regionZ);
			//renderNames(matrixStack, partialTicks, regionX, regionZ);
		}

		matrixStack.pop();
		
		// GL resets
		RenderSystem.setShaderColor(1, 1, 1, 1);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_LINE_SMOOTH);

		//if(names.isChecked())
		//	renderNames(matrixStack, partialTicks, regionX, regionZ);
	}
	
	private void renderBoxes(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		ArrayList<Entity> entities = new ArrayList<>();
		boolean result = entities.addAll(items);
		result = entities.addAll(more);
		if (entities.size() <= 0)
			return;

		float extraSize = boxSize.getSelected().extraSize;
		
		//for(ItemEntity e : items)
		for(Entity e : entities)
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

			matrixStack.pop();
		}
	}
	
	private void renderTracers(MatrixStack matrixStack, double partialTicks,
		int regionX, int regionZ)
	{
		ArrayList<Entity> entities = new ArrayList<>();
		entities.addAll(items);
		entities.addAll(more);
		if (entities.size() <= 0)
			return;

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

		//for(ItemEntity e : items)
		for(Entity e : entities)
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

	private void showNames(MatrixStack matrixStack, double partialTicks, int regionX, int regionZ) {
		ArrayList<Entity> entities = new ArrayList<>();
		entities.addAll(items);
		entities.addAll(more);
		if (entities.size() <= 0)
			return;

		for(Entity e : entities) {
			//
			String sJson = "{\"text\":\"";
			String sPat = "x ";

			// get current name
			String name = e.getName().getString();
			Text eDN = e.getDisplayName();
			String nameC = eDN.getString();
			if(e.hasCustomName()) {
				//name = e.getCustomName().getString();
				nameC = e.getCustomName().getString();
			}
			/*EntityType<?> et = e.getType();
			nameC = et.getName().getString();
			nameC = et.getTranslationKey();
			nameC = et.getUntranslatedName();
			nameC = e.getEntityName();
			int eId = e.getId();
			net.minecraft.util.Identifier id =  e.getType().getLootTableId();*/

			// has and get count
			boolean hasCount = false;
			int countI = 0;
			if (e.getType() == EntityType.ITEM) {
				countI = ((ItemEntity)e).getStack().getCount();
				hasCount = countI >= 1;

			} else if (e.getType() == EntityType.EXPERIENCE_ORB) {
				countI = ((ExperienceOrbEntity)e).getExperienceAmount();
				hasCount = countI >= 1;
			}

			// create and set new name
			if(hasCount) {
				if (name.contains(sPat)) {
					try {
						String sArr[] = name.split(sPat);
						name = sArr[1];
		
					} catch (Exception ex) {
						name = "Invalid";
						new net.wurstclient.command.CmdSyntaxError("Invalid" + ex.getMessage());
					}
				}
				
				sJson = sJson + Integer.toString(countI) + sPat;
			}

			sJson = sJson + name + "\"}";
			e.setCustomName(net.minecraft.text.Text.Serializer.fromJson(sJson));
			
			// set new name to visible
			if(!e.isCustomNameVisible()) {
				e.setCustomNameVisible(true);
			}
		}
	}

	private void renderNames(MatrixStack matrixStack, double partialTicks, int regionX, int regionZ) {
		// ItemStack stack = e.getStack();
		// GameRenderer.renderFloatingText(MC.textRenderer,
		// stack.getCount() + "x "
		// + stack.getName().asFormattedString(),
		// 0, 1, 0, 0, MC.getEntityRenderManager().cameraYaw,
		// MC.getEntityRenderManager().cameraPitch, false);
		// GL11.glDisable(GL11.GL_LIGHTING);

		//
		ArrayList<Entity> entities = new ArrayList<>();
		entities.addAll(items);
		entities.addAll(more);
		if (entities.size() <= 0)
			return;

		// get player last position
		Vec3d posClient = new Vec3d(
			MC.player.prevX + (MC.player.getX() - MC.player.prevX) * partialTicks - regionX,
			MC.player.prevY + (MC.player.getY() - MC.player.prevY) * partialTicks,
			MC.player.prevZ + (MC.player.getZ() - MC.player.prevZ) * partialTicks - regionZ
		);
		//double cEX =  MC.getCameraEntity().prevX + (MC.getCameraEntity().getX() - MC.getCameraEntity().prevX) * partialTicks - regionX;

		// textRenderer
		TextRenderer tr = MC.textRenderer;
		//tr = MC.getInstance().textRenderer;
//		TextureManager tm = MC.getTextureManager();
//		net.minecraft.client.font.FontManager fm = new net.minecraft.client.font.FontManager(tm);
//		tr =  fm.createTextRenderer();
//		net.minecraft.client.font.GlyphRenderer gr;
		
		int msgHeight = tr.fontHeight;

		float[] colorF = color.getColorF();
		int iColor = color.getColorI();

		for(Entity e : entities)
		{
			matrixStack.push();

			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_DEPTH_TEST);

			// get entity last position
			Vec3d ePos = new Vec3d(
				e.prevX + (e.getX() - e.prevX) * partialTicks - regionX,
				e.prevY + (e.getY() - e.prevY) * partialTicks,
				e.prevZ + (e.getZ() - e.prevZ) * partialTicks - regionZ
			);

			Vec2f lookAt = LookAt(ePos, posClient);

			String name = e.getName().getString();
			if(e.hasCustomName())
				name = e.getCustomName().getString();

/*			ItemStack eIS = e.getStack();
			if (eIS.getCount() > 0) {
				name = String.valueOf(eIS.getCount()) + "x " + e.getName().getString();
			}
*/

			// set origin matrix
			Box eBox = e.getBoundingBox();
			double eHY = eBox.maxY - eBox.minY;
			float eH = e.getHeight();
			//matrixStack.translate(ePos.x, ePos.y, ePos.z);
			matrixStack.translate(ePos.x, ePos.y + eHY, ePos.z);

			// flip text over Z
			matrixStack.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(180F));

			// face to player
			matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion((float)lookAt.y));
			matrixStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion((float)lookAt.x));

			// scale text
			float scaleFactor = (float) MC.getWindow().getScaleFactor();
			float newScaleFactor = 1;
			float scaleCompensation = newScaleFactor / scaleFactor;
			//matrixStack.scale(0.03F, 0.03F, 0.03F);
			matrixStack.scale(scaleCompensation, scaleCompensation, scaleCompensation);

			//
			//TextRenderer tr = WurstClient.MC.textRenderer;
			//int msgHeight = tr.fontHeight;
			int msgWidth = tr.getWidth(name);

			// center text & up text by height
			matrixStack.translate(-(msgWidth / 2), -(msgHeight * 2), 0);
			
			//
//			float[] colorF = color.getColorF();
//			RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.5F);

			// draw background
//			drawQuads(matrixStack, -1, -1, msgWidth + 1, msgHeight + 1, 0, 0, 0, 0.3F);
			
			// draw string
			tr.drawWithShadow(matrixStack, name, 0, 0, iColor);

			//
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_DEPTH_TEST);

			matrixStack.pop();
		}
	}

	private Vec2f LookAt(Vec3d from , Vec3d to)
	{
		//Entity e = Entity
		//EntityLookTarget eLookTar = new EntityLookTarget(entity, useEyeHeight);

		double dirx = to.getX() - from.getX();
		double diry = to.getY() - from.getY();
		double dirz = to.getZ() - from.getZ();
//		Vec3d dir = to.subtract(from);

		double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);
//		double length = dir.length();

		dirx /= len;
		diry /= len;
		dirz /= len;
//		dir.multiply(length);//?? divity
		//dir.x /= length;
		//dir.y /= length;
		//dir.z /= length;
		//dir = dir / length;
		//Vec3d lerp = dir.lerp(from, length);
		//lerp = dir.lerp(to.subtract(from), length);
		//dir.fromPolar(pitch, yaw);

		Double pitch = Math.asin(diry);
		Double yaw = Math.atan2(dirz, dirx);

		//to degree
		pitch = pitch * 180.0 / Math.PI;
		yaw = yaw * 180.0 / Math.PI;
		yaw += 90f;

		//return new Vec2f((float)pitch, (float)yaw);
		return new Vec2f(pitch.floatValue(), yaw.floatValue());
	}

	private void drawQuads(MatrixStack matrices, int x1, int y1, int x2, int y2,
		float r, float g, float b, float a)
	{
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		
		Tessellator tessellator = RenderSystem.renderThreadTesselator();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS,
			VertexFormats.POSITION_COLOR);
		bufferBuilder.vertex(matrix, x1, y2, 0.0F).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x2, y1, 0.0F).color(r, g, b, a).next();
		bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(r, g, b, a).next();
		tessellator.draw();
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
