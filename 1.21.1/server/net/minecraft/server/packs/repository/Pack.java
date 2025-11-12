/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.repository;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.OverlayMetadataSection;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlagSet;
import org.slf4j.Logger;

public class Pack {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final PackLocationInfo location;
    private final ResourcesSupplier resources;
    private final Metadata metadata;
    private final PackSelectionConfig selectionConfig;

    @Nullable
    public static Pack readMetaAndCreate(PackLocationInfo packLocationInfo, ResourcesSupplier resourcesSupplier, PackType packType, PackSelectionConfig packSelectionConfig) {
        int n = SharedConstants.getCurrentVersion().getPackVersion(packType);
        Metadata metadata = Pack.readPackMetadata(packLocationInfo, resourcesSupplier, n);
        return metadata != null ? new Pack(packLocationInfo, resourcesSupplier, metadata, packSelectionConfig) : null;
    }

    public Pack(PackLocationInfo packLocationInfo, ResourcesSupplier resourcesSupplier, Metadata metadata, PackSelectionConfig packSelectionConfig) {
        this.location = packLocationInfo;
        this.resources = resourcesSupplier;
        this.metadata = metadata;
        this.selectionConfig = packSelectionConfig;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static Metadata readPackMetadata(PackLocationInfo packLocationInfo, ResourcesSupplier resourcesSupplier, int n) {
        try (PackResources packResources = resourcesSupplier.openPrimary(packLocationInfo);){
            PackMetadataSection packMetadataSection = packResources.getMetadataSection(PackMetadataSection.TYPE);
            if (packMetadataSection == null) {
                LOGGER.warn("Missing metadata in pack {}", (Object)packLocationInfo.id());
                Metadata metadata = null;
                return metadata;
            }
            FeatureFlagsMetadataSection featureFlagsMetadataSection = packResources.getMetadataSection(FeatureFlagsMetadataSection.TYPE);
            FeatureFlagSet featureFlagSet = featureFlagsMetadataSection != null ? featureFlagsMetadataSection.flags() : FeatureFlagSet.of();
            InclusiveRange<Integer> inclusiveRange = Pack.getDeclaredPackVersions(packLocationInfo.id(), packMetadataSection);
            PackCompatibility packCompatibility = PackCompatibility.forVersion(inclusiveRange, n);
            OverlayMetadataSection overlayMetadataSection = packResources.getMetadataSection(OverlayMetadataSection.TYPE);
            List<String> list = overlayMetadataSection != null ? overlayMetadataSection.overlaysForVersion(n) : List.of();
            Metadata metadata = new Metadata(packMetadataSection.description(), packCompatibility, featureFlagSet, list);
            return metadata;
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to read pack {} metadata", (Object)packLocationInfo.id(), (Object)exception);
            return null;
        }
    }

    private static InclusiveRange<Integer> getDeclaredPackVersions(String string, PackMetadataSection packMetadataSection) {
        int n = packMetadataSection.packFormat();
        if (packMetadataSection.supportedFormats().isEmpty()) {
            return new InclusiveRange<Integer>(n);
        }
        InclusiveRange<Integer> inclusiveRange = packMetadataSection.supportedFormats().get();
        if (!inclusiveRange.isValueInRange(n)) {
            LOGGER.warn("Pack {} declared support for versions {} but declared main format is {}, defaulting to {}", new Object[]{string, inclusiveRange, n, n});
            return new InclusiveRange<Integer>(n);
        }
        return inclusiveRange;
    }

    public PackLocationInfo location() {
        return this.location;
    }

    public Component getTitle() {
        return this.location.title();
    }

    public Component getDescription() {
        return this.metadata.description();
    }

    public Component getChatLink(boolean bl) {
        return this.location.createChatLink(bl, this.metadata.description);
    }

    public PackCompatibility getCompatibility() {
        return this.metadata.compatibility();
    }

    public FeatureFlagSet getRequestedFeatures() {
        return this.metadata.requestedFeatures();
    }

    public PackResources open() {
        return this.resources.openFull(this.location, this.metadata);
    }

    public String getId() {
        return this.location.id();
    }

    public PackSelectionConfig selectionConfig() {
        return this.selectionConfig;
    }

    public boolean isRequired() {
        return this.selectionConfig.required();
    }

    public boolean isFixedPosition() {
        return this.selectionConfig.fixedPosition();
    }

    public Position getDefaultPosition() {
        return this.selectionConfig.defaultPosition();
    }

    public PackSource getPackSource() {
        return this.location.source();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Pack)) {
            return false;
        }
        Pack pack = (Pack)object;
        return this.location.equals(pack.location);
    }

    public int hashCode() {
        return this.location.hashCode();
    }

    public static interface ResourcesSupplier {
        public PackResources openPrimary(PackLocationInfo var1);

        public PackResources openFull(PackLocationInfo var1, Metadata var2);
    }

    public static final class Metadata
    extends Record {
        final Component description;
        private final PackCompatibility compatibility;
        private final FeatureFlagSet requestedFeatures;
        private final List<String> overlays;

        public Metadata(Component component, PackCompatibility packCompatibility, FeatureFlagSet featureFlagSet, List<String> list) {
            this.description = component;
            this.compatibility = packCompatibility;
            this.requestedFeatures = featureFlagSet;
            this.overlays = list;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Metadata.class, "description;compatibility;requestedFeatures;overlays", "description", "compatibility", "requestedFeatures", "overlays"}, this, object);
        }

        public Component description() {
            return this.description;
        }

        public PackCompatibility compatibility() {
            return this.compatibility;
        }

        public FeatureFlagSet requestedFeatures() {
            return this.requestedFeatures;
        }

        public List<String> overlays() {
            return this.overlays;
        }
    }

    public static enum Position {
        TOP,
        BOTTOM;


        public <T> int insert(List<T> list, T t, Function<T, PackSelectionConfig> function, boolean bl) {
            PackSelectionConfig packSelectionConfig;
            int n;
            Position position;
            Position position2 = position = bl ? this.opposite() : this;
            if (position == BOTTOM) {
                PackSelectionConfig packSelectionConfig2;
                int n2;
                for (n2 = 0; n2 < list.size() && (packSelectionConfig2 = function.apply(list.get(n2))).fixedPosition() && packSelectionConfig2.defaultPosition() == this; ++n2) {
                }
                list.add(n2, t);
                return n2;
            }
            for (n = list.size() - 1; n >= 0 && (packSelectionConfig = function.apply(list.get(n))).fixedPosition() && packSelectionConfig.defaultPosition() == this; --n) {
            }
            list.add(n + 1, t);
            return n + 1;
        }

        public Position opposite() {
            return this == TOP ? BOTTOM : TOP;
        }
    }
}

