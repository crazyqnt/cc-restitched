/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2021. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.shared.media.items.ItemPrintout;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import static dan200.computercraft.client.gui.FixedWidthFontRenderer.FONT_HEIGHT;
import static dan200.computercraft.client.gui.FixedWidthFontRenderer.FONT_WIDTH;
import static dan200.computercraft.client.render.PrintoutRenderer.*;
import static dan200.computercraft.shared.media.items.ItemPrintout.LINES_PER_PAGE;
import static dan200.computercraft.shared.media.items.ItemPrintout.LINE_MAX_LENGTH;

/**
 * Emulates map and item-frame rendering for printouts.
 */
public final class ItemPrintoutRenderer extends ItemMapLikeRenderer
{
    public static final ItemPrintoutRenderer INSTANCE = new ItemPrintoutRenderer();

    private ItemPrintoutRenderer()
    {
    }

    @Override
    protected void renderItem( MatrixStack transform, VertexConsumerProvider render, ItemStack stack, int light )
    {
        transform.multiply( Vec3f.POSITIVE_X.getDegreesQuaternion( 180f ) );
        transform.scale( 0.42f, 0.42f, -0.42f );
        transform.translate( -0.5f, -0.48f, 0.0f );

        drawPrintout( transform, render, stack, light );
    }

    private static void drawPrintout( MatrixStack transform, VertexConsumerProvider render, ItemStack stack, int light )
    {
        int pages = ItemPrintout.getPageCount( stack );
        boolean book = ((ItemPrintout) stack.getItem()).getType() == ItemPrintout.Type.BOOK;

        double width = LINE_MAX_LENGTH * FONT_WIDTH + X_TEXT_MARGIN * 2;
        double height = LINES_PER_PAGE * FONT_HEIGHT + Y_TEXT_MARGIN * 2;

        // Non-books will be left aligned
        if( !book )
        {
            width += offsetAt( pages );
        }

        double visualWidth = width, visualHeight = height;

        // Meanwhile books will be centred
        if( book )
        {
            visualWidth += 2 * COVER_SIZE + 2 * offsetAt( pages );
            visualHeight += 2 * COVER_SIZE;
        }

        double max = Math.max( visualHeight, visualWidth );

        // Scale the printout to fit correctly.
        float scale = (float) (1.0 / max);
        transform.scale( scale, scale, scale );
        transform.translate( (max - width) / 2.0, (max - height) / 2.0, 0.0 );

        Matrix4f matrix = transform.peek()
            .getPositionMatrix();
        drawBorder( matrix, render, 0, 0, -0.01f, 0, pages, book, light );
        drawText( matrix, render, X_TEXT_MARGIN, Y_TEXT_MARGIN, 0, light, ItemPrintout.getText( stack ), ItemPrintout.getColours( stack ) );
    }

    public boolean renderInFrame( MatrixStack matrixStack, VertexConsumerProvider consumerProvider, ItemStack stack, int light )
    {
        if( !(stack.getItem() instanceof ItemPrintout) )
        {
            return false;
        }

        // Move a little bit forward to ensure we're not clipping with the frame
        matrixStack.translate( 0.0f, 0.0f, -0.001f );
        matrixStack.multiply( Vec3f.POSITIVE_Z.getDegreesQuaternion( 180f ) );
        matrixStack.scale( 0.95f, 0.95f, -0.95f );
        matrixStack.translate( -0.5f, -0.5f, 0.0f );

        drawPrintout( matrixStack, consumerProvider, stack, light );

        return true;
    }
}
