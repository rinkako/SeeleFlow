/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/9
 */
package org.yurily.seele.server.engine.resourcing;

import org.yurily.seele.server.engine.resourcing.allocator.Allocator;
import org.yurily.seele.server.engine.resourcing.supplier.Supplier;

/**
 * Class : SelectorReflector
 * Usage :
 */
public final class SelectorReflector {
    /**
     * Create a new allocator by its name.
     *
     * @param allocatorName name of allocator to be created
     * @return Specific allocator instance
     */
    public static Allocator ReflectAllocator(String allocatorName) throws Exception {
        Class classType = Class.forName(SelectorReflector.ALLOCATOR_PACKAGE_PATH + allocatorName + SelectorReflector.ALLOCATOR_POSTFIX);
        return (Allocator) classType.newInstance();
    }

    /**
     * Create a new supplier by its name.
     *
     * @param supplierName name of supplier to be created
     * @return Specific supplier instance
     */
    public static Supplier ReflectSupplier(String supplierName) throws Exception {
        Class classType = Class.forName(SelectorReflector.SUPPLIER_PACKAGE_PATH + supplierName + SelectorReflector.FILTER_POSTFIX);
        return (Supplier) classType.newInstance();
    }

    /**
     * Package path of Allocators.
     */
    private static final String ALLOCATOR_PACKAGE_PATH = "org.yurily.seele.server.engine.resourcing.allocator.";

    /**
     * Package path of Supplier.
     */
    private static final String SUPPLIER_PACKAGE_PATH = "org.yurily.seele.server.engine.resourcing.supplier.";

    /**
     * Postfix of Allocator.
     */
    private static final String ALLOCATOR_POSTFIX = "Allocator";

    /**
     * Postfix of Filter.
     */
    private static final String FILTER_POSTFIX = "Filter";
}
