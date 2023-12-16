package db.workshift;

import db.workshift.users.User;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Shop {

    private static final int TIME_WINDOW_24_HOURS = 24;
    private static final int MAXIMUM_ALLOWED_USER_HOURS_IN_TIME_WINDOW = 8;

    private final Set<User> employees = new HashSet<>();
    private final Set<Shift> shifts = new HashSet<>();

    private Shop() {

    }

    static Shop create() {
        return new Shop();
    }

    void addUser(User user) {
        employees.add(user);
    }

    boolean hasUser(User user) {
        return employees.contains(user);
    }

    void addShift(LocalDateTime start, Duration duration) {
        shifts.add(Shift.create(start, duration));
    }

    List<Shift> getAvailableShifts() {
        return shifts
                .stream()
                .filter(Shift::unAssigned)
                .sorted(Comparator.comparing(Shift::getStart))
                .collect(Collectors.toList());
    }

    List<Shift> getAssignedShifts(User user) {
        return shifts
                .stream()
                .filter(shift -> shift.isAssignedTo(user))
                .sorted(Comparator.comparing(Shift::getStart))
                .collect(Collectors.toList());
    }

    void assignUserToShift(User user, Shift shift) throws HoursExceededException {
        Period window24Hours = new Period(
                shift.getStart().minusHours(TIME_WINDOW_24_HOURS - shift.getDuration().toHours()),
                shift.getStart().plusHours(TIME_WINDOW_24_HOURS));

        Duration totalHoursIn24HourWindow = getAssignedShifts(user)
                .stream()
                .reduce(Duration.ofHours(0),
                        (subtotal, assignedShift) -> subtotal.plus(assignedShift.overlap(window24Hours)),
                        Duration::plus);

        if (totalHoursIn24HourWindow.plus(shift.getDuration()).toHours() > MAXIMUM_ALLOWED_USER_HOURS_IN_TIME_WINDOW) {
            throw new HoursExceededException();
        }

        shift.assign(user);
    }


}
