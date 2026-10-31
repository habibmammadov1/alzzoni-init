package az.beit.alzoni.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteStep {
    private final ProcessRef process;
    private LocalDate startedAt;
    private LocalDate finishedAt;

    public RouteStep(ProcessRef process) {
        this.process = process;
    }

    public boolean isDone() {
        return finishedAt != null;
    }

    public boolean isRunning() {
        return startedAt != null && finishedAt == null;
    }
}
