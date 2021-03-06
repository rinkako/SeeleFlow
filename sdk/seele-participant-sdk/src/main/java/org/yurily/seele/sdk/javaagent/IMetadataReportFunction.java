/*
 * Project Seele Workflow
 * Author : Rinka
 */
package org.yurily.seele.sdk.javaagent;

/**
 * Class : IMetadataReportFunction
 * Usage :
 */
public interface IMetadataReportFunction {

    MetadataPackage report(MetadataPackage mp);

    default void reportAck(Object... args) { }
}
