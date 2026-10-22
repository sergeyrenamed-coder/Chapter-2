package com.rnmd.cursedquest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * All horror visuals are generated from GuiGraphics primitives. No image files,
 * third-party textures, shaders or client assets are required.
 */
public final class HorrorScreen extends Screen {
    private final int type;
    private int age;

    public HorrorScreen(int type) {
        super(Component.literal("Cursed Quest: visual incident"));
        this.type = Math.floorMod(type, 12);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        int duration = switch (type) {
            case 0 -> 42;
            case 1 -> 78;
            case 2 -> 62;
            case 3 -> 96;
            case 4 -> 88;
            case 5 -> 64;
            case 6 -> 48;
            case 7 -> 38;
            case 8 -> 92;
            case 9 -> 84;
            case 10 -> 74;
            default -> 62;
        };
        if (age >= duration) Minecraft.getInstance().setScreen(null);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int w = this.width;
        int h = this.height;
        switch (type) {
            case 0 -> renderRedFace(graphics, w, h);
            case 1 -> renderAnalogStatic(graphics, w, h);
            case 2 -> renderDrownedVision(graphics, w, h);
            case 3 -> renderFakeCrash(graphics, w, h);
            case 4 -> renderFakeDisconnect(graphics, w, h);
            case 5 -> renderPeripheralWatcher(graphics, w, h);
            case 6 -> renderSignalInversion(graphics, w, h);
            case 7 -> renderEyes(graphics, w, h);
            case 8 -> renderVhsFeed(graphics, w, h);
            case 9 -> renderFakeBlueScreen(graphics, w, h);
            case 10 -> renderWhisper(graphics, w, h);
            default -> renderTextCorruption(graphics, w, h);
        }
    }

    private void renderRedFace(GuiGraphics g, int w, int h) {
        int bg = age % 5 < 2 ? 0xE8BD0000 : 0xEF170000;
        g.fill(0, 0, w, h, bg);
        drawFace(g, w / 2, h / 2, Math.min(w / 2, h / 2) * (0.68F + (age % 4) * 0.025F), 0xFF070203, 0xFFFFE8E8, 0xFF870000);
        for (int i = 0; i < 10; i++) {
            int yy = Math.floorMod(age * 19 + i * 47, Math.max(1, h));
            g.fill(0, yy, w, yy + 2 + (i % 3), i % 2 == 0 ? 0x66FFFFFF : 0x66FF0000);
        }
        centeredScaled(g, "ТЫ НЕ ДОЛЖЕН БЫЛ ЭТО ВИДЕТЬ", w / 2, Math.max(16, h / 12), 0xFFFFFFFF, 1.45F);
        centeredScaled(g, "СМОТРИ НА МЕНЯ", w / 2, h - Math.max(45, h / 9), 0xFFFF3333, 1.8F);
    }

    private void renderAnalogStatic(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF07090B);
        for (int y = 0; y < h; y += 4) {
            int shade = ((y / 4 + age) % 3 == 0) ? 0x55777777 : 0x22222222;
            g.fill(0, y, w, y + 2, shade);
        }
        for (int i = 0; i < 95; i++) {
            int x = Math.floorMod(i * 97 + age * (i % 7 + 3), Math.max(1, w));
            int y = Math.floorMod(i * 61 + age * (i % 5 + 2), Math.max(1, h));
            int ww = 2 + (i % 17);
            g.fill(x, y, Math.min(w, x + ww), Math.min(h, y + 1 + i % 4), (i % 2 == 0) ? 0x99FFFFFF : 0x77770000);
        }
        int cx = w / 2 + (age % 7 - 3) * 2;
        int cy = h / 2;
        g.fill(cx - w / 10, cy - h / 3, cx + w / 10, cy + h / 4, 0xFF010101);
        g.fill(cx - w / 6, cy + h / 10, cx + w / 6, cy + h / 2, 0xFF010101);
        g.fill(cx - w / 18, cy - h / 6, cx - w / 35, cy - h / 9, 0xFFFFFFFF);
        g.fill(cx + w / 35, cy - h / 6, cx + w / 18, cy - h / 9, 0xFFFFFFFF);
        if (age > 40) drawFace(g, cx, cy, Math.min(w / 2, h / 2) * 0.74F, 0xFF050303, 0xFFFFFFFF, 0xFF7A0000);
        g.drawString(this.font, "CH 04 / NO SIGNAL", 14, 14, 0xFFDDDDDD, false);
        g.drawString(this.font, "REC  ●", 14, 30, 0xFFFF3333, false);
    }

    private void renderDrownedVision(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF001D25);
        for (int y = 0; y < h; y += 18) {
            int wobble = (int) (Math.sin((age + y) * 0.09) * 18);
            g.fill(0, y, w, y + 5, 0x2229B9C9);
            g.fill(Math.max(0, wobble), y + 6, w, y + 8, 0x2214AAB9);
        }
        for (int i = 0; i < 24; i++) {
            int x = Math.floorMod(i * 73 + age * (i % 3 + 1), Math.max(1, w));
            int y = Math.floorMod(h - i * 31 - age * 2, Math.max(1, h));
            g.fill(x, y, x + 4 + i % 8, y + 4 + i % 7, 0x667FEFF7);
        }
        int cx = w / 2;
        int cy = h / 2;
        g.fill(cx - w / 8, cy - h / 3, cx + w / 8, cy + h / 4, 0xFF00090C);
        g.fill(cx - w / 10, cy + h / 8, cx + w / 10, cy + h / 2, 0xFF00090C);
        g.fill(cx - w / 15, cy - h / 8, cx - w / 40, cy - h / 13, 0xFFE9FCFF);
        g.fill(cx + w / 40, cy - h / 8, cx + w / 15, cy - h / 13, 0xFFE9FCFF);
        centeredScaled(g, "НЕ ХВАТАЕТ ВОЗДУХА?", cx, h / 10, 0xFFBFFAFF, 1.25F);
        if (age > 43) drawFace(g, cx, cy, Math.min(w / 2, h / 2) * 0.62F, 0xFF001016, 0xFFF2FFFF, 0xFF005E6C);
    }

    private void renderFakeCrash(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF030506);
        int red = age % 12 < 6 ? 0xFFFF3333 : 0xFFB60000;
        g.fill(0, 0, w, Math.max(4, h / 100), red);
        g.drawString(this.font, "RNMD REALITY KERNEL v2.0.0", 28, 28, 0xFF8BFFB0, false);
        g.drawString(this.font, "FATAL EXCEPTION: MEMORY_OF_PLAYER_NOT_FOUND", 28, 58, red, false);
        g.drawString(this.font, "at cursedquest.reality.Watcher.observe(Unknown Source:404)", 28, 78, 0xFFB7B7B7, false);
        g.drawString(this.font, "Memory integrity: compromised", 28, 110, 0xFFB7B7B7, false);
        g.drawString(this.font, "Player soul backup: NOT FOUND", 28, 130, 0xFFFF5555, false);
        g.drawString(this.font, "Do not close the window. It will notice.", 28, 162, 0xFF8BFFB0, false);
        int barWidth = Math.max(100, w / 3);
        int barX = (w - barWidth) / 2;
        int barY = h - 75;
        g.fill(barX, barY, barX + barWidth, barY + 9, 0xFF282828);
        g.fill(barX, barY, barX + (barWidth * ((age * 7) % 101) / 100), barY + 9, red);
        g.drawCenteredString(this.font, "Сбой симулируется. Настоящий клиент не закрыт.", w / 2, h - 52, 0xFFFF5555);
    }

    private void renderFakeDisconnect(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF101010);
        g.fill(0, 0, w, 2, 0xFF555555);
        g.drawCenteredString(this.font, "Соединение потеряно", w / 2, h / 2 - 48, 0xFFFFFFFF);
        g.drawCenteredString(this.font, "Internal Exception: java.net.SocketException", w / 2, h / 2 - 25, 0xFFBBBBBB);
        g.drawCenteredString(this.font, "Connection reset by entity behind you", w / 2, h / 2 - 8, 0xFF888888);
        g.fill(w / 2 - 74, h / 2 + 20, w / 2 + 74, h / 2 + 42, 0xFF303030);
        g.drawCenteredString(this.font, age < 55 ? "Переподключение..." : "Сессия восстановлена (наверное)", w / 2, h / 2 + 27, 0xFFFFFFFF);
        if (age > 45) {
            g.fill(w / 2 - 76, h / 2 + 19, w / 2 - 74 + Math.min(150, age * 3), h / 2 + 22, 0xFF68C77A);
        }
        g.drawCenteredString(this.font, "Не закрывай игру. Это сообщение поддельное.", w / 2, h - 42, 0xFF666666);
    }

    private void renderPeripheralWatcher(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF020304);
        int side = age % 6 < 3 ? 0 : 1;
        int x = side == 0 ? -w / 18 : w - w / 7;
        int y = h / 7 + (age % 5 - 2) * 2;
        g.fill(x, y, x + w / 5, y + h * 3 / 4, 0xFF000000);
        g.fill(x + w / 40, y + h / 5, x + w / 40 + w / 20, y + h / 5 + h / 22, 0xFFE6E6E6);
        g.fill(x + w / 10, y + h / 5, x + w / 10 + w / 20, y + h / 5 + h / 22, 0xFFE6E6E6);
        g.fill(x + w / 27, y + h / 5, x + w / 27 + w / 48, y + h / 5 + h / 25, 0xFF070707);
        g.fill(x + w / 8, y + h / 5, x + w / 8 + w / 48, y + h / 5 + h / 25, 0xFF070707);
        if (age > 38) {
            g.fill(0, 0, w, h, 0x99000000);
            g.drawCenteredString(this.font, "ТЫ СМОТРЕЛ НЕ ТУДА", w / 2, h / 2, 0xFFDDDDDD);
        }
    }

    private void renderSignalInversion(GuiGraphics g, int w, int h) {
        int base = age % 4 < 2 ? 0xFFECECEC : 0xFF050505;
        g.fill(0, 0, w, h, base);
        for (int i = 0; i < 22; i++) {
            int y = Math.floorMod(i * 67 + age * 11, Math.max(1, h));
            int height = 2 + i % 13;
            int color = ((i + age) % 2 == 0) ? 0xCCFF0000 : 0xCC000000;
            g.fill(0, y, w, Math.min(h, y + height), color);
        }
        if (age % 8 < 3) drawFace(g, w / 2 + (age % 3 - 1) * 18, h / 2, Math.min(w / 2, h / 2) * 0.6F,
                0xFF050505, 0xFFFFFFFF, 0xFFAA0000);
        centeredScaled(g, "SIGNAL OVERRIDE", w / 2, h / 10, base == 0xFF050505 ? 0xFFFFFFFF : 0xFF000000, 1.5F);
    }

    private void renderEyes(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF000000);
        int cx = w / 2;
        int cy = h / 2;
        int blink = age % 9 < 2 ? h / 45 : 0;
        int eyeW = Math.max(18, w / 12);
        int eyeH = Math.max(7, h / 28) + blink;
        g.fill(cx - eyeW * 2, cy - eyeH, cx - eyeW, cy + eyeH, 0xFFF4F0E9);
        g.fill(cx + eyeW, cy - eyeH, cx + eyeW * 2, cy + eyeH, 0xFFF4F0E9);
        g.fill(cx - eyeW * 2 + eyeW / 2, cy - eyeH / 2, cx - eyeW + eyeW / 2, cy + eyeH / 2, 0xFF000000);
        g.fill(cx + eyeW + eyeW / 2, cy - eyeH / 2, cx + eyeW + eyeW, cy + eyeH / 2, 0xFF000000);
        g.fill(cx - w / 18, cy + h / 12, cx + w / 18, cy + h / 4, 0xFF240000);
        for (int i = 0; i < 8; i++) {
            int tx = cx - w / 20 + i * (w / 80);
            g.fill(tx, cy + h / 12, tx + Math.max(2, w / 160), cy + h / 8, 0xFFFFFFFF);
        }
        if (age > 26) {
            g.fill(0, 0, w, h, 0xAA550000);
            centeredScaled(g, "БЛИЖЕ", cx, h / 10, 0xFFFFFFFF, 2.1F);
        }
    }

    private void renderVhsFeed(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF151A19);
        for (int y = 0; y < h; y += 3) g.fill(0, y, w, y + 1, 0x33505050);
        for (int i = 0; i < 20; i++) {
            int yy = Math.floorMod(i * 53 + age * 7, Math.max(1, h));
            g.fill(0, yy, w, Math.min(h, yy + 1 + i % 4), i % 2 == 0 ? 0x4455FFFF : 0x44FF2222);
        }
        g.drawString(this.font, "● REC", 18, 18, 0xFFFF3333, false);
        g.drawString(this.font, "CAM_03 // EAST HALL", 18, 34, 0xFFBBBBBB, false);
        g.drawString(this.font, "00:04:04:04", w - 110, 18, 0xFFBBBBBB, false);
        int cx = w / 2;
        int cy = h / 2;
        g.fill(cx - w / 12, cy - h / 3, cx + w / 12, cy + h / 4, 0xFF010101);
        g.fill(cx - w / 7, cy + h / 8, cx + w / 7, cy + h / 2, 0xFF010101);
        if (age > 64) drawFace(g, cx, cy, Math.min(w / 2, h / 2) * 0.72F, 0xFF010101, 0xFFFFFFFF, 0xFF6D0000);
        g.drawCenteredString(this.font, "ЕСЛИ КАМЕРА ПУСТА — ПОЧЕМУ ОНА МАШЕТ?", w / 2, h - 30, 0xFFE4E4E4);
    }

    private void renderFakeBlueScreen(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF0755B5);
        int x = w / 8;
        int y = h / 6;
        g.drawString(this.font, ":(", x, y, 0xFFFFFFFF, false);
        g.drawString(this.font, "Ваша реальность столкнулась с проблемой и должна перезапуститься.", x, y + 52, 0xFFFFFFFF, false);
        g.drawString(this.font, "Мы только собираем некоторые данные о вашей личности.", x, y + 76, 0xFFFFFFFF, false);
        int progress = (age * 3) % 101;
        g.drawString(this.font, progress + "% завершено", x, y + 106, 0xFFFFFFFF, false);
        g.drawString(this.font, "Код остановки: SOUL_NOT_FOUND", x, y + 150, 0xFFFFFFFF, false);
        if (age > 48) {
            int jitter = age % 3 - 1;
            g.fill(w - w / 5 + jitter * 3, h / 4, w - w / 9, h * 3 / 4, 0xFF020202);
            g.fill(w - w / 6, h / 3, w - w / 9, h / 3 + 12, 0xFFFFFFFF);
            g.fill(w - w / 6, h / 3 + h / 9, w - w / 9, h / 3 + h / 9 + 12, 0xFFFFFFFF);
        }
    }

    private void renderWhisper(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF010207);
        int blink = age % 22 < 3 ? 0 : 1;
        if (blink != 0) {
            int side = age % 15 < 8 ? 1 : -1;
            int ex = w / 2 + side * (w / 3);
            int ey = h / 2 + (age % 7 - 3) * 2;
            g.fill(ex - 8, ey - 3, ex + 8, ey + 3, 0xFFEEEEEE);
            g.fill(ex - 2, ey - 2, ex + 3, ey + 3, 0xFFAA0000);
        }
        for (int y = h / 3; y < h; y += 35) {
            int width = Math.floorMod(y * 11 + age * 5, Math.max(1, w / 4));
            g.fill(w / 2 - width, y, w / 2 + width, y + 1, 0x22447799);
        }
        centeredScaled(g, age < 28 ? "..." : "НЕ ОБОРАЧИВАЙСЯ", w / 2, h / 2, 0xFFBFC6D5, age < 28 ? 1.0F : 1.35F);
        if (age > 53) centeredScaled(g, "ОН УЖЕ ЗДЕСЬ", w / 2, h / 2 + 32, 0xFF920000, 1.15F);
    }

    private void renderTextCorruption(GuiGraphics g, int w, int h) {
        g.fill(0, 0, w, h, 0xFF000000);
        String[] messages = {"Я НЕ СЛЫШАЛ", "ТЫ ОТВЕТИЛ", "СЕАНС ЗАПИСАН", "ОШИБКА: ЛИШНИЙ ИГРОК"};
        String text = messages[Math.floorMod(age / 7, messages.length)];
        for (int i = 0; i < 14; i++) {
            int y = Math.floorMod(i * 59 + age * 9, Math.max(1, h));
            int x = Math.floorMod(i * 31 + age * 13, Math.max(1, w));
            g.fill(0, y, w, y + 1 + i % 4, i % 2 == 0 ? 0x66FF0000 : 0x6666FFFF);
            g.drawString(this.font, text, x - w / 4, y, i % 3 == 0 ? 0xFFFF2222 : 0xFFCCCCCC, false);
        }
        centeredScaled(g, text, w / 2 + (age % 5 - 2) * 4, h / 2, 0xFFFFFFFF, 2.0F);
        centeredScaled(g, "ПРОЧИТАЙ ЕЩЁ РАЗ", w / 2, h / 2 + 50, 0xFFAE0000, 1.15F);
    }

    private void drawFace(GuiGraphics g, int cx, int cy, float scale, int faceColor, int eyeColor, int mouthColor) {
        int hw = Math.max(25, (int) (120 * scale));
        int hh = Math.max(30, (int) (155 * scale));
        int unit = Math.max(2, (int) (8 * scale));
        g.fill(cx - hw, cy - hh, cx + hw, cy + hh, faceColor);
        g.fill(cx - hw, cy - hh, cx + hw, cy - hh + unit * 2, 0xFF380000);
        int eyeY = cy - hh / 4;
        int eyeW = Math.max(10, (int) (35 * scale));
        int eyeH = Math.max(7, (int) (22 * scale));
        g.fill(cx - hw / 2 - eyeW / 2, eyeY - eyeH, cx - hw / 2 + eyeW, eyeY + eyeH, eyeColor);
        g.fill(cx + hw / 2 - eyeW, eyeY - eyeH, cx + hw / 2 + eyeW / 2, eyeY + eyeH, eyeColor);
        g.fill(cx - hw / 2, eyeY - eyeH / 2, cx - hw / 2 + eyeW / 2, eyeY + eyeH / 2, 0xFF050505);
        g.fill(cx + hw / 2 - eyeW / 2, eyeY - eyeH / 2, cx + hw / 2, eyeY + eyeH / 2, 0xFF050505);
        int mouthTop = cy + hh / 8;
        int mouthBottom = cy + hh * 3 / 4;
        g.fill(cx - hw / 2, mouthTop, cx + hw / 2, mouthBottom, mouthColor);
        g.fill(cx - hw / 2 + unit, mouthTop + unit, cx + hw / 2 - unit, mouthBottom - unit, 0xFF030303);
        int toothWidth = Math.max(3, (hw / 2) / 7);
        for (int i = 0; i < 7; i++) {
            int tx = cx - hw / 2 + unit + i * toothWidth;
            g.fill(tx, mouthTop + unit, tx + Math.max(2, toothWidth / 2), mouthTop + unit + Math.max(8, (int) (20 * scale)), eyeColor);
            g.fill(tx + toothWidth / 3, mouthBottom - Math.max(8, (int) (20 * scale)), tx + toothWidth, mouthBottom - unit, eyeColor);
        }
    }

    private void centeredScaled(GuiGraphics g, String text, int centerX, int y, int color, float scale) {
        g.pose().pushPose();
        g.pose().translate(centerX, y, 0.0F);
        g.pose().scale(scale, scale, 1.0F);
        g.drawCenteredString(this.font, text, 0, 0, color);
        g.pose().popPose();
    }
}
