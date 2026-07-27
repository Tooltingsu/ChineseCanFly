package com.chinesecanfly;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * ChineseCanFly（中文者可飞）
 *
 * <p>当客户端语言为 简体中文 / 繁体中文（台湾、香港）/ 文言文 时，
 * 在所有单人存档中解锁飞行权限（生存、冒险等任意游戏模式，双击空格起飞）。
 *
 * <p>切换为其他语言后立即自动收回（创造 / 旁观模式自带的飞行不受影响）。
 * 仅在单人存档（集成服务端）生效，进入多人服务器时本模组不做任何事。
 */
public class ChineseCanFlyClient implements ClientModInitializer {

    /** 判定为“中文”的语言代码（全部小写），可按需增删。 */
    private static final Set<String> CHINESE_LANGUAGES = Set.of(
            "zh_cn", // 简体中文（中国大陆）
            "zh_tw", // 繁體中文（台灣）
            "zh_hk", // 繁體中文（香港特別行政區）
            "lzh"    // 文言文（华夏）
    );

    /** 记录由本模组授予飞行权限的玩家，避免撤销其他来源的飞行权限。 */
    private final Set<UUID> modGrantedFlyingPlayers = new HashSet<>();
    private IntegratedServer trackedServer;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
    }

    private void onEndClientTick(MinecraftClient client) {
        IntegratedServer server = client.getServer();
        if (server == null) {
            return; // 在主菜单或多人服务器中：不生效
        }

        String language = client.getLanguageManager().getLanguage().toLowerCase(Locale.ROOT);
        boolean chinese = CHINESE_LANGUAGES.contains(language);

        // 修改玩家能力必须在（集成）服务端线程上执行，
        // 由服务端授予并同步，生存模式飞行才不会被判定为非法移动。
        server.execute(() -> {
            if (trackedServer != server) {
                // 已切换到另一个单人存档；旧存档的记录不能影响新存档。
                modGrantedFlyingPlayers.clear();
                trackedServer = server;
            }

            if (server.isRemote()) {
                // 已对局域网开放即视为多人游戏：立即收回本模组此前授予的权限，
                // 且绝不向房主或加入的玩家授予任何权限。
                revokeModGrantedFlying(server);
                return;
            }

            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                PlayerAbilities abilities = player.getAbilities();
                if (chinese) {
                    if (!abilities.allowFlying) {
                        abilities.allowFlying = true;
                        modGrantedFlyingPlayers.add(player.getUuid());
                        player.sendAbilitiesUpdate();
                    }
                } else if (modGrantedFlyingPlayers.remove(player.getUuid())) {
                    // 语言已切换为非中文：只收回本模组授予的飞行权限。
                    if (!player.isCreative() && !player.isSpectator()) {
                        abilities.allowFlying = false;
                        abilities.flying = false;
                        player.sendAbilitiesUpdate();
                    }
                }
            }
        });
    }

    private void revokeModGrantedFlying(IntegratedServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!modGrantedFlyingPlayers.remove(player.getUuid())) {
                continue;
            }

            PlayerAbilities abilities = player.getAbilities();
            if (!player.isCreative() && !player.isSpectator()) {
                abilities.allowFlying = false;
                abilities.flying = false;
                player.sendAbilitiesUpdate();
            }
        }
        modGrantedFlyingPlayers.clear();
    }
}
