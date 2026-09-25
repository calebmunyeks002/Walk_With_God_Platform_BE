package org.walkwithgod.config;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestControllerAdvice public class GlobalExceptionHandler{@ExceptionHandler(IllegalArgumentException.class)ResponseEntity<?> bad(IllegalArgumentException e){return ResponseEntity.badRequest().body(Map.of("message",e.getMessage()));}@ExceptionHandler(NoSuchElementException.class)ResponseEntity<?> notFound(NoSuchElementException e){return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Resource not found"));}}
