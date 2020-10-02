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
package com.fightcavewaves;

import com.google.common.collect.ImmutableMap;
import java.util.EnumMap;
import java.util.List;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FightCaveWavesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FightCaveWavesPlugin.class);
		RuneLite.main(args);
	}

	@Test
	public void fightCaveWavesTest()
	{
		final List<EnumMap<WaveMonster, Integer>> waves = FightCaveWavesPlugin.getWAVES();

		assertEquals(FightCaveWavesPlugin.MAX_WAVE, waves.size());

		assertEquals(ImmutableMap.of(WaveMonster.TZ_KIH, 1), waves.get(0));
		assertEquals(ImmutableMap.of(WaveMonster.TZ_KIH, 2), waves.get(1));
		assertEquals(ImmutableMap.of(WaveMonster.TZ_KEK, 1), waves.get(2));

		assertEquals(ImmutableMap.of(WaveMonster.TZ_KEK, 1, WaveMonster.TZ_KIH, 2), waves.get(4));
		assertEquals(ImmutableMap.of(WaveMonster.TZ_KEK, 2), waves.get(5));
		assertEquals(ImmutableMap.of(WaveMonster.TOK_XIL, 1), waves.get(6));

		assertEquals(ImmutableMap.of(WaveMonster.TOK_XIL, 1, WaveMonster.TZ_KEK, 1, WaveMonster.TZ_KIH, 2), waves.get(11));
		assertEquals(ImmutableMap.of(WaveMonster.TOK_XIL, 1, WaveMonster.TZ_KEK, 2), waves.get(12));
		assertEquals(ImmutableMap.of(WaveMonster.TOK_XIL, 2), waves.get(13));
		assertEquals(ImmutableMap.of(WaveMonster.YT_MEJKOT, 1), waves.get(14));

		assertEquals(ImmutableMap.of(WaveMonster.YT_MEJKOT, 1, WaveMonster.TOK_XIL, 1, WaveMonster.TZ_KEK, 1, WaveMonster.TZ_KIH, 2), waves.get(26));
		assertEquals(ImmutableMap.of(WaveMonster.YT_MEJKOT, 1, WaveMonster.TOK_XIL, 1, WaveMonster.TZ_KEK, 2), waves.get(27));
		assertEquals(ImmutableMap.of(WaveMonster.YT_MEJKOT, 1, WaveMonster.TOK_XIL, 2), waves.get(28));
		assertEquals(ImmutableMap.of(WaveMonster.YT_MEJKOT, 2), waves.get(29));
		assertEquals(ImmutableMap.of(WaveMonster.KET_ZEK, 1), waves.get(30));

		assertEquals(ImmutableMap.of(WaveMonster.KET_ZEK, 1, WaveMonster.YT_MEJKOT, 1, WaveMonster.TOK_XIL, 1, WaveMonster.TZ_KEK, 1, WaveMonster.TZ_KIH, 2), waves.get(57));
		assertEquals(ImmutableMap.of(WaveMonster.KET_ZEK, 1, WaveMonster.YT_MEJKOT, 1, WaveMonster.TOK_XIL, 1, WaveMonster.TZ_KEK, 2), waves.get(58));
		assertEquals(ImmutableMap.of(WaveMonster.KET_ZEK, 1, WaveMonster.YT_MEJKOT, 1, WaveMonster.TOK_XIL, 2), waves.get(59));
		assertEquals(ImmutableMap.of(WaveMonster.KET_ZEK, 1, WaveMonster.YT_MEJKOT, 2), waves.get(60));
		assertEquals(ImmutableMap.of(WaveMonster.KET_ZEK, 2), waves.get(61));
		assertEquals(ImmutableMap.of(WaveMonster.TZKOK_JAD, 1), waves.get(62));
	}
}
