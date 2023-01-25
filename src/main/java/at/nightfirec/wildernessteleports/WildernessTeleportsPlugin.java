/*
 * Copyright (c) 2021, Jordan <nightfirecat@protonmail.com>
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
package at.nightfirec.wildernessteleports;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.util.List;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Uncharged Glory Warning",
	description = "Warns you if you enter the wilderness with uncharged amulets of glory",
	tags = {"amulet","glory","wilderness","20","30","tele","teleport"}
)
public class WildernessTeleportsPlugin extends Plugin
{
	private static final List<Integer> CHARGED_GLORY_IDS = ImmutableList.of(
		ItemID.AMULET_OF_ETERNAL_GLORY,
		ItemID.AMULET_OF_GLORY1,
		ItemID.AMULET_OF_GLORY2,
		ItemID.AMULET_OF_GLORY3,
		ItemID.AMULET_OF_GLORY4,
		ItemID.AMULET_OF_GLORY5,
		ItemID.AMULET_OF_GLORY6,
		ItemID.AMULET_OF_GLORY_T1,
		ItemID.AMULET_OF_GLORY_T2,
		ItemID.AMULET_OF_GLORY_T3,
		ItemID.AMULET_OF_GLORY_T4,
		ItemID.AMULET_OF_GLORY_T5,
		ItemID.AMULET_OF_GLORY_T6
	);
	private static final List<Integer> UNCHARGED_GLORY_IDS = ImmutableList.of(
		ItemID.AMULET_OF_GLORY,
		ItemID.AMULET_OF_GLORY_T,
		ItemID.AMULET_OF_GLORY_8283 // mounted glory interface item
		// ItemID.AMULET_OF_GLORY_20586 // LMS glory; not included here as you cannot teleport from LMS anyway
	);

	@Inject
	private WildernessTeleportsOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Getter(AccessLevel.PACKAGE)
	private boolean inWilderness;

	@Accessors(fluent = true)
	@Getter(AccessLevel.PACKAGE)
	private boolean hasOnlyUnchargedGlories;

	private boolean gameStateChangeVarbitRace;

	@Provides
	WildernessTeleportsConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessTeleportsConfig.class);
	}

	@Override
	public void startUp()
	{
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			clientThread.invokeLater(() ->
			{
				checkInWilderness();
				checkChargedGlories();
			});
		}
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown()
	{
		gameStateChangeVarbitRace = false;
		overlayManager.remove(overlay);
	}

	@Subscribe
	void onItemContainerChanged(ItemContainerChanged event)
	{
		final int changedContainerId = event.getContainerId();
		if (changedContainerId != InventoryID.INVENTORY.getId() && changedContainerId != InventoryID.EQUIPMENT.getId())
		{
			return;
		}

		checkChargedGlories();
	}

	@Subscribe
	void onGameStateChanged(GameStateChanged event)
	{
		final GameState state = event.getGameState();
		if (state == GameState.LOADING || state == GameState.LOGGED_IN)
		{
			gameStateChangeVarbitRace = true;
		}

		// Item container can change before login state (say, during disconnections), so check for glories after we're
		// logged back in.
		if (state == GameState.LOGGED_IN)
		{
			checkChargedGlories();
		}
	}

	@Subscribe
	void onVarbitChanged(VarbitChanged event)
	{
		if (gameStateChangeVarbitRace)
		{
			return;
		}

		checkInWilderness();
	}

	@Subscribe
	void onGameTick(GameTick event)
	{
		gameStateChangeVarbitRace = false;
	}

	private void checkInWilderness()
	{
		inWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
	}

	private void checkChargedGlories()
	{
		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		final boolean hasUnchargedGlory = containerHasAnyId(inventory, UNCHARGED_GLORY_IDS) || containerHasAnyId(equipment, UNCHARGED_GLORY_IDS);
		final boolean hasChargedGlory = containerHasAnyId(inventory, CHARGED_GLORY_IDS) || containerHasAnyId(equipment, CHARGED_GLORY_IDS);
		hasOnlyUnchargedGlories = hasUnchargedGlory && !hasChargedGlory;
	}

	private static boolean containerHasAnyId(@Nullable final ItemContainer container, final List<Integer> itemIds)
	{
		if (container == null)
		{
			return false;
		}

		for (int item : itemIds)
		{
			if (container.contains(item))
			{
				return true;
			}
		}
		return false;
	}
}
