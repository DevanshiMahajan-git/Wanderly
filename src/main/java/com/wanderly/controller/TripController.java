package com.wanderly.controller;

import com.wanderly.model.*;
import com.wanderly.repository.TripRepository;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class TripController {

    private final TripRepository repo;

    public TripController(TripRepository repo) {
        this.repo = repo;
    }

    @ModelAttribute("statuses")
    public TripStatus[] statuses() {
        return TripStatus.values();
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TripStatus status,
            Model m) {

        // Normalize the search input.
        String searchQuery = (q == null) ? "" : q.trim();

        // Search by trip title or destination.
        List<Trip> trips = searchQuery.isBlank()
                ? repo.findAll()
                : repo.findByTitleContainingIgnoreCaseOrDestinationContainingIgnoreCase(
                        searchQuery, searchQuery);

        // Apply the optional status filter.
        if (status != null) {
            trips = trips.stream()
                    .filter(trip -> trip.getStatus() == status)
                    .collect(Collectors.toList());
        }

        // Pass trip data and filter selections to the dashboard.
        m.addAttribute("trips", trips);
        m.addAttribute("query", searchQuery);
        m.addAttribute("selectedStatus", status);

        // Dashboard statistics.
        m.addAttribute("totalTrips", repo.count());
        m.addAttribute("plannedTrips",
                repo.countByStatus(TripStatus.PLANNED));
        m.addAttribute("activeTrips",
                repo.countByStatus(TripStatus.IN_PROGRESS));
        m.addAttribute("completedTrips",
                repo.countByStatus(TripStatus.COMPLETED));

        return "dashboard";
    }

    @GetMapping("/trips/new")
    public String createForm(Model m) {
        Trip trip = new Trip();
        trip.setTravellers(2);
        trip.setStatus(TripStatus.PLANNED);

        m.addAttribute("trip", trip);
        m.addAttribute("formTitle", "Plan a new trip");

        return "trip-form";
    }

    @PostMapping("/trips")
    public String create(
            @Valid @ModelAttribute Trip trip,
            BindingResult r,
            Model m,
            RedirectAttributes a) {

        validateTripDates(trip, r);

        if (r.hasErrors()) {
            m.addAttribute("formTitle", "Plan a new trip");
            return "trip-form";
        }

        repo.save(trip);
        a.addFlashAttribute("success", "Your trip has been added.");

        return "redirect:/";
    }

    @GetMapping("/trips/{id}/edit")
    public String edit(@PathVariable Long id, Model m) {
        m.addAttribute("trip", repo.findById(id).orElseThrow());
        m.addAttribute("formTitle", "Edit your trip");

        return "trip-form";
    }

    @PostMapping("/trips/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute Trip trip,
            BindingResult r,
            Model m,
            RedirectAttributes a) {

        validateTripDates(trip, r);

        if (r.hasErrors()) {
            m.addAttribute("formTitle", "Edit your trip");
            return "trip-form";
        }

        trip.setId(id);
        repo.save(trip);

        a.addFlashAttribute("success", "Trip updated.");

        return "redirect:/";
    }

    @PostMapping("/trips/{id}/status")
    public String status(
            @PathVariable Long id,
            @RequestParam TripStatus status,
            @RequestParam(defaultValue = "Planner") String role,
            RedirectAttributes a) {

        Trip trip = repo.findById(id).orElseThrow();

        if (role.equalsIgnoreCase("Reviewer")
                || status == TripStatus.PLANNED) {

            trip.setStatus(status);
            repo.save(trip);

            a.addFlashAttribute("success", "Status updated.");

        } else {
            a.addFlashAttribute(
                    "error",
                    "A Reviewer is required for this status change in the demo workflow.");
        }

        return "redirect:/";
    }

    @PostMapping("/trips/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes a) {

        repo.deleteById(id);
        a.addFlashAttribute("success", "Trip removed.");

        return "redirect:/";
    }

    private void validateTripDates(Trip trip, BindingResult r) {
        if (trip.getStartDate() != null
                && trip.getEndDate() != null
                && trip.getEndDate().isBefore(trip.getStartDate())) {

            r.rejectValue(
                    "endDate",
                    "date.invalid",
                    "Return date must be on or after departure.");
        }
    }
}