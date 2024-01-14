package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;
    private UserController userController;
    private MockHttpServletRequest request;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
        request = new MockHttpServletRequest();
    }

    @Test
    public void whenRegisterUserThenOkAndRedirectVacanciesPage() {
        User user = new User("email@mail.ru", "User", "123");
        when(userService.save(user)).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenRegisterUserThenNotOkAndRedirect404Page() {
        User user = new User("email@mail.ru", "User", "123");
        when(userService.save(user)).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, user);

        assertThat(view).isEqualTo("errors/404");
        assertThat(model.getAttribute("message")).isEqualTo("Пользователь с такой почтой уже существует");
    }

    @Test
    public void whenLoginUserThenOkAndRedirectVacanciesPage() {
        User user = new User("email@mail.ru", "User", "123");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(request.getSession().getAttribute("user")).isEqualTo(user);
    }

    @Test
    public void whenLoginUserThenNotOkAndRedirectLoginPage() {
        User user = new User("email@mail.ru", "User", "123");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, request);

        assertThat(view).isEqualTo("users/login");
        assertThat(request.getSession().getAttribute("user")).isNull();
        assertThat(model.getAttribute("error")).isEqualTo("Почта или пароль введены неверно");
    }
}