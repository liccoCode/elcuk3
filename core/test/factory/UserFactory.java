package factory;

import models.User;

/**
 * Created by IntelliJ IDEA.
 * User: wyatt
 * Date: 7/16/13
 * Time: 10:30 AM
 */
public class UserFactory extends ModelFactory<User> {
    @Override
    public User define() {
        User user = new User();
        user.email = "wyatt@easya.cc";
        user.password = "123456";
        user.username = "wyatt";
        user.isService = false;
        return user;
    }
}
