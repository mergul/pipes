package com.streams.pipes.model;
import java.util.function.Function;
import static com.streams.pipes.model.ICustomerRegistrationValidator.*;
import static com.streams.pipes.model.ICustomerRegistrationValidator.ValidationResult.*;

public interface ICustomerRegistrationValidator extends Function<UserPayload, ValidationResult> {
    static ICustomerRegistrationValidator isEmailValid() {
        return customer -> customer.getId().contains("@") ? SUCCESS : EMAIL_NOT_VALID;
    }
    static ICustomerRegistrationValidator isPhoneValid() {
        return customer -> customer.getIsAdmin() ? SUCCESS : PHONE_NOT_VALID;
    }
    default ICustomerRegistrationValidator and(ICustomerRegistrationValidator other){
       return customer -> {
           ValidationResult result = this.apply(customer);
            return result.equals(SUCCESS)?other.apply(customer):result;
        };
    }

    enum ValidationResult{
        SUCCESS,
        PHONE_NOT_VALID,
        EMAIL_NOT_VALID
    }
}
