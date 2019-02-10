package ws.manu.reservation_system.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class CustomDateTimeValidator implements ConstraintValidator<CustomDateTime, LocalDateTime> {

    @Override
    public void initialize(CustomDateTime paramA) {
    }

    @Override
    public boolean isValid(LocalDateTime dateTime, ConstraintValidatorContext ctx) {
        if (dateTime == null) {
            return false;
        }

        // check that date is between mon-sat, i.e. sunday the shop is closed
        if (DayOfWeek.SUNDAY == dateTime.getDayOfWeek()) {
            return false;
        }

        // check that the time is between 8AM-5PM
        return dateTime.getHour() >= 8 && dateTime.getHour() < 17;
    }

}
