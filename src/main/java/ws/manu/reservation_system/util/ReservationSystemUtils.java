package ws.manu.reservation_system.util;

import ws.manu.reservation_system.model.Reservation;
import ws.manu.reservation_system.model.ServiceTier;
import ws.manu.reservation_system.model.User;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationSystemUtils {

    public static boolean isEligibleForApointmentBefore10(Reservation requestedReservation, User user) {
        return requestedReservation.getStart().getHour() >= 10 ||
                !requestedReservation.getStart().toLocalDate().isAfter(LocalDate.now().plus(3, ChronoUnit.DAYS)) ||
                !user.isNewUser();
    }

    public static Map<Long, LocalDateTime> nextAvailableAppointment(Reservation reservation) {
        Map<Long, LocalDateTime> retVal = new HashMap<>();
        retVal.put(ServiceTier.Tier1.getValue(), getNextAppointment(reservation, ServiceTier.Tier1));
        retVal.put(ServiceTier.Tier2.getValue(), getNextAppointment(reservation, ServiceTier.Tier2));
        retVal.put(ServiceTier.Tier3.getValue(), getNextAppointment(reservation, ServiceTier.Tier3));
        return retVal;
    }

    private static LocalDateTime getNextAppointment(Reservation reservation, ServiceTier serviceTier) {
        LocalDate nextAvailableAppointmentDate;

        // find current appointments end
        LocalDateTime currentAppointmentEnd = reservation.getStart();
        currentAppointmentEnd = currentAppointmentEnd.plus(reservation.getserviceTier().getValue(), ChronoUnit.MINUTES);

        // check if another inputted serviceTier appointment is possible today
        if (currentAppointmentEnd.plus(serviceTier.getValue(), ChronoUnit.MINUTES).getHour() >= 17) {
            // no more appointments possible as time is beyond closing so next appointment will be next day if not sunday
            nextAvailableAppointmentDate = currentAppointmentEnd.toLocalDate().plus(1, ChronoUnit.DAYS);
            if (nextAvailableAppointmentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                // if next day is sunday then move next tier 1 appointment to monday
                nextAvailableAppointmentDate = nextAvailableAppointmentDate.plus(1, ChronoUnit.DAYS);
            }
            // shop opens at 8
            return nextAvailableAppointmentDate.atTime(8, 0);
        } else {
            // another inputted serviceTier appointment is possible today
            return currentAppointmentEnd;
        }
    }

    // check if this user is requesting an appointment in the next 60 days
    public static boolean isAppointmentWithinNext60Days(Reservation requestedReservation) {
        return !requestedReservation.getStart().isAfter(LocalDateTime.now().plus(60, ChronoUnit.DAYS));
    }

    // check if this user has an booked appointment in the next 60 days already
    public static boolean isAppointmentAlreadyPresentForUser(List<Reservation> existingReservations, User user, Reservation requestedReservation) {
        if (existingReservations != null && existingReservations.size() > 0) {
            for (Reservation existingReservation : existingReservations) {
                if (existingReservation.getStart().isBefore(LocalDateTime.now().plus(60, ChronoUnit.DAYS))) {
                    return true;
                }
            }
        }
        return false;
    }


}
