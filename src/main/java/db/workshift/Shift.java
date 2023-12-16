package db.workshift;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class Shift {

    private final LocalDateTime start;
    private final Duration duration;
    private User assignee;

    private Shift(LocalDateTime start, Duration duration){
        this.start = start;
        this.duration = duration;
    }

    static Shift create(LocalDateTime start, Duration duration) {
        return new Shift(start, duration);
    }

    LocalDateTime getStart() {
        return start;
    }

    LocalDateTime getEnd() {
        return start.plus(duration);
    }

    Duration getDuration() {
        return duration;
    }

    public void assign(User user) {
        assignee = user;
    }

    boolean isAssigned() {
        return assignee != null;
    }

    boolean unAssigned() {
        return assignee == null;
    }

    boolean isAssignedTo(User user) {
        return assignee == user;
    }

    public Duration overlap(Period period) {
        Duration duration;
        if (period.end.isBefore(this.start) || this.getEnd().isBefore(period.start)) {
            duration = Duration.ofHours(0);
        } else {
            LocalDateTime latestStart = period.start.isBefore(this.start) ? this.start: period.start;
            LocalDateTime earliestEnd = period.end.isBefore(this.getEnd()) ? period.end: this.getEnd();
            duration = Duration.between(latestStart, earliestEnd);
        }
        return duration;
    }
}
