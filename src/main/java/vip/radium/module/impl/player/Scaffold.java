package vip.radium.module.impl.player;

import io.github.nevalackin.homoBus.Listener;
import io.github.nevalackin.homoBus.annotations.EventLink;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.RandomUtils;
import vip.radium.event.EventBusPriorities;
import vip.radium.event.impl.player.SafeWalkEvent;
import vip.radium.event.impl.player.SprintEvent;
import vip.radium.event.impl.player.UpdatePositionEvent;
import vip.radium.event.impl.player.WindowClickEvent;
import vip.radium.event.impl.render.overlay.Render2DEvent;
import vip.radium.event.impl.world.TickEvent;
import vip.radium.module.Module;
import vip.radium.module.ModuleCategory;
import vip.radium.module.ModuleInfo;
import vip.radium.module.impl.movement.Speed;
import vip.radium.property.Property;
import vip.radium.property.impl.DoubleProperty;
import vip.radium.property.impl.EnumProperty;
import vip.radium.property.impl.Representation;
import vip.radium.utils.*;
import vip.radium.utils.render.LockedResolution;
import vip.radium.utils.render.RenderingUtils;

import java.util.Random;

@ModuleInfo(label = "Scaffold", category = ModuleCategory.PLAYER)
public final class Scaffold extends Module {

    private static final BlockPos[] BLOCK_POSITIONS = new BlockPos[]{
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1)};

    private static final EnumFacing[] FACINGS = new EnumFacing[]{
            EnumFacing.EAST,
            EnumFacing.WEST,
            EnumFacing.SOUTH,
            EnumFacing.NORTH};

    private final EnumProperty<PlaceEventMode> placeEventModeProperty = new EnumProperty<>("Place Event", PlaceEventMode.TICK);
    private final EnumProperty<RotationsMode> rotationsModeProperty = new EnumProperty<>("Rotations", RotationsMode.WATCHDOG);
    private final EnumProperty<RaycastMode> raycastModeProperty = new EnumProperty<>("Raycast", RaycastMode.NONE);
    private final Property<Boolean> sprintProperty = new Property<>("Sprint", true);
    private final Property<Boolean> jumpProperty = new Property<>("Jump", false);
    private final Property<Boolean> swingProperty = new Property<>("Swing", true);
    private final Property<Boolean> keepYProperty = new Property<>("Keep Y", false);
    private final Property<Boolean> safeWalkProperty = new Property<>("Safe Walk", false);
    private final Property<Boolean> towerProperty = new Property<>("Tower", true);
    private final Property<Boolean> blockCountBarProperty = new Property<>("Block Count", true);
    private final Property<Integer> blockBarColor = new Property<>("Bar Color", 0xFFFF0000, blockCountBarProperty::getValue);
    private final DoubleProperty jumpEveryXBlocksProperty = new DoubleProperty("Jump Every X Blocks" , 1, jumpProperty::getValue, 1, 9, 1);
    private final DoubleProperty delayTicksProperty = new DoubleProperty("Delay Ticks", 1, 1, 5, 1, Representation.INT);
    private final DoubleProperty blockSlotProperty = new DoubleProperty("Override Slot", 9, 1, 9, 1);

    private final TimerUtil clickTimer = new TimerUtil();

    private boolean placed = false;
    private boolean isOnRightSide;
    private int blockCount;
    private int originalHotBarSlot;
    private int bestBlockStack;
    private BlockData data;
    private float[] angles;
    private float[] rotation;
    private int placeCounter;
    private int jumpPlaceCounter;
    private int ticksSincePlace;
    private int lastPos;
    private boolean towering;

    public Scaffold() {
        setSuffixListener(rotationsModeProperty);

    }

    @EventLink
    public final Listener<WindowClickEvent> onWindowClickEvent = event ->
            clickTimer.reset();

    @EventLink
    public final Listener<SafeWalkEvent> onSafeWalkEvent = safeWalkEvent ->
            safeWalkEvent.setCancelled(safeWalkProperty.getValue());

    @EventLink(EventBusPriorities.LOW)
    public final Listener<TickEvent> tickEventListener = e -> {
        if(placeEventModeProperty.getValue() != PlaceEventMode.TICK) return;
        if (data != null && bestBlockStack >= InventoryUtils.ONLY_HOT_BAR_BEGIN) {
            final EntityPlayerSP player = mc.thePlayer();

            if (++ticksSincePlace < delayTicksProperty.getValue()) return;

            player.inventory.currentItem = bestBlockStack - InventoryUtils.ONLY_HOT_BAR_BEGIN;

            if(raycastModeProperty.getValue() == RaycastMode.STRICT || raycastModeProperty.getValue() == RaycastMode.SILENT) {
                if (angles == null || rotation == null) {
                    if (mc.getPlayerController().onPlayerRightClick(player, mc.getWorld(),
                            player.getCurrentEquippedItem(),
                            data.pos, data.face, data.hitVec)) {
                        placeCounter++;
                        jumpPlaceCounter++;

                        this.towering = towerProperty.getValue() && mc.getGameSettings().keyBindJump.isKeyDown();

                        if (this.towering && MovementUtils.isDistFromGround(0.0626) &&
                                (placeCounter % 4 != 0)) {
                            player.motionY = MovementUtils.getJumpHeight() - 0.000454352838557992;
                        }


                        if (swingProperty.getValue()) player.swingItem();
                        else mc.sendPacket(new C0APacketAnimation());
                        placed = true;

                        ticksSincePlace = 0;
                    }
                }
                if (RotationUtils.isLookingAtBlock(data.face, data.pos, raycastModeProperty.getValue() == RaycastMode.STRICT,
                        4.5f, angles[0], angles[1])) {
                    if (mc.getPlayerController().onPlayerRightClick(player, mc.getWorld(),
                            player.getCurrentEquippedItem(),
                            data.pos, data.face, data.hitVec)) {
                        placeCounter++;
                        jumpPlaceCounter++;

                        this.towering = towerProperty.getValue() && mc.getGameSettings().keyBindJump.isKeyDown();

                        if (this.towering && MovementUtils.isDistFromGround(0.0626) &&
                                (placeCounter % 4 != 0)) {
                            player.motionY = MovementUtils.getJumpHeight() - 0.000454352838557992;
                        }


                        if (swingProperty.getValue()) player.swingItem();
                        else mc.sendPacket(new C0APacketAnimation());
                        placed = true;

                        ticksSincePlace = 0;
                    }
                }
            } else {

                if (mc.getPlayerController().onPlayerRightClick(player, mc.getWorld(),
                        player.getCurrentEquippedItem(),
                        data.pos, data.face, data.hitVec)) {
                    placeCounter++;
                    jumpPlaceCounter++;

                    this.towering = towerProperty.getValue() && mc.getGameSettings().keyBindJump.isKeyDown();

                    if (this.towering && MovementUtils.isDistFromGround(0.0626) &&
                            (placeCounter % 4 != 0)) {
                        player.motionY = MovementUtils.getJumpHeight() - 0.000454352838557992;
                    }


                    if (swingProperty.getValue()) player.swingItem();
                    else mc.sendPacket(new C0APacketAnimation());
                    placed = true;

                    ticksSincePlace = 0;
                }
            }
        }
    };

    @EventLink(EventBusPriorities.LOW)
    public final Listener<SprintEvent> sprintEventListener = e -> {
        if(mc.thePlayer().onGround && jumpProperty.getValue() && MovementUtils.isMoving() && jumpPlaceCounter == jumpEveryXBlocksProperty.getValue()) {
            mc.thePlayer().jump();
            jumpPlaceCounter = 0;
        }
    };

    @EventLink
    public final Listener<Render2DEvent> onRender2DEvent = event -> {
        if (blockCountBarProperty.getValue()) {
            final LockedResolution resolution = event.getResolution();
            final float x = resolution.getWidth() / 2.0F;
            final float y = resolution.getHeight() / 2.0F + 13;
            final float thickness = 2.5F;

            float percentage = Math.min(1, this.blockCount / 128.0F);

            final float width = 80.0F;
            final float half = width / 2;

            Gui.drawRect(x - half - 0.5, y - 0.5, x + half + 0.5, y + thickness + 0.5, 0x78000000);

            final int color = blockBarColor.getValue();

            RenderingUtils.drawGradientRect(x - half, y, x - half + width * percentage, y + thickness, false,
                    color, RenderingUtils.darker(color));
        }
    };

    @EventLink(EventBusPriorities.HIGHEST)
    public final Listener<UpdatePositionEvent> onUpdatePositionEvent = event -> {
        if(placeEventModeProperty.getValue() == PlaceEventMode.PRE) {
            if (!event.isPre()) return;
            if (data != null && bestBlockStack >= InventoryUtils.ONLY_HOT_BAR_BEGIN) {
                final EntityPlayerSP player = mc.thePlayer();

                if (++ticksSincePlace < delayTicksProperty.getValue()) return;

                player.inventory.currentItem = bestBlockStack - InventoryUtils.ONLY_HOT_BAR_BEGIN;

                if (raycastModeProperty.getValue() == RaycastMode.STRICT || raycastModeProperty.getValue() == RaycastMode.SILENT) {
                    if (angles == null || rotation == null) {
                        if (mc.getPlayerController().onPlayerRightClick(player, mc.getWorld(),
                                player.getCurrentEquippedItem(),
                                data.pos, data.face, data.hitVec)) {
                            placeCounter++;
                            jumpPlaceCounter++;

                            this.towering = towerProperty.getValue() && mc.getGameSettings().keyBindJump.isKeyDown();

                            if (this.towering && MovementUtils.isDistFromGround(0.0626) &&
                                    (placeCounter % 4 != 0)) {
                                player.motionY = MovementUtils.getJumpHeight() - 0.000454352838557992;
                            }


                            if (swingProperty.getValue()) player.swingItem();
                            else mc.sendPacket(new C0APacketAnimation());
                            placed = true;

                            ticksSincePlace = 0;
                        }
                    }
                    if (RotationUtils.isLookingAtBlock(data.face, data.pos, raycastModeProperty.getValue() == RaycastMode.STRICT,
                            4.5f, angles[0], angles[1])) {
                        if (mc.getPlayerController().onPlayerRightClick(player, mc.getWorld(),
                                player.getCurrentEquippedItem(),
                                data.pos, data.face, data.hitVec)) {
                            placeCounter++;
                            jumpPlaceCounter++;

                            this.towering = towerProperty.getValue() && mc.getGameSettings().keyBindJump.isKeyDown();

                            if (this.towering && MovementUtils.isDistFromGround(0.0626) &&
                                    (placeCounter % 4 != 0)) {
                                player.motionY = MovementUtils.getJumpHeight() - 0.000454352838557992;
                            }


                            if (swingProperty.getValue()) player.swingItem();
                            else mc.sendPacket(new C0APacketAnimation());
                            placed = true;

                            ticksSincePlace = 0;
                        }
                    }
                } else {

                    if (mc.getPlayerController().onPlayerRightClick(player, mc.getWorld(),
                            player.getCurrentEquippedItem(),
                            data.pos, data.face, data.hitVec)) {
                        placeCounter++;
                        jumpPlaceCounter++;

                        this.towering = towerProperty.getValue() && mc.getGameSettings().keyBindJump.isKeyDown();

                        if (this.towering && MovementUtils.isDistFromGround(0.0626) &&
                                (placeCounter % 4 != 0)) {
                            player.motionY = MovementUtils.getJumpHeight() - 0.000454352838557992;
                        }


                        if (swingProperty.getValue()) player.swingItem();
                        else mc.sendPacket(new C0APacketAnimation());
                        placed = true;

                        ticksSincePlace = 0;
                    }
                }
            }
        }

        if (!placed) {
            rotation = new float[]{90, RotationUtils.getScaffoldYaw()};
        }
        if (event.isPre()) {
            if (sprintProperty.getValue().equals(false)) {
                mc.thePlayer().setSprinting(false);
                mc.getNetHandler().sendPacket(new C0BPacketEntityAction(mc.thePlayer(), C0BPacketEntityAction.Action.STOP_SPRINTING));
            } else mc.thePlayer().setSprinting(true);

            updateBlockCount();

            this.data = null;

            bestBlockStack = findBestBlockStack(InventoryUtils.ONLY_HOT_BAR_BEGIN, InventoryUtils.END);

            if (bestBlockStack == -1 && clickTimer.hasElapsed(250)) {
                bestBlockStack = findBestBlockStack(InventoryUtils.EXCLUDE_ARMOR_BEGIN, InventoryUtils.ONLY_HOT_BAR_BEGIN);

                if (bestBlockStack == -1) {
                    return;
                }

                boolean override = true;
                for (int i = InventoryUtils.END - 1; i >= InventoryUtils.ONLY_HOT_BAR_BEGIN; i--) {
                    final ItemStack stack = mc.getStackInSlot(i);

                    if (!InventoryUtils.isValid(stack, true)) {
                        InventoryUtils.windowClick(bestBlockStack, i - InventoryUtils.ONLY_HOT_BAR_BEGIN,
                                InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                        bestBlockStack = i;
                        override = false;
                        clickTimer.reset();
                        break;
                    }
                }

                if (override) {
                    int blockSlot = blockSlotProperty.getValue().intValue() - 1;
                    InventoryUtils.windowClick(bestBlockStack, blockSlot,
                            InventoryUtils.ClickType.SWAP_WITH_HOT_BAR_SLOT);
                    bestBlockStack = blockSlot + InventoryUtils.ONLY_HOT_BAR_BEGIN;
                    clickTimer.reset();
                }
            }

            if (bestBlockStack >= InventoryUtils.ONLY_HOT_BAR_BEGIN) {
                final BlockPos blockUnder = getBlockUnder();
                BlockData data = getBlockData(blockUnder);

                if (data != null && bestBlockStack >= 36) {
                    if (validateReplaceable(data) && data.hitVec != null) {
                        if (rotationsModeProperty.getValue() == RotationsMode.RADS) {
                            angles = getRadsRotations(data);
                        }
                        if (rotationsModeProperty.getValue() == RotationsMode.GODBRIDGE) {
                            angles = getGodBridgeRotations(data);
                        }
                        if (rotationsModeProperty.getValue() == RotationsMode.WATCHDOG) {
                            angles = getWatchdogRotations(data);
                        }
                    } else {
                        data = null;
                    }
                }

                    if (angles != null) {
                        RotationUtils.rotate(event, angles, 90.0F, false);
                    }
                this.data = data;
            }
        }
        ;
    };

    private static int findBestBlockStack(int start, int end) {
        int bestSlot = -1;
        int blockCount = -1;

        for (int i = end - 1; i >= start; --i) {
            ItemStack stack = mc.getStackInSlot(i);

            if (stack != null &&
                    stack.getItem() instanceof ItemBlock &&
                    InventoryUtils.isGoodBlockStack(stack)) {
                if (stack.stackSize > blockCount) {
                    bestSlot = i;
                    blockCount = stack.stackSize;
                }
            }
        }

        return bestSlot;
    }

    private BlockPos getBlockUnder() {
        final EntityPlayerSP player = mc.thePlayer();
        final boolean useLastPos = this.keepYProperty.getValue() && !towering;
        final double playerPos = player.posY - 1.0;
        if (!useLastPos)
            lastPos = (int) player.posY;
        return new BlockPos(player.posX, useLastPos ? Math.min(lastPos, playerPos) : playerPos, player.posZ);
    }

    private float[] getGodBridgeRotations(final BlockData data) {
        
        float pitch = 78;
        
            for (float possiblePitch = 90; possiblePitch > 30; possiblePitch -= possiblePitch > (mc.thePlayer().isPotionActive(Potion.moveSpeed) ? 60 : 80) ? 1 : 10) {
                if (RotationUtils.isLookingAtBlock(data.face, data.pos, true, 5, RotationUtils.getScaffoldYaw() - 15, possiblePitch)) {
                    pitch = possiblePitch;
                }
            
        }
        
        return new float[]{
                (float) RotationUtils.getScaffoldYaw() - 15,
                (float) pitch
        };
    }

    private float[] getWatchdogRotations(final BlockData data) {

        final EntityPlayerSP player = mc.thePlayer();

        final Vec3 hitVec = data.hitVec;

        final double xDif = hitVec.xCoord - player.posX;
        final double zDif = hitVec.zCoord - player.posZ;

        final double yDif = hitVec.yCoord - (player.posY + player.getEyeHeight());
        final double xzDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);
        return new float[]{
                (float) RotationUtils.getScaffoldYaw() - 80,
                (float) (-(StrictMath.atan2(yDif, xzDist) * RotationUtils.TO_RADS))
        };
    }

    private float[] getRadsRotations(final BlockData data) {
        final EntityPlayerSP player = mc.thePlayer();

        final Vec3 hitVec = data.hitVec;

        final double xDif = hitVec.xCoord - player.posX;
        final double zDif = hitVec.zCoord - player.posZ;

        final double yDif = hitVec.yCoord - (player.posY + player.getEyeHeight());
        final double xzDist = StrictMath.sqrt(xDif * xDif + zDif * zDif);
        return new float[]{
                (float) RotationUtils.getScaffoldYaw() - 15,
                (float) (-(StrictMath.atan2(yDif, xzDist) * RotationUtils.TO_RADS))
        };
    }

    private static boolean validateBlockRange(final BlockData data) {
        final Vec3 pos = data.hitVec;
        if (pos == null)
            return false;
        final EntityPlayerSP player = mc.thePlayer();
        final double x = (pos.xCoord - player.posX);
        final double y = (pos.yCoord - (player.posY + player.getEyeHeight()));
        final double z = (pos.zCoord - player.posZ);
        return StrictMath.sqrt(x * x + y * y + z * z) <= 5.0D;
    }

    private static boolean validateReplaceable(final BlockData data) {
        final BlockPos pos = data.pos.offset(data.face);
        final World world = mc.getWorld();
        return world.getBlockState(pos)
                .getBlock()
                .isReplaceable(world, pos);
    }

    private static BlockData getBlockData(final BlockPos pos) {
        final BlockPos[] blockPositions = BLOCK_POSITIONS;
        final EnumFacing[] facings = FACINGS;
        final WorldClient world = mc.getWorld();

        // 1 of the 4 directions around player
        for (int i = 0; i < blockPositions.length; i++) {
            final BlockPos blockPos = pos.add(blockPositions[i]);
            if (InventoryUtils.isValidBlock(world.getBlockState(blockPos).getBlock(), false)) {
                final BlockData data = new BlockData(blockPos, facings[i]);
                if (validateBlockRange(data))
                    return data;
            }
        }

        // 2 Blocks Under e.g. When jumping
        final BlockPos posBelow = pos.add(0, -1, 0);
        if (InventoryUtils.isValidBlock(world.getBlockState(posBelow).getBlock(), false)) {
            final BlockData data = new BlockData(posBelow, EnumFacing.UP);
            if (validateBlockRange(data))
                return data;
        }

        // 2 Block extension & diagonal
        for (BlockPos blockPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blockPosition);
            for (int i = 0; i < blockPositions.length; i++) {
                final BlockPos blockPos1 = blockPos.add(blockPositions[i]);
                if (InventoryUtils.isValidBlock(world.getBlockState(blockPos1).getBlock(), false)) {
                    final BlockData data = new BlockData(blockPos1, facings[i]);
                    if (validateBlockRange(data))
                        return data;
                }
            }
        }

        // 3 Block extension
        for (final BlockPos blockPosition : blockPositions) {
            final BlockPos blockPos = pos.add(blockPosition);
            for (final BlockPos position : blockPositions) {
                final BlockPos blockPos1 = blockPos.add(position);
                for (int i = 0; i < blockPositions.length; i++) {
                    final BlockPos blockPos2 = blockPos1.add(blockPositions[i]);
                    if (InventoryUtils.isValidBlock(world.getBlockState(blockPos2).getBlock(), false)) {
                        final BlockData data = new BlockData(blockPos2, facings[i]);
                        if (validateBlockRange(data))
                            return data;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public void onEnable() {
        blockCount = 0;
        placeCounter = 0;
        jumpPlaceCounter = 0;
        ticksSincePlace = 0;
        lastPos = (int) mc.thePlayer().posY;
        originalHotBarSlot = mc.thePlayer().inventory.currentItem;
    }

    @Override
    public void onDisable() {
        angles = null;
        mc.thePlayer().inventory.currentItem = originalHotBarSlot;
    }

    public boolean isRotating() {
        return angles != null;
    }

    private void updateBlockCount() {
        blockCount = 0;

        for (int i = InventoryUtils.EXCLUDE_ARMOR_BEGIN; i < InventoryUtils.END; i++) {
            final ItemStack stack = mc.getStackInSlot(i);

            if (stack != null && stack.getItem() instanceof ItemBlock &&
                    InventoryUtils.isGoodBlockStack(stack))
                blockCount += stack.stackSize;
        }
    }

    private static class BlockData {
        private final BlockPos pos;
        private final Vec3 hitVec;
        private final EnumFacing face;

        public BlockData(BlockPos pos, EnumFacing face) {
            this.pos = pos;
            this.face = face;
            this.hitVec = getHitVec();
        }

        private Vec3 getHitVec() {
            final Vec3i directionVec = face.getDirectionVec();
            double x = directionVec.getX() * 0.5D;
            double z = directionVec.getZ() * 0.5D;

            if (face.getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE) {
                x = -x;
                z = -z;
            }

            final Vec3 hitVec = new Vec3(pos).addVector(x + z, directionVec.getY() * 0.5D, x + z);

            final Vec3 src = mc.thePlayer().getPositionEyes(1.0F);
            final MovingObjectPosition obj = mc.getWorld().rayTraceBlocks(src,
                    hitVec,
                    false,
                    false,
                    true);

            if (obj == null || obj.hitVec == null || obj.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK)
                return null;

            switch (face.getAxis()) {
                case Z:
                    obj.hitVec = new Vec3(obj.hitVec.xCoord, obj.hitVec.yCoord, Math.round(obj.hitVec.zCoord));
                    break;
                case X:
                    obj.hitVec = new Vec3(Math.round(obj.hitVec.xCoord), obj.hitVec.yCoord, obj.hitVec.zCoord);
                    break;
            }

            if (face != EnumFacing.DOWN && face != EnumFacing.UP) {
                final IBlockState blockState = mc.getWorld().getBlockState(obj.getBlockPos());
                final Block blockAtPos = blockState.getBlock();

                double blockFaceOffset;

                if (blockAtPos instanceof BlockSlab && !((BlockSlab) blockAtPos).isDouble()) {
                    final BlockSlab.EnumBlockHalf half = blockState.getValue(BlockSlab.HALF);

                    blockFaceOffset = RandomUtils.nextDouble(0.1, 0.4);

                    if (half == BlockSlab.EnumBlockHalf.TOP) {
                        blockFaceOffset += 0.5;
                    }
                } else {
                    blockFaceOffset = RandomUtils.nextDouble(0.1, 0.9);
                }

                obj.hitVec = obj.hitVec.addVector(0.0D, -blockFaceOffset, 0.0D);
            }

            return obj.hitVec;
        }
    }

    private enum PlaceEventMode {
        TICK, PRE
    }

    private enum RaycastMode {
        STRICT, SILENT, NONE;
    }

    private enum RotationsMode {
        RADS, WATCHDOG, GODBRIDGE;
    }
}