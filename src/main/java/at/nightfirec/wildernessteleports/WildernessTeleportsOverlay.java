/*
 * Copyright (c) 2021, Jordan Atwood <nightfirecat@protonmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package at.nightfirec.wildernessteleports;

import com.google.common.collect.ImmutableList;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.List;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.ImageComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;

class WildernessTeleportsOverlay extends Overlay
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
		ItemID.AMULET_OF_GLORY_8283, // mounted glory interface item
		ItemID.AMULET_OF_GLORY_20586 // LMS glory
	);
	private static final Color BACKGROUND_COLOR = new Color(150, 0, 0, 150);
	private final PanelComponent imagePanelComponent = new PanelComponent();

	private final WildernessTeleportsPlugin plugin;
	private final Client client;

	@Inject
	private WildernessTeleportsOverlay(WildernessTeleportsPlugin plugin, Client client, ItemManager itemManager)
	{
		setPosition(OverlayPosition.TOP_CENTER);
		setPriority(OverlayPriority.HIGH);
		this.plugin = plugin;
		this.client = client;
		imagePanelComponent.setBackgroundColor(BACKGROUND_COLOR);
		imagePanelComponent.getChildren().add(new ImageComponent(itemManager.getImage(ItemID.AMULET_OF_GLORY)));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final boolean inWilderness = client.getVar(Varbits.IN_WILDERNESS) == 1;

		if (!inWilderness)
		{
			return null;
		}

		boolean hasUnchargedGlory = false;
		boolean hasChargedGlory = false;

		final ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
		if (inventory != null)
		{
			hasUnchargedGlory |= containerHasAnyId(inventory, UNCHARGED_GLORY_IDS);
			hasChargedGlory |= containerHasAnyId(inventory, CHARGED_GLORY_IDS);
		}

		final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
		if (equipment != null)
		{
			hasUnchargedGlory |= containerHasAnyId(equipment, UNCHARGED_GLORY_IDS);
			hasChargedGlory |= containerHasAnyId(equipment, CHARGED_GLORY_IDS);
		}

		if (!hasUnchargedGlory || hasChargedGlory)
		{
			return null;
		}

		return imagePanelComponent.render(graphics);
	}

	private static boolean containerHasAnyId(@Nonnull final ItemContainer container, final List<Integer> itemIds)
	{
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
