package tk.lenkyun.foodbook.server.UserManagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;
import tk.lenkyun.foodbook.foodbook.Domain.Data.Authentication.FacebookAuthenticationInfo;
import tk.lenkyun.foodbook.foodbook.Domain.Data.Authentication.SessionAuthenticationInfo;
import tk.lenkyun.foodbook.foodbook.Domain.Data.Authentication.UserAuthenticationInfo;
import tk.lenkyun.foodbook.foodbook.Domain.Data.User.User;
import tk.lenkyun.foodbook.foodbook.Domain.Operation.RegistrationBuilder;
import tk.lenkyun.foodbook.server.Exception.NoPermissionException;
import tk.lenkyun.foodbook.server.PhotoManagement.Adapter.PhotoAdapter;
import tk.lenkyun.foodbook.server.Config;
import tk.lenkyun.foodbook.server.UserManagement.Adapter.SessionAdapter;
import tk.lenkyun.foodbook.server.UserManagement.Adapter.UserAdapter;
import tk.lenkyun.foodbook.server.UserManagement.Exception.DuplicateUserException;
import tk.lenkyun.foodbook.server.UserManagement.Utils.Token;

/**
 * Created by lenkyun on 5/11/2558.
 */
@Service
public class UserManager {
    @Autowired
    private UserAdapter userAdapter;
    @Autowired
    private SessionAdapter sessionAdapter;
    @Autowired
    private PhotoAdapter photoAdapter;

    public SessionAuthenticationInfo login(UserAuthenticationInfo authenticationInfo){
        User user = userAdapter.getUser(authenticationInfo);
        if(user == null)
            return null;

        SessionAuthenticationInfo session = sessionAdapter.createSession(user, Config.SESSION_SHORT);
        return session;
    }

    SessionAuthenticationInfo login(User user){
        return sessionAdapter.createSession(user, Config.SESSION_SHORT);
    }

    public User register(RegistrationBuilder helper) throws DuplicateUserException {
        if(userAdapter.getUserByUsername(helper.getUsername()) == null){
            return userAdapter.createUser(helper, null, null);
        }else{
            throw new DuplicateUserException(helper.getUsername());
        }
    }

    public SessionAuthenticationInfo login(FacebookAuthenticationInfo user) throws NoPermissionException{
        try {
            Facebook facebook = new FacebookTemplate(user.getInfo());
            org.springframework.social.facebook.api.User fbUser = facebook.userOperations().getUserProfile();

            User userObj = userAdapter.getUserByFacebookId(fbUser.getId());
            return sessionAdapter.createSession(userObj, Config.SESSION_SHORT);
        }catch (Exception e){
            throw new NoPermissionException();
        }
    }

    public Boolean getFollowingUserStatus(Token token, User following) throws NoPermissionException {
        if(token.isTimedOut())
            throw new NoPermissionException();

        return userAdapter.getIsFollowing(
                userAdapter.getUserById(token.getUid()),
                following
        );
    }

    public Boolean setFollowingUser(Token token, User user) {
        if(token.isTimedOut())
            throw new NoPermissionException();

        return userAdapter.setFollowing(
                userAdapter.getUserById(token.getUid()),
                user
        );
    }

    public Boolean unsetFollowingUser(Token token, User user) {
        if(token.isTimedOut())
            throw new NoPermissionException();

        return userAdapter.unsetFollowing(
                userAdapter.getUserById(token.getUid()),
                user
        );
    }
}
