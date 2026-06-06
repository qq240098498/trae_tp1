package com.huolala.service;

import com.huolala.entity.User;
import com.huolala.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public boolean login(String username, String password, HttpSession session) {
        User user = userRepository.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("loginUser", user);
            return true;
        }
        return false;
    }

    public void logout(HttpSession session) {
        session.removeAttribute("loginUser");
    }

    public boolean isLogin(HttpSession session) {
        return session.getAttribute("loginUser") != null;
    }

    public User getLoginUser(HttpSession session) {
        return (User) session.getAttribute("loginUser");
    }
}
