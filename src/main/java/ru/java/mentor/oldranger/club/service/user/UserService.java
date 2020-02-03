package ru.java.mentor.oldranger.club.service.user;

import ru.java.mentor.oldranger.club.dto.ProfileDto;
import ru.java.mentor.oldranger.club.dto.UserAuthDTO;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.model.user.UserProfile;
import ru.java.mentor.oldranger.club.model.user.UserStatistic;

import java.util.List;

public interface UserService {

    List<User> findAll();

    User findById(Long theId);

    void save(User user);

    void deleteById(Long theId);

    User getUserByNickName(String login);

    User getUserByEmail(String email);

    User getUserByEmailOrNickName(String login);

    User getUserByInviteKey(String key);

   UserAuthDTO buildUserDtoByUser(User user, boolean currentUser);
}