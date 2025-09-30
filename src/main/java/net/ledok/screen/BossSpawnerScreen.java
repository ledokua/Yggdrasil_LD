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
    // private TextFieldWidget bossLevelField; // Removed
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

        // Labels
        this.addDrawableChild(new TextWidget(startX - 110, startY + 5, 100, fieldHeight, Text.literal("Mob ID"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset + 5, 100, fieldHeight, Text.literal("Respawn Time"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset * 2 + 5, 100, fieldHeight, Text.literal("Portal Time"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset * 3 + 5, 100, fieldHeight, Text.literal("Loot Table"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset * 4 + 5, 100, fieldHeight, Text.literal("Exit Coords"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset * 5 + 5, 100, fieldHeight, Text.literal("Trigger Radius"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset * 6 + 5, 100, fieldHeight, Text.literal("Battle Radius"), this.textRenderer));
        this.addDrawableChild(new TextWidget(startX - 110, startY + yOffset * 7 + 5, 100, fieldHeight, Text.literal("Regeneration"), this.textRenderer));

        // Fields
        mobIdField = new TextFieldWidget(this.textRenderer, startX, startY, fieldWidth, fieldHeight, Text.literal("Mob ID"));
        mobIdField.setMaxLength(128);
        mobIdField.setText(handler.blockEntity.mobId);
        this.addDrawableChild(mobIdField);

        respawnTimeField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset, fieldWidth, fieldHeight, Text.literal("Respawn Time"));
        respawnTimeField.setMaxLength(128);
        respawnTimeField.setText(String.valueOf(handler.blockEntity.respawnTime));
        this.addDrawableChild(respawnTimeField);

        portalActiveTimeField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset * 2, fieldWidth, fieldHeight, Text.literal("Portal Time"));
        portalActiveTimeField.setMaxLength(128);
        portalActiveTimeField.setText(String.valueOf(handler.blockEntity.portalActiveTime));
        this.addDrawableChild(portalActiveTimeField);

        lootTableIdField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset * 3, fieldWidth, fieldHeight, Text.literal("Loot Table ID"));
        lootTableIdField.setMaxLength(128);
        lootTableIdField.setText(handler.blockEntity.lootTableId);
        this.addDrawableChild(lootTableIdField);

        exitPortalCoordsField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset * 4, fieldWidth, fieldHeight, Text.literal("Exit Coords"));
        exitPortalCoordsField.setMaxLength(128);
        exitPortalCoordsField.setText(handler.blockEntity.exitPortalCoords.getX() + " " + handler.blockEntity.exitPortalCoords.getY() + " " + handler.blockEntity.exitPortalCoords.getZ());
        this.addDrawableChild(exitPortalCoordsField);

        triggerRadiusField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset * 5, fieldWidth, fieldHeight, Text.literal("Trigger Radius"));
        triggerRadiusField.setMaxLength(128);
        triggerRadiusField.setText(String.valueOf(handler.blockEntity.triggerRadius));
        this.addDrawableChild(triggerRadiusField);

        battleRadiusField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset * 6, fieldWidth, fieldHeight, Text.literal("Battle Radius"));
        battleRadiusField.setMaxLength(128);
        battleRadiusField.setText(String.valueOf(handler.blockEntity.battleRadius));
        this.addDrawableChild(battleRadiusField);

        regenerationField = new TextFieldWidget(this.textRenderer, startX, startY + yOffset * 7, fieldWidth, fieldHeight, Text.literal("Regeneration"));
        regenerationField.setMaxLength(128);
        regenerationField.setText(String.valueOf(handler.blockEntity.regeneration));
        this.addDrawableChild(regenerationField);

        // Save Button
        ButtonWidget saveButton = ButtonWidget.builder(Text.literal("Save"), button -> sendUpdatePacket())
                .dimensions(startX, startY + yOffset * 8, fieldWidth, fieldHeight)
                .build();
        this.addDrawableChild(saveButton);
    }

    private void sendUpdatePacket() {
        try {
            String[] coords = exitPortalCoordsField.getText().split(" ");
            BlockPos exitCoords = new BlockPos(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));

            ModPackets.UpdateBossSpawnerPayload payload = new ModPackets.UpdateBossSpawnerPayload(
                    handler.blockEntity.getPos(),
                    mobIdField.getText(),
                    Integer.parseInt(respawnTimeField.getText()),
                    Integer.parseInt(portalActiveTimeField.getText()),
                    lootTableIdField.getText(),
                    exitCoords,
                    Integer.parseInt(triggerRadiusField.getText()),
                    Integer.parseInt(battleRadiusField.getText()),
                    Integer.parseInt(regenerationField.getText())
            );

            ClientPlayNetworking.send(payload);
            this.close();
        } catch (NumberFormatException e) {
            // Optionally: Log an error or show a message to the user on the screen.
            System.err.println("Invalid number format in one of the fields.");
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

