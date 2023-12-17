package db.workshift;

import db.workshift.users.User;
import jakarta.persistence.*;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime start;
    private Duration duration;

    @OneToOne
    private User assignee;

    protected Shift() {
    }

    private Shift(LocalDateTime start, Duration duration) {
        this.start = start;
        this.duration = duration;
    }

    static Shift create(LocalDateTime start, Duration duration) {
        Assert.notNull(start, "start cannot be null");
        Assert.notNull(duration, "duration cannot be null");

        return new Shift(start, duration);
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    LocalDateTime getEnd() {
        return start.plus(duration);
    }

    public Duration getDuration() {
        return duration;
    }

    public void assign(User user) {
        assignee = user;
    }

    boolean unAssigned() {
        return assignee == null;
    }

    boolean isAssignedTo(User user) {
        return assignee == user;
    }

    public boolean hasOverlap(Shift otherShift) {
        return overlap(new Period(otherShift.start, otherShift.getEnd())) != Duration.ZERO;
    }

    public Duration overlap(Period period) {
        Duration duration;
        if (period.end.isBefore(this.start) || this.getEnd().isBefore(period.start)) {
            duration = Duration.ZERO;
        } else {
            LocalDateTime latestStart = period.start.isBefore(this.start) ? this.start : period.start;
            LocalDateTime earliestEnd = period.end.isBefore(this.getEnd()) ? period.end : this.getEnd();
            duration = Duration.between(latestStart, earliestEnd);
        }
        return duration;
    }

    @Override
    public String toString() {
        return "Shift[id=%s, start='%s', duration='%s', assignee='%s']"
                .formatted(
                        id,
                        start.toString(),
                        duration.toString(),
                        assignee != null ? assignee.toString(): "");
    }

}
