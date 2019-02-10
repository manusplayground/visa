package ws.manu.reservation_system.controller;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import ws.manu.reservation_system.exception.UserNotFoundException;
import ws.manu.reservation_system.model.NextAvailableReservation;
import ws.manu.reservation_system.model.Reservation;
import ws.manu.reservation_system.model.ServiceTier;
import ws.manu.reservation_system.model.User;
import ws.manu.reservation_system.repository.AgentRepository;
import ws.manu.reservation_system.repository.NextAvailableReservationRepository;
import ws.manu.reservation_system.repository.ReservationRepository;
import ws.manu.reservation_system.repository.UserRepository;
import ws.manu.reservation_system.service.ReservationService;
import ws.manu.reservation_system.util.ReservationSystemUtils;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class UserController {
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private NextAvailableReservationRepository nextAvailableReservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AgentRepository agentRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // ------------ Get List of 5 latest available appointments ------------
    @RequestMapping(value = "/user/{userName}/appointments", method = RequestMethod.GET, produces = "application/json")
    public List<LocalDateTime> getAvailableAppointments(@PathVariable String userName,
                                                        @RequestParam("service_tier") ServiceTier tier) {
        // hardcoded locale to english
        Locale locale= Locale.US;

        // check if the user, who is trying to create reservation, is present or not
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new UserNotFoundException(messageSource.getMessage("error.userNotFound", new Object[]{userName}, locale));
        }

        // check if any next available appointment in the db are in the past, if yes then make it current
        reservationService.makeAllPastAvailableAppointmentsInPresent();

        // return 5 latest available appointments for the requested tier
        List<NextAvailableReservation> nextAvailableReservations = nextAvailableReservationRepository.findTop5ByServiceTierOrderByNextAvailableReservationAsc(tier);

        // just return an array of dates and times of available appointments
        List<LocalDateTime> retVal = new ArrayList<>();
        for (NextAvailableReservation next : nextAvailableReservations) {
            retVal.add(next.getNextAvailableReservation());
        }
        return retVal;
    }

    // ------------ Create a reservation ------------
    @RequestMapping(value = "/users/{userName}/appointments", method = RequestMethod.POST, produces = "application/json")
    public MappingJacksonValue addReservation(@PathVariable String userName,
                                              @Valid @RequestBody Reservation reservation) {
        // hardcoded locale to english
        Locale locale= Locale.US;

        // check if the user, who is trying to create reservation, is present or not
        User user = userRepository.findByUserName(userName);

        if (user == null) {
            throw new UserNotFoundException(messageSource.getMessage("error.userNotFound", new Object[]{userName}, locale));
        }

        // check that appointment is in future and within the 60 day threshold
        if (reservation.getStart().isAfter(LocalDateTime.now()) && !ReservationSystemUtils.isAppointmentWithinNext60Days(reservation)) {
            throw new IllegalArgumentException(messageSource.getMessage("error.appointmentAfter60DayThreshold", null, locale));
        }

        //  iff user is a returning users then allow them to book before 8am iff they are booking after next 3 days.
        // anyone can book between 8-5 if booking within next 3 days
        if (!ReservationSystemUtils.isEligibleForApointmentBefore10(reservation, user)) {
            throw new IllegalArgumentException(messageSource.getMessage("error.appointmentBefore10AM", null, locale));
        }

        // check that the user is not trying to book more than 1 appointments
        List<Reservation> existingReservationsForUser = reservationRepository.findByUserId(user.getId());
        if (ReservationSystemUtils.isAppointmentAlreadyPresentForUser(existingReservationsForUser, user, reservation)) {
            throw new IllegalArgumentException(messageSource.getMessage("error.appointmentExistsAlready", null, locale));
        }

        // find available agent for this reservation based on requested time (requested time shoould be one shown in output of /appointments GET call)
        List<NextAvailableReservation> agentsAvailable = nextAvailableReservationRepository.findOneByNextAvailableReservation(reservation.getStart());
        if (agentsAvailable == null || agentsAvailable.size() == 0) {
            throw new IllegalArgumentException(messageSource.getMessage("error.invalidAppointment", null, locale));
        }

        // set user details for reservation
        reservation.setUser(user);

        // capture users locale preference to show messages in native locale
        //TODO capture users timezone in future
        if (reservation.getUser().getLocale() == null && locale != null) {
            reservation.getUser().setLocale(locale);
        }

        // set end time of reservation on the basis of requested tier
        reservation.setEnd(reservation.getStart().plus(reservation.getserviceTier().getValue(), ChronoUnit.MINUTES));

        // mark the user who requested this reservation as a returning user (returning user is eligible for making appointments before 10am)
        reservation.getUser().setNewUser(false);

        // assign agent to this reservation
        reservation.setAgent(agentsAvailable.get(0).getAgent());

        // reset aganet's tier wise next available appointment times
        reservation.getAgent().getNext().clear();
        nextAvailableReservationRepository.deleteAllByAgentId(reservation.getAgent().getId());
        Map<Long, LocalDateTime> nextAvailableAppointmentPerTier = ReservationSystemUtils.nextAvailableAppointment(reservation);
        for (Long i : nextAvailableAppointmentPerTier.keySet()) {
            NextAvailableReservation n = new NextAvailableReservation();
            n.setServiceTier(ServiceTier.fromValue(i));
            n.setNextAvailableReservation(nextAvailableAppointmentPerTier.get(i));
            n.setAgent(reservation.getAgent());
            reservation.getAgent().addNextAvailableReservation(n);
        }

        // save reservation in database
        reservationService.addReservation(reservation);

        // prepare json to be sent in response
        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.filterOutAllExcept("agent", "start");
        FilterProvider filters = new SimpleFilterProvider().addFilter("AppointmentConfirmationFilter", filter);
        MappingJacksonValue mapping = new MappingJacksonValue(reservation);
        mapping.setFilters(filters);

        return mapping;
    }
}
