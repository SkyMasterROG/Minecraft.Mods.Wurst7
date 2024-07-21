/*
 * Copyright (c) 2014-2024 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.ChatInputListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.LanguageSetting;
import net.wurstclient.settings.LanguageSetting.Language;
import net.wurstclient.util.ChatUtils;
import net.wurstclient.util.GoogleTranslate;

@SearchTags({"chat translator", "ChatTranslate", "chat translate",
	"ChatTranslation", "chat translation", "AutoTranslate", "auto translate",
	"AutoTranslator", "auto translator", "AutoTranslation", "auto translation",
	"GoogleTranslate", "google translate", "GoogleTranslator",
	"google translator", "GoogleTranslation", "google translation"})
public final class ChatTranslatorHack extends Hack implements ChatInputListener
{
	private final LanguageSetting translateFrom =
		LanguageSetting.withAutoDetect("Translate from", Language.AUTO_DETECT);
	
	private final LanguageSetting translateTo =
		LanguageSetting.withoutAutoDetect("Translate to", Language.ENGLISH);
	
	public ChatTranslatorHack()
	{
		super("ChatTranslator");
		setCategory(Category.CHAT);
		addSetting(translateFrom);
		addSetting(translateTo);
	}
	
	@Override
	protected void onEnable()
	{
		EVENTS.add(ChatInputListener.class, this);
	}
	
	@Override
	protected void onDisable()
	{
		EVENTS.remove(ChatInputListener.class, this);
	}
	
	@Override
	public void onReceivedMessage(ChatInputEvent event)
	{
		String message = event.getComponent().getString();
		Language fromLang = translateFrom.getSelected();
		Language toLang = translateTo.getSelected();
		
		if(message.startsWith(ChatUtils.WURST_PREFIX)
			|| message.startsWith(toLang.getPrefix()))
			return;
		
		Thread.ofVirtual().name("ChatTranslator")
			.uncaughtExceptionHandler((t, e) -> e.printStackTrace())
			.start(() -> showTranslated(message, fromLang, toLang));
	}
	
	private void showTranslated(String message, Language fromLang,
		Language toLang)
	{
		String translated = GoogleTranslate.translate(message,
			fromLang.getValue(), toLang.getValue());
		
		if(translated != null)
			MC.inGameHud.getChatHud().addMessage(toLang.prefixText(translated));
	}
}
