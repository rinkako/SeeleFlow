/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class : ResourceServiceRestController
 * Usage :
 */
@RestController
@RequestMapping("api/resourcing")
public class ResourceServiceRestController {

    @RequestMapping(value="/")
    public String gateway() {
        return "Seele: you try to visit ResourceServiceRestController gateway";
    }
}
