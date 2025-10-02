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

    // --- All fields from the original code ---
    private TextFieldWidget mobIdField;
    private TextFieldWidget respawnTimeField;
    private TextFieldWidget portalActiveTimeField;
    private TextFieldWidget lootTableIdField;
    private TextFieldWidget exitPortalCoordsField;
    private TextFieldWidget triggerRadiusField;
    private TextFieldWidget battleRadiusField;
    private TextFieldWidget regenerationField;
    // --- New fields for the enter portal ---
    private TextFieldWidget enterPortalSpawnCoordsField;
    private TextFieldWidget enterPortalDestCoordsField;


    public BossSpawnerScreen(BossSpawnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        // Make the screen taller to accommodate all the new fields
        this.backgroundHeight = 220;
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = 130;
        int fieldHeight = 20;
        int yOffset = 24;

        // --- Column 1 ---
        int col1X = (this.width / 2) - fieldWidth - 5;
        int y = 20;

        // Mob ID
        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Mob ID"), this.textRenderer));
        mobIdField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        mobIdField.setMaxLength(128); // RESTORED
        this.addDrawableChild(mobIdField);
        y += yOffset * 1.5;

        // Loot Table
        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Loot Table ID"), this.textRenderer));
        lootTableIdField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        lootTableIdField.setMaxLength(128); // RESTORED
        this.addDrawableChild(lootTableIdField);
        y += yOffset * 1.5;

        // Enter Portal Spawn
        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Enter Portal Spawn"), this.textRenderer));
        enterPortalSpawnCoordsField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        enterPortalSpawnCoordsField.setMaxLength(64); // RESTORED
        this.addDrawableChild(enterPortalSpawnCoordsField);
        y += yOffset * 1.5;

        // Enter Portal Destination
        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Enter Portal Destination"), this.textRenderer));
        enterPortalDestCoordsField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        enterPortalDestCoordsField.setMaxLength(64); // RESTORED
        this.addDrawableChild(enterPortalDestCoordsField);
        y += yOffset * 1.5;

        // Exit Portal Destination
        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Exit Portal Destination"), this.textRenderer));
        exitPortalCoordsField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        exitPortalCoordsField.setMaxLength(64); // RESTORED
        this.addDrawableChild(exitPortalCoordsField);


        // --- Column 2 ---
        int col2X = (this.width / 2) + 5;
        y = 20;

        // Respawn Time
        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Respawn Time (ticks)"), this.textRenderer));
        respawnTimeField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        respawnTimeField.setMaxLength(64); // RESTORED
        this.addDrawableChild(respawnTimeField);
        y += yOffset * 1.5;

        // Portal Active Time
        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Portal Active Time"), this.textRenderer));
        portalActiveTimeField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        portalActiveTimeField.setMaxLength(64); // RESTORED
        this.addDrawableChild(portalActiveTimeField);
        y += yOffset * 1.5;

        // Trigger Radius
        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Trigger Radius"), this.textRenderer));
        triggerRadiusField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        triggerRadiusField.setMaxLength(64); // RESTORED
        this.addDrawableChild(triggerRadiusField);
        y += yOffset * 1.5;

        // Battle Radius
        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Battle Radius"), this.textRenderer));
        battleRadiusField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        battleRadiusField.setMaxLength(64); // RESTORED
        this.addDrawableChild(battleRadiusField);
        y += yOffset * 1.5;

        // Regeneration
        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Regeneration"), this.textRenderer));
        regenerationField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        regenerationField.setMaxLength(64); // RESTORED
        this.addDrawableChild(regenerationField);


        // --- Save Button ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> onSave())
                .dimensions(this.width / 2 - 50, this.height - 50, 100, 20)
                .build());

        // --- Load initial data into fields ---
        loadBlockEntityData();
    }

    private void loadBlockEntityData() {
        if (handler.blockEntity != null) {
            mobIdField.setText(handler.blockEntity.mobId);
            respawnTimeField.setText(String.valueOf(handler.blockEntity.respawnTime));
            portalActiveTimeField.setText(String.valueOf(handler.blockEntity.portalActiveTime));
            lootTableIdField.setText(handler.blockEntity.lootTableId);
            exitPortalCoordsField.setText(String.format("%d %d %d", handler.blockEntity.exitPortalCoords.getX(), handler.blockEntity.exitPortalCoords.getY(), handler.blockEntity.exitPortalCoords.getZ()));
            triggerRadiusField.setText(String.valueOf(handler.blockEntity.triggerRadius));
            battleRadiusField.setText(String.valueOf(handler.blockEntity.battleRadius));
            regenerationField.setText(String.valueOf(handler.blockEntity.regeneration));
            enterPortalSpawnCoordsField.setText(String.format("%d %d %d", handler.blockEntity.enterPortalSpawnCoords.getX(), handler.blockEntity.enterPortalSpawnCoords.getY(), handler.blockEntity.enterPortalSpawnCoords.getZ()));
            enterPortalDestCoordsField.setText(String.format("%d %d %d", handler.blockEntity.enterPortalDestCoords.getX(), handler.blockEntity.enterPortalDestCoords.getY(), handler.blockEntity.enterPortalDestCoords.getZ()));
        }
    }

    private BlockPos parseCoords(String text) {
        try {
            String[] parts = text.split(" ");
            if (parts.length == 3) {
                return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            }
        } catch (NumberFormatException ignored) { }
        return new BlockPos(0, 0, 0);
    }

    private void onSave() {
        try {
            ClientPlayNetworking.send(new ModPackets.UpdateBossSpawnerPayload(
                    handler.blockEntity.getPos(),
                    mobIdField.getText(),
                    Integer.parseInt(respawnTimeField.getText()),
                    Integer.parseInt(portalActiveTimeField.getText()),
                    lootTableIdField.getText(),
                    parseCoords(exitPortalCoordsField.getText()),
                    parseCoords(enterPortalSpawnCoordsField.getText()),
                    parseCoords(enterPortalDestCoordsField.getText()),
                    Integer.parseInt(triggerRadiusField.getText()),
                    Integer.parseInt(battleRadiusField.getText()),
                    Integer.parseInt(regenerationField.getText())
            ));
            this.close();
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in one of the fields.");
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        // No custom background texture, just the default gray
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // We don't call super.drawForeground to prevent the title from rendering in the top-left corner
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}

