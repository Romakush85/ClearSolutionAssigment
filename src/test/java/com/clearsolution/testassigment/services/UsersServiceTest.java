package com.clearsolution.testassigment.services;

import com.clearsolution.testassigment.configs.ModelMapperConfig;
import com.clearsolution.testassigment.controllers.UsersController;
import com.clearsolution.testassigment.exceptions.GlobalExceptionsHandler;
import com.clearsolution.testassigment.exceptions.UserNotFoundException;
import com.clearsolution.testassigment.exceptions.ValidationException;
import com.clearsolution.testassigment.exceptions.WrongRequestException;
import com.clearsolution.testassigment.models.entities.UserEntity;
import com.clearsolution.testassigment.repositories.UsersRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
class UsersServiceTest {
    @MockBean
    private UsersRepository usersRepository;

    private final ModelMapper userMapper = new ModelMapper();

    @Autowired
    private UsersService usersService;

    UserDTO newValidUser = new UserDTO.Builder()
            .id(0l).email("romakush@gmail.com").firstName("Roman").lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5)).address("Some address")
            .phoneNumber("+380935288886").build();

    UserDTO newInvalidUser = new UserDTO.Builder()
            .id(0l).email("romakushgmail.com").firstName("Roman").lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5)).address("Some address")
            .phoneNumber("380958886").build();

    UserDTO createdUser = new UserDTO.Builder()
            .id(1L).email("romakush@gmail.com").firstName("Roman").lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5)).address("Some address")
            .phoneNumber("+380935288886").build();

    UserDTO underAgeUser = new UserDTO.Builder()
            .id(1L).email("romakush@gmail.com").firstName("Roman").lastName("Kushnir")
            .birthDate(new Date(110, Calendar.JANUARY, 5)).address("Some address")
            .phoneNumber("+380935288886").build();

    UserDTO updatedUser = new UserDTO.Builder()
            .id(1L).email("romakush85@gmail.com").firstName("Roma").lastName("Kushnir")
            .birthDate(new Date(85, Calendar.JANUARY, 5)).address("Updated address")
            .phoneNumber("+380935288886").build();

    @Test
    void getUserByIdShouldReturnCreatedUser() {
        when(usersRepository.findById(1L)).thenReturn(Optional.ofNullable(this.userMapper.map(createdUser, UserEntity.class)));
        UserDTO result = usersService.getUserById(1L);
        assertEquals(createdUser, result);

    }

    @Test
    public void getUserByIdShouldThrowNotFoundException() {
        Long userId = 1L;
        when(usersRepository.findById(userId)).thenReturn(Optional.empty());
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () ->
            usersService.getUserById(userId));
        assertThat(exception.getMessage()).isEqualTo("User is not found by ID " + userId);
    }

    @Test
    void createUserWithExistingIdShouldThrowWrongRequestException() {
        when(usersRepository.findById(createdUser.getId())).thenReturn(Optional.of(this.userMapper.map(createdUser, UserEntity.class)));
        WrongRequestException exception = assertThrows(WrongRequestException.class, () -> {
            usersService.createUser(createdUser);
        });
        assertEquals("User with ID " + createdUser.getId() + " is already saved. To save new user use ID value '0'", exception.getMessage());
    }

    @Test
    void createUserWithExistingEmailShouldThrowValidationException() {
        when(usersRepository.findUserEntityByEmail(createdUser.getEmail())).thenReturn(Optional.of(this.userMapper.map(createdUser, UserEntity.class)));
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            usersService.createUser(createdUser);
        });
        assertEquals("User with email " + createdUser.getEmail() + " is already created", exception.getMessage());
    }

    @Test
    void createUserWithUnderageUserShouldThrowValidationException() {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            usersService.createUser(underAgeUser);
        });
        assertEquals("Registration of users under 18 is forbidden", exception.getMessage());
    }

    @Test
    void createUserShouldReturnValidCreatedUser() {
        when(usersRepository.save(userMapper.map(newValidUser, UserEntity.class)))
                .thenReturn(userMapper.map(createdUser, UserEntity.class));
        UserDTO result = usersService.createUser(newValidUser);
        assertThat(result).isEqualTo(createdUser);
    }

    @Test
    void updateUserWithNonExistingIdShouldThrowUserNotFoundException() {
        when(usersRepository.existsById(newValidUser.getId())).thenReturn(false);
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            usersService.updateUser(newValidUser);
        });
        assertEquals("User is not found by ID " + newValidUser.getId(), exception.getMessage());
    }

    @Test
    void updateUserShouldReturnUpdatedUser() {
        when(usersRepository.existsById(createdUser.getId())).thenReturn(true);
        when(usersRepository.save(userMapper.map(createdUser, UserEntity.class)))
                .thenReturn(userMapper.map(updatedUser, UserEntity.class));
        UserDTO result = usersService.updateUser(createdUser);
        assertEquals(result, updatedUser);
    }

    @Test
    void updateUserFieldsWithNonExistingIdShouldThrowUserNotFoundException() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("fieldName", "fieldValue");
        when(usersRepository.findById(newValidUser.getId())).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            usersService.updateUserFields(newValidUser.getId(), fields);
        });
        assertEquals("User is not found by ID " + newValidUser.getId(), exception.getMessage());
    }

    @Test
    void updateUserFieldsWithInvalidFieldNameShouldThrowWrongRequestException() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("invalidFieldName", "fieldValue");
        when(usersRepository.findById(createdUser.getId())).thenReturn(Optional.of(userMapper.map(createdUser, UserEntity.class)));
        WrongRequestException exception = assertThrows(WrongRequestException.class, () -> {
            usersService.updateUserFields(createdUser.getId(), fields);
        });
        assertEquals("User haven't the field invalidFieldName", exception.getMessage());
    }

    @Test
    void updateUserFieldsShouldUpdateValidFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("email", "romakush85@gmail.com");
        fields.put("firstName", "Roma");
        fields.put("address", "Updated address");
        when(usersRepository.findById(createdUser.getId())).thenReturn(Optional.of(userMapper.map(createdUser, UserEntity.class)));
        UserDTO result = usersService.updateUserFields(createdUser.getId(), fields);
        assertEquals(result, updatedUser);
    }

    @Test
    void getUsersByBirthDateRangeShouldReturnList() {
        Date from = new Date(85, 0, 1);
        Date to = new Date(85, 11, 31);
        List<UserDTO> users = new ArrayList<>();
        users.add(createdUser);
        when(usersRepository.findUserEntityByBirthDateBetween(from, to))
                .thenReturn(users.stream().map(user -> userMapper.map(user, UserEntity.class)).collect(Collectors.toList()));
        List<UserDTO> result = usersService.getUsersByBirthDateRange(from, to);
        assertEquals(users.size(), result.size());
    }


    @Test
    void deleteUserShouldThrowExceptionWhenUserNotFound() {
        when(usersRepository.existsById(newValidUser.getId())).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> usersService.deleteUser(newValidUser.getId()));
    }

}