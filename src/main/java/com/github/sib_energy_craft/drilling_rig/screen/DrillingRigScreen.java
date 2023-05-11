package com.github.sib_energy_craft.drilling_rig.screen;

import com.github.sib_energy_craft.drilling_rig.block.entity.AbstractDrillingRigBlockEntity;
import com.github.sib_energy_craft.energy_api.utils.Identifiers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * @since 0.0.1
 * @author sibmaks
 */
public class DrillingRigScreen extends HandledScreen<DrillingRigScreenHandler> {
    private static final int MOVE_LEFT_X = 5;
    private static final int MOVE_RIGHT_X = 29;
    private static final int MOVE_UP_X = 17;
    private static final int MOVE_DOWN_X = 17;

    private static final int WIDTH_UP_X = 55;
    private static final int WIDTH_DOWN_X = 66;
    private static final int HEIGHT_UP_X = 55;
    private static final int HEIGHT_DOWN_X = 66;

    private static final Identifier TEXTURE = Identifiers.of("textures/gui/container/drilling_rig.png");

    public DrillingRigScreen(@NotNull DrillingRigScreenHandler handler,
                             @NotNull PlayerInventory inventory,
                             @NotNull Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void drawBackground(@NotNull MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = this.x;
        int y = this.y;
        drawTexture(matrices, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);

        int progress = this.handler.getChargeProgress();
        drawTexture(matrices, x + 84, y + 36, 176, 0, 7, progress);

        var startPosition = this.handler.getStartPosition();
        if(startPosition.x > -AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            drawTexture(matrices, x + MOVE_LEFT_X, y + 46, 183, 0, 12, 12);
            if(isMouseOver(mouseX, mouseY, x + MOVE_LEFT_X, y + 46, 12, 12)) {
                drawTexture(matrices, x + MOVE_LEFT_X, y + 46, 183, 12, 12, 12);
            }
        }
        if(startPosition.x < AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            drawTexture(matrices, x + MOVE_RIGHT_X, y + 46, 207, 0, 12, 12);
            if(isMouseOver(mouseX, mouseY, x + MOVE_RIGHT_X, y + 46, 12, 12)) {
                drawTexture(matrices, x + MOVE_RIGHT_X, y + 46, 207, 12, 12, 12);
            }
        }
        if(startPosition.y < AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            drawTexture(matrices, x + MOVE_UP_X, y + 34, 195, 0, 12, 12);
            if(isMouseOver(mouseX, mouseY, x + MOVE_UP_X, y + 34, 12, 12)) {
                drawTexture(matrices, x + MOVE_UP_X, y + 34, 195, 12, 12, 12);
            }
        }
        if(startPosition.y > -AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            drawTexture(matrices, x + MOVE_DOWN_X, y + 58, 219, 0, 12, 12);
            if(isMouseOver(mouseX, mouseY, x + MOVE_DOWN_X, y + 58, 12, 12)) {
                drawTexture(matrices, x + MOVE_DOWN_X, y + 58, 219, 12, 12, 12);
            }
        }

        var size = this.handler.getSize();
        if(size.x > 1) {
            drawTexture(matrices, x + WIDTH_UP_X, y + 34, 231, 0, 11, 12);
            if(isMouseOver(mouseX, mouseY, x + WIDTH_UP_X, y + 34, 11, 12)) {
                drawTexture(matrices, x + WIDTH_UP_X, y + 34, 231, 12, 11, 12);
            }
        }
        if(size.x < AbstractDrillingRigBlockEntity.MAX_SIZE) {
            drawTexture(matrices, x + WIDTH_DOWN_X, y + 34, 242, 0, 11, 12);
            if(isMouseOver(mouseX, mouseY, x + WIDTH_DOWN_X, y + 34, 11, 12)) {
                drawTexture(matrices, x + WIDTH_DOWN_X, y + 34, 242, 12, 11, 12);
            }
        }

        if(size.y > 1) {
            drawTexture(matrices, x + HEIGHT_UP_X, y + 58, 231, 0, 11, 12);
            if(isMouseOver(mouseX, mouseY, x + HEIGHT_UP_X, y + 58, 11, 12)) {
                drawTexture(matrices, x + HEIGHT_UP_X, y + 58, 231, 12, 11, 12);
            }
        }
        if(size.y < AbstractDrillingRigBlockEntity.MAX_SIZE) {
            drawTexture(matrices, x + HEIGHT_DOWN_X, y + 58, 242, 0, 11, 12);
            if(isMouseOver(mouseX, mouseY, x + HEIGHT_DOWN_X, y + 58, 11, 12)) {
                drawTexture(matrices, x + HEIGHT_DOWN_X, y + 58, 242, 12, 11, 12);
            }
        }

        int whiteRgb = Color.WHITE.getRGB();

        var offsetText = Text.translatable("screen.drilling_rig.offset", startPosition.x, startPosition.y);
        int offsetTextX = 7 + (68 - textRenderer.getWidth(offsetText)) / 2;
        this.textRenderer.drawWithShadow(matrices, offsetText, x + offsetTextX, y + 18, whiteRgb);

        var widthText = Text.translatable("screen.drilling_rig.width", size.x);
        int widthTextX = MOVE_RIGHT_X + (23 - textRenderer.getWidth(widthText)) / 2;
        this.textRenderer.drawWithShadow(matrices, widthText, x + widthTextX, y + 36, whiteRgb);

        var heightText = Text.translatable("screen.drilling_rig.height", size.y);
        int heightTextX = MOVE_RIGHT_X + (23 - textRenderer.getWidth(heightText)) / 2;
        this.textRenderer.drawWithShadow(matrices, heightText, x + heightTextX, y + 60, whiteRgb);

        if(mouseX >= x + 84 && mouseX <= x + 84 + 7 &&
                mouseY >= y + 36 && mouseY <= y + 36 + 13) {
            int charge = this.handler.getCharge();
            int maxCharge = this.handler.getMaxCharge();
            var charging = Text.translatable("energy.range.text", charge, maxCharge);
            this.renderTooltip(matrices, charging, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        var client = this.client;
        if (client == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        var interactionManager = client.interactionManager;
        if (interactionManager == null) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        var startPosition = this.handler.getStartPosition();
        if (startPosition.x > -AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            if (isMouseOver(mouseX, mouseY, x + MOVE_LEFT_X, y + 46, 12, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.MOVE_LEFT.ordinal());
                return true;
            }
        }
        if (startPosition.x < AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            if (isMouseOver(mouseX, mouseY, x + MOVE_RIGHT_X, y + 46, 12, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.MOVE_RIGHT.ordinal());
            }
        }
        if(startPosition.y < AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            if(isMouseOver(mouseX, mouseY, x + MOVE_UP_X, y + 34, 12, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.MOVE_DOWN.ordinal());
            }
        }
        if(startPosition.y > -AbstractDrillingRigBlockEntity.MAX_OFFSET) {
            if(isMouseOver(mouseX, mouseY, x + MOVE_DOWN_X, y + 58, 12, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.MOVE_UP.ordinal());
            }
        }

        var size = this.handler.getSize();
        if(size.x > 1) {
            if(isMouseOver(mouseX, mouseY, x + WIDTH_UP_X, y + 34, 11, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.WIDTH_DOWN.ordinal());
            }
        }
        if(size.x < AbstractDrillingRigBlockEntity.MAX_SIZE) {
            if(isMouseOver(mouseX, mouseY, x + WIDTH_DOWN_X, y + 34, 11, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.WIDTH_UP.ordinal());
            }
        }
        if(size.y > 1) {
            if(isMouseOver(mouseX, mouseY, x + HEIGHT_UP_X, y + 58, 11, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.HEIGHT_DOWN.ordinal());
            }
        }
        if(size.y < AbstractDrillingRigBlockEntity.MAX_SIZE) {
            if(isMouseOver(mouseX, mouseY, x + HEIGHT_DOWN_X, y + 58, 11, 12)) {
                interactionManager.clickButton(this.handler.syncId, DrillingRigScreenButton.HEIGHT_UP.ordinal());
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private static boolean isMouseOver(float mouseX, float mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w &&
                mouseY >= y && mouseY <= y + h;
    }

    private static boolean isMouseOver(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w &&
                mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void render(@NotNull MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }
}