package com.amethyst.client.modules;

import com.amethyst.client.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncScreenshot extends Module {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Последний сохранённый скрин — на него ссылаются кнопки в чате
    private volatile File lastScreenshot = null;

    private static final int SCREENSHOT_KEY = Keyboard.KEY_F2;

    // Маркеры вшитые в hover-текст кнопок.
    // ScreenshotClickHandler читает их чтобы понять что нажато.
    public static final String MARKER_OPEN   = "§[SC:OPEN]";
    public static final String MARKER_COPY   = "§[SC:COPY]";
    public static final String MARKER_EXPORT = "§[SC:EXPORT]";

    public AsyncScreenshot() {
        super("AsyncScreenshot", "Screenshot with [OPEN] [COPY] [EXPORT] actions");
        this.setEnabled(true);
    }

    // ─── Захват по F2 ────────────────────────────────────────────────────────

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (Keyboard.getEventKey() == SCREENSHOT_KEY && Keyboard.getEventKeyState()) {
            takeScreenshotAsync();
        }
    }

    private void takeScreenshotAsync() {
        final BufferedImage screenshot = captureScreenshot();
        if (screenshot == null) {
            sendChat("§c✗ §7Failed to capture screenshot!");
            return;
        }

        executor.submit(() -> {
            try {
                File dir = getScreenshotDir();
                String ts = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
                File file = uniqueFile(dir, "screenshot_" + ts, ".png");
                ImageIO.write(screenshot, "PNG", file);
                lastScreenshot = file;
                mc.addScheduledTask(() -> sendSuccessMessage(file));
            } catch (IOException e) {
                mc.addScheduledTask(() -> sendChat("§c✗ §7Save failed: §f" + e.getMessage()));
                e.printStackTrace();
            }
        });
    }

    /** Захватывает текущий кадр через MC ScreenShotHelper */
    private BufferedImage captureScreenshot() {
        try {
            net.minecraft.client.shader.Framebuffer fb = mc.getFramebuffer();
            boolean fbEnabled = net.minecraft.client.renderer.OpenGlHelper.isFramebufferEnabled();
            int w = (fb != null && fbEnabled) ? fb.framebufferWidth  : mc.displayWidth;
            int h = (fb != null && fbEnabled) ? fb.framebufferHeight : mc.displayHeight;

            net.minecraft.util.ScreenShotHelper.saveScreenshot(
                    mc.mcDataDir, "__amethyst_tmp__.png", w, h, fb);

            File tmp = new File(mc.mcDataDir, "screenshots/__amethyst_tmp__.png");
            if (!tmp.exists()) return null;
            BufferedImage img = ImageIO.read(tmp);
            tmp.delete();
            return img;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ─── Сообщение в чат с кнопками ──────────────────────────────────────────

    private void sendSuccessMessage(File file) {
        ChatComponentText line = new ChatComponentText("§8[§dAmethyst§8] §7Screenshot! ");

        // [OPEN] — маркер в hover-тексте, перехватывается ScreenshotClickHandler
        ChatComponentText openBtn = new ChatComponentText("§8[§a§lOPEN§8]");
        openBtn.getChatStyle().setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§aОткрыть в просмотрщике\n§0" + MARKER_OPEN)
        ));

        ChatComponentText sep1 = new ChatComponentText(" §8│ ");

        // [COPY]
        ChatComponentText copyBtn = new ChatComponentText("§8[§b§lCOPY§8]");
        copyBtn.getChatStyle().setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§bСкопировать изображение\n§0" + MARKER_COPY)
        ));

        ChatComponentText sep2 = new ChatComponentText(" §8│ ");

        // [EXPORT]
        ChatComponentText exportBtn = new ChatComponentText("§8[§e§lEXPORT§8]");
        exportBtn.getChatStyle().setChatHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_TEXT,
                new ChatComponentText("§eСохранить как…\n§0" + MARKER_EXPORT)
        ));

        line.appendSibling(openBtn);
        line.appendSibling(sep1);
        line.appendSibling(copyBtn);
        line.appendSibling(sep2);
        line.appendSibling(exportBtn);

        if (mc.thePlayer != null) mc.thePlayer.addChatMessage(line);
    }

    // ─── Действия (вызываются из ScreenshotClickHandler) ─────────────────────

    public void openScreenshot() {
        File f = lastScreenshot;
        if (f == null || !f.exists()) { sendChat("§c✗ §7No screenshot available."); return; }
        executor.submit(() -> {
            try {
                Desktop.getDesktop().open(f);
                mc.addScheduledTask(() -> sendChat("§a✔ §7Opened: §f" + f.getName()));
            } catch (Exception e) {
                mc.addScheduledTask(() -> sendChat("§c✗ §7Cannot open: §f" + e.getMessage()));
            }
        });
    }

    public void copyScreenshot() {
        File f = lastScreenshot;
        if (f == null || !f.exists()) { sendChat("§c✗ §7No screenshot available."); return; }
        executor.submit(() -> {
            try {
                BufferedImage img = ImageIO.read(f);
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new TransferableImage(img), null);
                mc.addScheduledTask(() -> sendChat("§b✔ §7Image copied to clipboard!"));
            } catch (Exception e) {
                mc.addScheduledTask(() -> sendChat("§c✗ §7Copy failed: §f" + e.getMessage()));
            }
        });
    }

    public void exportScreenshot() {
        File src = lastScreenshot;
        if (src == null || !src.exists()) { sendChat("§c✗ §7No screenshot available."); return; }
        java.awt.EventQueue.invokeLater(() -> {
            try {
                javax.swing.JFileChooser chooser = new javax.swing.JFileChooser();
                chooser.setSelectedFile(new File(
                        System.getProperty("user.home") + "/Desktop/" + src.getName()));
                chooser.setDialogTitle("Export Screenshot");
                chooser.setFileFilter(
                        new javax.swing.filechooser.FileNameExtensionFilter("PNG Images", "png"));

                javax.swing.JFrame frame = new javax.swing.JFrame();
                frame.setAlwaysOnTop(true);
                frame.setUndecorated(true);
                frame.setVisible(true);
                frame.setLocationRelativeTo(null);

                int result = chooser.showSaveDialog(frame);
                frame.dispose();

                if (result == javax.swing.JFileChooser.APPROVE_OPTION) {
                    File dest = chooser.getSelectedFile();
                    if (!dest.getName().toLowerCase().endsWith(".png"))
                        dest = new File(dest.getAbsolutePath() + ".png");
                    ImageIO.write(ImageIO.read(src), "PNG", dest);
                    final File finalDest = dest;
                    mc.addScheduledTask(() ->
                            sendChat("§e✔ §7Exported to: §f" + finalDest.getAbsolutePath()));
                } else {
                    mc.addScheduledTask(() -> sendChat("§7Export cancelled."));
                }
            } catch (Exception e) {
                mc.addScheduledTask(() -> sendChat("§c✗ §7Export failed: §f" + e.getMessage()));
            }
        });
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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
        if (mc.thePlayer != null)
            mc.thePlayer.addChatMessage(new ChatComponentText(msg));
    }

    // ─── Transferable image ───────────────────────────────────────────────────

    private static class TransferableImage implements Transferable {
        private final BufferedImage image;
        TransferableImage(BufferedImage img) { this.image = img; }

        @Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[]{ DataFlavor.imageFlavor }; }
        @Override public boolean isDataFlavorSupported(DataFlavor f) { return DataFlavor.imageFlavor.equals(f); }
        @Override public Object getTransferData(DataFlavor f) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(f)) throw new UnsupportedFlavorException(f);
            return image;
        }
    }
}