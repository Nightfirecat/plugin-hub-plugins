package com.virtuallevelups;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class VirtualLevelUpsPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(VirtualLevelUpsPlugin.class);
		RuneLite.main(args);
	}
}
