package com.quietus.client.model.equipments;

import com.geckolib.constant.DataTickets;
import com.geckolib.constant.dataticket.DataTicket;
import com.geckolib.renderer.GeoArmorRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.TextureLayerGeoLayer;
import com.quietus.item.equipment.AmethystArmorItem;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.Set;

import static com.quietus.Quietus.MODID;

/**
 * Renders armor trims using masks laid out for the amethyst armor's GeckoLib model.
 *
 * <p>Vanilla trim textures use the vanilla humanoid armor UVs, so they cannot be
 * applied directly to this model. The masks in {@code gecko/trims} are white and
 * are tinted with the selected trim material's text color at render time.</p>
 */
public class AmethystArmorTrimLayer<R extends HumanoidRenderState & GeoRenderState>
        extends TextureLayerGeoLayer<AmethystArmorItem, GeoArmorRenderer.RenderData, R> {

    private static final Set<String> AVAILABLE_PATTERNS = Set.of(
            "sentry", "dune", "coast", "wild", "ward", "eye", "vex", "tide",
            "snout", "rib", "spire", "wayfinder", "shaper", "silence", "raiser", "host"
    );

    private static final DataTicket<Boolean> HAS_TRIM =
            DataTicket.create(MODID + ":amethyst_armor_has_trim", Boolean.class);
    private static final DataTicket<Identifier> TRIM_TEXTURE =
            DataTicket.create(MODID + ":amethyst_armor_trim_texture", Identifier.class);
    private static final DataTicket<Integer> TRIM_COLOR =
            DataTicket.create(MODID + ":amethyst_armor_trim_color", Integer.class);

    public AmethystArmorTrimLayer(GeoArmorRenderer<AmethystArmorItem, R> renderer) {
        super(renderer, Identifier.fromNamespaceAndPath(
                MODID, "textures/entity/equipment/gecko/trims/sentry_quietus_trim.png"));
    }

    @Override
    public void addRenderData(AmethystArmorItem animatable, GeoArmorRenderer.RenderData renderData,
                              R renderState, float partialTick) {
        ArmorTrim trim = renderData.itemStack().get(DataComponents.TRIM);

        if (trim == null) {
            renderState.addGeckolibData(HAS_TRIM, false);
            return;
        }

        String pattern = trim.pattern().value().assetId().getPath();
        if (!AVAILABLE_PATTERNS.contains(pattern)) {
            renderState.addGeckolibData(HAS_TRIM, false);
            return;
        }

        TextColor materialColor = trim.material().value().description().getStyle().getColor();
        int rgb = materialColor == null ? 0xFFFFFF : materialColor.getValue();

        renderState.addGeckolibData(HAS_TRIM, true);
        renderState.addGeckolibData(TRIM_TEXTURE, Identifier.fromNamespaceAndPath(
                MODID, "textures/entity/equipment/gecko/trims/" + pattern + "_quietus_trim.png"));
        renderState.addGeckolibData(TRIM_COLOR, ARGB.color(255, rgb));
    }

    @Override
    protected Identifier getTextureResource(R renderState) {
        return renderState.getGeckolibData(TRIM_TEXTURE);
    }

    @Override
    public void submitRenderTask(RenderPassInfo<R> renderPass, SubmitNodeCollector submitNodeCollector) {
        R renderState = renderPass.renderState();
        if (!renderState.getOrDefaultGeckolibData(HAS_TRIM, false)) {
            return;
        }

        int originalColor = renderPass.renderColor();
        int trimColor = renderState.getOrDefaultGeckolibData(TRIM_COLOR, 0xFFFFFFFF);
        renderState.addGeckolibData(DataTickets.RENDER_COLOR, ARGB.multiply(originalColor, trimColor));

        super.submitRenderTask(renderPass, submitNodeCollector);

        renderState.addGeckolibData(DataTickets.RENDER_COLOR, originalColor);
    }
}
