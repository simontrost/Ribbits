package com.yungnickyoung.minecraft.ribbits.mixin.mixins;

/**
 * Historical Ribbits-specific terrain mixin removed from its mixin configuration.
 * In the 26.3 port these hooks are owned by the bundled-as-separate-mod YUNG's API,
 * which implements EnhancedBeardifierData and applies enhanced terrain adaptations.
 * Keeping the old mixin would inject the same changes twice and refer to removed
 * DensityFunction interfaces. Do not re-enable without reviewing both mods.
 */
@Deprecated(forRemoval = true)
public final class BeardifierMixin {
    private BeardifierMixin() { }
}
