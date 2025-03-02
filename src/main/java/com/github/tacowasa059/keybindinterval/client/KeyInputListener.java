package com.github.tacowasa059.keybindinterval.client;

import com.github.tacowasa059.keybindinterval.KeybindInterval;
import com.github.tacowasa059.keybindinterval.common.YamlLoader;
import com.github.tacowasa059.keybindinterval.interfaces.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = KeybindInterval.MODID, value = Dist.CLIENT)
public class KeyInputListener {
    private static Map<String, Integer> IntervalTickCounts = new ConcurrentHashMap<>();
    private static Map<String, Integer> UsageTickCounts = new ConcurrentHashMap<>();

    public static void reset(){
        IntervalTickCounts = new ConcurrentHashMap<>();
        UsageTickCounts = new ConcurrentHashMap<>();
    }

    private static Integer getIntervalTick(String key){
        Map<String, Integer>map = YamlLoader.getOrDefault(key, new ConcurrentHashMap<>());
        if(map.get("interval") != null) return map.get("interval");
        return -1;
    }

    private static Integer getUsageTick(String key){
        Map<String, Integer>map = YamlLoader.getOrDefault(key, new ConcurrentHashMap<>());
        if(map.get("max_time") != null) return Math.max(1, map.get("max_time"));
        return 1;
    }

    @SubscribeEvent
    public static void timer(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            if(YamlLoader.get() == null) return;
            for(String keyName : YamlLoader.getKeys()){
                Integer MAX_TICKS = getIntervalTick(keyName);
                if(MAX_TICKS < 1) continue;

                Integer count = IntervalTickCounts.get(keyName);
                if(count==null) IntervalTickCounts.put(keyName, 0);
                else IntervalTickCounts.put(keyName, Math.min(count + 1, MAX_TICKS));

            }

            UsageTickCounts.replaceAll((k, v) -> Math.max(UsageTickCounts.get(k) - 1, 0)); // max( value - 1, 0)
        }
    }

    @SubscribeEvent
    public static void onKeyInput(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.player==null) return;



        KeyMapping[] keyMappings = Minecraft.getInstance().options.keyMappings;
        for (KeyMapping keyMapping : keyMappings) {
            String keyName = keyMapping.getName();
            if(!YamlLoader.contains(keyName)){
                YamlLoader.addData(keyName, "interval", -1);
                YamlLoader.addData(keyName, "max_time", -1);
            }

            if(mc.player.isCreative() ||mc.player.isSpectator()) continue;

            if (IntervalTickCounts.containsKey(keyName)) {
                boolean isKeyDown = keyMapping.isDown() || keyMapping.consumeClick();

                if (isKeyDown) {
                    Integer MAX_TICKS = getIntervalTick(keyName);

                    if(MAX_TICKS < 1) continue;

                    if (IntervalTickCounts.get(keyName) < MAX_TICKS) {
                        KeyMappingAccessor accessor = (KeyMappingAccessor)keyMapping;
                        accessor.keybindInterval$release();

                    } else {
                        // usage tickの追加
                        // 0になるまでは変更なし
                        // 0になったら、IntervalTickCounts.put(keyName, 0);
                        if(!UsageTickCounts.containsKey(keyName)){
                            Integer usageTick = getUsageTick(keyName);
                            UsageTickCounts.put(keyName, usageTick);
                        }
                    }
                }

                // UsageTickCountsからremove;
                if(UsageTickCounts.containsKey(keyName)){
                    if(UsageTickCounts.get(keyName)==0){
                        UsageTickCounts.remove(keyName);
                        IntervalTickCounts.put(keyName, 0);
                    }
                }
            }

        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if(mc.player.isCreative() ||mc.player.isSpectator()) return;

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = mc.font;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int numBars = IntervalTickCounts.size();
        if(numBars%2 == 1) numBars++;
        int halfBars = numBars/2;

        int spacing = 200;

        int maxBarWidth = (screenWidth - spacing) / (numBars + 1);
        int barWidth = Math.min(80, maxBarWidth - 10);
        int barHeight = 3;
        int startX = (screenWidth - spacing - (barWidth * numBars + 10 * (numBars - 1))) / 2;
        int barY = screenHeight - 28;

        int index = 0;
        for (String keyName : IntervalTickCounts.keySet()) {
            Integer MAX_TICKS = getIntervalTick(keyName);
            if(MAX_TICKS < 1) continue;

            float progress = (float) IntervalTickCounts.get(keyName) / MAX_TICKS;
            int filledWidth = (int) (barWidth * progress);
            int barX = startX + (barWidth + 10) * index;

            if(index >=halfBars){
                barX += spacing;
            }

            graphics.fill(barX-1, barY-1, barX + barWidth+1, barY + barHeight+1, 0xFF000000);
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF444444);


            if(UsageTickCounts.containsKey(keyName)){
                Integer value = UsageTickCounts.get(keyName);
                Integer max_value = getUsageTick(keyName);
                float usage_process = (float) value / max_value;
                filledWidth = (int) (barWidth * usage_process);
                graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFFFFA500);
            }else{
                graphics.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF00AAFF);
            }

            String text = Component.translatable(keyName).getString();
            int textX = barX + (barWidth - font.width(text)) / 2;
            int textY = barY - 10;
            graphics.drawString(font, text, textX, textY, 0xFFFFFF);

            index++;
        }
    }
}
