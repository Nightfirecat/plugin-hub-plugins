/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
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
package at.nightfirec.wildernesslines;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.Varbits;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

class WildernessLinesOverlay extends Overlay
{
	private final WildernessLinesPlugin plugin;
	private final WildernessLinesConfig config;
	private final Client client;

	@Inject
	private WildernessLinesOverlay(WildernessLinesPlugin plugin, WildernessLinesConfig config, Client client)
	{
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final boolean inWilderness = client.getVarbitValue(Varbits.IN_WILDERNESS) == 1;
		if (!inWilderness && config.onlyShowInWilderness())
		{
			return null;
		}

		if (config.showSpearLines())
		{
			renderPath(graphics, plugin.getSpearLinesToDisplay(), config.spearLinesColor());
		}
		renderPath(graphics, plugin.getMultiLinesToDisplay(), config.multiLinesColor());

		if (config.show20Line())
		{
			renderPath(graphics, plugin.get20LineToDisplay(), config.twentyLineColor());
		}

		if (config.show30Line())
		{
			renderPath(graphics, plugin.get30LineToDisplay(), config.thirtyLineColor());
		}

		return null;
	}

	private void renderPath(Graphics2D graphics, GeneralPath path, Color color)
	{
		graphics.setColor(color);
		graphics.setStroke(new BasicStroke(1));

		path = Geometry.filterPath(path, (p1, p2) ->
			Perspective.localToCanvas(client, new LocalPoint((int)p1[0], (int)p1[1]), client.getPlane()) != null &&
			Perspective.localToCanvas(client, new LocalPoint((int)p2[0], (int)p2[1]), client.getPlane()) != null);
		path = Geometry.transformPath(path, coords ->
		{
			Point point = Perspective.localToCanvas(client, new LocalPoint((int)coords[0], (int)coords[1]), client.getPlane());
			coords[0] = point.getX();
			coords[1] = point.getY();
		});

		graphics.draw(path);
	}
}
