package db.workshift

import spock.lang.Specification
import spock.lang.Unroll

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

@Unroll
class ShopSpec extends Specification {

    private static final Duration EIGHT_HOURS = Duration.ofHours(8)
    private static final LocalDateTime MIDNIGHT_DEC_17_2023 = LocalDate.of(2023, Month.DECEMBER, 17).atStartOfDay()
    private static final LocalDateTime DEC_17_2023_8AM = MIDNIGHT_DEC_17_2023.plus(EIGHT_HOURS)
    private static final String USERNAME_BENT = 'Bent Jørgensen'
    private static final User USER_BENT = User.create(USERNAME_BENT)

    void 'add user to shop'() {
        given: 'empty shop'
        Shop shop = Shop.create()

        expect: 'shop does not know user'
        !shop.hasUser(USER_BENT)

        when: 'adding user to shop'
        shop.addUser(USER_BENT)

        then: 'shop knows about user'
        shop.hasUser(USER_BENT)
    }

    void 'create a shift in a shop'() {
        given: 'a shop'
        Shop shop = Shop.create()

        expect: 'no available shifts'
        !shop.getAvailableShifts()

        when: 'adding shift to shop'
        shop.addShift(DEC_17_2023_8AM, EIGHT_HOURS)

        then: 'the shift is available'
        verifyAll(shop.getAvailableShifts()) {
            size() == 1
            get(0).start == DEC_17_2023_8AM
        }
    }

    void 'add user to a shift in a shop'() {
        given: 'a shop with a user and an available shift'
        Shop shop = Shop.create()
        shop.addUser(USER_BENT)
        shop.addShift(DEC_17_2023_8AM, EIGHT_HOURS)

        expect: 'available shifts'
        List<Shift> shifts = shop.getAvailableShifts()
        shifts

        when: 'assign user to shift in a shop'
        shop.assignUserToShift(USER_BENT, shifts.get(0))

        then: 'no available shifts'
        verifyAll(shop.getAvailableShifts()) {
            size() == 0
        }

        and: 'user is assigned the expected shift'
        verifyAll(shop.getAssignedShifts(USER_BENT)) {
            size() == 1
            get(0).start == DEC_17_2023_8AM
        }

    }

    void 'user not allowed to work in a shop for more than 8 hours within a 24 hour time window'() {
        given: 'a shop with a user and available shifts'
        Shop shop = Shop.create()
        shop.addUser(USER_BENT)
        addShifts(shop, MIDNIGHT_DEC_17_2023, Duration.ofHours(72), EIGHT_HOURS)

        expect: '9 available shifts'
        List<Shift> shifts = shop.getAvailableShifts()
        verifyAll(shifts) {
            size() == 9
            shifts*.start*.toString() == ['2023-12-17T00:00',
                                          '2023-12-17T08:00',
                                          '2023-12-17T16:00',
                                          '2023-12-18T00:00',
                                          '2023-12-18T08:00',
                                          '2023-12-18T16:00',
                                          '2023-12-19T00:00',
                                          '2023-12-19T08:00',
                                          '2023-12-19T16:00']
        }

        when: 'assign user to shift'
        shop.assignUserToShift(USER_BENT, shifts.get(initialShift))

        then: '8 available shifts'
        verifyAll(shop.getAvailableShifts()) {
            size() == 8
        }

        when: 'assign user to other shift in same 24 hour window'
        shop.assignUserToShift(USER_BENT, shifts.get(illegalShift))

        then: 'ikke tilladt'
        thrown(HoursExceededException)

        where:
        initialShift || illegalShift
        0            || 1
        0            || 2
        1            || 3
        8            || 7
        8            || 6
        7            || 5
    }

    private static Shop addShifts(
            Shop shop,
            LocalDateTime start,
            Duration totalDuration,
            Duration shiftDuration) {

        Duration assignedTotal = Duration.ofHours(0)

        while (assignedTotal < totalDuration) {
            shop.addShift(start + assignedTotal, shiftDuration)
            assignedTotal = assignedTotal + shiftDuration
        }

        return shop
    }

}
