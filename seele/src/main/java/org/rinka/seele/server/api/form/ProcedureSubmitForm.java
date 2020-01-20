/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.rinka.seele.server.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Class : ProcedureSubmitForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class ProcedureSubmitForm extends SeeleRestForm {

    @NotNull
    private String namespace;

    @NotNull
    private String supervisorId;

    @NotNull
    private String submitType = "direct";

    @NotNull
    private String procedureType;

    private String args;
}
