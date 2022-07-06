/*
 * Copyright (c) 2014-2022 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.clickgui.screens;

import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.wurstclient.settings.MobListSetting;
import net.wurstclient.util.ListWidget;

public final class EditMobListScreen extends Screen
{
	private final Screen prevScreen;
	private final MobListSetting mobListSet;
	
	private ListGui listGui;
	private TextFieldWidget mobIdField;
	private ButtonWidget addButton;
	private ButtonWidget removeButton;
	private ButtonWidget doneButton;

	//private static final ArrayList<CheckboxWidget> checkBoxList = new ArrayList<CheckboxWidget>();
	
	private EntityType<?> entityToAdd;
	
	public EditMobListScreen(Screen prevScreen, MobListSetting mobListSet)
	{
		super(Text.literal(""));
		this.prevScreen = prevScreen;
		this.mobListSet = mobListSet;
	}
	
	@Override
	public void init()
	{
		//
//		listGui = new ListGui(client, this, mobListSet.getMobIDs());
		listGui = new ListGui(client, this, mobListSet.getMobMap());

		//
		mobIdField = new TextFieldWidget(
			client.textRenderer,
			width / 2 - 152, height - 55, 150, 18,
			Text.literal("")
		);
		addSelectableChild(mobIdField);
		mobIdField.setMaxLength(256);
		
		addDrawableChild(
			addButton = new ButtonWidget(
				width / 2 - 2, height - 56, 30, 20,
				Text.literal("Add"),
				b -> {
					//mobListSet.add(entityToAdd);
					mobListSet.put(entityToAdd, false, null);
					mobIdField.setText("");
				}
			)
		);
		
		addDrawableChild(
			removeButton = new ButtonWidget(
				width / 2 + 52, height - 56, 100, 20,
				Text.literal("Remove Selected"),
				b -> {
					mobListSet.remove(listGui.selected);
					//remove(child);
				}
			)
		);
		
		addDrawableChild(
			new ButtonWidget(
				width - 108, 8, 100, 20,
				Text.literal("Reset to Defaults"),
				b -> client.setScreen(
					new ConfirmScreen(
						b2 -> {
							if(b2)
								mobListSet.resetToDefaults();
							client.setScreen(EditMobListScreen.this);
						},
						Text.literal("Reset to Defaults"),
						Text.literal("Are you sure?")
					)
				)
			)
		);
		
		addDrawableChild(
			doneButton = new ButtonWidget(
				width / 2 - 100, height - 28, 200, 20,
				Text.literal("Done"),
				b -> client.setScreen(prevScreen)
			)
		);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean childClicked = super.mouseClicked(mouseX, mouseY, button);
		
		mobIdField.mouseClicked(mouseX, mouseY, button);
		listGui.mouseClicked(mouseX, mouseY, button);
		
		if(!childClicked && (mouseX < (width - 220) / 2
			|| mouseX > width / 2 + 129 || mouseY < 32 || mouseY > height - 64))
			listGui.selected = -1;
		
		return childClicked;
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button,
		double deltaX, double deltaY)
	{
		listGui.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		listGui.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY,
		double amount)
	{
		listGui.mouseScrolled(mouseX, mouseY, amount);
		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		switch(keyCode)
		{
			case GLFW.GLFW_KEY_ENTER:
			if(addButton.active)
				addButton.onPress();
			break;
			
			case GLFW.GLFW_KEY_DELETE:
			if(!mobIdField.isFocused())
				removeButton.onPress();
			break;
			
			case GLFW.GLFW_KEY_ESCAPE:
			doneButton.onPress();
			break;
			
			default:
			break;
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public void tick()
	{
		mobIdField.tick();
		
		String idStr = mobIdField.getText();
		entityToAdd = getMobFromName(idStr);
		addButton.active = entityToAdd != null;
		
		removeButton.active =
			listGui.selected >= 0 && listGui.selected < listGui.list.size();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY,
		float partialTicks)
	{
		//
		listGui.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//
		drawCenteredText(matrixStack, client.textRenderer,
			mobListSet.getName() + " (" + listGui.getItemCount() + ")",
			width / 2, 12, 0xffffff);
		
		matrixStack.push();
		matrixStack.translate(0, 0, 300);
		
		mobIdField.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		matrixStack.translate(-64 + width / 2 - 152, 0, 0);
		
		if(mobIdField.getText().isEmpty() && !mobIdField.isFocused())
			drawStringWithShadow(matrixStack, client.textRenderer,
				"mob id", 68, height - 50, 0x808080);
		
		int border = mobIdField.isFocused() ? 0xffffffff : 0xffa0a0a0;
		int black = 0xff000000;
		
		fill(matrixStack, 48, height - 56, 64, height - 36, border);
		fill(matrixStack, 49, height - 55, 64, height - 37, black);
		fill(matrixStack, 214, height - 56, 244, height - 55, border);
		fill(matrixStack, 214, height - 37, 244, height - 36, border);
		fill(matrixStack, 244, height - 56, 246, height - 36, border);
		fill(matrixStack, 214, height - 55, 243, height - 52, black);
		fill(matrixStack, 214, height - 40, 243, height - 37, black);
		fill(matrixStack, 214, height - 55, 216, height - 37, black);
		fill(matrixStack, 242, height - 55, 245, height - 37, black);
		
		matrixStack.pop();
		
		listGui.renderIconAndGetName(matrixStack, entityToAdd,
			width / 2 - 164, height - 52, false);
	}
	
	@Override
	public boolean shouldPause()
	{
		return false;
	}
	
	@Override
	public boolean shouldCloseOnEsc()
	{
		return false;
	}
	
	private static class ListGui extends ListWidget
	{
		private final MinecraftClient mc;
		private final List<String> list;
		private int selected = -1;

		private HashMap<String, CheckboxWidget> checkBoxMap;
		private Rectangle checkBoxRect;

		private final Map<String, ArrayList<String>> map;
		
		public ListGui(MinecraftClient mc, EditMobListScreen screen, List<String> list)
		{
			super(mc, screen.width, screen.height, 32, screen.height - 64, 30);
			this.mc = mc;
			this.list = list;

			this.map = null;

			//
			checkBoxMap = new HashMap<>();
			this.checkBoxRect = new Rectangle(0, 0, 20, 20);
		}

		public ListGui(MinecraftClient mc, EditMobListScreen screen, Map<String, ArrayList<String>> map)
		{
			super(mc, screen.width, screen.height, 32, screen.height - 64, 30);
			this.mc = mc;
			this.list = new ArrayList<>(map.keySet());

			this.map = map;

			//
			checkBoxMap = new HashMap<>();
			this.checkBoxRect = new Rectangle(0, 0, 20, 20);
		}
		
		@Override
		protected int getItemCount()
		{
			return list.size();
		}
		
		@Override
		protected boolean selectItem(int index, int button, double mouseX, double mouseY)
		{
			if(index >= 0 && index < list.size())
				selected = index;
			
			return true;
		}
		
		@Override
		protected boolean isSelectedItem(int index)
		{
			return index == selected;
		}
		
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button)
		{
			boolean childClicked = super.mouseClicked(mouseX, mouseY, button);

			if (childClicked && (0 == button)) {
				int index = getCheckboxAtPosition(mouseX, mouseY);
				if (index >= 0) {
					String listEntry = list.get(index);

//					checkBoxMap.get(listEntry).onClick(mouseX, mouseY);
					checkBoxMap.get(listEntry).mouseClicked(mouseX, mouseY, button);//checkBoxMap.get(listEntry).onPress();
					//checkBoxMap.get(listEntry).mouseReleased(mouseX, mouseY, button);//checkBoxMap.get(listEntry).onRelease(mouseX, mouseY);

					/*boolean isChecked = false;
					if (checkBoxMap.get(listEntry).isMouseOver(mouseX, mouseY)) {
						isChecked = !checkBoxMap.get(listEntry).isChecked();
						isChecked = false;
					}*/
				}
			}

			return childClicked;
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button)
		{
			super.mouseReleased(mouseX, mouseY, button);

			if (0 == button) {
				int index = getCheckboxAtPosition(mouseX, mouseY);
				if (index >= 0) {
					String listEntry = list.get(index);
					checkBoxMap.get(listEntry).mouseReleased(mouseX, mouseY, button);
				}
			}

			return false;
		}

		@Override
		protected void renderBackground()
		{

		}

		@Override
		protected void renderItem(MatrixStack matrixStack, int index, int x, int y,
			int itemHeight, int mouseX, int mouseY, float partialTicks)
		{
			String listEntry = list.get(index);

			//
			EntityType<?> entityType = null;
			try
			{
				Identifier id = Identifier.tryParse(listEntry);
				if (Registry.ENTITY_TYPE.containsId(id))
					entityType = Registry.ENTITY_TYPE.get(id);
			}
			catch(InvalidIdentifierException e)
			{
				//e.printStackTrace();//entityType = Registry.ENTITY_TYPE.get(0);
			}

			// render Icon and Name 
			TextRenderer fr = mc.textRenderer;
			String displayName =
				renderIconAndGetName(matrixStack, entityType, x + 1, y + 1, true);

			fr.draw(matrixStack, displayName, x + 28, y, 0xf0f0f0);
			fr.draw(matrixStack, "ID: " + listEntry, x + 28, y + 9, 0xa0a0a0);//fr.draw(matrixStack, "TK: " + entityType.getTranslationKey(), x + 28, y + 9, 0xa0a0a0);
			fr.draw(matrixStack, "Item: " + "WIP", x + 28, y + 18, 0xa0a0a0);

			Identifier lTId = entityType.getLootTableId();

			// render CheckboxWidget
			checkBoxRect.x = x - checkBoxRect.width - 10;
			if (((y + this.itemHeight) > this.bottom) || (y <= this.top)) {
				return;
			}

			Rectangle rect = new Rectangle(checkBoxRect);//Rect2i rect = checkBoxRect;
			rect.y = y + ((this.itemHeight - rect.height) / 2);

			if (checkBoxMap.isEmpty() || !checkBoxMap.containsKey(listEntry)) {
				boolean showMessage = false;
				boolean isChecked = false;
				try
				{
					showMessage = map.get(listEntry).get(0) != listEntry;

					isChecked = Integer.parseInt(
						map.get(listEntry).get(1),
						10
					) > 0;
				}
				catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e)
				{
					//e.printStackTrace();
				}

				checkBoxMap.put(
					listEntry,
					new CheckboxWidget(
						rect.x, rect.y, rect.width, rect.height,
						Text.literal(listEntry),
						isChecked, showMessage
					)
				);
			}

			boolean checked = checkBoxMap.get(listEntry).isChecked();
			if (checked) {
				checked = false;
			}

//			if (!Registry.ENTITY_TYPE.containsId(id)) {
//				checkBoxWid.visible = false;
//			}

			checkBoxMap.get(listEntry).x = rect.x;//checkBoxWid.x = rect.x;
			checkBoxMap.get(listEntry).y = rect.y;//checkBoxWid.y = rect.y;
			//checkBoxMap.replace(listEntry, checkBoxWid);
			//CheckboxWidget checkBoxWid = new CheckboxWidget(
			//	rect.x, rect.y, rect.width, rect.height, Text.literal(listEntry), checked, true);

			checkBoxMap.get(listEntry).render(matrixStack, mouseX, mouseY, partialTicks);//checkBoxWid.render(matrixStack, mouseX, mouseY, partialTicks);
		}

		@Override
		public int getItemAtPosition(double mouseX, double mouseY)
		{
			return super.getItemAtPosition(mouseX, mouseY);
		}

		private String renderIconAndGetName(MatrixStack matrixStack,
			EntityType<?> entityType, int x, int y, boolean large)
		{
			MatrixStack modelViewStack = RenderSystem.getModelViewStack();
			modelViewStack.push();
			modelViewStack.translate(x, y, 0);
			if(large)
				modelViewStack.scale(1.5F, 1.5F, 1.5F);
			else
				modelViewStack.scale(0.75F, 0.75F, 0.75F);
			
			DiffuseLighting.enableGuiDepthLighting();

			Item item = Registry.ITEM.get(Identifier.tryParse("minecraft:player_head"));

			if(null == entityType)
			{
				//Item item = Registry.ITEM.get(Identifier.tryParse("minecraft:player_head"));
				mc.getItemRenderer().renderInGuiWithOverrides(
					item.getDefaultStack(), 0, 0);

				DiffuseLighting.disableGuiDepthLighting();
				
				modelViewStack.pop();
				RenderSystem.applyModelViewMatrix();
				
				matrixStack.push();
				matrixStack.translate(x, y, 0);
				if(large)
					matrixStack.scale(2, 2, 2);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				TextRenderer fr = mc.textRenderer;
				fr.drawWithShadow(matrixStack, "?", 3, 2, 0xf0f0f0);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				matrixStack.pop();
				
				return "\u00a7ounknown mob\u00a7r";
			}

			Identifier idET = Registry.ENTITY_TYPE.getId(entityType);//EntityType.getId(entityType);
			Identifier idItem = Identifier.tryParse(idET.toString() + "_spawn_egg");

			item = Registry.ITEM.get(Identifier.tryParse("minecraft:egg"));
			if (Registry.ITEM.containsId(idItem)) {
				item = Registry.ITEM.get(idItem);
			}

			mc.getItemRenderer().renderInGuiWithOverrides(
				item.getDefaultStack(), 0, 0);

			DiffuseLighting.disableGuiDepthLighting();
			
			modelViewStack.pop();
			RenderSystem.applyModelViewMatrix();
			
			//return entityType.getUntranslatedName();
			return entityType.getName().getString();
		}

		public int getCheckboxAtPosition(double mouseX, double mouseY)
		{
			int i = checkBoxRect.x;
			int j = checkBoxRect.x + checkBoxRect.width;

			//int fixY = rect.y = y + ((this.itemHeight - rect.height) / 2);
			int fixY = (this.itemHeight - checkBoxRect.height) / 2;

			int floor = MathHelper.floor(mouseY - top + fixY);
			int k = floor - headerHeight + (int)scrollAmount - 4;
			int l = k / itemHeight;

			//return mouseX < getScrollbarPosition() && mouseX >= i && mouseX <= j
			//	&& l >= 0 && k >= 0 && l < getItemCount() ? l : -1;
			int index = -1;
			if (mouseX < getScrollbarPosition() && mouseX >= i && mouseX <= j) {
				if (l >= 0 && k >= 0 && l < getItemCount()) {
					index = l;
				}
			}

			return index;
		}
	}

	public EntityType<?> getMobFromName(String name)
	{
		try {
			if (!name.isEmpty()) {
				Identifier id = Identifier.tryParse(name);
				if (Registry.ENTITY_TYPE.containsId(id)) {
					return Registry.ENTITY_TYPE.get(id);
				}
			}
			
		} catch(InvalidIdentifierException e) {
			//return null;
		}

		return null;
	}
}
