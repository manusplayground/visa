package ws.manu.reservation_system.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CustomDateTimeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomDateTime {

    String message() default "Please check the requested appointment time as our working hours are from monday to saturday from 8am to 5pm";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}