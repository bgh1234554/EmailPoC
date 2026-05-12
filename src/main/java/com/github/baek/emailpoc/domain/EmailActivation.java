package com.github.baek.emailpoc.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class EmailActivation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @Column(unique = true, length = 100)
    private String token;

    private boolean activated;

    private boolean expired;

    private LocalDateTime expiredAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    //모든 필드를 수정할 수 있으면 안되니까
    @Builder
    private EmailActivation(String email, String token, LocalDateTime expiredAt) {
        this.email = email;
        this.token = token;
        this.expiredAt = expiredAt;
        this.activated = false;
        this.expired = false;
    }

    public boolean isTimeExpired() {
        if(LocalDateTime.now().isAfter(expiredAt)){
            this.expired = true;
        }
        return expired || LocalDateTime.now().isAfter(expiredAt);
    }

    public void activate() {
        this.activated = true;
    }

    public void expire() {
        this.expired = true;
    }
}
