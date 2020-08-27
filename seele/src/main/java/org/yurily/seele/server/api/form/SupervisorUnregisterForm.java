/*
 * Author : Rinka
 * Date   : 2020/1/20
 */
package org.yurily.seele.server.api.form;

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
public class SupervisorUnregisterForm extends NamespaceForm {

    @NotNull
    private String supervisorId;
}
