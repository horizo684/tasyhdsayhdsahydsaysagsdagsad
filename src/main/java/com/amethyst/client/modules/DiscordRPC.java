package com.amethyst.client.modules;

import com.amethyst.client.Module;
import com.amethyst.client.AmethystClient;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.IPCListener;
import com.jagrosh.discordipc.entities.RichPresence;
import com.jagrosh.discordipc.exceptions.NoDiscordClientException;
import net.minecraft.client.Minecraft;

import java.time.OffsetDateTime;

/**
 * Discord RPC модуль для AmethystClient
 * 
 * ВАЖНО: Замени APPLICATION_ID на свой!
 * 
 * Как получить свой ID:
 * 1. Открой https://discord.com/developers/applications
 * 2. Создай новое приложение "New Application"
 * 3. Скопируй "Application ID" из раздела "General Information"
 * 4. Вставь сюда вместо примера
 * 
 * Если хочешь использовать тестовый ID - оставь как есть,
 * но тогда иконки и название могут не совпадать
 */
public class DiscordRPC extends Module {
    
    private IPCClient client;
    private Minecraft mc = Minecraft.getMinecraft();
    
    // ═════════════════════════════════════════════════════════════════════
    // 🔧 НАСТРОЙ ЗДЕСЬ: Вставь свой Discord Application ID
    // ═════════════════════════════════════════════════════════════════════
    // 
    // Получить свой ID:
    // https://discord.com/developers/applications
    // 
    // Пример (ЗАМЕНИ НА СВОЙ):
    private static final long APPLICATION_ID = 1471057629802663968;
    
    // Если не хочешь создавать своё приложение, используй этот тестовый ID:
    // private static final long APPLICATION_ID = 1301656962292588544L; // Пример
    // 
    // НО лучше создать своё для правильных иконок!
    // ═════════════════════════════════════════════════════════════════════
    
    private long startTimestamp;
    private boolean connected = false;
    private int connectionAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 3;
    
    public DiscordRPC() {
        super("Discord RPC", "Shows your activity in Discord");
        this.setEnabled(false); // По умолчанию выключен
    }
    
    @Override
    public void onEnable() {
        if (connectionAttempts >= MAX_RECONNECT_ATTEMPTS) {
            System.err.println("[AmethystClient] Discord RPC: Maximum reconnection attempts reached.");
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "§c[Discord RPC] Не удалось подключиться после " + MAX_RECONNECT_ATTEMPTS + " попыток."
                ));
            }
            this.setEnabled(false);
            return;
        }
        
        try {
            client = new IPCClient(APPLICATION_ID);
            
            client.setListener(new IPCListener() {
                @Override
                public void onReady(IPCClient client) {
                    connected = true;
                    connectionAttempts = 0; // Сброс счётчика при успешном подключении
                    startTimestamp = System.currentTimeMillis();
                    updatePresence();
                    System.out.println("[AmethystClient] Discord RPC connected successfully!");
                    
                    if (mc.thePlayer != null) {
                        mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                            "§a[Discord RPC] Успешно подключено!"
                        ));
                    }
                }
                
                @Override
                public void onClose(IPCClient client, String reason) {
                    connected = false;
                    System.out.println("[AmethystClient] Discord RPC disconnected: " + reason);
                }
            });
            
            client.connect();
            connectionAttempts++;
            
        } catch (NoDiscordClientException e) {
            System.err.println("[AmethystClient] Discord not running!");
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "§c[Discord RPC] Discord не запущен! Открой Discord и попробуй снова."
                ));
            }
            this.setEnabled(false);
        } catch (Exception e) {
            System.err.println("[AmethystClient] Failed to connect to Discord RPC:");
            e.printStackTrace();
            
            if (mc.thePlayer != null) {
                mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                    "§c[Discord RPC] Ошибка подключения! Попытка " + connectionAttempts + "/" + MAX_RECONNECT_ATTEMPTS
                ));
            }
            
            if (connectionAttempts >= MAX_RECONNECT_ATTEMPTS) {
                this.setEnabled(false);
            }
        }
    }
    
    @Override
    public void onDisable() {
        if (client != null && connected) {
            try {
                client.close();
                connected = false;
                connectionAttempts = 0;
                System.out.println("[AmethystClient] Discord RPC disconnected.");
                
                if (mc.thePlayer != null) {
                    mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText(
                        "§e[Discord RPC] Отключено."
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Обновляет Rich Presence в Discord
     * Вызывается каждые 5 секунд из EventHandler
     */
    public void updatePresence() {
        if (client == null || !connected || mc.thePlayer == null) {
            return;
        }
        
        try {
            RichPresence.Builder builder = new RichPresence.Builder();
            
            // Время начала игры
            builder.setStartTimestamp(OffsetDateTime.now().minusSeconds((System.currentTimeMillis() - startTimestamp) / 1000));
            
            // ═══════════════════════════════════════════════════════════════
            // 🎨 КАСТОМИЗАЦИЯ: Меняй текст здесь!
            // ═══════════════════════════════════════════════════════════════
            
            // Большая иконка (загрузи в Discord Developer Portal с именем "amethyst_logo")
            builder.setLargeImage("amethyst_logo", "AmethystClient v" + AmethystClient.VERSION);
            
            // Маленькая иконка (загрузи с именем "minecraft_icon")
            builder.setSmallImage("minecraft_icon", "Minecraft 1.8.9");
            
            // Если на сервере - показываем IP
            if (mc.getCurrentServerData() != null) {
                String serverIP = mc.getCurrentServerData().serverIP;
                String serverName = mc.getCurrentServerData().serverName;
                
                builder.setDetails("🌐 Playing on server")
                       .setState(serverIP);
                
                // Можно добавить название сервера если есть
                if (serverName != null && !serverName.isEmpty() && !serverName.equals(serverIP)) {
                    builder.setState(serverName + " (" + serverIP + ")");
                }
            } else {
                // В одиночной игре
                builder.setDetails("⚡ Using AmethystClient")
                       .setState("🎮 In Singleplayer");
            }
            
            // Количество активных модулей (показывается как "Party")
            int enabledModules = AmethystClient.moduleManager.getEnabledModules().size();
            int totalModules = AmethystClient.moduleManager.getModules().size();
            
            // Формат: "8 of 15 modules active"
            builder.setParty("modules", enabledModules, totalModules);
            
            // Дополнительно: можно добавить кнопки (требует Application ID с verified status)
            // builder.addButton("Discord Server", "https://discord.gg/your_server");
            // builder.addButton("Download", "https://github.com/your_repo");
            
            // ═══════════════════════════════════════════════════════════════
            
            // Отправляем обновлённый статус
            client.sendRichPresence(builder.build());
            
        } catch (Exception e) {
            System.err.println("[AmethystClient] Failed to update Discord RPC:");
            e.printStackTrace();
            
            // Если ошибка критическая - отключаем модуль
            if (!client.getStatus().toString().equals("CONNECTED")) {
                connected = false;
                this.setEnabled(false);
            }
        }
    }
    
    /**
     * Проверяет соединение с Discord
     */
    public boolean isConnected() {
        return connected && client != null;
    }
    
    /**
     * Получить время работы RPC в секундах
     */
    public long getUptime() {
        if (!connected) return 0;
        return (System.currentTimeMillis() - startTimestamp) / 1000;
    }
    
    /**
     * Форсировать обновление (для тестирования)
     */
    public void forceUpdate() {
        if (connected) {
            updatePresence();
        }
    }
}