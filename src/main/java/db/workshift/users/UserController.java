package db.workshift.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "api/users")
public class UserController {

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    /**
     * lazy, no service layer, directly in repo
     */
    private final UserRepository userRepository;

    UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public User create(@RequestBody Map<String, String> input) {
        LOG.info("create <%s>".formatted(input));

        User user = User.create(input.get("name"));
        User persistedUser = userRepository.save(user);

        LOG.info("user created <%s>".formatted(persistedUser));
        return user;
    }

}
