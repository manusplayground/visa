package ws.manu.reservation_system.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ws.manu.reservation_system.model.NextAvailableReservation;
import ws.manu.reservation_system.model.Reservation;
import ws.manu.reservation_system.repository.NextAvailableReservationRepository;
import ws.manu.reservation_system.repository.ReservationRepository;
import ws.manu.reservation_system.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {
    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NextAvailableReservationRepository nextAvailableReservationRepository;

    // Inserts row into table
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = false)
    public void addReservation(Reservation reservation) {
        List<Reservation> existingReservations = reservationRepository.findByAgentIdAndStartGreaterThanEqualAndEndLessThanEqual(reservation.getAgent().getId(),
                reservation.getStart(), reservation.getEnd());

        // check if requested tax filing agent is not already booked for the time requested
        // we are avoiding phantom reads using range locks from serializable isolation level
        if (existingReservations.size() > 0) {
            throw new RuntimeException(messageSource.getMessage("error.appointmentNoLongerAvailable", new Object[]{reservation.getAgent().getId(), reservation.getStart()}, reservation.getUser().getLocale()));
        }

        reservationRepository.save(reservation);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.SERIALIZABLE, readOnly = false)
    public void makeAllPastAvailableAppointmentsInPresent() {
        // check if any next available appointment in the db are in the past or not during office hours,
        // if yes then make it current
        List<NextAvailableReservation> nextAvailableReservations = nextAvailableReservationRepository.findAll();

        for (NextAvailableReservation r : nextAvailableReservations) {
            if (r.getNextAvailableReservation().isBefore((LocalDateTime.now())) ||
                    r.getNextAvailableReservation().getHour() >= 17 ||
                    r.getNextAvailableReservation().getHour() < 8) {
                if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SUNDAY) {
                    r.setNextAvailableReservation(LocalDate.now().atTime(8, 0).plus(1, ChronoUnit.DAYS));
                } else if (LocalDateTime.now().getDayOfWeek() == DayOfWeek.SATURDAY &&
                        LocalDateTime.now().getHour() >= 17) {
                    r.setNextAvailableReservation(LocalDate.now().atTime(8, 0).plus(2, ChronoUnit.DAYS));
                } else if (LocalDateTime.now().getHour() >= 17) {
                    r.setNextAvailableReservation(LocalDate.now().atTime(8, 0).plus(1, ChronoUnit.DAYS));
                } else {
                    r.setNextAvailableReservation(LocalDateTime.now().plus(1, ChronoUnit.MINUTES));
                }
            }
        }
    }
}

