package com.mootrius.ironman;
import net.runelite.client.config.*;
@ConfigGroup("mootriusIronmanProgression")
public interface MootriusConfig extends Config {
 @ConfigItem(keyName="completed",name="Legacy completed steps",description="Existing progress; retained for migration",hidden=true)
 default String completed(){return "";}
 @ConfigItem(keyName="autoSkills",name="Auto-complete skill targets",description="Check guide steps when the required real skill level is reached",position=1)
 default boolean autoSkills(){return true;}
 @ConfigItem(keyName="autoQuests",name="Auto-complete completed quests",description="Check steps for quests reported finished by RuneLite",position=2)
 default boolean autoQuests(){return true;}
 @ConfigItem(keyName="showOverlay",name="Show current step overlay",description="Display your next unchecked step on screen",position=3)
 default boolean showOverlay(){return false;}
}
