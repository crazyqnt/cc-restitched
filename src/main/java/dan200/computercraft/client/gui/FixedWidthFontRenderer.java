/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2021. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.gui;

import dan200.computercraft.client.FrameInfo;
import dan200.computercraft.client.render.RenderTypes;
import dan200.computercraft.core.terminal.Terminal;
import dan200.computercraft.core.terminal.TextBuffer;
import dan200.computercraft.shared.util.Colour;
import dan200.computercraft.shared.util.Palette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Matrix4f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static dan200.computercraft.client.render.RenderTypes.FULL_BRIGHT_LIGHTMAP;

public final class FixedWidthFontRenderer
{
    public static final int FONT_HEIGHT = 9;
    public static final int FONT_WIDTH = 6;
    public static final float WIDTH = 256.0f;
    public static final float BACKGROUND_START = (WIDTH - 6.0f) / WIDTH;
    public static final float BACKGROUND_END = (WIDTH - 4.0f) / WIDTH;
    private static final Matrix4f IDENTITY = AffineTransformation.identity()
        .getMatrix();
    public static final Identifier FONT = new Identifier( "computercraft", "textures/gui/term_font.png" );


    private FixedWidthFontRenderer()
    {
    }

    public static void drawString( @Nonnull Matrix4f transform, @Nonnull VertexConsumer renderer, float x, float y, @Nonnull TextBuffer text,
                                   @Nonnull TextBuffer textColour, @Nullable TextBuffer backgroundColour, @Nonnull Palette palette, boolean greyscale,
                                   float leftMarginSize, float rightMarginSize, int light )
    {
        if( backgroundColour != null )
        {
            drawBackground( transform, renderer, x, y, backgroundColour, palette, greyscale, leftMarginSize, rightMarginSize, FONT_HEIGHT );
        }

        for( int i = 0; i < text.length(); i++ )
        {
            double[] colour = palette.getColour( getColour( textColour.charAt( i ), Colour.BLACK ) );
            float r, g, b;
            if( greyscale )
            {
                r = g = b = toGreyscale( colour );
            }
            else
            {
                r = (float) colour[0];
                g = (float) colour[1];
                b = (float) colour[2];
            }

            // Draw char
            int index = text.charAt( i );
            if( index > 255 )
            {
                index = '?';
            }
            drawChar( transform, renderer, x + i * FONT_WIDTH, y, index, r, g, b, light );
        }

    }

    private static void drawBackground( @Nonnull Matrix4f transform, @Nonnull VertexConsumer renderer, float x, float y,
                                        @Nonnull TextBuffer backgroundColour, @Nonnull Palette palette, boolean greyscale, float leftMarginSize,
                                        float rightMarginSize, float height )
    {
        if( leftMarginSize > 0 )
        {
            drawQuad( transform, renderer, x - leftMarginSize, y, leftMarginSize, height, palette, greyscale, backgroundColour.charAt( 0 ) );
        }

        if( rightMarginSize > 0 )
        {
            drawQuad( transform,
                renderer,
                x + backgroundColour.length() * FONT_WIDTH,
                y,
                rightMarginSize,
                height,
                palette,
                greyscale,
                backgroundColour.charAt( backgroundColour.length() - 1 ) );
        }

        // Batch together runs of identical background cells.
        int blockStart = 0;
        char blockColour = '\0';
        for( int i = 0; i < backgroundColour.length(); i++ )
        {
            char colourIndex = backgroundColour.charAt( i );
            if( colourIndex == blockColour )
            {
                continue;
            }

            if( blockColour != '\0' )
            {
                drawQuad( transform, renderer, x + blockStart * FONT_WIDTH, y, FONT_WIDTH * (i - blockStart), height, palette, greyscale, blockColour );
            }

            blockColour = colourIndex;
            blockStart = i;
        }

        if( blockColour != '\0' )
        {
            drawQuad( transform,
                renderer,
                x + blockStart * FONT_WIDTH,
                y,
                FONT_WIDTH * (backgroundColour.length() - blockStart),
                height,
                palette,
                greyscale,
                blockColour );
        }
    }

    public static int getColour( char c, Colour def )
    {
        return 15 - Terminal.getColour( c, def );
    }

    public static float toGreyscale( double[] rgb )
    {
        return (float) ((rgb[0] + rgb[1] + rgb[2]) / 3);
    }

    private static void drawChar( Matrix4f transform, VertexConsumer buffer, float x, float y, int index, float r, float g, float b, int light )
    {
        // Short circuit to avoid the common case - the texture should be blank here after all.
        if( index == '\0' || index == ' ' )
        {
            return;
        }

        int column = index % 16;
        int row = index / 16;

        int xStart = 1 + column * (FONT_WIDTH + 2);
        int yStart = 1 + row * (FONT_HEIGHT + 2);

        buffer.vertex( transform, x, y, 0f )
            .color( r, g, b, 1.0f )
            .texture( xStart / WIDTH, yStart / WIDTH )
            .light( light )
            .next();
        buffer.vertex( transform, x, y + FONT_HEIGHT, 0f )
            .color( r, g, b, 1.0f )
            .texture( xStart / WIDTH, (yStart + FONT_HEIGHT) / WIDTH )
            .light( light )
            .next();
        buffer.vertex( transform, x + FONT_WIDTH, y, 0f )
            .color( r, g, b, 1.0f )
            .texture( (xStart + FONT_WIDTH) / WIDTH, yStart / WIDTH )
            .light( light )
            .next();
        buffer.vertex( transform, x + FONT_WIDTH, y, 0f )
            .color( r, g, b, 1.0f )
            .texture( (xStart + FONT_WIDTH) / WIDTH, yStart / WIDTH )
            .light( light )
            .next();
        buffer.vertex( transform, x, y + FONT_HEIGHT, 0f )
            .color( r, g, b, 1.0f )
            .texture( xStart / WIDTH, (yStart + FONT_HEIGHT) / WIDTH )
            .light( light )
            .next();
        buffer.vertex( transform, x + FONT_WIDTH, y + FONT_HEIGHT, 0f )
            .color( r, g, b, 1.0f )
            .texture( (xStart + FONT_WIDTH) / WIDTH, (yStart + FONT_HEIGHT) / WIDTH )
            .light( light )
            .next();
    }

    private static void drawQuad( Matrix4f transform, VertexConsumer buffer, float x, float y, float width, float height, Palette palette,
                                  boolean greyscale, char colourIndex )
    {
        double[] colour = palette.getColour( getColour( colourIndex, Colour.BLACK ) );
        float r, g, b;
        if( greyscale )
        {
            r = g = b = toGreyscale( colour );
        }
        else
        {
            r = (float) colour[0];
            g = (float) colour[1];
            b = (float) colour[2];
        }

        drawQuad( transform, buffer, x, y, width, height, r, g, b );
    }

    private static void drawQuad( Matrix4f transform, VertexConsumer buffer, float x, float y, float width, float height, float r, float g, float b )
    {
        buffer.vertex( transform, x, y, 0 )
            .color( r, g, b, 1.0f )
            .texture( BACKGROUND_START, BACKGROUND_START )
            .next();
        buffer.vertex( transform, x, y + height, 0 )
            .color( r, g, b, 1.0f )
            .texture( BACKGROUND_START, BACKGROUND_END )
            .next();
        buffer.vertex( transform, x + width, y, 0 )
            .color( r, g, b, 1.0f )
            .texture( BACKGROUND_END, BACKGROUND_START )
            .next();
        buffer.vertex( transform, x + width, y, 0 )
            .color( r, g, b, 1.0f )
            .texture( BACKGROUND_END, BACKGROUND_START )
            .next();
        buffer.vertex( transform, x, y + height, 0 )
            .color( r, g, b, 1.0f )
            .texture( BACKGROUND_START, BACKGROUND_END )
            .next();
        buffer.vertex( transform, x + width, y + height, 0 )
            .color( r, g, b, 1.0f )
            .texture( BACKGROUND_END, BACKGROUND_END )
            .next();
    }

    public static void drawTerminalWithoutCursor( @Nonnull Matrix4f transform, @Nonnull VertexConsumer buffer, float x, float y,
                                                  @Nonnull Terminal terminal, boolean greyscale, float topMarginSize, float bottomMarginSize,
                                                  float leftMarginSize, float rightMarginSize )
    {
        Palette palette = terminal.getPalette();
        int height = terminal.getHeight();

        // Top and bottom margins
        drawBackground( transform,
            buffer,
            x,
            y - topMarginSize,
            terminal.getBackgroundColourLine( 0 ),
            palette,
            greyscale,
            leftMarginSize,
            rightMarginSize,
            topMarginSize );

        drawBackground( transform,
            buffer,
            x,
            y + height * FONT_HEIGHT,
            terminal.getBackgroundColourLine( height - 1 ),
            palette,
            greyscale,
            leftMarginSize,
            rightMarginSize,
            bottomMarginSize );

        // The main text
        for( int i = 0; i < height; i++ )
        {
            drawString( transform,
                buffer,
                x,
                y + FixedWidthFontRenderer.FONT_HEIGHT * i,
                terminal.getLine( i ),
                terminal.getTextColourLine( i ),
                terminal.getBackgroundColourLine( i ),
                palette,
                greyscale,
                leftMarginSize,
                rightMarginSize, FULL_BRIGHT_LIGHTMAP );
        }
    }

    public static void drawCursor( @Nonnull Matrix4f transform, @Nonnull VertexConsumer buffer, float x, float y, @Nonnull Terminal terminal,
                                   boolean greyscale )
    {
        Palette palette = terminal.getPalette();
        int width = terminal.getWidth();
        int height = terminal.getHeight();

        int cursorX = terminal.getCursorX();
        int cursorY = terminal.getCursorY();
        if( terminal.getCursorBlink() && cursorX >= 0 && cursorX < width && cursorY >= 0 && cursorY < height && FrameInfo.getGlobalCursorBlink() )
        {
            double[] colour = palette.getColour( 15 - terminal.getTextColour() );
            float r, g, b;
            if( greyscale )
            {
                r = g = b = toGreyscale( colour );
            }
            else
            {
                r = (float) colour[0];
                g = (float) colour[1];
                b = (float) colour[2];
            }

            drawChar( transform, buffer, x + cursorX * FONT_WIDTH, y + cursorY * FONT_HEIGHT, '_', r, g, b, FULL_BRIGHT_LIGHTMAP );
        }
    }

    public static void drawTerminal( @Nonnull Matrix4f transform, @Nonnull VertexConsumer buffer, float x, float y, @Nonnull Terminal terminal,
                                     boolean greyscale, float topMarginSize, float bottomMarginSize, float leftMarginSize, float rightMarginSize )
    {
        drawTerminalWithoutCursor( transform, buffer, x, y, terminal, greyscale, topMarginSize, bottomMarginSize, leftMarginSize, rightMarginSize );
        drawCursor( transform, buffer, x, y, terminal, greyscale );
    }

    public static void drawTerminal( @Nonnull Matrix4f transform, float x, float y, @Nonnull Terminal terminal, boolean greyscale, float topMarginSize,
                                     float bottomMarginSize, float leftMarginSize, float rightMarginSize )
    {
        VertexConsumerProvider.Immediate renderer = MinecraftClient.getInstance()
            .getBufferBuilders()
            .getEntityVertexConsumers();
        VertexConsumer buffer = renderer.getBuffer( RenderTypes.TERMINAL_WITH_DEPTH );
        drawTerminal( transform, buffer, x, y, terminal, greyscale, topMarginSize, bottomMarginSize, leftMarginSize, rightMarginSize );
        renderer.draw();
    }

    public static void drawTerminal( float x, float y, @Nonnull Terminal terminal, boolean greyscale, float topMarginSize, float bottomMarginSize,
                                     float leftMarginSize, float rightMarginSize )
    {
        drawTerminal( IDENTITY, x, y, terminal, greyscale, topMarginSize, bottomMarginSize, leftMarginSize, rightMarginSize );
    }

    public static void drawEmptyTerminal( float x, float y, float width, float height )
    {
        Colour colour = Colour.BLACK;
        drawEmptyTerminal( IDENTITY, x, y, width, height );
    }

    public static void drawEmptyTerminal( @Nonnull Matrix4f transform, float x, float y, float width, float height )
    {
        VertexConsumerProvider.Immediate renderer = MinecraftClient.getInstance()
            .getBufferBuilders()
            .getEntityVertexConsumers();
        drawEmptyTerminal( transform, renderer, x, y, width, height );
        renderer.draw();
    }

    public static void drawEmptyTerminal( @Nonnull Matrix4f transform, @Nonnull VertexConsumerProvider renderer, float x, float y, float width,
                                          float height )
    {
        Colour colour = Colour.BLACK;
        drawQuad( transform, renderer.getBuffer( RenderTypes.TERMINAL_WITH_DEPTH ), x, y, width, height, colour.getR(), colour.getG(), colour.getB() );
    }

    public static void drawBlocker( @Nonnull Matrix4f transform, @Nonnull VertexConsumerProvider renderer, float x, float y, float width, float height )
    {
        Colour colour = Colour.BLACK;
        drawQuad( transform, renderer.getBuffer( RenderTypes.TERMINAL_BLOCKER ), x, y, width, height, colour.getR(), colour.getG(), colour.getB() );
    }
}
