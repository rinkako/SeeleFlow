/*
 * Author : Rinka
 * Date   : 2020/2/12
 */
package org.yurily.seele.server.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Class : NamespaceForm
 * Usage :
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NamespaceForm extends SeeleRestForm {

    @NotNull
    private String namespace;
}
