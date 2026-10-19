package com.mootrius.ironman;
import com.google.gson.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.ColorScheme;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.regex.*;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.Quest;
import net.runelite.api.QuestState;

public class MootriusPanel extends PluginPanel {
    private final ConfigManager manager;
    private final List<Section> sections = new ArrayList<>();
    private final Set<String> completed = new HashSet<>();
    private final JComboBox<String> sectionSelector = new JComboBox<>();
    private final JTextField search = new JTextField();
    private final JLabel progress = new JLabel();
    private final JPanel content = new JPanel();
    private int selected = 0;
    private String account = "default";
    private static final Pattern LEVEL = Pattern.compile("(?i)\\b(?:until|to|reach|at least|getting|have|achieve)\\s+(?:level\\s+)?(\\d{1,2})\\s+(Attack|Strength|Defence|Defense|Ranged|Magic|Prayer|Thieving|Agility|Hunter|Crafting|Woodcutting|Firemaking|Fishing|Cooking|Farming|Herblore|Mining|Smithing|Slayer|Fletching|Construction|Runecraft|Sailing)\\b|\\b(Attack|Strength|Defence|Defense|Ranged|Magic|Prayer|Thieving|Agility|Hunter|Crafting|Woodcutting|Firemaking|Fishing|Cooking|Farming|Herblore|Mining|Smithing|Slayer|Fletching|Construction|Runecraft|Sailing)\\s+(?:to\\s+)?(?:level\\s+)?(\\d{1,2})\\b");
    private final JLabel accountLabel = new JLabel("Account: not logged in");
    static class Entry { String text, type; int page; }
    static class Section { String section; int page; List<Entry> entries; }
    public MootriusPanel(ConfigManager manager, MootriusConfig config) {
        super(false); this.manager=manager;
        setLayout(new BorderLayout(0,8)); setBackground(ColorScheme.DARK_GRAY_COLOR);
        try (InputStream stream=getClass().getResourceAsStream("/guide.json")) {
            if(stream==null) throw new IOException("guide.json missing");
            JsonArray arr=new JsonParser().parse(new InputStreamReader(stream,StandardCharsets.UTF_8)).getAsJsonArray();
            for(JsonElement element:arr){
                JsonObject obj=element.getAsJsonObject(); Section s=new Section();
                s.section=obj.get("section").getAsString(); s.page=obj.get("page").getAsInt(); s.entries=new ArrayList<>();
                for(JsonElement item:obj.getAsJsonArray("entries")){
                    JsonObject entry=item.getAsJsonObject(); Entry e=new Entry();
                    e.text=entry.get("text").getAsString(); e.type=entry.get("type").getAsString(); e.page=entry.get("page").getAsInt(); s.entries.add(e);
                }
                sections.add(s); sectionSelector.addItem(s.section+" (p."+s.page+")");
            }
        } catch(Exception ex) { add(new JLabel("Guide load error: "+ex.getMessage()),BorderLayout.CENTER); return; }
        if(config.completed()!=null && !config.completed().isEmpty()) completed.addAll(Arrays.asList(config.completed().split(",")));
        JPanel header=new JPanel(new GridLayout(0,1,0,5)); header.setOpaque(false);
        JLabel title=new JLabel("MOOTRIUS IRONMAN HELPER"); title.setForeground(new Color(231,181,64)); header.add(title);
        header.add(accountLabel); header.add(progress); header.add(sectionSelector); search.setToolTipText("Filter current section"); header.add(search);
        JButton source=new JButton("Open original guide"); source.addActionListener(e->{try{net.runelite.client.util.LinkBrowser.browse("https://oldschool.runescape.wiki/w/Guide:Mootrius_Ironman_Guide");}catch(Exception ignored){}});header.add(source);
        add(header,BorderLayout.NORTH);
        content.setLayout(new BoxLayout(content,BoxLayout.Y_AXIS)); content.setBackground(ColorScheme.DARK_GRAY_COLOR);
        JScrollPane scroll=new JScrollPane(content); scroll.setBorder(null); scroll.getVerticalScrollBar().setUnitIncrement(18); add(scroll,BorderLayout.CENTER);
        sectionSelector.addActionListener(e->{selected=sectionSelector.getSelectedIndex();render();scroll.getVerticalScrollBar().setValue(0);});
        search.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){render();}public void removeUpdate(javax.swing.event.DocumentEvent e){render();}public void changedUpdate(javax.swing.event.DocumentEvent e){render();}});
        render();
    }
    private void persist(){manager.setConfiguration("mootriusironman", "progress_"+account, String.join(",",completed));}
    public void setAccount(String name){
        String next=name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]","_");
        if(next.equals(account))return;
        SwingUtilities.invokeLater(()->{
            if(next.equals(account))return;
            account=next;
            completed.clear();
            String saved=manager.getConfiguration("mootriusironman","progress_"+account);
            if(saved!=null && !saved.isEmpty())completed.addAll(Arrays.asList(saved.split(",")));
            accountLabel.setText("Account: "+name);
            render();
        });
    }
    public String nextStep(){
        for(int i=0;i<sections.size();i++)for(int j=0;j<sections.get(i).entries.size();j++){
            Entry e=sections.get(i).entries.get(j);
            if("task".equals(e.type) && !completed.contains(i+"_"+j))return e.text;
        }
        return null;
    }
    public void checkAutoProgress(Client client,MootriusConfig settings){
        java.util.Set<String> detected=new java.util.HashSet<>();
        for(int i=0;i<sections.size();i++)for(int j=0;j<sections.get(i).entries.size();j++){
            Entry e=sections.get(i).entries.get(j);
            String id=i+"_"+j;
            if(!"task".equals(e.type) || completed.contains(id))continue;
            if(settings.autoSkills()){
                Matcher m=LEVEL.matcher(e.text);
                if(m.find() && !e.text.toLowerCase(Locale.ROOT).contains(" and ")){
                    try{
                        String skillName=m.group(2)!=null?m.group(2):m.group(3);
                        int level=Integer.parseInt(m.group(1)!=null?m.group(1):m.group(4));
                        if(skillName.equalsIgnoreCase("Defense"))skillName="Defence";
                        Skill skill=Skill.valueOf(skillName.toUpperCase(Locale.ROOT));
                        if(client.getRealSkillLevel(skill)>=level)detected.add(id);
                    }catch(IllegalArgumentException ignored){}
                }
            }
            if(settings.autoQuests()){
                String label=e.text.trim();
                if(label.toLowerCase(Locale.ROOT).startsWith("complete ")){
                    String questName=label.substring(9).replaceFirst("(?i)^the ","").replaceFirst("(?i)\\s+(?:quest|miniquest)\\b.*$","").replaceFirst("[.\\s]+$", "").trim();
                    for(Quest q:Quest.values()){
                        String qName=q.getName().replaceFirst("(?i)^the ","");
                        if(qName.equalsIgnoreCase(questName) && q.getState(client)==QuestState.FINISHED){detected.add(id);break;}
                    }
                }
            }
        }
        if(!detected.isEmpty())SwingUtilities.invokeLater(()->{if(completed.addAll(detected)){persist();render();}});
    }
    private void render(){
        if(sections.isEmpty() || selected<0)return;
        content.removeAll(); Section s=sections.get(selected); String filter=search.getText().trim().toLowerCase(Locale.ROOT);
        int total=0,done=0;
        for(int i=0;i<sections.size();i++)for(int j=0;j<sections.get(i).entries.size();j++)if(sections.get(i).entries.get(j).type.equals("task")){total++;if(completed.contains(i+"_"+j))done++;}
        progress.setText("Completed "+done+" / "+total+" checklist steps");
        for(int j=0;j<s.entries.size();j++){
            Entry entry=s.entries.get(j); if(!entry.text.toLowerCase(Locale.ROOT).contains(filter))continue;
            final String id=selected+"_"+j;
            if(entry.type.equals("task")){
                JCheckBox cb=new JCheckBox("<html><div style='width:205px'>"+html(entry.text)+"</div></html>",completed.contains(id));
                cb.setOpaque(false); cb.setForeground(Color.WHITE); cb.setAlignmentX(Component.LEFT_ALIGNMENT);
                cb.addActionListener(e->{if(cb.isSelected())completed.add(id);else completed.remove(id);persist();updateProgress();});
                content.add(cb);
            } else {
                JLabel note=new JLabel("<html><div style='width:210px;color:#b9b9b9'>"+html(entry.text)+"</div></html>");
                note.setAlignmentX(Component.LEFT_ALIGNMENT); note.setBorder(BorderFactory.createEmptyBorder(4,4,8,4));content.add(note);
            }
        }
        content.revalidate();content.repaint();
    }
    private void updateProgress(){int total=0,done=0;for(int i=0;i<sections.size();i++)for(int j=0;j<sections.get(i).entries.size();j++)if(sections.get(i).entries.get(j).type.equals("task")){total++;if(completed.contains(i+"_"+j))done++;}progress.setText("Completed "+done+" / "+total+" checklist steps");}
    private static String html(String s){return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;").replace("\"","&quot;");}
}
