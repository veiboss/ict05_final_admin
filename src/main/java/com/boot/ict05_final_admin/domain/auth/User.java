package com.boot.ict05_final_admin.domain.auth;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
        name = "member",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "member_email"),
                @UniqueConstraint(columnNames = "member_phone")
        }
)
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Column(name = "member_email", nullable = false, length = 254)
    private String email;

    @Column(name = "member_password", nullable = false, length = 255)
    private String password;

    @Column(name = "member_name", nullable = false, length = 100)
    private String name;

    @Column(name = "member_phone", length = 20)
    private String phone;

    // DB에 컬럼이 없으므로 우선 Transient (필요하면 테이블에 role 컬럼 추가)
    @Transient
    private UserRole role;

    // --- UserDetails ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // role이 없으므로 기본 ROLE_HQ 부여(본사 시스템)
        return List.of((GrantedAuthority) () -> "ROLE_HQ");
    }

    @Override public String getUsername() { return email; }
    @Override public String getPassword() { return password; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
