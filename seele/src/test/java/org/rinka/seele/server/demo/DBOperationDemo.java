/*
 * Project Seele Workflow
 * Author : Rinka
 * Date   : 2020/1/1
 */
package org.rinka.seele.server.demo;

import org.junit.jupiter.api.Test;
import org.rinka.seele.server.dao.UserRepository;
import org.rinka.seele.server.entity.User;
import org.rinka.seele.server.util.SummaryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.Optional;

@SpringBootTest
public class DBOperationDemo {

    @Autowired
    private UserRepository userRepository;

    @Test
    void add_data() throws Exception {
        User user = new User(null, "SeeleInternalTest", "SeeleInternalTestUser",
                SummaryHelper.generateSHA256("1"), "a@a.com", "111");
        // INSERT
        user = this.userRepository.saveAndFlush(user);
        Long id = user.getUid();
        Assert.notNull(id);
        System.out.println(user.toString());
        // FIND
        Optional<User> findbackOp = this.userRepository.findById(id);
        Assert.isTrue(findbackOp.isPresent());
        User findback = findbackOp.get();
        Assert.isTrue(findback.equals(user));
        // UPDATE
        findback.setEmail("b@b.com");
        findback = this.userRepository.saveAndFlush(findback);
        Optional<User> findbackAfterUpdateOp = this.userRepository.findById(id);
        Assert.isTrue(findbackAfterUpdateOp.isPresent());
        User findbackAfterUpdate = findbackAfterUpdateOp.get();
        System.out.println(findbackAfterUpdate);
        Assert.isTrue(findbackAfterUpdate.equals(findback));
        Assert.isTrue(findbackAfterUpdate.getEmail().equals("b@b.com"));
        // DELETE
        this.userRepository.delete(findbackAfterUpdate);
        Optional<User> deleted = this.userRepository.findById(id);
        Assert.isTrue(!deleted.isPresent());
    }
}
