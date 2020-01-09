/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/9
 */
package org.rinka.seele.server.engine.resourcing;

import org.rinka.seele.server.engine.resourcing.allocator.Allocator;
import org.rinka.seele.server.engine.resourcing.supplier.Supplier;

/**
 * Class : SelectorReflector
 * Usage :
 */
public final class SelectorReflector {
    /**
     * Create a new allocator by its name.
     * @param allocatorName name of allocator to be created
     * @param rstid rs request id
     * @param rtid process rtid
     * @return Specific allocator
     * @throws Exception reflect instance create failed
     */
    public static Allocator ReflectAllocator(String allocatorName, String rstid, String rtid) throws Exception {
        try {
            Class classType = Class.forName(SelectorReflector.ALLOCATOR_PACKAGE_PATH + allocatorName + SelectorReflector.ALLOCATOR_POSTFIX);
            return (Allocator) classType.newInstance();
        }
        catch (Exception ex) {
            // TODO
            throw ex;
        }
    }

    /**
     * Create a new supplier by its name.
     * @param supplierName name of filter to be created
     * @param rstid rs request id
     * @param rtid process rtid
     * @return Specific filter
     * @throws Exception reflect instance create failed
     */
    public static Supplier ReflectSupplier(String supplierName, String rstid, String rtid) throws Exception {
        try {
            Class classType = Class.forName(SelectorReflector.SUPPLIER_PACKAGE_PATH + supplierName + SelectorReflector.FILTER_POSTFIX);
            return (Supplier) classType.newInstance();
        }
        catch (Exception ex) {
            // TODO
            throw ex;
        }
    }

    /**
     * Package path of Allocators.
     */
    private static final String ALLOCATOR_PACKAGE_PATH = "org.rinka.seele.server.engine.resourcing.allocator.";

    /**
     * Package path of Allocators.
     */
    private static final String SUPPLIER_PACKAGE_PATH = "org.rinka.seele.server.engine.resourcing.supplier.";

    /**
     * Postfix of Allocator.
     */
    private static final String ALLOCATOR_POSTFIX = "Allocator";

    /**
     * Postfix of Filter.
     */
    private static final String FILTER_POSTFIX = "Filter";
}
