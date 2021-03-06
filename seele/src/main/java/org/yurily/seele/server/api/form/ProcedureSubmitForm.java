/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Class : ProcedureSubmitForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class ProcedureSubmitForm extends NamespaceForm {

    @NotNull
    private String requestId;

    @NotNull
    private String supervisorId;

    @NotNull
    private String submitType = "direct";

    @NotNull
    private String taskName;

    private String skillRequirement;

    @NotNull
    private PrincipleForm principle;

    private Map<String, Object> args;
}
