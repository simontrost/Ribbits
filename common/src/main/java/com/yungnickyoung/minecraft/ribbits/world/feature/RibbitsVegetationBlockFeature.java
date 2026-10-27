package com.yungnickyoung.minecraft.ribbits.world.feature;

import net.minecraft.core.BlockPos;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.List;
import java.util.Optional;

public record RibbitsVegetationBlockFeature(Optional<BlockStateProvider> onSolidStateProvider,
                                          Optional<BlockStateProvider> onLiquidStateProvider,
                                          List<BlockState> cannotPlaceOn) implements Feature {
    public static final MapCodec<RibbitsVegetationBlockFeature> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    BlockStateProvider.CODEC.xmap(holder -> holder.value(), net.minecraft.core.Holder::direct).optionalFieldOf("on_solid_state_provider").forGetter(RibbitsVegetationBlockFeature::onSolidStateProvider),
                    BlockStateProvider.CODEC.xmap(holder -> holder.value(), net.minecraft.core.Holder::direct).optionalFieldOf("on_liquid_state_provider").forGetter(RibbitsVegetationBlockFeature::onLiquidStateProvider),
                    BlockState.CODEC.listOf().optionalFieldOf("cannot_place_on", List.of()).forGetter(RibbitsVegetationBlockFeature::cannotPlaceOn))
            .apply(instance, RibbitsVegetationBlockFeature::new));

    @Override
    public MapCodec<RibbitsVegetationBlockFeature> codec() {
        return CODEC;
    }

    public boolean place(WorldGenLevel worldGenLevel, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        Optional<BlockStateProvider> onSolidBlockStates = this.onSolidStateProvider;
        List<BlockState> cannotPlaceOn = this.cannotPlaceOn;

        // Check for blocks we can't place on.
        if (cannotPlaceOn.contains(worldGenLevel.getBlockState(origin.below()))) {
            return false;
        }

        // Check for water, in which case we place lily pads instead.
        if (this.onLiquidStateProvider.isPresent() && worldGenLevel.getBlockState(origin.below()).is(Blocks.WATER)) {
            BlockStateProvider onWaterBlockStates = this.onLiquidStateProvider.get();
            worldGenLevel.setBlock(origin, onWaterBlockStates.getState(worldGenLevel, random, origin), 2);
            return true;
        }

        // If we have no block states to place, return false.
        if (onSolidBlockStates.isEmpty()) {
            return false;
        }

        BlockState toPlace = onSolidBlockStates.get().getState(worldGenLevel, random, origin);

        // Place block if it can survive.
        if (toPlace.canSurvive(worldGenLevel, origin)) {
            if (toPlace.getBlock() instanceof DoublePlantBlock) {
                if (!worldGenLevel.isEmptyBlock(origin.above())) {
                    return false;
                }

                DoublePlantBlock.placeAt(worldGenLevel, toPlace, origin, 3);
            } else {
                worldGenLevel.setBlock(origin, toPlace, 3);
            }

            return true;
        } else {
            return false;
        }
    }
}
