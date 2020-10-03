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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

class WaveOverlay extends OverlayPanel
{
	private static final Color HEADER_COLOR = ColorScheme.BRAND_ORANGE;

	private final FightCaveWavesConfig config;
	private final FightCaveWavesPlugin plugin;

	@Inject
	private WaveOverlay(FightCaveWavesConfig config, FightCaveWavesPlugin plugin)
	{
		setPosition(OverlayPosition.TOP_RIGHT);
		this.config = config;
		this.plugin = plugin;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final List<Map<WaveMonster, Integer>> activeWaves = plugin.getActiveWaves();
		final int currentWave = plugin.getCurrentWave();

		if (!(plugin.inFightCave() || plugin.inInferno())
			|| currentWave < 0
			|| activeWaves == null)
		{
			return null;
		}

		panelComponent.getChildren().clear();

		final int waveIndex = currentWave - 1;

		if (config.waveDisplay() == WaveDisplayMode.CURRENT
			|| config.waveDisplay() == WaveDisplayMode.BOTH)
		{
			final Map<WaveMonster, Integer> waveContents = activeWaves.get(waveIndex);

			addWaveInfo("Wave " + plugin.getCurrentWave(), waveContents);
		}

		if ((config.waveDisplay() == WaveDisplayMode.NEXT
			|| config.waveDisplay() == WaveDisplayMode.BOTH)
			&& currentWave < activeWaves.size())
		{
			final Map<WaveMonster, Integer> waveContents = activeWaves.get(waveIndex + 1);

			addWaveInfo("Next wave", waveContents);
		}

		return super.render(graphics);
	}

	private void addWaveInfo(final String headerText, final Map<WaveMonster, Integer> waveContents)
	{
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(headerText)
			.color(HEADER_COLOR)
			.build());

		for (LineComponent line : buildWaveLines(waveContents))
		{
			panelComponent.getChildren().add(line);
		}
	}

	private static Collection<LineComponent> buildWaveLines(final Map<WaveMonster, Integer> wave)
	{
		final List<Map.Entry<WaveMonster, Integer>> monsters = new ArrayList<>(wave.entrySet());
		monsters.sort(Comparator.comparingInt(entry -> entry.getKey().getLevel()));
		final List<LineComponent> outputLines = new ArrayList<>();

		for (Map.Entry<WaveMonster, Integer> monsterEntry : monsters)
		{
			final WaveMonster monster = monsterEntry.getKey();
			final int quantity = monsterEntry.getValue();
			final LineComponent line = LineComponent.builder()
				.left(FightCaveWavesPlugin.formatMonsterQuantity(monster, quantity))
				.build();

			outputLines.add(line);
		}

		return outputLines;
	}
}
