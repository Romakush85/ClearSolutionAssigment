package com.clearsolution.testassigment.services;

import com.clearsolution.testassigment.exceptions.UserNotFoundException;
import com.clearsolution.testassigment.exceptions.ValidationException;
import com.clearsolution.testassigment.exceptions.WrongRequestException;
import com.clearsolution.testassigment.models.DTOs.UserDTO;
import com.clearsolution.testassigment.models.entities.UserEntity;
import com.clearsolution.testassigment.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class UsersService {
    private final UsersRepository usersRepository;
    private final ModelMapper userMapper;

    @Value("${MIN_USER_AGE}")
    private int minUserAge;

    @Autowired
    public UsersService(UsersRepository usersRepository, ModelMapper userMapper) {
        this.usersRepository = usersRepository;
        this.userMapper = userMapper;
    }

    public UserDTO getUserById(Long id) {
        UserEntity foundedUser = usersRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User is not found by ID " + id));
        return convertToDto(foundedUser);
    }

    public UserDTO createUser(UserDTO dto) {
        if(usersRepository.findById(dto.getId()).isPresent()) {
            throw new WrongRequestException("User with ID " + dto.getId()
                    + " is already saved. To save new user use ID value '0'");
        }
        if(usersRepository.findUserEntityByEmail(dto.getEmail()).isPresent()) {
            throw new ValidationException("User with email " + dto.getEmail() + " is already created");
        }
        if(getUserAge(dto.getBirthDate()) < minUserAge) {
            throw new ValidationException("Registration of users under 18 is forbidden");
        }
        return convertToDto(usersRepository.save(convertToEntity(dto)));
    }

    @Transactional
    public UserDTO updateUser(UserDTO dto) {
        if(!usersRepository.existsById(dto.getId())) throw new UserNotFoundException("User is not found by ID " + dto.getId());
        return convertToDto(usersRepository.save(convertToEntity(dto)));
    }

    @Transactional
    public UserDTO updateUserFields(Long id, Map<String, Object> fields) {
        UserEntity userToBeUpdated = usersRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User is not found by ID " + id));
        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Object fieldValue = entry.getValue();
            try {
                Field field = userToBeUpdated.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(userToBeUpdated, fieldValue);
            } catch (NoSuchFieldException e) {
                throw new WrongRequestException("User haven't the field " + fieldName);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field");
            }
        }
        return convertToDto(userToBeUpdated);
    }

    public List<UserDTO> getUsersByBirthDateRange(Date from, Date to) {
        return usersRepository.findUserEntityByBirthDateBetween(from, to).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public void deleteUser(Long id) {
        if(!usersRepository.existsById(id)) throw new UserNotFoundException("User is not found by ID " + id);
        usersRepository.deleteById(id);
    }

    private int getUserAge(Date birthDate) {
        LocalDate birthLocalDate = LocalDate.ofInstant(birthDate.toInstant(), ZoneId.systemDefault());
        LocalDate registrationDate = LocalDate.now();
        Period period = Period.between(birthLocalDate, registrationDate);
        return period.getYears();
    }

    private UserEntity convertToEntity(UserDTO dto) { return userMapper.map(dto, UserEntity.class); }

    private UserDTO convertToDto(UserEntity entity) { return userMapper.map(entity, UserDTO.class); }
}
