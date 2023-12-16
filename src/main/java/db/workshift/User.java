package db.workshift;

public class User {

    final String name;

    private User(String name) {
        this.name = name;
    }

    static User create(String name) {
        return new User(name);
    }

}
