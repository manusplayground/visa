package ws.manu.reservation_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "agent")
public class Agent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonIgnore
    private Integer id;

    @OneToMany(mappedBy = "agent",
            cascade = CascadeType.ALL)
    @JsonIgnore
    private List<NextAvailableReservation> next;
    private String name;

    public Agent(LocalDateTime next, String name) {
        super();
        this.name = name;
    }

    public Agent() {
        super();
    }

    public void addNextAvailableReservation(NextAvailableReservation nextAvailableReservation) {
        nextAvailableReservation.setAgent(this);
        next.add(nextAvailableReservation);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NextAvailableReservation> getNext() {
        return next;
    }

    public void setNext(List<NextAvailableReservation> next) {
        this.next = next;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id) &&
                Objects.equals(next, agent.next) &&
                Objects.equals(name, agent.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, next, name);
    }
}
