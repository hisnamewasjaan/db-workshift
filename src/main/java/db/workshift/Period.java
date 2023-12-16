package db.workshift;

import java.time.LocalDateTime;

public class Period {

    LocalDateTime start;
    LocalDateTime end;

    Period(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }


}
