package com.mootrius.ironman;
import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
public class MootriusOverlay extends OverlayPanel {
 private final MootriusPanel panel;
 private final MootriusConfig config;
 @Inject public MootriusOverlay(MootriusPanel panel,MootriusConfig config){this.panel=panel;this.config=config;setPosition(OverlayPosition.TOP_LEFT);}
 @Override public Dimension render(Graphics2D graphics){
  if(!config.showOverlay())return null;
  String step=panel.nextStep();
  if(step==null)return null;
  panelComponent.getChildren().add(LineComponent.builder().left("Mootrius Ironman").build());
  // Truncate to avoid stretching across the game view.
  panelComponent.getChildren().add(LineComponent.builder().left(step.length()>65?step.substring(0,62)+"...":step).build());
  return super.render(graphics);
 }
}
