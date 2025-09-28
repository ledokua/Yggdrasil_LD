package net.ledok.screen;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.ledok.networking.ModPackets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class BossSpawnerScreen extends HandledScreen<BossSpawnerScreenHandler> {

    private TextFieldWidget mobIdField;
    private TextFieldWidget respawnTimeField;
    private TextFieldWidget bossLevelField;
    private TextFieldWidget portalActiveTimeField;
    private TextFieldWidget lootTableIdField;
    private TextFieldWidget exitPortalCoordsField;
    private TextFieldWidget triggerRadiusField;
    private TextFieldWidget battleRadiusField;
    private TextFieldWidget regenerationField;

    public BossSpawnerScreen(BossSpawnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int fieldWidth = 150;
        int fieldHeight = 20;
        int startX = (this.width - fieldWidth) / 2;
        int startY = 20;
        int yOffset = 24;

        this.addDrawableChild(new TextWidget(startX - 100, startY + 5, 90, 20, Text.literal("Mob ID:"), this.textRenderer));
        mobIdField = new TextFieldWidget(this.textRenderer, startX, startY, fieldWidth, fieldHeight, Text.literal("Mob ID"));
        mobIdField.setText(handler.blockEntity.mobId);
        this.addDrawableChild(mobIdField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Respawn Time:"), this.textRenderer));
        respawnTimeField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Respawn Time"));
        respawnTimeField.setText(String.valueOf(handler.blockEntity.respawnTime));
        this.addDrawableChild(respawnTimeField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Boss Level:"), this.textRenderer));
        bossLevelField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Boss Level"));
        bossLevelField.setText(String.valueOf(handler.blockEntity.bossLevel));
        this.addDrawableChild(bossLevelField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Portal Time:"), this.textRenderer));
        portalActiveTimeField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Portal Time"));
        portalActiveTimeField.setText(String.valueOf(handler.blockEntity.portalActiveTime));
        this.addDrawableChild(portalActiveTimeField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Loot Table ID:"), this.textRenderer));
        lootTableIdField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Loot Table"));
        lootTableIdField.setText(handler.blockEntity.lootTableId);
        this.addDrawableChild(lootTableIdField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Exit Coords:"), this.textRenderer));
        exitPortalCoordsField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("X Y Z"));
        BlockPos coords = handler.blockEntity.exitPortalCoords;
        exitPortalCoordsField.setText(coords.getX() + " " + coords.getY() + " " + coords.getZ());
        this.addDrawableChild(exitPortalCoordsField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Trigger Radius:"), this.textRenderer));
        triggerRadiusField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Trigger Radius"));
        triggerRadiusField.setText(String.valueOf(handler.blockEntity.triggerRadius));
        this.addDrawableChild(triggerRadiusField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Battle Radius:"), this.textRenderer));
        battleRadiusField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Battle Radius"));
        battleRadiusField.setText(String.valueOf(handler.blockEntity.battleRadius));
        this.addDrawableChild(battleRadiusField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(new TextWidget(startX - 100, startY + yOffset + 5, 90, 20, Text.literal("Regeneration:"), this.textRenderer));
        regenerationField = new TextFieldWidget(this.textRenderer, startX, startY += yOffset, fieldWidth, fieldHeight, Text.literal("Regeneration"));
        regenerationField.setText(String.valueOf(handler.blockEntity.regeneration));
        this.addDrawableChild(regenerationField); // FIX: Use addDrawableChild for text fields

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
            saveAndClose();
        }).dimensions(this.width / 2 - 50, startY + yOffset + 5, 100, 20).build());
    }

    private void saveAndClose() {
        String[] coordsText = exitPortalCoordsField.getText().split(" ");
        BlockPos exitCoords = new BlockPos(
                Integer.parseInt(coordsText[0]),
                Integer.parseInt(coordsText[1]),
                Integer.parseInt(coordsText[2])
        );

        var payload = new ModPackets.UpdateBossSpawnerPayload(
                handler.blockEntity.getPos(),
                mobIdField.getText(),
                Integer.parseInt(respawnTimeField.getText()),
                Integer.parseInt(bossLevelField.getText()),
                Integer.parseInt(portalActiveTimeField.getText()),
                lootTableIdField.getText(),
                exitCoords,
                Integer.parseInt(triggerRadiusField.getText()),
                Integer.parseInt(battleRadiusField.getText()),
                Integer.parseInt(regenerationField.getText())
        );

        ClientPlayNetworking.send(payload);
        this.close();
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // This method is for drawing the GUI's texture (like a chest).
        // Since we don't have one, this should be empty. The dark background overlay is
        // handled automatically by the parent HandledScreen class.
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // We render the background first, which is the dark overlay.
        this.renderBackground(context, mouseX, mouseY, delta);
        // Then, we let the parent class render all the widgets, title, etc.
        super.render(context, mouseX, mouseY, delta);
        // Finally, we draw any tooltips if the mouse is hovering over an element.
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

