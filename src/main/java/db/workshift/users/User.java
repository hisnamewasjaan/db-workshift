package db.workshift.users;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;
    private String name;

    protected User() {
    }

    private User(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User[id=%s, name='%s']".formatted(id, name);
    }

    public static User create(String name) {
        return new User(name);
    }

    public String getName() {
        return name;
    }

    public UUID getId() {
        return id;
    }

}
