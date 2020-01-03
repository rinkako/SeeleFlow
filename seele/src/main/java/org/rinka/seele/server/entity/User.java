/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "user")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long uid;

    @Column(nullable = false, length = 255, unique = true)
    private String username;

    @Column(nullable = false, length = 255, unique = true)
    private String nickname;

    @Column(nullable = false, length = 64)
    private String password;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 31)
    private String phone = "";
}
