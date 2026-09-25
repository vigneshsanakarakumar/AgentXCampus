package com.agentx.campus.service;

import com.agentx.campus.agent.NotificationAgent;
import com.agentx.campus.model.*;
import com.agentx.campus.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HostelTransportService {

    private final HostelBlockRepository blockRepo;
    private final HostelRoomRepository roomRepo;
    private final HostelAllocationRepository allocationRepo;
    private final GatePassRequestRepository gatePassRepo;
    private final BusRouteRepository routeRepo;
    private final BusStopRepository stopRepo;
    private final StudentBusPassRepository passRepo;
    private final UserRepository userRepo;
    private final StudentProfileRepository profileRepo;
    private final NotificationAgent notificationAgent;

    public HostelTransportService(HostelBlockRepository blockRepo, HostelRoomRepository roomRepo,
                                   HostelAllocationRepository allocationRepo, GatePassRequestRepository gatePassRepo,
                                   BusRouteRepository routeRepo, BusStopRepository stopRepo,
                                   StudentBusPassRepository passRepo, UserRepository userRepo,
                                   StudentProfileRepository profileRepo, NotificationAgent notificationAgent) {
        this.blockRepo = blockRepo; this.roomRepo = roomRepo;
        this.allocationRepo = allocationRepo; this.gatePassRepo = gatePassRepo;
        this.routeRepo = routeRepo; this.stopRepo = stopRepo;
        this.passRepo = passRepo; this.userRepo = userRepo;
        this.profileRepo = profileRepo; this.notificationAgent = notificationAgent;
    }

    // ─── HOSTEL ──────────────────────────────────────────────────────────────

    public Map<String, Object> getStudentHostelInfo(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        Optional<HostelAllocation> alloc = allocationRepo
                .findFirstByStudent_IdAndStatusOrderByFromDateDesc(user.getId(), "ACTIVE");

        Map<String, Object> result = new HashMap<>();
        if (alloc.isPresent()) {
            HostelAllocation a = alloc.get();
            result.put("allocated", true);
            result.put("roomNumber", a.getRoomNumber());
            result.put("blockName", a.getBlockName());
            result.put("wardenName", a.getWardenName());
            result.put("wardenContact", a.getWardenContact());
            result.put("amenities", a.getAmenities());
            result.put("fromDate", a.getFromDate());
            result.put("status", a.getStatus());
        } else {
            result.put("allocated", false);
            result.put("message", "No active hostel allocation found.");
        }
        return result;
    }

    public Map<String, Object> getStudentBusInfo(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        Optional<StudentBusPass> pass = passRepo
                .findFirstByStudent_IdAndStatusOrderByValidFromDesc(user.getId(), "ACTIVE");

        Map<String, Object> result = new HashMap<>();
        if (pass.isPresent()) {
            StudentBusPass p = pass.get();
            result.put("hasPass", true);
            result.put("routeNumber", p.getRouteNumber());
            result.put("routeName", p.getRouteName());
            result.put("departureTime", p.getDepartureTime());
            result.put("returnTime", p.getReturnTime());
            result.put("boardingStop", p.getBoardingStopName());
            result.put("validFrom", p.getValidFrom());
            result.put("validTo", p.getValidTo());
            result.put("status", p.getStatus());
            if (p.getRoute() != null) {
                List<BusStop> stops = stopRepo.findByRoute_IdOrderByStopOrder(p.getRoute().getId());
                result.put("allStops", stops);
            }
        } else {
            result.put("hasPass", false);
            result.put("message", "No active bus pass found.");
            result.put("availableRoutes", routeRepo.findAll());
        }
        return result;
    }

    @Transactional
    public GatePassRequest submitGatePass(String username, Map<String, Object> body) {
        User student = userRepo.findByUsername(username).orElseThrow();
        User admin = userRepo.findAll().stream()
                .filter(u -> "ADMIN".equals(u.getRole().name())).findFirst().orElse(null);

        GatePassRequest req = new GatePassRequest();
        req.setStudent(student);
        req.setPurpose(body.getOrDefault("purpose", "").toString());
        req.setDestination(body.containsKey("destination") ? body.get("destination").toString() : null);
        req.setAssignedToRole("ADMIN");

        // Parse datetime strings
        try { req.setOutDateTime(LocalDateTime.parse(body.get("outDateTime").toString())); }
        catch (Exception e) { req.setOutDateTime(LocalDateTime.now().plusHours(1)); }
        try { req.setExpectedReturnDateTime(LocalDateTime.parse(body.get("expectedReturnDateTime").toString())); }
        catch (Exception e) { req.setExpectedReturnDateTime(LocalDateTime.now().plusHours(4)); }

        req = gatePassRepo.save(req);

        try {
            if (admin != null) {
                notificationAgent.notifyUser(admin,
                        "Gate Pass Request: " + req.getStudentName(),
                        "Destination: " + req.getDestination() + " | Out: " + req.getOutDateTime(),
                        "GATE_PASS_UPDATE");
            }
        } catch (Exception ignored) {}
        return req;
    }

    public List<GatePassRequest> getStudentGatePasses(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();
        return gatePassRepo.findByStudentOrderByCreatedAtDesc(user);
    }

    @Transactional
    public GatePassRequest updateGatePassStatus(Long id, String status, String adminUsername) {
        GatePassRequest req = gatePassRepo.findById(id).orElseThrow();
        User admin = userRepo.findByUsername(adminUsername).orElseThrow();
        req.setStatus(status.toUpperCase());
        req.setApprovedBy(admin);
        req = gatePassRepo.save(req);

        try {
            notificationAgent.notifyUser(req.getStudent(),
                    "Gate Pass " + req.getStatus(),
                    "Your gate pass request has been " + req.getStatus().toLowerCase() + ".",
                    "GATE_PASS_UPDATE");
        } catch (Exception ignored) {}
        return req;
    }

    public List<GatePassRequest> getAllGatePasses() {
        return gatePassRepo.findAllByOrderByCreatedAtDesc();
    }

    public List<HostelBlock> getAllBlocks() { return blockRepo.findAll(); }

    // ─── TRANSPORT ───────────────────────────────────────────────────────────

    public List<BusRoute> getAllRoutes() { return routeRepo.findAll(); }

    public List<BusStop> getRouteStops(Long routeId) {
        return stopRepo.findByRoute_IdOrderByStopOrder(routeId);
    }
}
