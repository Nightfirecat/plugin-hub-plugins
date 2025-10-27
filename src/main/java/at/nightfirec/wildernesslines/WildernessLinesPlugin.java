/*
 * Copyright (c) 2018, Woox <https://github.com/wooxsolo>
 * Copyright (c) 2021, Jordan <nightfirecat@protonmail.com>
 * Copyright (c) 2024, tsbreuer <tsbreuer@gmail.com>
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
package at.nightfirec.wildernesslines;

import com.google.common.collect.ImmutableList;
import com.google.inject.Provides;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.Perspective;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WorldViewLoaded;
import net.runelite.api.gameval.VarbitID;
import net.runelite.api.geometry.Geometry;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.LinkBrowser;

@PluginDescriptor(
	name = "Wilderness Lines",
	description = "Show wilderness multicombat areas, the dragon spear range to those areas, and level 20 and 30 lines.",
	tags = {"dragon spear", "multicombat", "wildy", "20", "30", "wilderness level"}
)
public class WildernessLinesPlugin extends Plugin
{
	private static final Area WILDERNESS = new Area(new Rectangle(2941, 3522, 528, 471));
	private static final List<Rectangle> WILDERNESS_MULTI_AREAS = ImmutableList.of(
		new Rectangle(3008, 3600, 64, 112), // Dark warrior's palace
		new Rectangle(3072, 3654, 1, 2), // Two tiles next to southern rev caves entrance which used to be a BH "singles" lure spot
		new Rectangle(2946, 3816, 14, 16), // Chaos altar
		new Rectangle(2984, 3912, 24, 16), // Balance crossing to wilderness agility course
		new Rectangle(3008, 3856, 40, 48), // North of kbd entrance
		new Rectangle(3021, 3855, 2, 1), // Two tiles NE of kbd entrance cage
		new Rectangle(3048, 3896, 24, 8), // North of rune rocks
		new Rectangle(3072, 3880, 64, 24), // North of lava maze
		new Rectangle(3112, 3872, 24, 8), // Northeast of lava maze
		new Rectangle(3136, 3840, 256, 64), // Northeast f2p wilderness
		new Rectangle(3200, 3904, 192, 64), // Northeast p2p wilderness
		new Rectangle(3152, 3752, 176, 88), // North-mid east f2p wilderness
		new Rectangle(3192, 3525, 136, 227), // East f2p wilderness
		new Rectangle(3328, 3525, 17, 8), // Small east f2p wilderness strip NE of lumberyard
		new Rectangle(3191, 3689, 1, 1), // One silly tile that used to be a BH "singles" lure spot
		new Rectangle(3136, 3525, 56, 59), // Wilderness north of Grand Exchange
		new Rectangle(3152, 3584, 40, 36), // SE of Ferox 1
		new Rectangle(3146, 3598, 6, 22), // SE of Ferox 2
		new Rectangle(3147, 3596, 5, 2), // SE of Ferox 2 extension 1
		new Rectangle(3149, 3595, 3, 1), // SE of Ferox 2 extension 2
		new Rectangle(3150, 3594, 2, 1), // SE of Ferox 2 extension 3
		new Rectangle(3151, 3593, 1, 1), // SE of Ferox 2 extension 4
		new Rectangle(3152, 3620, 10, 6), // SE of Ferox 3
		new Rectangle(3187, 3620, 5, 28), // East of Ferox 1
		new Rectangle(3179, 3636, 8, 4), // East of Ferox 2, south of bridge
		new Rectangle(3176, 3640, 11, 8), // East of Ferox 2, bridge and north of bridge
		new Rectangle(3174, 3647, 2, 1), // Two dumb tiles north of bridge east of Ferox
		new Rectangle(3264, 10048, 192, 320), // Slayer dungeon, Vet'ion, Venenatis, Callisto, and escape caves
		new Rectangle(3218, 10330, 31, 24), // Scorpia's cave
		new Rectangle(3008, 10112, 64, 64) // Wilderness God Wars Dungeon
	);
	private static final int SPEAR_RANGE = 5;
	private static final Rectangle AGILITY_COURSE_BRIDGE = new Rectangle(2998, 3917, 1, 14);
	private static final Rectangle NORTH_LAVA_MAZE_SHORTCUT = new Rectangle(3092, 3880, 1, 1);
	private static final Line2D[] TWENTY_LINES = {
		// overworld
		new Line2D.Float(2946, 3680, 3384, 3680),

		// rev caves
		new Line2D.Float(3202, 10080, 3205, 10080),
		new Line2D.Float(3216, 10080, 3224, 10080),
		new Line2D.Float(3228, 10080, 3230, 10080),
		new Line2D.Float(3234, 10080, 3245, 10080),

		// wilderness slayer caves
		new Line2D.Float(3335, 10080, 3344, 10080),
		new Line2D.Float(3349, 10080, 3367, 10080),
		new Line2D.Float(3381, 10080, 3385, 10080),
		new Line2D.Float(3394, 10080, 3397, 10080),
		new Line2D.Float(3410, 10080, 3416, 10080),
		new Line2D.Float(3436, 10080, 3449, 10080),
	};
	private static final Line2D[] THIRTY_LINES = {
		// overworld
		new Line2D.Float(2946, 3760, 3375, 3760),

		// rev caves
		new Line2D.Float(3164, 10160, 3185, 10160),
		new Line2D.Float(3194, 10160, 3221, 10160),
		new Line2D.Float(3235, 10160, 3255, 10160),

		// wilderness slayer caves
		new Line2D.Float(3333, 10160, 3349, 10160),
		new Line2D.Float(3356, 10160, 3368, 10160),
		new Line2D.Float(3421, 10160, 3428, 10160),
	};

	private static final Area MULTI_AREA = new Area();
	private static final Area SPEAR_MULTI_AREA = new Area();
	private static final Area FORCE_MOVE_IGNORE_AREA = new Area();

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

		FORCE_MOVE_IGNORE_AREA.add(new Area(AGILITY_COURSE_BRIDGE));
		FORCE_MOVE_IGNORE_AREA.add(new Area(NORTH_LAVA_MAZE_SHORTCUT));
	}

	private static final String GITHUB_REPO = "https://github.com/nightfirecat/plugin-hub-plugins";

	@Getter(AccessLevel.PACKAGE)
	private GeneralPath drawPathsMulti = new GeneralPath();
	@Getter(AccessLevel.PACKAGE)
	private GeneralPath drawPathsSpear = new GeneralPath();
	@Getter(AccessLevel.PACKAGE)
	private GeneralPath drawPaths20Lines = new GeneralPath();
	@Getter(AccessLevel.PACKAGE)
	private GeneralPath drawPaths30Lines = new GeneralPath();

	private final Set<WorldPoint> multiAreaMismatchedPoints = new HashSet<>();
	private WorldPoint wpLastTick;
	private boolean alertingMultiAreaMismatch;

	@Inject
	private WildernessLinesOverlay overlay;


	@Inject
	private WildernessLinesConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ScheduledExecutorService executor;

	@Inject
	private Client client;

	@Provides
	WildernessLinesConfig getConfig(ConfigManager configManager)
	{
		return configManager.getConfig(WildernessLinesConfig.class);
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

		drawPathsMulti.reset();
		drawPathsSpear.reset();
		drawPaths20Lines.reset();
		drawPaths30Lines.reset();
		multiAreaMismatchedPoints.clear();
		alertingMultiAreaMismatch = false;
	}

	@Subscribe
	private void onWorldViewLoaded(WorldViewLoaded worldViewLoaded)
	{
		if (worldViewLoaded.getWorldView() == client.getTopLevelWorldView())
		{
			executor.execute(() ->
			{
				drawPathsMulti = getMultiLinesToDisplay();
				drawPathsSpear = getSpearLinesToDisplay();
				drawPaths20Lines = get20LineToDisplay();
				drawPaths30Lines = get30LineToDisplay();

			});
		}
	}

	@Subscribe
	private void onGameTick(GameTick e)
	{
		if (client.getVarbitValue(VarbitID.INSIDE_WILDERNESS) != 0)
		{
			final WorldPoint currentWp = client.getLocalPlayer().getWorldLocation();

			if (wpLastTick != null
				// Some teleports, such as the Royal seed pod, have entrance animations which preserve var values until
				// the animation completes. Due to that, it is most practical to avoid tracking movement over more than
				// 2 tiles at once, as it is a teleport and not valid for consideration. This also should remove a class
				// of bugs where you could teleport from low wilderness to another area in the wilderness with a
				// change of multi-combat status.
				&& currentWp.distanceTo(wpLastTick) <= 2)
			{
				final boolean inMultiCombat = client.getVarbitValue(VarbitID.MULTIWAY_INDICATOR) == 1;
				final Point location = new Point(wpLastTick.getX(), wpLastTick.getY());

				final boolean areaMismatch = (inMultiCombat ^ MULTI_AREA.contains(location))
					// When crossing several force-move areas such as the wilderness agility course bridge, north lava
					// maze shortcut, etc., the multi-combat indicator does not change until you've either fallen off
					// (which typically moves you more than 2 tiles, therefore is guarded above) or reached the other
					// side. Hence, do not warn for such areas.
					&& !FORCE_MOVE_IGNORE_AREA.contains(location);
				if (areaMismatch)
				{
					multiAreaMismatchedPoints.add(wpLastTick);
				}

				final boolean shouldAlert = areaMismatch && config.multiAreaMismatchAlert();
				if (shouldAlert && !alertingMultiAreaMismatch)
				{
					client.addChatMessage(ChatMessageType.PUBLICCHAT, "Wilderness Lines", ColorUtil.prependColorTag("Encountered unexpected multi-combat areas", Color.RED), "");
					client.addChatMessage(ChatMessageType.PUBLICCHAT, "Wilderness Lines", "Please type '::reportUnexpectedAreas' to report this bug to " + GITHUB_REPO, "");
				}

				alertingMultiAreaMismatch = shouldAlert;
			}

			wpLastTick = currentWp;
		}
		else
		{
			// Tracking wp at all times could lead to false positives when teleporting in or out of wildy.
			wpLastTick = null;
		}
	}

	@Subscribe
	private void onCommandExecuted(CommandExecuted e)
	{
		if (!e.getCommand().equalsIgnoreCase("reportUnexpectedAreas"))
		{
			return;
		}

		if (multiAreaMismatchedPoints.isEmpty())
		{
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "Wilderness Lines", "No unexpected areas cached, opening issue report form", "");
			LinkBrowser.browse(GITHUB_REPO + "/issues/new?labels=plugin:%20wilderness-lines");
			return;
		}

		// Check size; copy to clipboard and open not-prepopulated form if length > 5k
		final StringBuilder pointsListBuilder = new StringBuilder();
		multiAreaMismatchedPoints.forEach((point) -> pointsListBuilder.append("* ").append(point.getX()).append(", ").append(point.getY()).append("%0A"));
		final String pointsList = pointsListBuilder.toString();
		final String issueList;
		if (pointsList.length() > 5000)
		{
			final StringSelection stringSelection = new StringSelection(pointsList.replaceAll("%0A", "\n"));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(stringSelection, null);
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "Wilderness Lines", "Too many unexpected areas to pre-populate form, please paste from clipboard", "");
			issueList = "<!--%20PASTE%20HERE%20-->%0A";
		}
		else
		{
			client.addChatMessage(ChatMessageType.PUBLICCHAT, "Wilderness Lines", "Pre-populating and opening issue report form", "");
			issueList = pointsList;
		}

		LinkBrowser.browse(GITHUB_REPO + "/issues/new?labels=plugin:%20wilderness-lines&body=The%20plugin%20is%20not%20correct%20for%20the%20following%20areas:%0A%0A" + issueList);
	}

	private void transformWorldToLocal(float[] coords)
	{
		final LocalPoint lp = LocalPoint.fromWorld(client.getTopLevelWorldView(), (int)coords[0], (int)coords[1]);
		coords[0] = lp.getX() - Perspective.LOCAL_TILE_SIZE / 2f;
		coords[1] = lp.getY() - Perspective.LOCAL_TILE_SIZE / 2f;
	}

	private GeneralPath getMultiLinesToDisplay()
	{
		return getLinesToDisplay(filterUndergroundAreas(MULTI_AREA));
	}

	private GeneralPath getSpearLinesToDisplay()
	{
		return getLinesToDisplay(filterUndergroundAreas(SPEAR_MULTI_AREA));
	}

	private GeneralPath get20LineToDisplay()
	{
		return getLinesToDisplay(TWENTY_LINES);
	}

	private GeneralPath get30LineToDisplay()
	{
		return getLinesToDisplay(THIRTY_LINES);
	}

	private GeneralPath getLinesToDisplay(final Shape... shapes)
	{
		final WorldView topLevelWorldView = client.getTopLevelWorldView();
		final Rectangle sceneRect = new Rectangle(
			topLevelWorldView.getBaseX() + 1, topLevelWorldView.getBaseY() + 1,
			Constants.SCENE_SIZE - 2, Constants.SCENE_SIZE - 2);

		final GeneralPath paths = new GeneralPath();
		for (final Shape shape : shapes)
		{
			GeneralPath lines = new GeneralPath(shape);
			lines = Geometry.clipPath(lines, sceneRect);
			lines = Geometry.splitIntoSegments(lines, 1);
			lines = Geometry.transformPath(lines, this::transformWorldToLocal);
			paths.append(lines, false);
		}
		return paths;
	}

	/**
	 * Subtracts all area appearing in the underground (i.e. above {@code Constants.OVERWORLD_MAX_Y}) from the given
	 * area.
	 *
	 * @param area An {@link Area}, such as that representing multi-combat zones.
	 * @return A new {@link Area} with underground geometry omitted.
	 */
	private static Area filterUndergroundAreas(final Area area)
	{
		final Area newArea = (Area) area.clone();
		newArea.intersect(WILDERNESS);
		return newArea;
	}
}
