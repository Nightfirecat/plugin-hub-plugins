/*
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

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.concurrent.ScheduledExecutorService;
import net.runelite.api.Client;
import net.runelite.api.Experience;
import static net.runelite.api.Experience.MAX_REAL_LEVEL;
import static net.runelite.api.Experience.MAX_SKILL_XP;
import static net.runelite.api.Experience.MAX_VIRT_LEVEL;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageCapture;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class VirtualLevelUpsPluginTest
{
	private static final Skill SKILL = Skill.HITPOINTS;

	@Inject
	private VirtualLevelUpsPlugin plugin;

	@Mock
	@Bind
	private ImageCapture imageCapture;

	@Mock
	@Bind
	private ClientUI clientUI;

	@Mock
	@Bind
	private OverlayManager overlayManager;

	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private ChatboxPanelManager chatboxPanelManager;

	@Mock
	@Bind
	private Notifier notifier;

	@Mock
	@Bind
	private ConfigManager configManager;

	@Mock
	@Bind
	private RuneLiteConfig runeLiteConfig;

	@Mock
	@Bind
	private VirtualLevelUpsConfig config;

	@Mock
	@Bind
	private ScheduledExecutorService executor;

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VirtualLevelUpsPlugin.class);
		RuneLite.main(args);
	}

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	@Test
	public void virtualLevelGained()
	{
		int level = MAX_REAL_LEVEL;
		statChanged(SKILL, Experience.getXpForLevel(level), level);

		level++;
		statChanged(SKILL, Experience.getXpForLevel(level), level);

		assertTrue(plugin.getSkillsLeveledUp().contains(SKILL));
	}

	@Test
	public void testXpSetAfterNullState()
	{
		final int level = MAX_REAL_LEVEL + 1;
		statChanged(SKILL, Experience.getXpForLevel(level), level);

		assertTrue(plugin.getSkillsLeveledUp().isEmpty());
	}

	@Test
	public void testXpBelow100()
	{
		int level = MAX_REAL_LEVEL - 1;
		statChanged(SKILL, Experience.getXpForLevel(level), level);

		level++;
		statChanged(SKILL, Experience.getXpForLevel(level), level);

		assertTrue(plugin.getSkillsLeveledUp().isEmpty());
	}

	@Test
	public void testNoLevelGained()
	{
		final int level = MAX_REAL_LEVEL;
		statChanged(SKILL, Experience.getXpForLevel(level), level);
		statChanged(SKILL, Experience.getXpForLevel(level) + 1, level);

		assertTrue(plugin.getSkillsLeveledUp().isEmpty());
	}

	@Test
	public void textMaxXp()
	{
		final int level = MAX_VIRT_LEVEL;
		statChanged(SKILL, MAX_SKILL_XP - 1, level);
		statChanged(SKILL, MAX_SKILL_XP, level);

		assertTrue(plugin.getSkillsLeveledUp().contains(SKILL));
	}

	@Test
	public void testSkillBoostAtMaxXp()
	{
		statChanged(SKILL, MAX_SKILL_XP, MAX_VIRT_LEVEL);
		statChanged(SKILL, MAX_SKILL_XP, MAX_VIRT_LEVEL, MAX_VIRT_LEVEL + 1);

		assertEquals(1, plugin.getSkillsLeveledUp().size());
	}

	private void statChanged(final Skill skill, final int xp, final int level)
	{
		statChanged(skill, xp, level, level);
	}

	private void statChanged(final Skill skill, final int xp, final int level, final int boostedLevel)
	{
		when(client.getSkillExperience(skill)).thenReturn(xp);
		plugin.onStatChanged(new StatChanged(skill, xp, level, boostedLevel));
	}
}
