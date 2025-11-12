/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.server.packs;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;

public class CompositePackResources
implements PackResources {
    private final PackResources primaryPackResources;
    private final List<PackResources> packResourcesStack;

    public CompositePackResources(PackResources packResources, List<PackResources> list) {
        this.primaryPackResources = packResources;
        ArrayList<PackResources> arrayList = new ArrayList<PackResources>(list.size() + 1);
        arrayList.addAll(Lists.reverse(list));
        arrayList.add(packResources);
        this.packResourcesStack = List.copyOf(arrayList);
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getRootResource(String ... stringArray) {
        return this.primaryPackResources.getRootResource(stringArray);
    }

    @Override
    @Nullable
    public IoSupplier<InputStream> getResource(PackType packType, ResourceLocation resourceLocation) {
        for (PackResources packResources : this.packResourcesStack) {
            IoSupplier<InputStream> ioSupplier = packResources.getResource(packType, resourceLocation);
            if (ioSupplier == null) continue;
            return ioSupplier;
        }
        return null;
    }

    @Override
    public void listResources(PackType packType, String string, String string2, PackResources.ResourceOutput resourceOutput) {
        HashMap<ResourceLocation, IoSupplier<InputStream>> hashMap = new HashMap<ResourceLocation, IoSupplier<InputStream>>();
        for (PackResources packResources : this.packResourcesStack) {
            packResources.listResources(packType, string, string2, hashMap::putIfAbsent);
        }
        hashMap.forEach(resourceOutput);
    }

    @Override
    public Set<String> getNamespaces(PackType packType) {
        HashSet<String> hashSet = new HashSet<String>();
        for (PackResources packResources : this.packResourcesStack) {
            hashSet.addAll(packResources.getNamespaces(packType));
        }
        return hashSet;
    }

    @Override
    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> metadataSectionSerializer) throws IOException {
        return this.primaryPackResources.getMetadataSection(metadataSectionSerializer);
    }

    @Override
    public PackLocationInfo location() {
        return this.primaryPackResources.location();
    }

    @Override
    public void close() {
        this.packResourcesStack.forEach(PackResources::close);
    }
}

