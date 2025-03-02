package com.github.tacowasa059.keybindinterval.server;

import com.github.tacowasa059.keybindinterval.KeybindInterval;
import com.github.tacowasa059.keybindinterval.common.YamlLoader;
import com.github.tacowasa059.keybindinterval.networks.NetworkHandler;
import com.github.tacowasa059.keybindinterval.networks.SyncSettingPacket;
import com.github.tacowasa059.keybindinterval.networks.SyncYamlPacket;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber(modid = KeybindInterval.MODID)
public class CommandRegistration {


    @SubscribeEvent
    public static void commandRegister(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("keybindinterval") // コマンド変更
                .requires(s -> s.hasPermission(2))
                .then(Commands.argument("targets", net.minecraft.commands.arguments.EntityArgument.players())
                        .then(Commands.argument("path", StringArgumentType.word()).suggests(ALL_KEYS_SUGGESTION)
                                .then(Commands.argument("category", StringArgumentType.word()).suggests(CATEGORY_SUGGESTION)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(-1))
                                                .executes(ctx -> lockSetting(ctx.getSource(),
                                                        net.minecraft.commands.arguments.EntityArgument.getPlayers(ctx, "targets"),
                                                        StringArgumentType.getString(ctx, "category"),
                                                        StringArgumentType.getString(ctx, "path"),
                                                        IntegerArgumentType.getInteger(ctx, "value"), false))
                                        )
                                )
                        )
                )
                // `locksettings config load`
                .then(Commands.literal("config")
                        .then(Commands.literal("reload")
                                .executes(ctx -> {
                                    YamlLoader.loadYaml(ctx.getSource().getServer());
                                    // 全プレイヤーにパケットを送信
                                    NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new SyncYamlPacket(YamlLoader.get()));
                                    ctx.getSource().sendSuccess(() -> Component.literal(ChatFormatting.GREEN+ "[keybindinterval]"
                                            +ChatFormatting.WHITE+"Configuration reloaded!"), true);
                                    return 1;
                                })
                        )

                        // `locksettings config get <key>`
                        .then(Commands.literal("get")
                                .then(Commands.argument("key", StringArgumentType.word()).suggests(ALL_KEYS_SUGGESTION)
                                        .executes(ctx -> {
                                            String key = StringArgumentType.getString(ctx, "key");
                                            String value = key + " : " + YamlLoader.getOrDefault(key, new HashMap<>()).toString();
                                            ctx.getSource().sendSuccess(() -> Component.literal(ChatFormatting.GREEN+ "[keybindinterval] " +
                                                    ChatFormatting.AQUA+ value), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("set")
                                .then(Commands.argument("path", StringArgumentType.word()).suggests(ALL_KEYS_SUGGESTION)
                                        .then(Commands.argument("category", StringArgumentType.word()).suggests(CATEGORY_SUGGESTION)
                                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                                        .executes(ctx -> lockSetting(ctx.getSource(), ctx.getSource().getServer().getPlayerList().getPlayers(),
                                                                StringArgumentType.getString(ctx, "category"),
                                                                StringArgumentType.getString(ctx, "path"),
                                                                IntegerArgumentType.getInteger(ctx, "value"), true))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("save")
                                .executes(ctx -> {
                                    YamlLoader.saveToYaml();
                                    ctx.getSource().sendSuccess(() -> Component.literal(ChatFormatting.GREEN+ "[keybindinterval] "
                                            +ChatFormatting.WHITE+"Configuration saved!"), true);
                                    return 1;
                                })
                        )
                )
        );
    }

    /**
     * 設定をロック/変更する
     */
    private static int lockSetting(CommandSourceStack src, Collection<ServerPlayer> targets, String category, String path, Integer value, boolean isGlobal) {
        // プレイヤーごとに変更用のpacketを送信する

        SyncSettingPacket packet = new SyncSettingPacket(path, category, value);
        for (ServerPlayer player : targets) {
            NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
        }
        src.sendSuccess(() -> Component.literal(ChatFormatting.GREEN+"[keybindinterval] "+"Changed setting for Players " + targets.size() +
                ": "+ ChatFormatting.AQUA + path + " " +category + ChatFormatting.GREEN+" -> " + ChatFormatting.AQUA + value), true);

        // サーバーデータも書き換え
        if(isGlobal){
            YamlLoader.addData(path, category, value);
        }
        // サーバー側の設定もする
        return targets.size();
    }

    /**
     * カテゴリのサジェスト ("unlock", "key", "value", "active")
     */
    private static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTION = (context, builder) ->
            net.minecraft.commands.SharedSuggestionProvider.suggest(Arrays.asList("interval", "max_time"), builder);

    private static final SuggestionProvider<CommandSourceStack> ALL_KEYS_SUGGESTION
            = ((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(getAllKeys(), builder));

    /**
     * processedData の全てのキーを取得
     */
    public static Set<String> getAllKeys() {
        Map<String, Map<String, Integer>> map = YamlLoader.get();
        if(map==null) return new HashSet<>();
        return map.keySet();
    }
}

