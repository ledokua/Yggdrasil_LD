package net.ledok.registry;

public record LootBoxDefinition(
    String id,
    String name,
    String lootTableId,
    int color,
    boolean glow
) {}
