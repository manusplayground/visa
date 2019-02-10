package ws.manu.reservation_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ws.manu.reservation_system.model.Agent;
import ws.manu.reservation_system.model.ServiceTier;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Integer> {
    List<Agent> findTop5ByNext_ServiceTierOrderByNext_NextAvailableReservationAsc(ServiceTier tier);

    void deleteAllById(Integer id);
}