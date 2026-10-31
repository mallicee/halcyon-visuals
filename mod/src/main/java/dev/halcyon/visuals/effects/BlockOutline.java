package dev.halcyon.visuals.effects;

import dev.halcyon.visuals.config.HalcyonConfig;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;

import static dev.halcyon.visuals.render.Render2D.lerpColor;

/** Обводка выбранного блока: цвет акцента или переливающийся градиент, плюс полупрозрачная заливка. */
public final class BlockOutline {
    private BlockOutline() {}

    /** @return true — пусть игра рисует ванильную обводку. */
    public static boolean render(WorldRenderContext ctx, WorldRenderContext.BlockOutlineContext o) {
        HalcyonConfig cfg = HalcyonConfig.get();
        if (!cfg.outline.enabled || ctx.world() == null) return true;
        MatrixStack m = ctx.matrixStack();
        if (m == null) return true;

        BlockPos pos = o.blockPos();
        VoxelShape shape = o.blockState().getOutlineShape(ctx.world(), pos, ShapeContext.of(o.entity()));
        if (shape.isEmpty()) return true;
        double ox = pos.getX() - o.cameraX(), oy = pos.getY() - o.cameraY(), oz = pos.getZ() - o.cameraZ();
        int c1 = cfg.accentRgb(), c2 = 0x20D6C8;
        boolean gradient = cfg.outline.color.equals("gradient");
        double t = Util.getMeasuringTimeMs() / 1800.0;

        VertexConsumerProvider consumers = ctx.consumers();
        if (cfg.outline.fill && consumers != null && cfg.outline.fillAlpha > 0) {
            VertexConsumer fill = consumers.getBuffer(RenderLayer.getDebugFilledBox());
            float r = ((c1 >> 16) & 0xFF) / 255f, g = ((c1 >> 8) & 0xFF) / 255f, b = (c1 & 0xFF) / 255f;
            for (Box box : shape.getBoundingBoxes()) {
                WorldRenderer.renderFilledBox(m, fill,
                        box.minX + ox - 0.002, box.minY + oy - 0.002, box.minZ + oz - 0.002,
                        box.maxX + ox + 0.002, box.maxY + oy + 0.002, box.maxZ + oz + 0.002,
                        r, g, b, (float) cfg.outline.fillAlpha);
            }
        }

        VertexConsumer lines = o.vertexConsumer();
        MatrixStack.Entry entry = m.peek();
        shape.forEachEdge((x1, y1, z1, x2, y2, z2) -> {
            float nx = (float) (x2 - x1), ny = (float) (y2 - y1), nz = (float) (z2 - z1);
            float len = MathHelper.sqrt(nx * nx + ny * ny + nz * nz);
            if (len == 0) return;
            nx /= len; ny /= len; nz /= len;
            int a = gradient ? lerpColor(c1, c2, wave(x1 + y1 + z1, t)) : 0xFF000000 | c1;
            int b = gradient ? lerpColor(c1, c2, wave(x2 + y2 + z2, t)) : 0xFF000000 | c1;
            lines.vertex(entry, (float) (x1 + ox), (float) (y1 + oy), (float) (z1 + oz))
                    .color((a >> 16) & 0xFF, (a >> 8) & 0xFF, a & 0xFF, 230).normal(entry, nx, ny, nz);
            lines.vertex(entry, (float) (x2 + ox), (float) (y2 + oy), (float) (z2 + oz))
                    .color((b >> 16) & 0xFF, (b >> 8) & 0xFF, b & 0xFF, 230).normal(entry, nx, ny, nz);
        });
        return false;
    }

    private static float wave(double p, double t) {
        return (float) (0.5 - 0.5 * Math.cos((p * 0.5 + t) * Math.PI * 2));
    }
}
