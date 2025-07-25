package com.example.matching_fit.domain.user.entity;
import com.example.matching_fit.domain.user.enums.LoginType;
import com.example.matching_fit.domain.user.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Setter
public class User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false, unique = true)
    private String email;
    @Column(length = 255, nullable = false)
    private String password;
    private String kakaoId;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    private LoginType loginType;
    @Column(length = 255)
    private String refreshToken;
    @Column
    private String companyName;
    @Enumerated(EnumType.STRING)
    private Role role;
    @CreatedDate
    private LocalDateTime createdAt;

    public User(long id, String email, String name) {
        this.setId(id);
        this.email = email;
        this.name = name;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(this.role);
        return roles
                .stream()
                .map((name) -> new SimpleGrantedAuthority("ROLE_" + name))
                .toList();
    }

    public List<String> getAuthoritiesAsStringList() {
        List<String> authorities = new ArrayList<>();

        return authorities;
    }
}
