package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultifaceBlock extends Block {
   private static final float AABB_OFFSET = 1.0F;
   private static final VoxelShape UP_AABB = Block.box(0.0D, 15.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape DOWN_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
   private static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 0.0D, 1.0D, 16.0D, 16.0D);
   private static final VoxelShape EAST_AABB = Block.box(15.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
   private static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 1.0D);
   private static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 15.0D, 16.0D, 16.0D, 16.0D);
   private static final Map<Direction, BooleanProperty> PROPERTY_BY_DIRECTION = PipeBlock.PROPERTY_BY_DIRECTION;
   private static final Map<Direction, VoxelShape> SHAPE_BY_DIRECTION = Util.make(Maps.newEnumMap(Direction.class), (p_153923_) -> {
      p_153923_.put(Direction.NORTH, NORTH_AABB);
      p_153923_.put(Direction.EAST, EAST_AABB);
      p_153923_.put(Direction.SOUTH, SOUTH_AABB);
      p_153923_.put(Direction.WEST, WEST_AABB);
      p_153923_.put(Direction.UP, UP_AABB);
      p_153923_.put(Direction.DOWN, DOWN_AABB);
   });
   protected static final Direction[] DIRECTIONS = Direction.values();
   private final ImmutableMap<BlockState, VoxelShape> shapesCache;
   private final boolean canRotate;
   private final boolean canMirrorX;
   private final boolean canMirrorZ;

   public MultifaceBlock(BlockBehaviour.Properties pProperties) {
      super(pProperties);
      this.registerDefaultState(getDefaultMultifaceState(this.stateDefinition));
      this.shapesCache = this.getShapeForEachState(MultifaceBlock::calculateMultifaceShape);
      this.canRotate = Direction.Plane.HORIZONTAL.stream().allMatch(this::isFaceSupported);
      this.canMirrorX = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.X).filter(this::isFaceSupported).count() % 2L == 0L;
      this.canMirrorZ = Direction.Plane.HORIZONTAL.stream().filter(Direction.Axis.Z).filter(this::isFaceSupported).count() % 2L == 0L;
   }

   protected boolean isFaceSupported(Direction p_153921_) {
      return true;
   }

   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
      for(Direction direction : DIRECTIONS) {
         if (this.isFaceSupported(direction)) {
            pBuilder.add(getFaceProperty(direction));
         }
      }

   }

   /**
    * Update the provided state given the provided neighbor direction and neighbor state, returning a new state.
    * For example, fences make their connections to the passed in state if possible, and wet concrete powder immediately
    * returns its solidified counterpart.
    * Note that this method should ideally consider only the specific direction passed in.
    */
   public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos) {
      if (!hasAnyFace(pState)) {
         return Blocks.AIR.defaultBlockState();
      } else {
         return hasFace(pState, pDirection) && !canAttachTo(pLevel, pDirection, pNeighborPos, pNeighborState) ? removeFace(pState, getFaceProperty(pDirection)) : pState;
      }
   }

   public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
      return this.shapesCache.get(pState);
   }

   public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
      boolean flag = false;

      for(Direction direction : DIRECTIONS) {
         if (hasFace(pState, direction)) {
            BlockPos blockpos = pPos.relative(direction);
            if (!canAttachTo(pLevel, direction, blockpos, pLevel.getBlockState(blockpos))) {
               return false;
            }

            flag = true;
         }
      }

      return flag;
   }

   public boolean canBeReplaced(BlockState pState, BlockPlaceContext pUseContext) {
      return hasAnyVacantFace(pState);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext pContext) {
      Level level = pContext.getLevel();
      BlockPos blockpos = pContext.getClickedPos();
      BlockState blockstate = level.getBlockState(blockpos);
      return Arrays.stream(pContext.getNearestLookingDirections()).map((p_153865_) -> {
         return this.getStateForPlacement(blockstate, level, blockpos, p_153865_);
      }).filter(Objects::nonNull).findFirst().orElse((BlockState)null);
   }

   @Nullable
   public BlockState getStateForPlacement(BlockState pCurrentState, BlockGetter pLevel, BlockPos pPos, Direction pLookingDirection) {
      if (!this.isFaceSupported(pLookingDirection)) {
         return null;
      } else {
         BlockState blockstate;
         if (pCurrentState.is(this)) {
            if (hasFace(pCurrentState, pLookingDirection)) {
               return null;
            }

            blockstate = pCurrentState;
         } else if (this.isWaterloggable() && pCurrentState.getFluidState().isSourceOfType(Fluids.WATER)) {
            blockstate = this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true));
         } else {
            blockstate = this.defaultBlockState();
         }

         BlockPos blockpos = pPos.relative(pLookingDirection);
         return canAttachTo(pLevel, pLookingDirection, blockpos, pLevel.getBlockState(blockpos)) ? blockstate.setValue(getFaceProperty(pLookingDirection), Boolean.valueOf(true)) : null;
      }
   }

   /**
    * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehavior.BlockStateBase#rotate} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState rotate(BlockState pState, Rotation pRotation) {
      return !this.canRotate ? pState : this.mapDirections(pState, pRotation::rotate);
   }

   /**
    * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
    * blockstate.
    * @deprecated call via {@link net.minecraft.world.level.block.state.BlockBehavior.BlockStateBase#mirror} whenever
    * possible. Implementing/overriding is fine.
    */
   public BlockState mirror(BlockState pState, Mirror pMirror) {
      if (pMirror == Mirror.FRONT_BACK && !this.canMirrorX) {
         return pState;
      } else {
         return pMirror == Mirror.LEFT_RIGHT && !this.canMirrorZ ? pState : this.mapDirections(pState, pMirror::mirror);
      }
   }

   private BlockState mapDirections(BlockState pState, Function<Direction, Direction> pDirectionalFunction) {
      BlockState blockstate = pState;

      for(Direction direction : DIRECTIONS) {
         if (this.isFaceSupported(direction)) {
            blockstate = blockstate.setValue(getFaceProperty(pDirectionalFunction.apply(direction)), pState.getValue(getFaceProperty(direction)));
         }
      }

      return blockstate;
   }

   public boolean spreadFromRandomFaceTowardRandomDirection(BlockState pState, ServerLevel pLevel, BlockPos pPos, Random pRandom) {
      List<Direction> list = Lists.newArrayList(DIRECTIONS);
      Collections.shuffle(list);
      return list.stream().filter((p_153955_) -> {
         return hasFace(pState, p_153955_);
      }).anyMatch((p_153846_) -> {
         return this.spreadFromFaceTowardRandomDirection(pState, pLevel, pPos, p_153846_, pRandom, false);
      });
   }

   public boolean spreadFromFaceTowardRandomDirection(BlockState pState, LevelAccessor pLevel, BlockPos pPos, Direction pFace, Random pRandom, boolean pPostProcessing) {
      List<Direction> list = Arrays.asList(DIRECTIONS);
      Collections.shuffle(list, pRandom);
      return list.stream().anyMatch((p_153886_) -> {
         return this.spreadFromFaceTowardDirection(pState, pLevel, pPos, pFace, p_153886_, pPostProcessing);
      });
   }

   public boolean spreadFromFaceTowardDirection(BlockState pState, LevelAccessor pLevel, BlockPos pPos, Direction pFace, Direction pDirection, boolean pPostProcessing) {
      Optional<Pair<BlockPos, Direction>> optional = this.getSpreadFromFaceTowardDirection(pState, pLevel, pPos, pFace, pDirection);
      if (optional.isPresent()) {
         Pair<BlockPos, Direction> pair = optional.get();
         return this.spreadToFace(pLevel, pair.getFirst(), pair.getSecond(), pPostProcessing);
      } else {
         return false;
      }
   }

   protected boolean canSpread(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
      return Stream.of(DIRECTIONS).anyMatch((p_153929_) -> {
         return this.getSpreadFromFaceTowardDirection(pState, pLevel, pPos, pDirection, p_153929_).isPresent();
      });
   }

   private Optional<Pair<BlockPos, Direction>> getSpreadFromFaceTowardDirection(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection, Direction pMatched) {
      if (pMatched.getAxis() != pDirection.getAxis() && hasFace(pState, pDirection) && !hasFace(pState, pMatched)) {
         if (this.canSpreadToFace(pLevel, pPos, pMatched)) {
            return Optional.of(Pair.of(pPos, pMatched));
         } else {
            BlockPos blockpos = pPos.relative(pMatched);
            if (this.canSpreadToFace(pLevel, blockpos, pDirection)) {
               return Optional.of(Pair.of(blockpos, pDirection));
            } else {
               BlockPos blockpos1 = blockpos.relative(pDirection);
               Direction direction = pMatched.getOpposite();
               return this.canSpreadToFace(pLevel, blockpos1, direction) ? Optional.of(Pair.of(blockpos1, direction)) : Optional.empty();
            }
         }
      } else {
         return Optional.empty();
      }
   }

   private boolean canSpreadToFace(BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
      BlockState blockstate = pLevel.getBlockState(pPos);
      if (!this.canSpreadInto(blockstate)) {
         return false;
      } else {
         BlockState blockstate1 = this.getStateForPlacement(blockstate, pLevel, pPos, pDirection);
         return blockstate1 != null;
      }
   }

   private boolean spreadToFace(LevelAccessor pAccessor, BlockPos pPos, Direction pFace, boolean pPostProcessing) {
      BlockState blockstate = pAccessor.getBlockState(pPos);
      BlockState blockstate1 = this.getStateForPlacement(blockstate, pAccessor, pPos, pFace);
      if (blockstate1 != null) {
         if (pPostProcessing) {
            pAccessor.getChunk(pPos).markPosForPostprocessing(pPos);
         }

         return pAccessor.setBlock(pPos, blockstate1, 2);
      } else {
         return false;
      }
   }

   private boolean canSpreadInto(BlockState pState) {
      return pState.isAir() || pState.is(this) || pState.is(Blocks.WATER) && pState.getFluidState().isSource();
   }

   private static boolean hasFace(BlockState pState, Direction pDirection) {
      BooleanProperty booleanproperty = getFaceProperty(pDirection);
      return pState.hasProperty(booleanproperty) && pState.getValue(booleanproperty);
   }

   private static boolean canAttachTo(BlockGetter pLevel, Direction pDirection, BlockPos pPos, BlockState pState) {
      return Block.isFaceFull(pState.getCollisionShape(pLevel, pPos), pDirection.getOpposite());
   }

   private boolean isWaterloggable() {
      return this.stateDefinition.getProperties().contains(BlockStateProperties.WATERLOGGED);
   }

   private static BlockState removeFace(BlockState pState, BooleanProperty pFaceProp) {
      BlockState blockstate = pState.setValue(pFaceProp, Boolean.valueOf(false));
      return hasAnyFace(blockstate) ? blockstate : Blocks.AIR.defaultBlockState();
   }

   public static BooleanProperty getFaceProperty(Direction pDirection) {
      return PROPERTY_BY_DIRECTION.get(pDirection);
   }

   private static BlockState getDefaultMultifaceState(StateDefinition<Block, BlockState> pStateDefinition) {
      BlockState blockstate = pStateDefinition.any();

      for(BooleanProperty booleanproperty : PROPERTY_BY_DIRECTION.values()) {
         if (blockstate.hasProperty(booleanproperty)) {
            blockstate = blockstate.setValue(booleanproperty, Boolean.valueOf(false));
         }
      }

      return blockstate;
   }

   private static VoxelShape calculateMultifaceShape(BlockState p_153959_) {
      VoxelShape voxelshape = Shapes.empty();

      for(Direction direction : DIRECTIONS) {
         if (hasFace(p_153959_, direction)) {
            voxelshape = Shapes.or(voxelshape, SHAPE_BY_DIRECTION.get(direction));
         }
      }

      return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
   }

   protected static boolean hasAnyFace(BlockState pState) {
      return Arrays.stream(DIRECTIONS).anyMatch((p_153947_) -> {
         return hasFace(pState, p_153947_);
      });
   }

   private static boolean hasAnyVacantFace(BlockState pState) {
      return Arrays.stream(DIRECTIONS).anyMatch((p_153932_) -> {
         return !hasFace(pState, p_153932_);
      });
   }
}