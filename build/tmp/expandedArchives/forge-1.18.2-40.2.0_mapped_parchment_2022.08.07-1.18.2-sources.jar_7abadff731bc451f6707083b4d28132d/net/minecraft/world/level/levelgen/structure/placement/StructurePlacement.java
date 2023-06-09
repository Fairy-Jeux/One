package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;

public interface StructurePlacement {
   Codec<StructurePlacement> CODEC = Registry.STRUCTURE_PLACEMENT_TYPE.byNameCodec().dispatch(StructurePlacement::type, StructurePlacementType::codec);

   boolean isFeatureChunk(ChunkGenerator pGenerator, long pSeed, int pX, int pZ);

   StructurePlacementType<?> type();
}