package cc.martincao.rentigo.rentigobackend.user;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "role")
public class Role {
    // Role ID
    @Id
    @Column(columnDefinition = "TINYINT")
    private Integer id;

    // Role name
    @Column(nullable = false, unique = true, length = 32)
    private String name;

    // Users with this role (inverse side)
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    // Getters & Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }
    // Setter for users is intentionally omitted to avoid misuse
}