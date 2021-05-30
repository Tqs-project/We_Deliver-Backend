package deti.tqs.webmarket.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(columnDefinition = "VARCHAR(20) CHECK (role IN ('ADMIN', 'RIDER', 'CUSTOMER'))")
    private String role;

    // TODO encrypt password
    @Column(nullable = false)
    private String password;

    @Size(min = 9, max = 9)
    @Column(length = 9)
    private String phoneNumber;

    // riders and customers
    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Rider rider;

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Customer customer;

    public User() {}

    public User(String username, String email, String role, String password, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }
}
