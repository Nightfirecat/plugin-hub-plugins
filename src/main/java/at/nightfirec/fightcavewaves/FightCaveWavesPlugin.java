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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.apache.commons.lang3.ArrayUtils;

@PluginDescriptor(
	name = "Fight Cave Waves",
	description = "Displays current and upcoming wave monsters in the Fight Caves",
	tags = {"bosses", "combat", "minigame", "overlay", "pve", "pvm", "jad", "fire", "cape", "wave", "inferno", "zuk"}
)
public class FightCaveWavesPlugin extends Plugin
{
	private static final Pattern WAVE_PATTERN = Pattern.compile(".*Wave: (\\d+).*");
	private static final String INFERNO_WAVE_COMPLETE = "Wave completed!";
	@VisibleForTesting
	static final int FIGHT_CAVE_REGION = 9551;
	@VisibleForTesting
	static final int INFERNO_REGION = 9043;
	private static final int MAX_MONSTER_SPAWNS_PER_WAVE = 2;

	@VisibleForTesting
	static final int MAX_FIGHT_CAVE_WAVE = 63;
	@VisibleForTesting
	static final int MAX_INFERNO_WAVE = 69;

	@Getter
	static final List<Map<WaveMonster, Integer>> FIGHT_CAVE_WAVES = new ArrayList<>();

	@Getter
	static final List<Map<WaveMonster, Integer>> INFERNO_WAVES = new ArrayList<>();

	@Getter
	private int currentWave = -1;

	@Getter
	@Nullable
	private List<Map<WaveMonster, Integer>> activeWaves;

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private WaveOverlay waveOverlay;

	static
	{
		initializeFightCaveMonsters();
		initializeInfernoMonsters();
	}

	@Provides
	FightCaveWavesConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FightCaveWavesConfig.class);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(waveOverlay);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(waveOverlay);
		resetWaves();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		if (!inFightCave() && !inInferno())
		{
			resetWaves();
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE)
		{
			return;
		}

		if (activeWaves == INFERNO_WAVES
			&& INFERNO_WAVE_COMPLETE.equals(event.getMessage()))
		{
			currentWave++;
			return;
		}

		final Matcher waveMatcher = WAVE_PATTERN.matcher(event.getMessage());

		if (!(inFightCave() || inInferno())
			|| !waveMatcher.matches())
		{
			return;
		}

		currentWave = Integer.parseInt(waveMatcher.group(1));

		if (inFightCave())
		{
			activeWaves = FIGHT_CAVE_WAVES;
		}
		else
		{
			activeWaves = INFERNO_WAVES;
		}
	}

	boolean inFightCave()
	{
		return ArrayUtils.contains(client.getMapRegions(), FIGHT_CAVE_REGION);
	}

	boolean inInferno()
	{
		return ArrayUtils.contains(client.getMapRegions(), INFERNO_REGION);
	}

	private void resetWaves()
	{
		currentWave = -1;
		activeWaves = null;
	}

	static String formatMonsterQuantity(final WaveMonster monster, final int quantity, final boolean commonNames, final boolean showMonsterLevel)
	{
		return String.format("%dx %s", quantity, monster.displayString(commonNames, showMonsterLevel));
	}

	private static void initializeFightCaveMonsters()
	{
		final FightCaveMonster[] fightCaveMonsters = FightCaveMonster.values();

		// Add wave 1, future waves are derived from its contents
		FIGHT_CAVE_WAVES.add(ImmutableMap.of(fightCaveMonsters[0], fightCaveMonsters[0].getCountPerSpawn()));

		for (int wave = 1; wave < MAX_FIGHT_CAVE_WAVE; wave++)
		{
			final Map<WaveMonster, Integer> prevWave = new HashMap<>(FIGHT_CAVE_WAVES.get(wave - 1));
			int maxMonsterOrdinal = -1;

			for (int i = 0; i < fightCaveMonsters.length; i++)
			{
				final int ordinalMonsterSpawnCount = prevWave.getOrDefault(fightCaveMonsters[i], 0) / fightCaveMonsters[i].getCountPerSpawn();

				if (ordinalMonsterSpawnCount == MAX_MONSTER_SPAWNS_PER_WAVE)
				{
					maxMonsterOrdinal = i;
					break;
				}
			}

			if (maxMonsterOrdinal >= 0)
			{
				prevWave.remove(fightCaveMonsters[maxMonsterOrdinal]);
			}

			final int addedMonsterOrdinal = maxMonsterOrdinal >= 0 ? maxMonsterOrdinal + 1 : 0;
			final FightCaveMonster addedMonster = fightCaveMonsters[addedMonsterOrdinal];
			final int addedMonsterQuantity = prevWave.getOrDefault(addedMonster, 0) + addedMonster.getCountPerSpawn();

			prevWave.put(addedMonster, addedMonsterQuantity);

			FIGHT_CAVE_WAVES.add(prevWave);
		}
	}

	private static void initializeInfernoMonsters()
	{
		final InfernoMonster[] infernoMonsters = InfernoMonster.values();

		// Add wave 1, future waves are derived from its contents
		INFERNO_WAVES.add(ImmutableMap.of(
			InfernoMonster.JAL_NIB, InfernoMonster.JAL_NIB.getCountPerSpawn(),
			InfernoMonster.JAL_MEJRAH, InfernoMonster.JAL_MEJRAH.getCountPerSpawn()
		));

		while (true)
		{
			final Map<WaveMonster, Integer> prevWave = new HashMap<>(INFERNO_WAVES.get(INFERNO_WAVES.size() - 1));

			// Check for waves with double-spawns to either stop adding waves (for double mages) or insert a double nibbler wave
			// Note: because prevWave is defined prior to this check, double nibbler waves will never enter this check
			if (prevWave.size() == 2 && prevWave.entrySet().stream().anyMatch(entry -> entry.getValue() == entry.getKey().getCountPerSpawn() * 2))
			{
				// Double mage is the last wave where this iterative wave algorithm makes sense, don't continue building waves after this
				if (prevWave.containsKey(InfernoMonster.JAL_ZEK))
				{
					break;
				}
				else
				{
					// Add a double nibbler wave after each wave containing only a single nibbler spawn and two identical spawns
					INFERNO_WAVES.add(ImmutableMap.of(InfernoMonster.JAL_NIB, InfernoMonster.JAL_NIB.getCountPerSpawn() * 2));
				}
			}

			int maxMonsterOrdinal = -1;
			for (int i = 0; i < infernoMonsters.length; i++)
			{
				final int ordinalMonsterSpawnCount = prevWave.getOrDefault(infernoMonsters[i], 0) / infernoMonsters[i].getCountPerSpawn();

				if (ordinalMonsterSpawnCount == MAX_MONSTER_SPAWNS_PER_WAVE)
				{
					 maxMonsterOrdinal = i;
					 break;
				}
			}

			if (maxMonsterOrdinal >= 0)
			{
				prevWave.remove(infernoMonsters[maxMonsterOrdinal]);
			}

			final int addedMonsterOrdinal = maxMonsterOrdinal >= 1 ? maxMonsterOrdinal + 1 : 1;
			final InfernoMonster addedMonster = infernoMonsters[addedMonsterOrdinal];
			final int addedMonsterQuantity = prevWave.getOrDefault(addedMonster, 0) + addedMonster.getCountPerSpawn();

			prevWave.put(addedMonster, addedMonsterQuantity);

			INFERNO_WAVES.add(prevWave);
		}

		INFERNO_WAVES.add(ImmutableMap.of(InfernoMonster.JALTOK_JAD, 1));
		INFERNO_WAVES.add(ImmutableMap.of(InfernoMonster.JALTOK_JAD, 3));
		INFERNO_WAVES.add(ImmutableMap.of(InfernoMonster.TZKAL_ZUK, 1));
	}
}
