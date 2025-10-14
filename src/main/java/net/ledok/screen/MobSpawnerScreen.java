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

public class MobSpawnerScreen extends HandledScreen<MobSpawnerScreenHandler> {

    private TextFieldWidget mobIdField;
    private TextFieldWidget respawnTimeField;
    private TextFieldWidget lootTableIdField;
    private TextFieldWidget triggerRadiusField;
    private TextFieldWidget battleRadiusField;
    private TextFieldWidget regenerationField;
    private TextFieldWidget skillExperienceField;
    private TextFieldWidget mobCountField;
    private TextFieldWidget mobSpreadField;
    private TextFieldWidget mobHealthField;
    private TextFieldWidget mobAttackDamageField;

    public MobSpawnerScreen(MobSpawnerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundHeight = 240;
    }

    @Override
    protected void init() {
        super.init();

        int fieldWidth = 150;
        int fieldHeight = 20;
        int yOffset = 24;

        // --- Column 1 ---
        int col1X = (this.width / 2) - fieldWidth - 10;
        int y = 20;

        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Mob ID"), this.textRenderer));
        mobIdField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        mobIdField.setMaxLength(128);
        this.addDrawableChild(mobIdField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Loot Table ID"), this.textRenderer));
        lootTableIdField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        lootTableIdField.setMaxLength(128);
        this.addDrawableChild(lootTableIdField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Mob Count"), this.textRenderer));
        mobCountField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        mobCountField.setMaxLength(4);
        this.addDrawableChild(mobCountField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Mob Spread Radius"), this.textRenderer));
        mobSpreadField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        mobSpreadField.setMaxLength(4);
        this.addDrawableChild(mobSpreadField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col1X, y - 15, fieldWidth, fieldHeight, Text.literal("Custom Mob Health"), this.textRenderer));
        mobHealthField = new TextFieldWidget(this.textRenderer, col1X, y, fieldWidth, fieldHeight, Text.literal(""));
        mobHealthField.setMaxLength(8);
        this.addDrawableChild(mobHealthField);


        // --- Column 2 ---
        int col2X = (this.width / 2) + 5;
        y = 20;

        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Respawn Time (ticks)"), this.textRenderer));
        respawnTimeField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        respawnTimeField.setMaxLength(8);
        this.addDrawableChild(respawnTimeField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Trigger Radius"), this.textRenderer));
        triggerRadiusField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        triggerRadiusField.setMaxLength(4);
        this.addDrawableChild(triggerRadiusField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Battle Radius"), this.textRenderer));
        battleRadiusField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        battleRadiusField.setMaxLength(4);
        this.addDrawableChild(battleRadiusField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Regeneration / 5s"), this.textRenderer));
        regenerationField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        regenerationField.setMaxLength(4);
        this.addDrawableChild(regenerationField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Skill XP on Win"), this.textRenderer));
        skillExperienceField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        skillExperienceField.setMaxLength(8);
        this.addDrawableChild(skillExperienceField);
        y += yOffset * 1.5;

        this.addDrawableChild(new TextWidget(col2X, y - 15, fieldWidth, fieldHeight, Text.literal("Custom Attack Damage"), this.textRenderer));
        mobAttackDamageField = new TextFieldWidget(this.textRenderer, col2X, y, fieldWidth, fieldHeight, Text.literal(""));
        mobAttackDamageField.setMaxLength(8);
        this.addDrawableChild(mobAttackDamageField);

        // --- Save Button ---
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> onSave())
                .dimensions(this.width / 2 - 50, this.height - 50, 100, 20)
                .build());

        loadBlockEntityData();
    }

    private void loadBlockEntityData() {
        if (handler.blockEntity != null) {
            mobIdField.setText(handler.blockEntity.mobId);
            respawnTimeField.setText(String.valueOf(handler.blockEntity.respawnTime));
            lootTableIdField.setText(handler.blockEntity.lootTableId);
            triggerRadiusField.setText(String.valueOf(handler.blockEntity.triggerRadius));
            battleRadiusField.setText(String.valueOf(handler.blockEntity.battleRadius));
            regenerationField.setText(String.valueOf(handler.blockEntity.regeneration));
            skillExperienceField.setText(String.valueOf(handler.blockEntity.skillExperiencePerWin));
            mobCountField.setText(String.valueOf(handler.blockEntity.mobCount));
            mobSpreadField.setText(String.valueOf(handler.blockEntity.mobSpread));
            mobHealthField.setText(String.valueOf(handler.blockEntity.mobHealth));
            mobAttackDamageField.setText(String.valueOf(handler.blockEntity.mobAttackDamage));
        }
    }

    private void onSave() {
        try {
            ClientPlayNetworking.send(new ModPackets.UpdateMobSpawnerPayload(
                    handler.blockEntity.getPos(),
                    mobIdField.getText(),
                    Integer.parseInt(respawnTimeField.getText()),
                    lootTableIdField.getText(),
                    Integer.parseInt(triggerRadiusField.getText()),
                    Integer.parseInt(battleRadiusField.getText()),
                    Integer.parseInt(regenerationField.getText()),
                    Integer.parseInt(skillExperienceField.getText()),
                    Integer.parseInt(mobCountField.getText()),
                    Integer.parseInt(mobSpreadField.getText()),
                    Double.parseDouble(mobHealthField.getText()),
                    Double.parseDouble(mobAttackDamageField.getText())
            ));
            this.close();
        } catch (NumberFormatException e) {
            System.err.println("Invalid number format in one of the fields.");
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    }

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
