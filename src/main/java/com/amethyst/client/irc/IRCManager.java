package com.amethyst.client.irc;

import com.amethyst.client.AmethystClient;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IRC (In-game Rich Communication) Manager
 * 
 * Позволяет клиентам обмениваться данными через Minecraft plugin channels:
 * - Определение кто использует мод
 * - Обмен кастомными метками (custom labels)
 * - Синхронизация цветов
 * 
 * Использует канал: "amethyst:irc"
 */
public class IRCManager {
    
    private static final String CHANNEL = "amethyst:irc";
    private static final int PROTOCOL_VERSION = 1;
    
    private final Minecraft mc;
    
    // UUID -> IRCUser (все пользователи мода на сервере)
    private final Map<UUID, IRCUser> users = new ConcurrentHashMap<>();
    
    // Мои данные
    private String myCustomLabel = "AMETHYST USER";
    private int myColor = 0xFF9966FF; // Default purple
    
    // Cooldown для отправки пакетов (не спамим сервер)
    private long lastBroadcast = 0;
    private static final long BROADCAST_COOLDOWN = 5000; // 5 секунд
    
    public IRCManager(Minecraft mc) {
        this.mc = mc;
    }
    
    /**
     * Вызывается при подключении к серверу
     */
    @SubscribeEvent
    public void onClientConnectedToServer(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        System.out.println("[IRC] Connected to server");
        
        // Очищаем список пользователей
        users.clear();
        
        // Через 2 секунды отправляем announce (даём серверу время загрузиться)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                announcePresence();
            }
        }, 2000);
    }
    
    /**
     * Вызывается при отключении от сервера
     */
    @SubscribeEvent
    public void onClientDisconnectionFromServer(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        System.out.println("[IRC] Disconnected from server, clearing users");
        users.clear();
    }
    
    /**
     * Получение кастомных пакетов
     */
    @SubscribeEvent
    public void onClientCustomPayload(FMLNetworkEvent.ClientCustomPacketEvent event) {
        FMLProxyPacket packet = event.packet;
        
        if (!packet.channel().equals(CHANNEL)) {
            return;
        }
        
        System.out.println("[IRC] Received packet on channel: " + CHANNEL);
        
        try {
            ByteBuf payload = packet.payload();
            
            // Читаем тип пакета
            byte packetType = payload.readByte();
            
            System.out.println("[IRC] Packet type: 0x" + Integer.toHexString(packetType & 0xFF));
            
            switch (packetType) {
                case 0x01: // ANNOUNCE - другой клиент объявляет о себе
                    handleAnnounce(payload);
                    break;
                    
                case 0x02: // UPDATE - обновление данных клиента
                    handleUpdate(payload);
                    break;
                    
                case 0x03: // GOODBYE - клиент отключился
                    handleGoodbye(payload);
                    break;
            }
        } catch (Exception e) {
            System.err.println("[IRC] Error handling custom payload: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Обрабатывает announce от другого клиента
     */
    private void handleAnnounce(ByteBuf buf) {
        try {
            // Читаем данные
            int version = buf.readInt();
            if (version != PROTOCOL_VERSION) {
                System.out.println("[IRC] Incompatible protocol version: " + version);
                return;
            }
            
            // UUID игрока
            long mostSig = buf.readLong();
            long leastSig = buf.readLong();
            UUID uuid = new UUID(mostSig, leastSig);
            
            // Custom label
            int labelLen = buf.readInt();
            byte[] labelBytes = new byte[labelLen];
            buf.readBytes(labelBytes);
            String label = new String(labelBytes, StandardCharsets.UTF_8);
            
            // Color
            int color = buf.readInt();
            
            // Добавляем пользователя
            IRCUser user = new IRCUser(uuid, label, color);
            users.put(uuid, user);
            
            System.out.println("[IRC] User joined: " + label + " (" + uuid + ")");
            debugPrintUsers();
            
            // Отвечаем своим announce (но не чаще раз в 5 секунд)
            if (System.currentTimeMillis() - lastBroadcast > BROADCAST_COOLDOWN) {
                announcePresence();
            }
            
        } catch (Exception e) {
            System.err.println("[IRC] Error handling announce: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает update от другого клиента
     */
    private void handleUpdate(ByteBuf buf) {
        try {
            long mostSig = buf.readLong();
            long leastSig = buf.readLong();
            UUID uuid = new UUID(mostSig, leastSig);
            
            int labelLen = buf.readInt();
            byte[] labelBytes = new byte[labelLen];
            buf.readBytes(labelBytes);
            String label = new String(labelBytes, StandardCharsets.UTF_8);
            
            int color = buf.readInt();
            
            // Обновляем данные
            IRCUser user = users.get(uuid);
            if (user != null) {
                user.setCustomLabel(label);
                user.setColor(color);
                System.out.println("[IRC] User updated: " + label);
            }
            
        } catch (Exception e) {
            System.err.println("[IRC] Error handling update: " + e.getMessage());
        }
    }
    
    /**
     * Обрабатывает goodbye от другого клиента
     */
    private void handleGoodbye(ByteBuf buf) {
        try {
            long mostSig = buf.readLong();
            long leastSig = buf.readLong();
            UUID uuid = new UUID(mostSig, leastSig);
            
            IRCUser user = users.remove(uuid);
            if (user != null) {
                System.out.println("[IRC] User left: " + user.getCustomLabel());
            }
            
        } catch (Exception e) {
            System.err.println("[IRC] Error handling goodbye: " + e.getMessage());
        }
    }
    
    /**
     * Объявляет о своём присутствии на сервере
     */
    public void announcePresence() {
        if (mc.thePlayer == null || !mc.getNetHandler().getNetworkManager().isChannelOpen()) {
            return;
        }
        
        try {
            ByteBuf buf = Unpooled.buffer();
            
            // Тип пакета: ANNOUNCE
            buf.writeByte(0x01);
            
            // Версия протокола
            buf.writeInt(PROTOCOL_VERSION);
            
            // UUID игрока
            UUID uuid = mc.thePlayer.getUniqueID();
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
            
            // Custom label
            byte[] labelBytes = myCustomLabel.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(labelBytes.length);
            buf.writeBytes(labelBytes);
            
            // Color
            buf.writeInt(myColor);
            
            // Отправляем
            PacketBuffer packetBuf = new PacketBuffer(buf);
            C17PacketCustomPayload packet = new C17PacketCustomPayload(CHANNEL, packetBuf);
            mc.getNetHandler().addToSendQueue(packet);
            
            lastBroadcast = System.currentTimeMillis();
            System.out.println("[IRC] Announced presence: " + myCustomLabel);
            
        } catch (Exception e) {
            System.err.println("[IRC] Error announcing presence: " + e.getMessage());
        }
    }
    
    /**
     * Отправляет обновление своих данных
     */
    public void broadcastUpdate() {
        if (mc.thePlayer == null || !mc.getNetHandler().getNetworkManager().isChannelOpen()) {
            return;
        }
        
        try {
            ByteBuf buf = Unpooled.buffer();
            
            buf.writeByte(0x02); // UPDATE
            
            UUID uuid = mc.thePlayer.getUniqueID();
            buf.writeLong(uuid.getMostSignificantBits());
            buf.writeLong(uuid.getLeastSignificantBits());
            
            byte[] labelBytes = myCustomLabel.getBytes(StandardCharsets.UTF_8);
            buf.writeInt(labelBytes.length);
            buf.writeBytes(labelBytes);
            
            buf.writeInt(myColor);
            
            PacketBuffer packetBuf = new PacketBuffer(buf);
            C17PacketCustomPayload packet = new C17PacketCustomPayload(CHANNEL, packetBuf);
            mc.getNetHandler().addToSendQueue(packet);
            
            System.out.println("[IRC] Broadcasted update");
            
        } catch (Exception e) {
            System.err.println("[IRC] Error broadcasting update: " + e.getMessage());
        }
    }
    
    /**
     * Получает IRC данные игрока по UUID
     */
    public IRCUser getUser(UUID uuid) {
        return users.get(uuid);
    }
    
    /**
     * Проверяет использует ли игрок мод
     */
    public boolean isUserOnline(UUID uuid) {
        return users.containsKey(uuid);
    }
    
    /**
     * Получает всех пользователей мода
     */
    public Collection<IRCUser> getAllUsers() {
        return users.values();
    }
    
    /**
     * Отладочный метод - выводит всех IRC пользователей
     */
    public void debugPrintUsers() {
        System.out.println("[IRC Debug] Total IRC users: " + users.size());
        for (Map.Entry<UUID, IRCUser> entry : users.entrySet()) {
            System.out.println("[IRC Debug] - " + entry.getKey() + " -> " + entry.getValue().getCustomLabel());
        }
    }
    
    /**
     * Устанавливает свою кастомную метку
     */
    public void setMyCustomLabel(String label) {
        if (label == null || label.isEmpty()) {
            label = "AMETHYST USER";
        }
        
        // Ограничение длины
        if (label.length() > 32) {
            label = label.substring(0, 32);
        }
        
        this.myCustomLabel = label;
        broadcastUpdate();
        
        // Сохраняем в конфиг
        if (AmethystClient.config != null) {
            AmethystClient.config.set("irc.customLabel", label);
        }
    }
    
    /**
     * Устанавливает свой цвет
     */
    public void setMyColor(int color) {
        this.myColor = color;
        broadcastUpdate();
        
        // Сохраняем в конфиг
        if (AmethystClient.config != null) {
            AmethystClient.config.set("irc.color", color);
        }
    }
    
    /**
     * Загружает настройки из конфига
     */
    public void loadSettings() {
        if (AmethystClient.config != null) {
            myCustomLabel = AmethystClient.config.getString("irc.customLabel", "AMETHYST USER");
            myColor = AmethystClient.config.getInt("irc.color", 0xFF9966FF);
        }
    }
    
    public String getMyCustomLabel() {
        return myCustomLabel;
    }
    
    public int getMyColor() {
        return myColor;
    }
}