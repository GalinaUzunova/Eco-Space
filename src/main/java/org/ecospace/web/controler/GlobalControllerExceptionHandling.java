package org.ecospace.web.controler;

import org.ecospace.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalControllerExceptionHandling {

    @ExceptionHandler(SubscriptionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)

    public ModelAndView handleException(SubscriptionNotFoundException e) {

        return new ModelAndView("not-found");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handelUserNotFound(UserNotFoundException e) {

        return new ModelAndView("not-found");
    }

    @ExceptionHandler(ProductNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView productNotFound(ProductNotFound e) {

        return new ModelAndView("not-found");

    }

    @ExceptionHandler(AccesDeniedException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public ModelAndView notAuthoriseEx(AccesDeniedException e) {

        return new ModelAndView("internal-server-error");
    }
    @ExceptionHandler(PaymentException.class)
    public ModelAndView handlePaymentException(){

        return  new ModelAndView("internal-server-error");

    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleFAilNotification(){
        return new ModelAndView("internal-server-error");

    }
    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllOthersE(Exception e) {

        return new ModelAndView("internal-server-error");
    }

}

