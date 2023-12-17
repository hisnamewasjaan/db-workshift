package db.workshift;

import db.workshift.users.User;
import org.springframework.stereotype.Service;

import java.util.stream.StreamSupport;

@Service
public class WorkshiftBrain {

    private final ShopRepository shopRepository;

    public WorkshiftBrain(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop assignUserToShift(Shop shop, Shift shift, User user)
            throws HoursExceededException, DaysInRowExceededException, ConflictingShiftException {

        validateNoConflictingShiftRule(shift, user);

        shop.assignUserToShift(user, shift);
        return shopRepository.save(shop);
    }

    private void validateNoConflictingShiftRule(Shift shift, User user) throws ConflictingShiftException {
        /*
        A user can not work in multiple shops at the same time
        */
        boolean isAssignedConflictingShift = StreamSupport
                .stream(shopRepository.findAll().spliterator(), false)
                .anyMatch(shop1 -> shop1.getAssignedShifts(user)
                        .stream()
                        .anyMatch(shift1 -> shift1.hasOverlap(shift)));
        if (isAssignedConflictingShift) {
            throw new ConflictingShiftException();
        }
    }

}
