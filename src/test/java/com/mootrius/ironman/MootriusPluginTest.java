package com.mootrius.ironman;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MootriusPluginTest {
 public static void main(String[] args) throws Exception {
  ExternalPluginManager.loadBuiltin(MootriusPlugin.class);
  RuneLite.main(args);
 }
}
