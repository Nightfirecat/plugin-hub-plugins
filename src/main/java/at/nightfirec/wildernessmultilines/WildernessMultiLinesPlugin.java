/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
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
package at.nightfirec.wildernessmultilines;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Wilderness Multi Lines",
	description = "Show wilderness multicombat areas and dragon spear range to those areas.",
	tags = {"dragon", "spear", "multicombat", "wildy"}
)
public class WildernessMultiLinesPlugin extends Plugin
{
	private static final List<Rectangle> WILDERNESS_MULTI_AREAS = ImmutableList.of(
		new Rectangle(3008, 3600, 64, 112), // Dark warrior's palace
		new Rectangle(2946, 3816, 14, 16), // Chaos altar
		new Rectangle(2984, 3912, 24, 16), // Balance crossing to wilderness agility course
		new Rectangle(3008, 3856, 40, 48), // North of kbd entrance
		new Rectangle(3048, 3896, 24, 8), // North of rune rocks
		new Rectangle(3072, 3880, 64, 24), // North of lava maze
		new Rectangle(3112, 3872, 24, 8), // Northeast of lava maze
		new Rectangle(3136, 3840, 256, 64), // Northeast f2p wilderness
		new Rectangle(3200, 3904, 192, 64), // Northeast p2p wilderness
		new Rectangle(3152, 3752, 176, 88), // North-mid east f2p wilderness
		new Rectangle(3192, 3523, 136, 229), // East f2p wilderness
		new Rectangle(3136, 3523, 56, 61), // Wilderness north of Grand Exchange
		new Rectangle(3152, 3584, 40, 36), // SE of Ferox 1
		new Rectangle(3146, 3598, 6, 22), // SE of Ferox 2
		new Rectangle(3147, 3596, 5, 2), // SE of Ferox 2 extension 1
		new Rectangle(3149, 3595, 3, 1), // SE of Ferox 2 extension 2
		new Rectangle(3150, 3594, 2, 1), // SE of Ferox 2 extension 3
		new Rectangle(3151, 3593, 1, 1), // SE of Ferox 2 extension 4
		new Rectangle(3152, 3620, 10, 6), // SE of Ferox 3
		new Rectangle(3187, 3620, 5, 28), // East of Ferox 1
		new Rectangle(3176, 3636, 11, 12), // East of Ferox 2
		new Rectangle(3175, 3647, 1, 1) // One dumb tile north of bridge east of Ferox
	);
	private static final int SPEAR_RANGE = 4;

	private static final Area MULTI_AREA = new Area();
	private static final Area SPEAR_MULTI_AREA = new Area();

	static
	{
		for (final Rectangle multiArea : WILDERNESS_MULTI_AREAS)
		{
			MULTI_AREA.add(new Area(multiArea));
			for (int i = 0; i <= SPEAR_RANGE; i++)
			{
				final Rectangle spearArea = new Rectangle(multiArea);
				spearArea.grow(SPEAR_RANGE - i, i);
				SPEAR_MULTI_AREA.add(new Area(spearArea));
			}
		}
	}

	@Inject
	private WildernessMultiLinesOverlay overlay;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private Client client;

	@Provides
	WildernessMultiLinesConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessMultiLinesConfig.class);
	}

	@Override
	public void startUp()
	{
		overlayManager.add(overlay);
	}

	@Override
	public void shutDown()
	{
		overlayManager.remove(overlay);
	}

	private void transformWorldToLocal(float[] coords)
	{
		final LocalPoint lp = LocalPoint.fromWorld(client, (int)coords[0], (int)coords[1]);
		coords[0] = lp.getX() - Perspective.LOCAL_TILE_SIZE / 2f;
		coords[1] = lp.getY() - Perspective.LOCAL_TILE_SIZE / 2f;
	}

	GeneralPath getMultiLinesToDisplay()
	{
		return getLinesToDisplay(false);
	}

	GeneralPath getSpearLinesToDisplay()
	{
		return getLinesToDisplay(true);
	}

	private GeneralPath getLinesToDisplay(final boolean spearLines)
	{
		final Area area = spearLines ? SPEAR_MULTI_AREA : MULTI_AREA;

		final Rectangle sceneRect = new Rectangle(
			client.getBaseX() + 1, client.getBaseY() + 1,
			Constants.SCENE_SIZE - 2, Constants.SCENE_SIZE - 2);

		GeneralPath lines = new GeneralPath(area);
		lines = Geometry.clipPath(lines, sceneRect);
		lines = Geometry.splitIntoSegments(lines, 1);
		lines = Geometry.transformPath(lines, this::transformWorldToLocal);
		return lines;
	}
}