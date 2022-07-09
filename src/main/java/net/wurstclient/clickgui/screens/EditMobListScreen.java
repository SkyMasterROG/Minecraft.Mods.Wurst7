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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.logging.log4j.util.TriConsumer;
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
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.wurstclient.settings.MobListSetting;
import net.wurstclient.util.ListWidget;

public final class EditMobListScreen extends Screen
{
	private final Screen prevScreen;
	private final MobListSetting mobListSet;
	
	private final ArrayList<String> listSugg;

	private ListGui listGui;
	private SuggestionList listSuggW;

	private TextFieldWidget mobIdField;
	private TextFieldWidget itemIdField;
	private ButtonWidget addButton;
	private ButtonWidget saveButton;

	private ButtonWidget removeButton;
	private ButtonWidget doneButton;

	//private static CheckboxWidget checkboxPlaceholder;
	private static ButtonWidget buttonPlaceholder;
	private static MobListSetting.MobValue mobValuePlaceholder;

	private final Rectangle textFieldRect;
	private final Rectangle buttondRect;
	private Rectangle listSuggRect;
	
	private EntityType<?> entityToAdd;
	
	public EditMobListScreen(Screen prevScreen, MobListSetting mobListSet)
	{
		super(Text.literal(""));
		this.prevScreen = prevScreen;
		this.mobListSet = mobListSet;

		this.textFieldRect = new Rectangle(0, 0, 200, 18);
		this.buttondRect = new Rectangle(0, 0, 60, 20);

		//Registry.ENTITY_TYPE.getEntry(rawId);
		Set<RegistryKey<EntityType<?>>> keys = Registry.ENTITY_TYPE.getKeys();
		int sizeI = keys.size();

		this.listSugg = new ArrayList<>();
		int charMax = 0;
		String signMax = "";
		for (RegistryKey<EntityType<?>> key : keys) {
			Identifier id = key.getRegistry();
			id = key.getValue();
			String s = key.toString();

			s = id.toString();
			if(s.length() > charMax) {
				charMax = s.length();
				char[] array = new char[charMax];
				Arrays.fill(array, '0');
				signMax = new String(array);
			}

			this.listSugg.add(id.toString());

			id = null;
		}
		sizeI = this.listSugg.size();
		//listSuggRect = new Rectangle((int)tfR.getMaxX(), tfR.y, 30, tfR.height - 2);
	}

	/*private static void setValue(int index, MobListSetting.MobValue value)
	{
		mobListSet.set(index, value);
	}*/
	
	@Override
	public void init()
	{
		//
		listGui = new ListGui(client, this, mobListSet.getMobValues());
		Rectangle cbR = listGui.checkboxRect;
		try {
			Arrays.stream(listGui.list.toArray()).parallel()
				.map(e -> new CheckboxWidget(
					cbR.x, cbR.y, cbR.width, cbR.height,
					Text.literal(e.toString()),
					((MobListSetting.MobValue)e).isEnabled(), true
				))
				.filter(Objects::nonNull)
				.forEachOrdered(e -> listGui.checkBoxList.add(e));

		} catch (Exception e) {
			e.printStackTrace();
		}

		//
		Rectangle tfR = textFieldRect;
		tfR.x = (width / 2) - (tfR.width / 2);
		tfR.y = listGui.getButtom() + 4;
		mobIdField = new TextFieldWidget(
			client.textRenderer,
			tfR.x, tfR.y, tfR.width, tfR.height,
			Text.literal("")
		);
		addSelectableChild(mobIdField);
		mobIdField.setMaxLength(256);

		// TODO mouse click do not work
		Rectangle bRectField = new Rectangle((int)tfR.getMaxX(), tfR.y, 30, tfR.height - 2);
		bRectField.x -= bRectField.width + 1;
		bRectField.y += (tfR.height - bRectField.height) / 2;
		addDrawableChild(
			addButton = new ButtonWidget(
				bRectField.x, bRectField.y, bRectField.width, bRectField.height,
				Text.literal("Add"),
				b -> {
					MobListSetting.MobValue value = mobListSet.add(entityToAdd);
					listGui.checkBoxList.add(
						new CheckboxWidget(
							cbR.x, cbR.y, cbR.width, cbR.height,
							Text.literal(value.toString()),
							value.isEnabled(), true
						)
					);
					mobIdField.setText("");
				}
			)
		);

		//tfR.x = width / 2 - 152;
		tfR.y += tfR.height + 4;//height - 55;
		itemIdField = new TextFieldWidget(
			client.textRenderer,
			tfR.x, tfR.y, tfR.width, tfR.height,
			Text.literal("")
		);
		addSelectableChild(itemIdField);
		itemIdField.setMaxLength(256);

		bRectField.y = tfR.y + ((tfR.height - bRectField.height) / 2);
		addDrawableChild(
			saveButton = new ButtonWidget(
				bRectField.x, bRectField.y, bRectField.width, bRectField.height,
				Text.literal("Save"),
				b -> {
					int index = listGui.selected;
					mobListSet.set(index, listGui.list.get(index));
					//listGui.checkBoxList.remove(listGui.selected);
					//mobIdField.setText("");
				}
			)
		);

		//
		listSuggRect = new Rectangle(tfR.x, listGui.getButtom() + 1, tfR.width, client.textRenderer.fontHeight * 4);
		listSuggW = new SuggestionList(client, this, listSuggRect, client.textRenderer.fontHeight, Collections.unmodifiableList(this.listSugg));
		
		//
		Rectangle bR = buttondRect;
		bR.x = tfR.x + tfR.width + 4;
		bR.y = listGui.getButtom() + 4;
		addDrawableChild(
			removeButton = new ButtonWidget(
				bR.x, bR.y, bR.width, bR.height,
				Text.literal("Remove"),
				b -> {
					mobListSet.remove(listGui.selected);
					listGui.checkBoxList.remove(listGui.selected);
					//remove(child);
				}
			)
		);

		bR.y += tfR.height + 4;
		addDrawableChild(
			doneButton = new ButtonWidget(
				bR.x, bR.y, bR.width, bR.height,
				Text.literal("Done"),
				b -> client.setScreen(prevScreen)
			)
		);
		
		// Reset to Defaults
		addDrawableChild(
			new ButtonWidget(
				width - bR.width - 4, 8, bR.width, bR.height,
				Text.literal("Reset"),
				b -> client.setScreen(
					new ConfirmScreen(
						b2 -> {
							if(b2) {
								mobListSet.resetToDefaults();

								listGui.checkBoxList.clear();
								Arrays.stream(listGui.list.toArray()).parallel()
									.map(e -> new CheckboxWidget(
										cbR.x, cbR.y, cbR.width, cbR.height,
										Text.literal(e.toString()),
										((MobListSetting.MobValue)e).isEnabled(), true
									))
									.filter(Objects::nonNull)
									.forEachOrdered(e -> listGui.checkBoxList.add(e));
							}
							client.setScreen(EditMobListScreen.this);
						},
						Text.literal("Reset to Defaults"),
						Text.literal("Are you sure?")
					)
				)
			)
		);

		// https://maven.fabricmc.net/docs/yarn-1.16.5+build.2/net/minecraft/client/gui/widget/package-summary.html
		addDrawableChild(
			buttonPlaceholder = new ButtonWidget(
				0, 0, 0, 0,
				Text.literal("placeholder"),
				b -> {
					mobListSet.set(listGui.selected, mobValuePlaceholder);
					//listGui.checkBoxList.remove(listGui.selected);
				}
			)
		);
		buttonPlaceholder.visible = false;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean childClicked = super.mouseClicked(mouseX, mouseY, button);
		
		mobIdField.mouseClicked(mouseX, mouseY, button);
		itemIdField.mouseClicked(mouseX, mouseY, button);
		listGui.mouseClicked(mouseX, mouseY, button);
		//listSuggW.mouseClicked(mouseX, mouseY, button);
		
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
		//listSuggW.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);

		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button)
	{
		listGui.mouseReleased(mouseX, mouseY, button);
		//listSuggW.mouseReleased(mouseX, mouseY, button);

		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY,
		double amount)
	{
		listGui.mouseScrolled(mouseX, mouseY, amount);
		//listSuggW.mouseScrolled(mouseX, mouseY, amount);

		return super.mouseScrolled(mouseX, mouseY, amount);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		switch(keyCode)
		{
			case GLFW.GLFW_KEY_ENTER:
			if(addButton.active && addButton.visible)
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
		//
		Text message = Text.literal("");
		mobIdField.tick();

		if (listGui.selected >= 0 && listGui.selected < listGui.list.size()) {
			mobIdField.setText(listGui.list.get(listGui.selected).toString());
			//message = Text.literal(listGui.list.get(listGui.selected).toString());
			mobIdField.setMessage(message);
			mobIdField.active = false;
			mobIdField.setEditable(false);
			addButton.visible = false;
			saveButton.visible = true;

			if (listGui.list.get(listGui.selected).hasItem()) {
				itemIdField.setText(listGui.list.get(listGui.selected).getItemId().toString());
			}
		}
		else if (!mobIdField.isFocused()) {
			mobIdField.setText("");
			mobIdField.active = true;
			mobIdField.setEditable(true);
			addButton.visible = true;
			saveButton.visible = false;
		}

		//
		String mobIdS = "";
		if (mobIdField.isFocused() && mobIdField.isActive())
			mobIdS = mobIdField.getText();

		listSuggW.visible = listSuggW.toSuggest(mobIdS);
			
		entityToAdd = getMobFromName(mobIdS);
		addButton.active = entityToAdd != null;

		//
		itemIdField.tick();
		String itemIdS = itemIdField.getText();
//		Item itemToAdd = getMobFromName(itemIdS);
//		addButton.active = entityToAdd != null;
		
		//
		removeButton.active =
			listGui.selected >= 0 && listGui.selected < listGui.list.size();
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		//
		listGui.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//
		drawCenteredText(matrixStack, client.textRenderer,
			mobListSet.getName() + " (" + listGui.getItemCount() + ")",
			width / 2, 12, 0xffffff);
		
		matrixStack.push();
		//matrixStack.translate(0, 0, 300);
		
		mobIdField.render(matrixStack, mouseX, mouseY, partialTicks);
		itemIdField.render(matrixStack, mouseX, mouseY, partialTicks);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//matrixStack.translate(-64 + width / 2 - 152, 0, 0);
		
		TextRenderer tr = client.textRenderer;
		int xI = mobIdField.x + 4;
		int yI = mobIdField.y + ((mobIdField.getHeight() - tr.fontHeight) / 2);
		if(mobIdField.getText().isEmpty() && !mobIdField.isFocused())
			drawStringWithShadow(matrixStack, tr,
				"mob id", xI, yI, 0x808080);

		yI = itemIdField.y + ((itemIdField.getHeight() - tr.fontHeight) / 2);
		if(itemIdField.getText().isEmpty() && !itemIdField.isFocused())
			drawStringWithShadow(matrixStack, tr,
				"item id", xI, yI, 0x808080);
		
		/*int border = mobIdField.isFocused() ? 0xffffffff : 0xffa0a0a0;
		int black = 0xff000000;
		
 		fill(matrixStack, 48, height - 56, 64, height - 36, border);
		fill(matrixStack, 49, height - 55, 64, height - 37, black);
		fill(matrixStack, 214, height - 56, 244, height - 55, border);
		fill(matrixStack, 214, height - 37, 244, height - 36, border);
		fill(matrixStack, 244, height - 56, 246, height - 36, border);
		fill(matrixStack, 214, height - 55, 243, height - 52, black);
		fill(matrixStack, 214, height - 40, 243, height - 37, black);
		fill(matrixStack, 214, height - 55, 216, height - 37, black);
		fill(matrixStack, 242, height - 55, 245, height - 37, black);*/

		matrixStack.pop();

		// TODO create suggestion textlist
		//https://fabricmc.net/wiki/tutorial:command_suggestions
		//https://maven.fabricmc.net/docs/yarn-1.18.2+build.1/net/minecraft/command/argument/ItemStringReader.html
		//https://fabricmc.net/wiki/tutorial:custom_resources
		//https://stackoverflow.com/questions/41295375/how-to-show-suggestion-list-for-autocomplete-textview-that-shows-only-those-word
		listSuggW.render(matrixStack, mouseX, mouseY, partialTicks);

		// TODO render background Rectangle
		xI = mobIdField.x - 25;
		yI = mobIdField.y - 4;
		if (itemIdField.isFocused()) {
			yI = itemIdField.y - 4;
		}
		listGui.renderIconAndGetName(matrixStack, entityToAdd,
			xI, yI, true);
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
	
	//
	private static class ListGui extends ListWidget
	{
		private final MinecraftClient mc;
		private final List<MobListSetting.MobValue> list;
		private int selected = -1;

		private final ArrayList<CheckboxWidget> checkBoxList;
		private final Rectangle checkboxRect;
		
		public ListGui(MinecraftClient mc, EditMobListScreen screen, List<MobListSetting.MobValue> list)
		{
			super(mc, screen.width, screen.height, 32, screen.height - 64, 30);
			this.mc = mc;
			this.list = list;

			//
			this.checkboxRect = new Rectangle(0, 0, 20, 20);
			//this.checkBoxMap = new HashMap<>();
			/*try {
				Rectangle cbR = checkBoxRect;
				Arrays.stream(list.toArray()).parallel()
					.map(e -> new CheckboxWidget(
						cbR.x, cbR.y, cbR.width, cbR.height,
						Text.literal(e.toString()),
						((MobListSetting.MobValue)e).isEnabled(), true)
					)
					.filter(Objects::nonNull)
					.forEachOrdered(e -> this.checkBoxList.add(e));

			} catch (Exception e) {
				e.printStackTrace();
			}*/
			this.checkBoxList = new ArrayList<>();
		}

		private int getButtom()
		{
			return this.bottom;
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
					String listEntry = list.get(index).toString();

//					checkBoxMap.get(listEntry).onClick(mouseX, mouseY);
					checkBoxList.get(index).mouseClicked(mouseX, mouseY, button);

					boolean isChecked = checkBoxList.get(index).isChecked();
					if (checkBoxList.get(index).isMouseOver(mouseX, mouseY)) {
						isChecked = checkBoxList.get(index).isChecked();
						isChecked = false;
					}
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
					String listEntry = list.get(index).toString();
					checkBoxList.get(index).mouseReleased(mouseX, mouseY, button);

					//boolean isChecked = checkBoxList.get(index).isChecked();
					if (checkBoxList.get(index).isMouseOver(mouseX, mouseY)) {
						boolean checked = checkBoxList.get(index).isChecked();

						//mobListSet.set(index, list.get(index));
						//checkboxPlaceholder = checkBoxList.get(index);
						//setValue(index, list.get(index));

						list.get(index).set(checked);
						mobValuePlaceholder = list.get(index);
						selectItem(index, button, mouseX, mouseY);

						if (buttonPlaceholder.active)
							buttonPlaceholder.onPress();
					}
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
			MobListSetting.MobValue listEntry = list.get(index);

			//
			EntityType<?> entityType = null;
			try
			{
				if (Registry.ENTITY_TYPE.containsId(listEntry.getMobId()))
					entityType = Registry.ENTITY_TYPE.get(listEntry.getMobId());
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
			fr.draw(matrixStack, "ID: " + listEntry.toString(), x + 28, y + 9, 0xa0a0a0);//fr.draw(matrixStack, "TK: " + entityType.getTranslationKey(), x + 28, y + 9, 0xa0a0a0);
			fr.draw(matrixStack, "Item: " + "WIP", x + 28, y + 18, 0xa0a0a0);

//			Identifier lTId = entityType.getLootTableId();

			// render CheckboxWidget
			checkboxRect.x = x - checkboxRect.width - 10;
			if (((y + this.itemHeight) > this.bottom) || (y <= this.top)) {
				return;
			}

			Rectangle rect = new Rectangle(checkboxRect);//Rect2i rect = checkBoxRect;
			rect.y = y + ((this.itemHeight - rect.height) / 2);

			/*if (checkBoxMap.isEmpty() || !checkBoxMap.containsKey(listEntry.toString())) {
				boolean showMessage = true;
				boolean isChecked = false;
				//try
				//{
					//showMessage = map.get(listEntry).get(0) != listEntry;

					isChecked = listEntry.isEnabled();
				//}
				//catch (NullPointerException | NumberFormatException | IndexOutOfBoundsException e)
				//{
					//e.printStackTrace();
				//}

				checkBoxMap.put(
					listEntry.toString(),
					new CheckboxWidget(
						rect.x, rect.y, rect.width, rect.height,
						Text.literal(listEntry.toString()),
						isChecked, showMessage
					)
				);
			}*/

			boolean checked = checkBoxList.get(index).isChecked();//checkBoxMap.get(listEntry.toString()).isChecked();
			if (checked) {
				checked = false;
			}

//			if (!Registry.ENTITY_TYPE.containsId(id)) {
//				checkBoxWid.visible = false;
//			}

			checkBoxList.get(index).x = rect.x;//checkBoxMap.get(listEntry.toString()).x = rect.x;
			checkBoxList.get(index).y = rect.y;//checkBoxMap.get(listEntry.toString()).y = rect.y;
			//CheckboxWidget checkBoxWid = new CheckboxWidget(
			//	rect.x, rect.y, rect.width, rect.height, Text.literal(listEntry), checked, true);

			checkBoxList.get(index).render(matrixStack, mouseX, mouseY, partialTicks);
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
			int i = checkboxRect.x;
			int j = checkboxRect.x + checkboxRect.width;

			//int fixY = rect.y = y + ((this.itemHeight - rect.height) / 2);
			int fixY = (this.itemHeight - checkboxRect.height) / 2;

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

	private static class SuggestionList// extends ListWidget
	{
		private final MinecraftClient mc;

		private final EditMobListScreen screen;
		private final Rectangle rectangle;

		private final List<String> source;
		private final List<String> suggestL;

		private boolean visible;
		private int selected = -1;
		//private String suggestS;
		
		public SuggestionList(MinecraftClient mc, EditMobListScreen screen, Rectangle rectangle, int itemHeight, List<String> source)
		{
			//super(mc, rectangle.width, rectangle.height, rectangle.y - rectangle.height, rectangle.y, itemHeight);
			this.mc = mc;

			this.screen = screen;
			this.rectangle = rectangle;

			this.source = source;
			this.suggestL = new ArrayList<>();
		}

		private Boolean toSuggest(String suggest)
		{
			if (null != suggest && suggest.length() > 1) {
				//this.suggestS = suggest;
				//this.visible = true;

				this.suggestL.clear();
				for (String entry : this.source) {
					if (entry.toLowerCase().contains(suggest.toLowerCase()))
						this.suggestL.add(entry);

					if (this.suggestL.size() > 6) {
						this.suggestL.add("...");
						break;
					}
				}

				return true;
			}

			//this.visible = false;
			this.suggestL.clear();
			return false;
		}

		//@Override
		protected int getItemCount() {
			return suggestL.size();
		}

		//@Override
		protected boolean isSelectedItem(int index) {
			return index == selected;
		}

		//@Override
		//protected void renderBackground() {}

		//@Override
		protected void render(MatrixStack matrixStack,
			//int index, int x, int y, int itemHeight,
			int mouseX, int mouseY, float partialTicks)
		{
			if (!this.visible)
				return;

			if (this.suggestL.size() < 1)
				return;

			GL11.glDisable(GL11.GL_DEPTH_TEST);
			
			// TODO render background Rectangle
			TextRenderer tr = mc.textRenderer;
			//String entry = "ABCDEFGHIJKLMNOPQRSTUVWX1234567890";
			Rectangle rect = new Rectangle(this.rectangle);
			tr.draw(matrixStack, "SuggestionList", rect.x, rect.y - (rect.height + tr.fontHeight + 2), 0xF0F0F000);
			rect.x += 2;
			for (String entry : this.suggestL) {
				rect.y -= tr.fontHeight + 1;
				if (rect.y < (this.rectangle.y - this.rectangle.height)) {
					tr.draw(matrixStack, "...", rect.x, rect.y, 0xF0F0F000);
					break;
				}
				tr.draw(matrixStack, entry, rect.x, rect.y, 0xF0F0F000);
				
			}

			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		//@Override
		//protected void renderHoleBackground(int top, int bottom, int topAlpha, int bottomAlpha) {}
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
