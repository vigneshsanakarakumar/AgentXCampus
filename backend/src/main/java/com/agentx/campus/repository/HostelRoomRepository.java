package com.agentx.campus.repository;

import com.agentx.campus.model.HostelRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HostelRoomRepository extends JpaRepository<HostelRoom, Long> {
    List<HostelRoom> findByBlock_IdOrderByRoomNumber(Long blockId);
}
