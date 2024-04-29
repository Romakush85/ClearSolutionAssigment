package com.clearsolution.testassigment.controllers;
import com.clearsolution.testassigment.models.DTOs.UserDTO;
import com.clearsolution.testassigment.services.UsersService;
import com.clearsolution.testassigment.exceptions.ValidationException;
import com.clearsolution.testassigment.exceptions.WrongRequestException;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Tag(name="Users")
public class UsersController {
    private final UsersService usersService;
    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @Operation(summary="Find user by it's ID")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return new ResponseEntity<>(usersService.getUserById(id), HttpStatus.OK);
    }

    @Operation(summary="Creates a new user")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO dto) {
        UserDTO createdUser = usersService.createUser(dto);
        return  ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdUser);
    }

    @Operation(summary = "Update entire user")
    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        if (!id.equals(dto.getId())) {
            throw new WrongRequestException("User's ID in path doesn't match user's ID in request body ");
        }
        UserDTO updatedUser = usersService.updateUser(dto);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Update user's  fields",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schema = @Schema(type = "object", example = "{\"email\": \"updated@email.com\"}"))))
    @PatchMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserFields(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        UserDTO updatedUser = usersService.updateUserFields(id, fields);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/birthdate")
    @Operation(summary = "Find users by birth date range ")
    public ResponseEntity<List<UserDTO>> getUsersByBirthDateRange(
            @Parameter(description = "Start date (format: yyyy-MM-dd)", example = "1980-10-10", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,

            @Parameter(description = "End date (format: yyyy-MM-dd)", example = "2000-10-10", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date to) {
        if (from.after(to)) {
            throw new ValidationException("Invalid date range: 'from' should be before 'to'");
        }
        List<UserDTO> users = usersService.getUsersByBirthDateRange(from, to);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        usersService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

}
