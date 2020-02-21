/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/19
 */
package org.rinka.seele.server.engine.resourcing.transition;

/**
 * Class : TransitionRequestResult
 * Usage :
 */
public enum TransitionRequestResult {
    Invalid,
    Duplicated,
    Submitted,
    Executed,
    FinalStateReject,
    Banned
}
