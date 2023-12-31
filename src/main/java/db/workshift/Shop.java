package db.workshift;

import db.workshift.users.User;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
public class Shop {

    private static final int TIME_WINDOW_24_HOURS = 24;
    private static final int MAXIMUM_ALLOWED_USER_HOURS_IN_TIME_WINDOW = 8;

    @OneToMany
    private final Set<User> employees = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL)
    private final Set<Shift> shifts = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    protected Shop() {
    }

    static Shop create() {
        return new Shop();
    }

    public UUID getId() {
        return id;
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

    public Optional<Shift> findShift(UUID uuid) {
        return getAvailableShifts()
                .stream()
                .filter(shift -> shift.getId().equals(uuid))
                .findFirst();
    }

    List<Shift> getAssignedShifts(User user) {
        return shifts
                .stream()
                .filter(shift -> shift.isAssignedTo(user))
                .sorted(Comparator.comparing(Shift::getStart))
                .collect(Collectors.toList());
    }

    void assignUserToShift(User user, Shift shift) throws HoursExceededException, DaysInRowExceededException {
        validate24HourWindowRule(user, shift);
        validate5DaysInRowRule(user, shift);

        shift.assign(user);
    }

    private void validate5DaysInRowRule(User user, Shift shift) throws DaysInRowExceededException {
        List<Shift> assignedShifts = getAssignedShifts(user);

        int daysAfter = 0;
        LocalDate dayAfter = shift.getStart().toLocalDate().plusDays(1);
        while (hasShiftOnDay(assignedShifts, dayAfter)) {
            daysAfter++;
            dayAfter = dayAfter.plusDays(1);
        }

        int daysBefore = 0;
        LocalDate dayBefore = shift.getStart().toLocalDate().minusDays(1);
        while (hasShiftOnDay(assignedShifts, dayBefore)) {
            daysBefore++;
            dayBefore = dayBefore.minusDays(1);
        }

        if (daysBefore + daysAfter + 1 > 5) {
            throw new DaysInRowExceededException();
        }

    }

    private boolean hasShiftOnDay(List<Shift> assignedShifts, LocalDate day) {
        return assignedShifts.stream().anyMatch(shift -> shift.getStart().toLocalDate().equals(day));
    }

    private void validate24HourWindowRule(User user, Shift shift) throws HoursExceededException {
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
    }

    @Override
    public String toString() {
        return "Shop[id=%s, employees='%s', shifts='%s']".formatted(id, employees.toString(), shifts.toString());
    }

}
