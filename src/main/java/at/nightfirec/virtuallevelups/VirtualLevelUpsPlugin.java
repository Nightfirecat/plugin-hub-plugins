/*
 * Copyright (c) 2018, Magic fTail
 * Copyright (c) 2020, Jordan <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.nightfirec.virtuallevelups;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Provides;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.SpriteID;
import net.runelite.api.events.StatChanged;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageCapture;
import net.runelite.client.util.ImageUtil;

@PluginDescriptor(
	name = "Virtual Level Ups",
	description = "Display level up dialogs upon reaching virtual level up milestones",
	tags = {"skill", "experience"}
)
@Slf4j
public class VirtualLevelUpsPlugin extends Plugin
{
	@Getter(AccessLevel.PACKAGE)
	@Inject
	private Client client;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private ClientThread clientThread;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private ChatboxPanelManager chatboxPanelManager;

	@Inject
	private DrawManager drawManager;

	@Inject
	private ImageCapture imageCapture;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private VirtualLevelUpsConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private VirtualLevelUpsOverlay overlay;

	@Inject
	private SpriteManager spriteManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private ConfigManager configManager;

	@Getter(AccessLevel.PACKAGE)
	@Inject
	private ChatMessageManager chatMessageManager;

	@Getter(AccessLevel.PACKAGE)
	private BufferedImage reportButton;

	private final Map<Skill, Integer> previousXpMap = new EnumMap<>(Skill.class);
	@VisibleForTesting
	@Getter(AccessLevel.PACKAGE)
	private final List<Skill> skillsLeveledUp = new ArrayList<>();

	private VirtualLevelUpsInterfaceInput input;

	@Provides
	VirtualLevelUpsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(VirtualLevelUpsConfig.class);
	}

	@Override
	public void startUp()
	{
		clientThread.invoke(this::initializePreviousXpMap);

		overlayManager.add(overlay);
		spriteManager.getSpriteAsync(SpriteID.CHATBOX_REPORT_BUTTON, 0, s -> reportButton = s);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(overlay);

		if (input != null && chatboxPanelManager.getCurrentInput() == input)
		{
			chatboxPanelManager.close();
		}
		previousXpMap.clear();
		skillsLeveledUp.clear();
		input = null;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		switch(event.getGameState())
		{
			case LOGIN_SCREEN:
			case HOPPING:
			case LOGGING_IN:
			case LOGIN_SCREEN_AUTHENTICATOR:
				previousXpMap.clear();
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		final Skill skill = event.getSkill();

		final int xpAfter = client.getSkillExperience(skill);
		final int levelAfter = Experience.getLevelForXp(xpAfter);

		final int xpBefore = previousXpMap.getOrDefault(skill, -1);
		final int levelBefore = xpBefore == -1 ? -1 : Experience.getLevelForXp(xpBefore);

		previousXpMap.put(skill, xpAfter);

		// Do not proceed if any of the following are true:
		//  * xpBefore == -1               (don't fire when first setting new known value)
		//  * xpBefore >= xpAfter          (do not allow 200m -> 200m exp drops)
		//  * levelAfter <= MAX_REAL_LEVEL (we don't care about real level ups)
		//    * xpAfter != 200m _and_      (unless we're reaching 200m exp for the first time...)
		//    * levelBefore >= levelAfter  (stop if if we're not actually reaching a virtual level)
		if (xpBefore == -1 || levelAfter <= Experience.MAX_REAL_LEVEL || xpAfter <= xpBefore
			|| (xpAfter != Experience.MAX_SKILL_XP && levelBefore >= levelAfter))
		{
			return;
		}

		skillsLeveledUp.add(skill);
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked event)
	{
		if (input != null)
		{
			input.triggerClose();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (input != null)
		{
			input.closeIfTriggered();
		}

		if (skillsLeveledUp.isEmpty() || !chatboxPanelManager.getContainerWidget().isHidden())
		{
			return;
		}

		final Skill skill = skillsLeveledUp.remove(0);

		input = new VirtualLevelUpsInterfaceInput(this, skill);
		chatboxPanelManager.openInput(input);
	}

	private void initializePreviousXpMap()
	{
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			previousXpMap.clear();
		}
		else
		{
			for (final Skill skill : Skill.values())
			{
				previousXpMap.put(skill, client.getSkillExperience(skill));
			}
		}
	}

	void takeScreenshot(final Skill skill)
	{
		if (!config.takeScreenshots())
		{
			return;
		}

		final int skillExperience = client.getSkillExperience(skill);

		final String skillLevel;
		if (skillExperience == Experience.MAX_SKILL_XP)
		{
			skillLevel = "MAX_EXP";
		}
		else
		{
			skillLevel = Experience.getLevelForXp(skillExperience) + "";
		}

		final String fileName = skill.getName() + '(' + skillLevel + ')';
		final String subDir = "Levels";

		Consumer<Image> imageCallback = (img) ->
		{
			// This callback is on the game thread, move to the executor thread
			executor.submit(() -> takeScreenshot(fileName, subDir, img));
		};

		if (configManager.getConfiguration("screenshot", "displayDate").equals("true"))
		{
			overlay.queueForTimestamp(imageCallback);
		}
		else
		{
			drawManager.requestNextFrameListener(imageCallback);
		}
	}

	void takeScreenshot(String fileName, String subDir, Image image)
	{
		final boolean includeFrame = configManager.getConfiguration("screenshot", "includeFrame").equals("true");

		final BufferedImage screenshot;
		if (!includeFrame)
		{
			screenshot = ImageUtil.bufferedImageFromImage(image);
		}
		else
		{
			screenshot = imageCapture.addClientFrame(image);
		}

		imageCapture.saveScreenshot(
			screenshot,
			fileName,
			subDir,
			configManager.getConfiguration("screenshot", "notifyWhenTaken").equals("true"),
			false);
	}
}
