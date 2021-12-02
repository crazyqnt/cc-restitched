/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2021. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */

package dan200.computercraft.client.render;

import dan200.computercraft.shared.peripheral.monitor.TileMonitor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import java.util.EnumSet;
import static net.minecraft.util.math.Direction.*;

/**
 * Overrides monitor highlighting to only render the outline of the <em>whole</em> monitor, rather than the current block. This means you do not get an
 * intrusive outline on top of the screen.
 */
@Environment( EnvType.CLIENT )
public final class MonitorHighlightRenderer
{
    private MonitorHighlightRenderer()
    {
    }

    public static boolean drawHighlight( MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double d, double e, double f, BlockPos pos, BlockState blockState )
    {
        // Preserve normal behaviour when crouching.
        if( entity.isInSneakingPose() )
        {
            return false;
        }

        World world = entity.getEntityWorld();

        BlockEntity tile = world.getBlockEntity( pos );
        if( !(tile instanceof TileMonitor monitor) )
        {
            return false;
        }

        // Determine which sides are part of the external faces of the monitor, and so which need to be rendered.
        EnumSet<Direction> faces = EnumSet.allOf( Direction.class );
        Direction front = monitor.getFront();
        faces.remove( front );
        if( monitor.getXIndex() != 0 )
        {
            faces.remove( monitor.getRight()
                .getOpposite() );
        }
        if( monitor.getXIndex() != monitor.getWidth() - 1 )
        {
            faces.remove( monitor.getRight() );
        }
        if( monitor.getYIndex() != 0 )
        {
            faces.remove( monitor.getDown()
                .getOpposite() );
        }
        if( monitor.getYIndex() != monitor.getHeight() - 1 )
        {
            faces.remove( monitor.getDown() );
        }

        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera()
            .getPos();
        matrixStack.push();
        matrixStack.translate( pos.getX() - cameraPos.getX(), pos.getY() - cameraPos.getY(), pos.getZ() - cameraPos.getZ() );

        // I wish I could think of a better way to do this
        Matrix4f transform = matrixStack.peek()
            .getPositionMatrix();
        Matrix3f normal = matrixStack.peek().getNormalMatrix();
        if( faces.contains( NORTH ) || faces.contains( WEST ) )
        {
            line( vertexConsumer, transform, normal, 0, 0, 0, UP );
        }
        if( faces.contains( SOUTH ) || faces.contains( WEST ) )
        {
            line( vertexConsumer, transform, normal, 0, 0, 1, UP );
        }
        if( faces.contains( NORTH ) || faces.contains( EAST ) )
        {
            line( vertexConsumer, transform, normal, 1, 0, 0, UP );
        }
        if( faces.contains( SOUTH ) || faces.contains( EAST ) )
        {
            line( vertexConsumer, transform, normal, 1, 0, 1, UP );
        }
        if( faces.contains( NORTH ) || faces.contains( DOWN ) )
        {
            line( vertexConsumer, transform, normal, 0, 0, 0, EAST );
        }
        if( faces.contains( SOUTH ) || faces.contains( DOWN ) )
        {
            line( vertexConsumer, transform, normal, 0, 0, 1, EAST );
        }
        if( faces.contains( NORTH ) || faces.contains( UP ) )
        {
            line( vertexConsumer, transform, normal, 0, 1, 0, EAST );
        }
        if( faces.contains( SOUTH ) || faces.contains( UP ) )
        {
            line( vertexConsumer, transform, normal, 0, 1, 1, EAST );
        }
        if( faces.contains( WEST ) || faces.contains( DOWN ) )
        {
            line( vertexConsumer, transform, normal, 0, 0, 0, SOUTH );
        }
        if( faces.contains( EAST ) || faces.contains( DOWN ) )
        {
            line( vertexConsumer, transform, normal, 1, 0, 0, SOUTH );
        }
        if( faces.contains( WEST ) || faces.contains( UP ) )
        {
            line( vertexConsumer, transform, normal, 0, 1, 0, SOUTH );
        }
        if( faces.contains( EAST ) || faces.contains( UP ) )
        {
            line( vertexConsumer, transform, normal, 1, 1, 0, SOUTH );
        }

        matrixStack.pop();

        return true;
    }

    private static void line( VertexConsumer buffer, Matrix4f transform, Matrix3f normal, float x, float y, float z, Direction direction )
    {
        buffer.vertex( transform, x, y, z )
            .color( 0, 0, 0, 0.4f )
            .normal( normal, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ() )
            .next();
        buffer.vertex( transform, x + direction.getOffsetX(), y + direction.getOffsetY(), z + direction.getOffsetZ() )
            .color( 0, 0, 0, 0.4f )
            .normal( normal, direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ() )
            .next();
    }
}
