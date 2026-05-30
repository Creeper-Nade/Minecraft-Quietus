package com.quietus.client.model.equipments;


import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.layer.builtin.AutoGlowingGeoLayer;
import com.quietus.client.model.QuietusEmissiveLayer;
import com.quietus.item.equipment.AmethystArmorItem;


import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;

import java.util.List;

public class AmethystArmorRenderer<R extends HumanoidRenderState & GeoRenderState> extends GeoArmorRenderer<AmethystArmorItem,R> {

    /**
     * EmissiveLayer with Geckolib is found crashing on mac with ARM architecture (as of geckolib 5.1.0 with Neoforge 21.5.75). 
     */
    private static final boolean IS_MAC_ARM = Util.getPlatform() == Util.OS.OSX
            && System.getProperty("os.arch").contains("aarch64");

    protected GeoBone waistBone = null;
    public AmethystArmorRenderer() {
        super(new AmethystArmorModel());
        if (IS_MAC_ARM) this.withRenderLayer(new AutoGlowingGeoLayer<>(this));
        else this.withRenderLayer(new QuietusEmissiveLayer<>(this)); // do not render quietus emissive on mac with ARM architecture
    }




// Overriding stuffs from GeoArmorRenderer to add the bone of waist
    @Override
    public List<ArmorSegment> getSegmentsForSlot(R renderState, EquipmentSlot slot) {
        // When the game asks what to render for the LEGS slot, we tell it to render the standard
        // left and right legs, PLUS a CHEST segment. We use CHEST because it intrinsically tracks
        // the body part's rotation and position (x, -y, z), perfectly mimicking a waist.
        if (slot == EquipmentSlot.LEGS) {
            return List.of(ArmorSegment.LEFT_LEG, ArmorSegment.RIGHT_LEG, ArmorSegment.CHEST);
        }
        return super.getSegmentsForSlot(renderState, slot);
    }

    @Override
    public String getBoneNameForSegment(R renderState, ArmorSegment segment) {
        // Cast the renderState to GeoRenderState to access GeckoLib's data tickets safely
        EquipmentSlot slot = ((GeoRenderState) renderState).getGeckolibData(CURRENT_SLOT);

        // When the LEGS pass asks for the bone name of the CHEST segment we injected above,
        // we intercept it and hand it your custom waist bone instead of "armorBody".
        if (slot == EquipmentSlot.LEGS && segment == ArmorSegment.CHEST) {
            return "armorWaist";
        }

        return super.getBoneNameForSegment(renderState, segment);
    }
}
