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
 * Class : SupervisorRegisterForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class SupervisorRegisterForm extends NamespaceForm {

    @NotNull
    private String supervisorId;

    @NotNull
    private String host;

    @NotNull
    private String callback;

    private String fallback;
}
