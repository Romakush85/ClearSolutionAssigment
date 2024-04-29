package com.clearsolution.testassigment.controllers;

import com.clearsolution.testassigment.exceptions.GlobalExceptionsHandler;
import com.clearsolution.testassigment.exceptions.UserNotFoundException;
import com.clearsolution.testassigment.exceptions.ValidationException;
import com.clearsolution.testassigment.exceptions.WrongRequestException;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import com.clearsolution.testassigment.models.DTOs.UserDTO;
import com.clearsolution.testassigment.services.UsersService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(UsersController.class)
class UsersControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsersService usersService;


    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders
                .standaloneSetup(new UsersController(usersService))
                .setControllerAdvice(new GlobalExceptionsHandler())
                .build();
    }

     UserDTO newValidUser = new UserDTO.Builder()
            .id(0l)
            .email("romakush@gmail.com")
            .firstName("Roman")
            .lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5))
            .address("Some address")
            .phoneNumber("+380935288886")
            .build();

    UserDTO newInvalidUser = new UserDTO.Builder()
            .id(0l)
            .email("romakushgmail.com")
            .firstName("Roman")
            .lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5))
            .address("Some address")
            .phoneNumber("380958886")
            .build();

    UserDTO createdUser = new UserDTO.Builder()
            .id(1L)
            .email("romakush@gmail.com")
            .firstName("Roman")
            .lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5))
            .address("Some address")
            .phoneNumber("+380935288886")
            .build();

    UserDTO updatedUser = new UserDTO.Builder()
            .id(1L)
            .email("romakush85@gmail.com")
            .firstName("Roma")
            .lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5))
            .address("Updated address")
            .phoneNumber("+380935288886")
            .build();


    @Test
    void getUserByIdShouldReturnCreatedUser() throws Exception {
        when(usersService.getUserById(1L)).thenReturn(createdUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Roman"))
                .andExpect(jsonPath("$.email").value("romakush@gmail.com"));
    }

    @Test
    void createUserShouldReturnStatusCreatedInCaseOfValidUser() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        String json = objectMapper.writeValueAsString(newValidUser);

        when(usersService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Roman"))
                .andExpect(jsonPath("$.email").value("romakush@gmail.com"));
    }

    @Test
    void createUserShouldReturnMethodArgumentNotValidExceptionInCaseOfInvalidUser() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        String json = objectMapper.writeValueAsString(newInvalidUser);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    if (!(result.getResolvedException() instanceof MethodArgumentNotValidException)) {
                        throw new AssertionError("Expected MethodArgumentNotValidException");
                    }
                });
    }

    @Test
    void updateUserShouldReturnUpdatedUser() throws Exception {
        when(usersService.updateUser(any(UserDTO.class))).thenReturn(updatedUser);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        String json = objectMapper.writeValueAsString(updatedUser);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("Roma"))
                .andExpect(jsonPath("$.email").value("romakush85@gmail.com"));
    }

    @Test
    void updateUserShouldReturnWrongRequestException() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        String json = objectMapper.writeValueAsString(updatedUser);

        mockMvc.perform(put("/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    if (!(result.getResolvedException() instanceof WrongRequestException)) {
                        throw new AssertionError("Expected WrongRequestException");
                    }
                });
    }

    @Test
    void updateUserShouldReturnUserNotFoundExceptionInCaseOfWrongId() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
        String json = objectMapper.writeValueAsString(updatedUser);

        when(usersService.updateUser(any(UserDTO.class))).thenThrow(UserNotFoundException.class);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    if (!(result.getResolvedException() instanceof UserNotFoundException)) {
                        throw new AssertionError("Expected UserNotFoundException");
                    }
                });
    }

    @Test
    void updateUserFieldsShouldReturnUpdatedUser() throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("email", "romakush85@gmail.com");
        fields.put("firstName", "Roma");
        fields.put("address", "Updated address");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonFields = objectMapper.writeValueAsString(fields);

        when(usersService.updateUserFields(createdUser.getId(), fields)).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFields))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("romakush85@gmail.com"))
                .andExpect(jsonPath("$.firstName").value("Roma"))
                .andExpect(jsonPath("$.address").value("Updated address"));
    }

    @Test
    void updateUserFieldsShouldReturnWrongRequestExceptionForInvalidField() throws Exception {
        Map<String, Object> fields = new HashMap<>();
        fields.put("invalidField", "InvalidValue");
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonFields = objectMapper.writeValueAsString(fields);

        when(usersService.updateUserFields(createdUser.getId(), fields)).thenThrow(WrongRequestException.class);

        // Perform the PATCH request
        mockMvc.perform(patch("/users/{id}", createdUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFields))
                .andExpect(status().isBadRequest())
                .andExpect(result -> {
                    if (!(result.getResolvedException() instanceof WrongRequestException)) {
                        throw new AssertionError("Expected WrongRequestException");
                    }
                });
    }

    @Test
    void getUsersByBirthDateRangeShouldReturnListOfUsers() throws Exception {
        List<UserDTO> users = new ArrayList<>();
        users.add(createdUser);
    when(usersService.getUsersByBirthDateRange(any(Date.class), any(Date.class))).thenReturn(users);

    mockMvc.perform(get("/users/birthdate")
                .param("from", "1985-01-01")
                .param("to", "1999-12-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].firstName").value("Roman"))
            .andExpect(jsonPath("$[0].lastName").value("Kushnir"));
}

    @Test
    void getUsersByBirthDateRangeShouldReturnValidationError() throws Exception {
        mockMvc.perform(get("/users/birthdate")
                        .param("from", "2000-07-05")
                        .param("to", "1999-05-05"))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof ValidationException))
                .andExpect(jsonPath("$.message").value("Invalid date range: 'from' should be before 'to'"));
    }

    @Test
    void deleteUserShouldReturnUserNotFoundExceptionInCaseOfWrongId() throws Exception {
        doThrow(UserNotFoundException.class).when(usersService).deleteUser(100l);

        mockMvc.perform(delete("/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    if (!(result.getResolvedException() instanceof UserNotFoundException)) {
                        throw new AssertionError("Expected UserNotFoundException");
                    }
                });
    }

}