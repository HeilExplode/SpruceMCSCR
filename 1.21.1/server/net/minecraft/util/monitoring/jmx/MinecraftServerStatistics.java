/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.monitoring.jmx;

import com.mojang.logging.LogUtils;
import java.lang.management.ManagementFactory;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;

public final class MinecraftServerStatistics
implements DynamicMBean {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftServer server;
    private final MBeanInfo mBeanInfo;
    private final Map<String, AttributeDescription> attributeDescriptionByName = Stream.of(new AttributeDescription("tickTimes", this::getTickTimes, "Historical tick times (ms)", long[].class), new AttributeDescription("averageTickTime", this::getAverageTickTime, "Current average tick time (ms)", Long.TYPE)).collect(Collectors.toMap(attributeDescription -> attributeDescription.name, Function.identity()));

    private MinecraftServerStatistics(MinecraftServer minecraftServer) {
        this.server = minecraftServer;
        MBeanAttributeInfo[] mBeanAttributeInfoArray = (MBeanAttributeInfo[])this.attributeDescriptionByName.values().stream().map(AttributeDescription::asMBeanAttributeInfo).toArray(MBeanAttributeInfo[]::new);
        this.mBeanInfo = new MBeanInfo(MinecraftServerStatistics.class.getSimpleName(), "metrics for dedicated server", mBeanAttributeInfoArray, null, null, new MBeanNotificationInfo[0]);
    }

    public static void registerJmxMonitoring(MinecraftServer minecraftServer) {
        try {
            ManagementFactory.getPlatformMBeanServer().registerMBean(new MinecraftServerStatistics(minecraftServer), new ObjectName("net.minecraft.server:type=Server"));
        }
        catch (InstanceAlreadyExistsException | MBeanRegistrationException | MalformedObjectNameException | NotCompliantMBeanException jMException) {
            LOGGER.warn("Failed to initialise server as JMX bean", (Throwable)jMException);
        }
    }

    private float getAverageTickTime() {
        return this.server.getCurrentSmoothedTickTime();
    }

    private long[] getTickTimes() {
        return this.server.getTickTimesNanos();
    }

    @Override
    @Nullable
    public Object getAttribute(String string) {
        AttributeDescription attributeDescription = this.attributeDescriptionByName.get(string);
        return attributeDescription == null ? null : attributeDescription.getter.get();
    }

    @Override
    public void setAttribute(Attribute attribute) {
    }

    @Override
    public AttributeList getAttributes(String[] stringArray) {
        List<Attribute> list = Arrays.stream(stringArray).map(this.attributeDescriptionByName::get).filter(Objects::nonNull).map(attributeDescription -> new Attribute(attributeDescription.name, attributeDescription.getter.get())).collect(Collectors.toList());
        return new AttributeList(list);
    }

    @Override
    public AttributeList setAttributes(AttributeList attributeList) {
        return new AttributeList();
    }

    @Override
    @Nullable
    public Object invoke(String string, Object[] objectArray, String[] stringArray) {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return this.mBeanInfo;
    }

    static final class AttributeDescription {
        final String name;
        final Supplier<Object> getter;
        private final String description;
        private final Class<?> type;

        AttributeDescription(String string, Supplier<Object> supplier, String string2, Class<?> clazz) {
            this.name = string;
            this.getter = supplier;
            this.description = string2;
            this.type = clazz;
        }

        private MBeanAttributeInfo asMBeanAttributeInfo() {
            return new MBeanAttributeInfo(this.name, this.type.getSimpleName(), this.description, true, false, false);
        }
    }
}

