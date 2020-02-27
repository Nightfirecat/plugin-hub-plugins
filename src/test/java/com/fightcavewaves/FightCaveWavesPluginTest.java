package com.fightcavewaves;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FightCaveWavesPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FightCaveWavesPlugin.class);
		RuneLite.main(args);
	}
}
