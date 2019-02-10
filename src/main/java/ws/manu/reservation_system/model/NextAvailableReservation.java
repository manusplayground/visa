package ws.manu.reservation_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "next_available_reservation")
public class NextAvailableReservation {
    @JsonIgnore
    ServiceTier serviceTier;
    @JsonIgnore
    LocalDateTime nextAvailableReservation;
    @Id
    @SequenceGenerator(name = "seq", initialValue = 100)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq")
    @JsonIgnore
    private int id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    @JsonIgnore
    private Agent agent;

    public NextAvailableReservation() {
    }

    public NextAvailableReservation(ServiceTier serviceTier) {
        this.serviceTier = serviceTier;
    }

    public NextAvailableReservation(ServiceTier serviceTier, LocalDateTime next) {
        this.serviceTier = serviceTier;
        this.nextAvailableReservation = next;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NextAvailableReservation that = (NextAvailableReservation) o;
        return id == that.id &&
                serviceTier == that.serviceTier &&
                Objects.equals(nextAvailableReservation, that.nextAvailableReservation) &&
                Objects.equals(agent, that.agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, serviceTier, nextAvailableReservation, agent);
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getNextAvailableReservation() {
        return nextAvailableReservation;
    }

    public void setNextAvailableReservation(LocalDateTime nextAvailableReservation) {
        this.nextAvailableReservation = nextAvailableReservation;
    }

    public ServiceTier getServiceTier() {
        return serviceTier;
    }

    public void setServiceTier(ServiceTier serviceTier) {
        this.serviceTier = serviceTier;
    }
}
