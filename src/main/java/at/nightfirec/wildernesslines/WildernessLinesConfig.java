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
package at.nightfirec.wildernesslines;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup("wilderness-multi-lines")
public interface WildernessLinesConfig extends Config
{
	@ConfigItem(
		position = 0,
		keyName = "onlyShowInWilderness",
		name = "Only show in Wilderness",
		description = "Only show the lines if you are in the Wilderness"
	)
	default boolean onlyShowInWilderness()
	{
		return true;
	}

	@ConfigSection(
		name = "Multi Lines",
		description = "",
		position = 1
	)
	String multiLines = "multiLines";

	@ConfigItem(
		position = 1,
		keyName = "multiLinesColor",
		name = "Multi lines color",
		description = "Color of lines bordering multi-combat zones in the wilderness",
		section = multiLines
	)
	@Alpha
	default Color multiLinesColor()
	{
		return Color.RED;
	}

	@ConfigItem(
		position = 2,
		keyName = "showSpearLines",
		name = "Show spear lines",
		description = "Show the area in which you can potentially speared into a multi-combat zone",
		section = multiLines
	)
	default boolean showSpearLines()
	{
		return false;
	}

	@ConfigItem(
		position = 3,
		keyName = "spearLinesColor",
		name = "Spear lines color",
		description = "Color of lines bordering spear areas surrounding multi-combat zones in the wilderness",
		section = multiLines
	)
	@Alpha
	default Color spearLinesColor()
	{
		return Color.ORANGE;
	}

	@ConfigSection(
		name = "Level Lines",
		description = "",
		position = 2
	)
	String levelLines = "levelLines";

	//config for lvl10
	@ConfigItem(
			position = 1,
			keyName = "show10Line",
			name = "Show 10 line",
			description = "Show a line which separates 10 from 11 wilderness",
			section = levelLines
	)
	default boolean show10Line()
	{
		return false;
	}

	@ConfigItem(
			position = 2,
			keyName = "tenLineColor",
			name = "10 line color",
			description = "Color of the line separating 10 from 11 wilderness",
			section = levelLines
	)
	@Alpha
	default Color tenLineColor()
	{
		return Color.WHITE;
	}

	//config for lvl20
	@ConfigItem(
		position = 3,
		keyName = "show20Line",
		name = "Show 20 line",
		description = "Show a line which separates 20 from 21 wilderness",
		section = levelLines
	)
	default boolean show20Line()
	{
		return false;
	}

	@ConfigItem(
		position = 4,
		keyName = "twentyLineColor",
		name = "20 line color",
		description = "Color of the line separating 20 from 21 wilderness",
		section = levelLines
	)
	@Alpha
	default Color twentyLineColor()
	{
		return Color.YELLOW;
	}

	//config for lvl30
	@ConfigItem(
		position = 5,
		keyName = "show30Line",
		name = "Show 30 line",
		description = "Show a line which separates 30 from 31 wilderness",
		section = levelLines
	)
	default boolean show30Line()
	{
		return true;
	}

	@ConfigItem(
		position = 6,
		keyName = "thirtyLineColor",
		name = "30 line color",
		description = "Color of the line separating 30 from 31 wilderness",
		section = levelLines
	)
	@Alpha
	default Color thirtyLineColor()
	{
		return Color.CYAN;
	}

	//config for lvl40
	@ConfigItem(
			position = 7,
			keyName = "show40Line",
			name = "Show 40 line",
			description = "Show a line which separates 40 from 41 wilderness",
			section = levelLines
	)
	default boolean show40Line()
	{
		return false;
	}

	@ConfigItem(
			position = 8,
			keyName = "fortyLineColor",
			name = "40 line color",
			description = "Color of the line separating 40 from 41 wilderness",
			section = levelLines
	)
	@Alpha
	default Color fortyLineColor()
	{
		return Color.PINK;
	}

	//config for lvl50
	@ConfigItem(
			position = 9,
			keyName = "show50Line",
			name = "Show 50 line",
			description = "Show a line which separates 50 from 51 wilderness",
			section = levelLines
	)
	default boolean show50Line()
	{
		return false;
	}

	@ConfigItem(
			position = 10,
			keyName = "fiftyLineColor",
			name = "50 line color",
			description = "Color of the line separating 50 from 51 wilderness",
			section = levelLines
	)
	@Alpha
	default Color fiftyLineColor()
	{
		return Color.BLACK;
	}
}
