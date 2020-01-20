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
 * Class : SupervisorUnregisterForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SupervisorUnregisterForm extends SeeleRestForm {

    @NotNull
    private String namespace;

    @NotNull
    private String supervisorId;
}
