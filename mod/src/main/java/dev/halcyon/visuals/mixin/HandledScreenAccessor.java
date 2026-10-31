package dev.halcyon.visuals.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x") int halcyon$getX();
    @Accessor("y") int halcyon$getY();
    @Accessor("backgroundWidth") int halcyon$getBackgroundWidth();
    @Accessor("backgroundHeight") int halcyon$getBackgroundHeight();
    @Accessor("playerInventoryTitleY") int halcyon$getPlayerInventoryTitleY();
}
