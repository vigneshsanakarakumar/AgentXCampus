package com.agentx.campus.repository;

import com.agentx.campus.model.BusStop;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusStopRepository extends JpaRepository<BusStop, Long> {
    List<BusStop> findByRoute_IdOrderByStopOrder(Long routeId);
}
