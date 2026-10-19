package com.mootrius.ironman;
import com.google.inject.Provides;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
        name = "Mootrius Ironman Guide",
        description = "An interactive Ironman progression guide based on Mootrius's OSRS Ironman Guide.",
        tags = {"ironman", "guide", "progression", "checklist"},
        internalName = "mootrius-ironman-guide"
)
public class MootriusPlugin extends Plugin {
 @Inject private ClientToolbar toolbar;
 @Inject private ConfigManager configManager;
 @Inject private MootriusConfig config;
 @Inject private Client client;
 @Inject private OverlayManager overlayManager;
 private NavigationButton navigationButton;
 private MootriusPanel panel;
 private MootriusOverlay overlay;
 private int ticks;
 @Provides MootriusConfig provideConfig(ConfigManager manager){return manager.getConfig(MootriusConfig.class);}
 @Override protected void startUp(){
  panel=new MootriusPanel(configManager,config);
  overlay=new MootriusOverlay(panel,config);
  overlayManager.add(overlay);
  BufferedImage icon=new BufferedImage(16,16,BufferedImage.TYPE_INT_ARGB);
  java.awt.Graphics2D g=icon.createGraphics();g.setColor(new java.awt.Color(231,181,64));g.fillRoundRect(1,1,14,14,3,3);g.setColor(java.awt.Color.BLACK);g.drawLine(8,3,8,13);g.dispose();
  navigationButton=NavigationButton.builder().tooltip("Mootrius Ironman Helper").icon(icon).priority(7).panel(panel).build();
  toolbar.addNavigation(navigationButton);
 }
 @Subscribe public void onGameTick(GameTick tick){
  if(client.getGameState()!=GameState.LOGGED_IN || client.getLocalPlayer()==null)return;
  String name=client.getLocalPlayer().getName();
  if(name==null)return;
  panel.setAccount(name);
  if(++ticks%10==0 && (config.autoSkills() || config.autoQuests()))panel.checkAutoProgress(client,config);
 }
 @Override protected void shutDown(){if(navigationButton!=null)toolbar.removeNavigation(navigationButton);if(overlay!=null)overlayManager.remove(overlay);}
}
