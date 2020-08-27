/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/2/16
 */
package org.yurily.seele.server.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Class : PrincipleForm
 * Usage :
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PrincipleForm extends SeeleRestForm {

    @NotNull
    private String dispatcherName;

    @NotNull
    private String dispatchType;

    private Map<String, Object> dispatcherArgs;
}
