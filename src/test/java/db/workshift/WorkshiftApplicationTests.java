package db.workshift;

import db.workshift.users.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WorkshiftApplicationTests {

    @Autowired
    UserController userController;

    @Test
    void contextLoads() {
        org.junit.jupiter.api.Assertions.assertNotNull(userController);
    }

}
