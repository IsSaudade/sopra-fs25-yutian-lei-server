package ch.uzh.ifi.hase.soprafs24.controller;

import ch.uzh.ifi.hase.soprafs24.entity.User;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserGetDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPostDTO;
import ch.uzh.ifi.hase.soprafs24.rest.dto.UserPutDTO;
import ch.uzh.ifi.hase.soprafs24.rest.mapper.DTOMapper;
import ch.uzh.ifi.hase.soprafs24.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

/**
 * User Controller
 * This class is responsible for handling all REST request that are related to
 * the user.
 * The controller will receive the request and delegate the execution to the
 * UserService and finally return the result.
 */
@RestController
public class UserController {

    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public List<UserGetDTO> getAllUsers() {
        // fetch all users in the internal representation
        List<User> users = userService.getUsers();
        List<UserGetDTO> userGetDTOs = new ArrayList<>();

        // convert each user to the API representation
        for (User user : users) {
            userGetDTOs.add(DTOMapper.INSTANCE.convertEntityToUserGetDTO(user));
        }
        return userGetDTOs;
    }

    @GetMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO getUser(@PathVariable Long userId) {
        // fetch user by id
        User user = userService.getUserById(userId);
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(user);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public UserGetDTO createUser(@RequestBody UserPostDTO userPostDTO) {
        // convert API user to internal representation
        User userInput = DTOMapper.INSTANCE.convertUserPostDTOtoEntity(userPostDTO);

        // create user
        User createdUser = userService.createUser(userInput);

        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(createdUser);
    }

    @PutMapping("/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void updateUser(
            @PathVariable Long userId,
            @RequestBody UserPutDTO userPutDTO,
            @RequestHeader(value = "CurrentUserId", required = false) String currentUserIdStr) {

        log.info("Received PUT request to update user {} with CurrentUserId header: {}", userId, currentUserIdStr);

        // Convert string ID to Long (if provided)
        Long currentUserId = null;
        if (currentUserIdStr != null && !currentUserIdStr.isEmpty()) {
            try {
                currentUserId = Long.parseLong(currentUserIdStr);
                log.info("Parsed currentUserId from header: {}", currentUserId);
            } catch (NumberFormatException e) {
                log.error("Failed to parse currentUserId '{}': {}", currentUserIdStr, e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Invalid user ID format in CurrentUserId header");
            }
        } else {
            log.warn("No CurrentUserId header provided");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Authentication required to update user profile");
        }

        // STRICT permission check - users can ONLY update their own profile
        if (!userId.equals(currentUserId)) {
            log.warn("Permission denied: User {} attempted to update user {}", currentUserId, userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You can only update your own profile");
        }

        log.info("User {} authorized to update their own profile", userId);

        // update user with permission check
        userService.updateUser(userId, currentUserId, userPutDTO);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public UserGetDTO loginUser(@RequestBody UserPostDTO userPostDTO) {
        // login user
        User loggedInUser = userService.loginUser(userPostDTO.getUsername(), userPostDTO.getPassword());

        // convert internal representation of user back to API
        return DTOMapper.INSTANCE.convertEntityToUserGetDTO(loggedInUser);
    }

    @PostMapping("/logout/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ResponseBody
    public void logoutUser(@PathVariable Long userId) {
        // logout user
        userService.logoutUser(userId);
    }
}