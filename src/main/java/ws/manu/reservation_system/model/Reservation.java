package ws.manu.reservation_system.model;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.format.annotation.DateTimeFormat;
import ws.manu.reservation_system.validators.CustomDateTime;

import javax.persistence.*;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@JsonFilter("AppointmentConfirmationFilter")
public class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'hh:mm:ss.SSS")
    @CustomDateTime
    @FutureOrPresent
    private LocalDateTime start;

    private LocalDateTime end;

    @Column(name = "service_tier")
    @NotNull
    private ServiceTier serviceTier;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @JsonUnwrapped
    private Agent agent;

    public Reservation(LocalDateTime start_date_time, ServiceTier serviceTier) {
        super();
        this.start = start_date_time;
        this.serviceTier = serviceTier;
    }

    public Reservation() {
        super();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ServiceTier getserviceTier() {
        return serviceTier;
    }

    public void setType(ServiceTier type) {
        this.serviceTier = serviceTier;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start_date_time) {
        this.start = start_date_time;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end_date_time) {
        this.end = end_date_time;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

}
