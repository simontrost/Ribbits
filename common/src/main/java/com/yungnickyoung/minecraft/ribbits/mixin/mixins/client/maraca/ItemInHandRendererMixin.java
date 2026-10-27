package com.yungnickyoung.minecraft.ribbits.mixin.mixins.client.maraca;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yungnickyoung.minecraft.ribbits.client.render.MaracaInHandRenderer;
import com.yungnickyoung.minecraft.ribbits.module.ItemModule;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Keeps Ribbits' first-person maraca shake animation with Minecraft 26.3's
 * FirstPersonHandsAndItemsRenderer (which replaced ItemInHandRenderer).
 *
 * The injection runs immediately after vanilla pushes the item pose.  We
 * render only the active maraca on that pose, pop it, and cancel the rest;
 * other items continue on the unchanged vanilla rendering path.
 *
 * The class/file name remains unchanged to preserve ribbits.mixins.json.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Inject(
            method = "submitArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void ribbits$submitArmWithItem(PlayerRenderState playerState,
                                          FirstPersonHandsAndItemsRenderState state,
                                          float partialTicks, float xRot,
                                          InteractionHand hand, float attack,
                                          ItemStack itemStack, float inverseArmHeight,
                                          PoseStack poseStack,
                                          SubmitNodeCollector submitNodeCollector,
                                          int lightCoords, CallbackInfo ci) {
        AvatarRenderState avatar = playerState.avatarRenderState;
        if (avatar == null || state.isScoping || !itemStack.is(ItemModule.MARACA.get())
                || !avatar.isUsingItem || state.useItemRemainingTicks <= 0
                || avatar.useItemHand != hand) {
            return;
        }

        boolean isMainHand = hand == InteractionHand.MAIN_HAND;
        HumanoidArm arm = isMainHand ? avatar.mainArm : avatar.mainArm.getOpposite();
        int side = arm == HumanoidArm.RIGHT ? 1 : -1;

        // Vanilla 26.3 applyItemArmTransform, replicated rather than shadowing
        // a private Minecraft method. The maraca keeps the original equip pose.
        poseStack.translate(side * 0.56F, -0.52F - inverseArmHeight * 0.6F, -0.72F);
        MaracaInHandRenderer.applyMaracaTransform(poseStack, partialTicks, arm, itemStack, inverseArmHeight);

        (isMainHand ? state.mainHandRenderState : state.offHandRenderState)
                .submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, 0);

        // The vanilla pushPose() has already executed at the injection site.
        poseStack.popPose();
        ci.cancel();
    }
}
