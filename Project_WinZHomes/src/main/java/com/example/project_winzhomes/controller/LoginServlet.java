package com.example.project_winzhomes.controller;

import com.example.project_winzhomes.model.Role;
import com.example.project_winzhomes.service.IRoleService;
import com.example.project_winzhomes.service.IUserService;
import com.example.project_winzhomes.service.impl.RoleService;
import com.example.project_winzhomes.service.impl.UserService;
import com.example.project_winzhomes.model.User;

import org.mindrot.jbcrypt.BCrypt;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private IUserService userService = new UserService();
    private IRoleService roleService = new RoleService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            default:
                loginAuthorize(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            request.setCharacterEncoding("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "";
        }

        switch (action) {
            default:
                login(request, response);
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) {
        RequestDispatcher dispatcher = request.getRequestDispatcher("view/login.jsp");
        try {
            dispatcher.forward(request, response);
        } catch (ServletException | IOException e) {
            e.printStackTrace();
        }
    }

    protected void loginAuthorize(HttpServletRequest request, HttpServletResponse response) {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        boolean loginCheck = false;

        User currentUser = null;
        Role currentRole = null;
        for (User user : userService.findAll()) {
            for (Role role : roleService.findAll()) {
                if (user.getUsername().equals(username)) {
                    if (user.getRoleId() == role.getId()) {
                        currentUser = user;
                        currentRole = role;
                        break;
                    }
                }
            }
        }

        boolean passwordCheck = BCrypt.checkpw(password, currentUser.getPassword());
        String message;
        RequestDispatcher dispatcher;
        if (passwordCheck) {
            HttpSession session = request.getSession();
            session.setAttribute("username", currentUser.getUsername());
            session.setAttribute("role", currentRole.getRoleName());
            loginCheck = true;
            message = "Success!";
            request.setAttribute("message", message);
            request.setAttribute("check", loginCheck);

            if (currentRole.getRoleName().equals("Admin")) {
                UserServlet userServlet = new UserServlet();
                userServlet.doGet(request, response);
            } else if (currentRole.getRoleName().equals("Customer")) {
                dispatcher = request.getRequestDispatcher("view/index.jsp");
                try {
                    dispatcher.forward(request, response);
                } catch (ServletException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                dispatcher = request.getRequestDispatcher("view/login.jsp");
                message = "Wrong username or password!";
                request.setAttribute("message", message);
                request.setAttribute("loginCheck", loginCheck);
                try {
                    dispatcher.forward(request, response);
                } catch (ServletException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
