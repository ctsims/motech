package org.motechproject.security.service;

import org.motechproject.security.ex.InvalidTokenException;
import org.motechproject.security.ex.UserNotFoundException;
import org.motechproject.security.password.NonAdminUserException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

public interface PasswordRecoveryService {

    void passwordRecoveryRequest(String email, Locale locale) throws UserNotFoundException;

    void resetPassword(String token, String password, String passwordConfirmation) throws InvalidTokenException;

    void cleanUpExpiredRecoveries();

    boolean validateToken(String token);

    void oneTimeTokenOpenId(String email, Locale locale) throws UserNotFoundException, NonAdminUserException;

    void validateTokenAndLoginUser(String token, HttpServletRequest request, HttpServletResponse response) throws IOException;
}