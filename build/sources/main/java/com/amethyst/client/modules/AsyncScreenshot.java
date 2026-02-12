package com.amethyst.client.modules;

import com.amethyst.client.Module.Category;
import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncScreenshot extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Последний сохранённый скрин
    private volatile File lastScreenshot = null;
    private volatile BufferedImage lastImage = null;

    private static final int SCREENSHOT_KEY = Keyboard.KEY_F2;

    // Маркеры для перехвата кликов
    public static final String MARKER_OPEN   = "§[SC:OPEN]";
    public static final String MARKER_COPY   = "§[SC:COPY]";
    public static final String MARKER_EXPORT = "§[SC:EXPORT]";

    public AsyncScreenshot() {
        super("AsyncScreenshot", "F2 screenshot with OPEN / COPY / EXPORT", 0, Category.MISC);
        this.setEnabled(true);
    }

    // ─── Перехват F2 ─────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (Keyboard.getEventKey() == SCREENSHOT_KEY && Keyboard.getEventKeyState()) {
            // Захватываем кадр прямо сейчас, в GL-потоке
            BufferedImage img = captureCurrentFrame();
            if (img == null) {
                sendChat("§c✗ §7Failed to capture screenshot!");
                return;
            }
            lastImage = img;
            // Сохраняем асинхронно, не тормозя игру
            executor.submit(() -> saveAndNotify(img));
        }
    }

    // ─── Захват кадра через OpenGL (надёжный способ) ──────────────────────────

    private BufferedImage captureCurrentFrame() {
        try {
            Framebuffer fb = mc.getFramebuffer();
            int width, height;

            if (fb != null && net.minecraft.client.renderer.OpenGlHelper.isFramebufferEnabled()) {
                width  = fb.framebufferWidth;
                height = fb.framebufferHeight;
                fb.bindFramebuffer(true);
            } else {
                width  = mc.displayWidth;
                height = mc.displayHeight;
            }

            ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
            GL11.glReadPixels(0, 0, width, height, GL12.GL_BGRA, GL11.GL_UNSIGNED_BYTE, buf);

            if (fb != null && net.minecraft.client.renderer.OpenGlHelper.isFramebufferEnabled()) {
                fb.unbindFramebuffer();
            }

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = new int[width * height];
            buf.asIntBuffer().get(pixels);

            // OpenGL читает снизу вверх — переворачиваем
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int srcIdx = (height - 1 - y) * width + x;
                    img.setRGB(x, y, pixels[srcIdx] | 0xFF000000);
                }
            }
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ─── Сохранение на диск ───────────────────────────────────────────────────

    private void saveAndNotify(BufferedImage img) {
        try {
            File dir = getScreenshotDir();
            String ts = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            File file = uniqueFile(dir, "screenshot_" + ts, ".png");
            ImageIO.write(img, "PNG", file);
            lastScreenshot = file;
            mc.addScheduledTask(() -> sendSuccessMessage(file));
        } catch (IOException e) {
            mc.addScheduledTask(() -> sendChat("§c✗ §7Save failed: §f" + e.getMessage()));
            e.printStackTrace();
        }
    }

    // ─── Сообщение в чат с кнопками ──────────────────────────────────────────

    private void sendSuccessMessage(File file) {
        // Основной текст
        ChatComponentText line = new ChatComponentText(
            "§8[§dAmethyst§8] §7Screenshot saved! "
        );

        // Кнопка [OPEN]
        ChatComponentText openBtn = new ChatComponentText("§8[§a§lOPEN§8]");
        openBtn.getChatStyle()
            .setChatClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/screenshot_open"
            ))
            .setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§aВідкрити скріншот")
            ));

        // Кнопка [COPY]
        ChatComponentText sep1 = new ChatComponentText(" §8│ ");
        ChatComponentText copyBtn = new ChatComponentText("§8[§b§lCOPY§8]");
        copyBtn.getChatStyle()
            .setChatClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/screenshot_copy"
            ))
            .setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§bСкопіювати зображення")
            ));

        // Кнопка [EXPORT]
        ChatComponentText sep2 = new ChatComponentText(" §8│ ");
        ChatComponentText exportBtn = new ChatComponentText("§8[§e§lEXPORT§8]");
        exportBtn.getChatStyle()
            .setChatClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/screenshot_export"
            ))
            .setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§eЗберегти як…")
            ));

        // Имя файла серым
        ChatComponentText fileName = new ChatComponentText(
            " §8(" + file.getName() + ")"
        );

        line.appendSibling(openBtn);
        line.appendSibling(sep1);
        line.appendSibling(copyBtn);
        line.appendSibling(sep2);
        line.appendSibling(exportBtn);
        line.appendSibling(fileName);

        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(line);
        }
    }

    // ─── Действия (вызываются из ScreenshotClickHandler) ─────────────────────

    /** Открывает последний скрин в системном просмотрщике */
    public void openScreenshot() {
        File f = lastScreenshot;
        if (f == null || !f.exists()) {
            sendChat("§c✗ §7No screenshot available.");
            return;
        }
        executor.submit(() -> {
            try {
                Desktop.getDesktop().open(f);
                mc.addScheduledTask(() ->
                    sendChat("§a✔ §7Opened: §f" + f.getName()));
            } catch (Exception e) {
                mc.addScheduledTask(() ->
                    sendChat("§c✗ §7Cannot open file: §f" + e.getMessage()));
            }
        });
    }

    /** Копирует изображение в буфер обмена */
    public void copyScreenshot() {
        BufferedImage img = lastImage;
        if (img == null) {
            // Пробуем прочитать с диска
            File f = lastScreenshot;
            if (f == null || !f.exists()) {
                sendChat("§c✗ §7No screenshot available.");
                return;
            }
            executor.submit(() -> {
                try {
                    BufferedImage loaded = ImageIO.read(f);
                    copyImageToClipboard(loaded);
                    mc.addScheduledTask(() ->
                        sendChat("§b✔ §7Image copied to clipboard!"));
                } catch (Exception e) {
                    mc.addScheduledTask(() ->
                        sendChat("§c✗ §7Copy failed: §f" + e.getMessage()));
                }
            });
        } else {
            executor.submit(() -> {
                try {
                    copyImageToClipboard(img);
                    mc.addScheduledTask(() ->
                        sendChat("§b✔ §7Image copied to clipboard!"));
                } catch (Exception e) {
                    mc.addScheduledTask(() ->
                        sendChat("§c✗ §7Copy failed: §f" + e.getMessage()));
                }
            });
        }
    }

    /** Открывает диалог «Сохранить как» */
    public void exportScreenshot() {
        File src = lastScreenshot;
        BufferedImage img = lastImage;

        if ((src == null || !src.exists()) && img == null) {
            sendChat("§c✗ §7No screenshot available.");
            return;
        }

        // Swing-диалог должен вызываться в EDT
        java.awt.EventQueue.invokeLater(() -> {
            try {
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            try {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                String defaultName = src != null ? src.getName() : "screenshot.png";
                chooser.setSelectedFile(new File(
                    System.getProperty("user.home") + "/Desktop/" + defaultName));
                chooser.setDialogTitle("Export Screenshot");
                chooser.setFileFilter(
                    new javax.swing.filechooser.FileNameExtensionFilter(
                        "PNG Images (*.png)", "png"));

                // Временный невидимый фрейм чтобы диалог вышел поверх
                javax.swing.JFrame frame = new javax.swing.JFrame();
                frame.setAlwaysOnTop(true);
                frame.setUndecorated(true);
                frame.setOpacity(0.0f);
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);

                int result = chooser.showSaveDialog(frame);
                frame.dispose();

                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    File dest = chooser.getSelectedFile();
                    if (!dest.getName().toLowerCase().endsWith(".png")) {
                        dest = new File(dest.getAbsolutePath() + ".png");
                    }
                    // Берём оригинальный файл или рендерим из памяти
                    BufferedImage toSave = img;
                    if (toSave == null) toSave = ImageIO.read(src);
                    ImageIO.write(toSave, "PNG", dest);

                    final File finalDest = dest;
                    mc.addScheduledTask(() ->
                        sendChat("§e✔ §7Exported to: §f" + finalDest.getAbsolutePath()));
                } else {
                    mc.addScheduledTask(() -> sendChat("§7Export cancelled."));
                }
            } catch (Exception e) {
                mc.addScheduledTask(() ->
                    sendChat("§c✗ §7Export failed: §f" + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private void copyImageToClipboard(BufferedImage img) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
            .setContents(new TransferableImage(img), null);
    }

    private File getScreenshotDir() {
        File dir = new File(mc.mcDataDir, "screenshots");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private File uniqueFile(File dir, String base, String ext) {
        File f = new File(dir, base + ext);
        int n = 1;
        while (f.exists()) f = new File(dir, base + "_" + (n++) + ext);
        return f;
    }

    private void sendChat(String msg) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
        }
    }

    // ─── Transferable image (для копирования в буфер) ─────────────────────────

    private static final class TransferableImage implements Transferable {
        private final BufferedImage image;
        TransferableImage(BufferedImage img) { this.image = img; }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ DataFlavor.imageFlavor };
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor f) {
            return DataFlavor.imageFlavor.equals(f);
        }

        @Override
        public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(f)) throw new UnsupportedFlavorException(f);
            return image;
        }
    }
}