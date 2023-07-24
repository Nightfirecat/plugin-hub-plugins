/*
 * Copyright (c) 2020, Jordan Atwood <nightfirecat@protonmail.com>
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
package at.nightfirec.fightcavewaves;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.externalplugins.ExternalPluginManager;
import net.runelite.client.ui.overlay.OverlayManager;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FightCaveWavesPluginTest
{
	@Mock
	@Bind
	private Client client;

	@Mock
	@Bind
	private FightCaveWavesConfig config;

	@Mock
	@Bind
	private ScheduledExecutorService executor;

	@Mock
	@Bind
	private ConfigManager configManager;

	@Mock
	@Bind
	private OverlayManager overlayManager;

	@Inject
	private FightCaveWavesPlugin plugin;

	private static final GameStateChanged LOGGED_IN = new GameStateChanged();

	static
	{
		LOGGED_IN.setGameState(GameState.LOGGED_IN);
	}

	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FightCaveWavesPlugin.class);
		RuneLite.main(args);
	}

	@Before
	public void before()
	{
		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	@Test
	public void fightCaveWavesTest()
	{
		final List<Map<WaveMonster, Integer>> waves = FightCaveWavesPlugin.getFIGHT_CAVE_WAVES();

		assertEquals(FightCaveWavesPlugin.MAX_FIGHT_CAVE_WAVE, waves.size());

		assertEquals(ImmutableMap.of(FightCaveMonster.TZ_KIH, 1), waves.get(0));
		assertEquals(ImmutableMap.of(FightCaveMonster.TZ_KIH, 2), waves.get(1));
		assertEquals(ImmutableMap.of(FightCaveMonster.TZ_KEK, 1), waves.get(2));

		assertEquals(ImmutableMap.of(FightCaveMonster.TZ_KEK, 1, FightCaveMonster.TZ_KIH, 2), waves.get(4));
		assertEquals(ImmutableMap.of(FightCaveMonster.TZ_KEK, 2), waves.get(5));
		assertEquals(ImmutableMap.of(FightCaveMonster.TOK_XIL, 1), waves.get(6));

		assertEquals(ImmutableMap.of(FightCaveMonster.TOK_XIL, 1, FightCaveMonster.TZ_KEK, 1, FightCaveMonster.TZ_KIH, 2), waves.get(11));
		assertEquals(ImmutableMap.of(FightCaveMonster.TOK_XIL, 1, FightCaveMonster.TZ_KEK, 2), waves.get(12));
		assertEquals(ImmutableMap.of(FightCaveMonster.TOK_XIL, 2), waves.get(13));
		assertEquals(ImmutableMap.of(FightCaveMonster.YT_MEJKOT, 1), waves.get(14));

		assertEquals(ImmutableMap.of(FightCaveMonster.YT_MEJKOT, 1, FightCaveMonster.TOK_XIL, 1, FightCaveMonster.TZ_KEK, 1, FightCaveMonster.TZ_KIH, 2), waves.get(26));
		assertEquals(ImmutableMap.of(FightCaveMonster.YT_MEJKOT, 1, FightCaveMonster.TOK_XIL, 1, FightCaveMonster.TZ_KEK, 2), waves.get(27));
		assertEquals(ImmutableMap.of(FightCaveMonster.YT_MEJKOT, 1, FightCaveMonster.TOK_XIL, 2), waves.get(28));
		assertEquals(ImmutableMap.of(FightCaveMonster.YT_MEJKOT, 2), waves.get(29));
		assertEquals(ImmutableMap.of(FightCaveMonster.KET_ZEK, 1), waves.get(30));

		assertEquals(ImmutableMap.of(FightCaveMonster.KET_ZEK, 1, FightCaveMonster.YT_MEJKOT, 1, FightCaveMonster.TOK_XIL, 1, FightCaveMonster.TZ_KEK, 1, FightCaveMonster.TZ_KIH, 2), waves.get(57));
		assertEquals(ImmutableMap.of(FightCaveMonster.KET_ZEK, 1, FightCaveMonster.YT_MEJKOT, 1, FightCaveMonster.TOK_XIL, 1, FightCaveMonster.TZ_KEK, 2), waves.get(58));
		assertEquals(ImmutableMap.of(FightCaveMonster.KET_ZEK, 1, FightCaveMonster.YT_MEJKOT, 1, FightCaveMonster.TOK_XIL, 2), waves.get(59));
		assertEquals(ImmutableMap.of(FightCaveMonster.KET_ZEK, 1, FightCaveMonster.YT_MEJKOT, 2), waves.get(60));
		assertEquals(ImmutableMap.of(FightCaveMonster.KET_ZEK, 2), waves.get(61));
		assertEquals(ImmutableMap.of(FightCaveMonster.TZTOK_JAD, 1), waves.get(62));
	}

	@Test
	public void infernoWavesTest()
	{
		final List<Map<WaveMonster, Integer>> waves = FightCaveWavesPlugin.getINFERNO_WAVES();

		assertEquals(FightCaveWavesPlugin.MAX_INFERNO_WAVE, waves.size());

		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_MEJRAH, 1), waves.get(0));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_MEJRAH, 2), waves.get(1));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 6), waves.get(2));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_AK, 1), waves.get(3));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_AK, 1, InfernoMonster.JAL_MEJRAH, 1), waves.get(4));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_AK, 1, InfernoMonster.JAL_MEJRAH, 2), waves.get(5));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_AK, 2), waves.get(6));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 6), waves.get(7));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_IMKOT, 1), waves.get(8));

		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_IMKOT, 2), waves.get(15));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 6), waves.get(16));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_XIL, 1), waves.get(17));

		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_XIL, 2), waves.get(32));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 6), waves.get(33));
		assertEquals(ImmutableMap.of(InfernoMonster.JAL_NIB, 3, InfernoMonster.JAL_ZEK, 1), waves.get(34));

		assertEquals(ImmutableMap.builder()
			.put(InfernoMonster.JAL_NIB, 3)
			.put(InfernoMonster.JAL_ZEK, 1)
			.put(InfernoMonster.JAL_XIL, 1)
			.put(InfernoMonster.JAL_IMKOT, 1)
			.put(InfernoMonster.JAL_AK, 1)
			.put(InfernoMonster.JAL_MEJRAH, 2)
			.build(), waves.get(61));
		assertEquals(ImmutableMap.builder()
			.put(InfernoMonster.JAL_NIB, 3)
			.put(InfernoMonster.JAL_ZEK, 1)
			.put(InfernoMonster.JAL_XIL, 1)
			.put(InfernoMonster.JAL_IMKOT, 1)
			.put(InfernoMonster.JAL_AK, 2)
			.build(), waves.get(62));
		assertEquals(ImmutableMap.builder()
			.put(InfernoMonster.JAL_NIB, 3)
			.put(InfernoMonster.JAL_ZEK, 1)
			.put(InfernoMonster.JAL_XIL, 1)
			.put(InfernoMonster.JAL_IMKOT, 2)
			.build(), waves.get(63));
		assertEquals(ImmutableMap.builder()
			.put(InfernoMonster.JAL_NIB, 3)
			.put(InfernoMonster.JAL_ZEK, 1)
			.put(InfernoMonster.JAL_XIL, 2)
			.build(), waves.get(64));
		assertEquals(ImmutableMap.builder()
			.put(InfernoMonster.JAL_NIB, 3)
			.put(InfernoMonster.JAL_ZEK, 2)
			.build(), waves.get(65));
		assertEquals(ImmutableMap.of(InfernoMonster.JALTOK_JAD, 1), waves.get(66));
		assertEquals(ImmutableMap.of(InfernoMonster.JALTOK_JAD, 3), waves.get(67));
		assertEquals(ImmutableMap.of(InfernoMonster.TZKAL_ZUK, 1), waves.get(68));
	}

	@Test
	public void testLoginOutsideCaves()
	{
		plugin.onGameStateChanged(LOGGED_IN);

		assertEquals(-1, plugin.getCurrentWave());
		assertNull(plugin.getActiveWaves());
	}

	@Test
	public void testLoginWithinFightCaves()
	{
		when(client.getMapRegions()).thenReturn(new int[]{ FightCaveWavesPlugin.FIGHT_CAVE_REGION });

		plugin.onGameStateChanged(LOGGED_IN);

		assertEquals(-1, plugin.getCurrentWave());
		assertNull(plugin.getActiveWaves());
	}

	@Test
	public void testLoginWithinInferno()
	{
		when(client.getMapRegions()).thenReturn(new int[]{ FightCaveWavesPlugin.INFERNO_REGION});

		plugin.onGameStateChanged(LOGGED_IN);

		assertEquals(-1, plugin.getCurrentWave());
		assertNull(plugin.getActiveWaves());
	}

	@Test
	public void testFightCavesWaveOne()
	{
		when(client.getMapRegions()).thenReturn(new int[]{ FightCaveWavesPlugin.FIGHT_CAVE_REGION });

		plugin.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>Wave: 1</col>", "", 0));

		assertEquals(1, plugin.getCurrentWave());
		assertEquals(FightCaveWavesPlugin.FIGHT_CAVE_WAVES, plugin.getActiveWaves());
	}

	@Test
	public void testInfernoWaveOne()
	{
		when(client.getMapRegions()).thenReturn(new int[]{ FightCaveWavesPlugin.INFERNO_REGION});

		plugin.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>Wave: 1</col>", "", 0));

		assertEquals(1, plugin.getCurrentWave());
		assertEquals(FightCaveWavesPlugin.INFERNO_WAVES, plugin.getActiveWaves());
	}

	@Test
	public void testInfernoWaveComplete()
	{
		when(client.getMapRegions()).thenReturn(new int[]{ FightCaveWavesPlugin.INFERNO_REGION});

		plugin.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "<col=ef1020>Wave: 1</col>", "", 0));
		plugin.onChatMessage(new ChatMessage(null, ChatMessageType.GAMEMESSAGE, "", "Wave complete!", "", 0));

		assertEquals(2, plugin.getCurrentWave());
		assertEquals(FightCaveWavesPlugin.INFERNO_WAVES, plugin.getActiveWaves());
	}

	@Test
	public void testWaveMonsterDisplayString()
	{
		assertEquals("Bat", FightCaveMonster.TZ_KIH.displayString(true, false));
		assertEquals("Nibbler", InfernoMonster.JAL_NIB.displayString(true, false));
		assertEquals("Zuk - Level 1400", InfernoMonster.TZKAL_ZUK.displayString(true, true));
		assertEquals("TzTok-Jad", FightCaveMonster.TZTOK_JAD.displayString(false, false));
		assertEquals("JalTok-Jad - Level 900", InfernoMonster.JALTOK_JAD.displayString(false, true));
	}
}
