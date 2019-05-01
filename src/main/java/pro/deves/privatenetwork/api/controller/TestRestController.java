package pro.deves.privatenetwork.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.deves.privatenetwork.api.model.User;
import pro.deves.privatenetwork.api.repository.UserRepository;

import java.util.List;

/**
 * A Test rest controller is only during development used
 */
@RestController
public class TestRestController {

    @Autowired
    private UserRepository userRepository;

    @RequestMapping("/users")
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }
}
