package db.workshift;

import db.workshift.users.User;
import db.workshift.users.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(value = "api/shops")
public class ShopController {

    private static final Logger LOG = LoggerFactory.getLogger(ShopController.class);

    /**
     * lazy, no service layer, directly in repo
     */
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final WorkshiftBrain workshiftBrain;

    ShopController(ShopRepository shopRepository, UserRepository userRepository, WorkshiftBrain workshiftBrain) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.workshiftBrain = workshiftBrain;
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Shop createShop(@RequestBody Map<String, String> input) {
        LOG.info("create <%s>".formatted(input));

        Shop shop = shopRepository.save(Shop.create());

        LOG.info("shop created <%s>".formatted(shop));
        return shop;
    }

    @PostMapping(value = "/{id}/shift")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Shop addShift(@PathVariable String id,
                         @RequestBody Shift input) {
        LOG.info("addShift <%s>".formatted(input));

        /* :) */
        Optional<Shop> shoptional = shopRepository.findById(UUID.fromString(id));
        Shop shop = shoptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        shop.addShift(input.getStart(), input.getDuration());
        Shop persistedShop = shopRepository.save(shop);

        LOG.info("shift added <%s>".formatted(persistedShop));
        return persistedShop;
    }

    @PostMapping(value = "/{id}/user/{userId}")
    @ResponseStatus(code = HttpStatus.CREATED)
    public Shop addUser(@PathVariable String id,
                        @PathVariable String userId) {
        LOG.info("addUser, shop <%s>, user <%s>".formatted(id, userId));

        Optional<Shop> shoptional = shopRepository.findById(UUID.fromString(id));
        Shop shop = shoptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        User user = userOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        shop.addUser(user);
        Shop persistedShop = shopRepository.save(shop);

        LOG.info("user added <%s>".formatted(persistedShop));
        return persistedShop;
    }

    @PutMapping(value = "/{id}/shift/{shiftId}/assign/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public Shop assignUserToShift(@PathVariable String id,
                                  @PathVariable String shiftId,
                                  @PathVariable String userId) {
        LOG.info("assignUserToShift, shop <%s>, shift <%s>, user <%s>".formatted(id, shiftId, userId));

        Optional<Shop> shoptional = shopRepository.findById(UUID.fromString(id));
        Shop shop = shoptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shop not found"));

        Optional<Shift> shiftOptional = shop.findShift(UUID.fromString(shiftId));
        Shift shift = shiftOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found"));

        Optional<User> userOptional = userRepository.findById(UUID.fromString(userId));
        User user = userOptional.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));


        try {
            Shop persistedShop = workshiftBrain.assignUserToShift(shop, shift, user);

            LOG.info("user <%s> assigned shift in shop <%s>".formatted(userId, persistedShop));
            return persistedShop;
        } catch (HoursExceededException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Hours exceeded within 24 hour window", e);
        } catch (DaysInRowExceededException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Days in row exceeded", e);
        } catch (ConflictingShiftException e) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Conflicting shift detected", e);
        }
    }

}
