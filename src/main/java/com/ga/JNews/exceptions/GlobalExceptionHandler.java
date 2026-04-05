package com.ga.JNews.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle Information already exists error
     * @param e Exception
     * @return ResponseEntity
     */
    @ExceptionHandler(value = InformationExistException.class)
    public ResponseEntity<String> handleInformationExist(Exception e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

    /**
     * Handle Information not found error
     * @param e Exception
     * @return ResponseEntity
     */
    @ExceptionHandler(value = InformationNotFoundException.class)
    public ResponseEntity<String> handleInformationNotFound(Exception e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    /**
     * Handle bad request exception
     * @param e Exception
     * @return ResponseEntity String
     */
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<String> handleBadRequest(Exception e) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    /**
     * Handle access denied exception
     * @param e Exception
     * @return ResponseEntity String
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(Exception e) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(e.getMessage());
    }

    /**
     * Handle unauthorized authentication exception
     * @param e Exception
     * @return ResponseEntity String
     */
    @ExceptionHandler(value = AuthenticationException.class)
    public ResponseEntity<String> handleAuthentication(Exception e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    /**
     * Handle unauthorized authentication exception
     * @param e Exception
     * @return ResponseEntity String
     */
    @ExceptionHandler(value = BadCredentialException.class)
    public ResponseEntity<String> handleBadCredential(Exception e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }
}
