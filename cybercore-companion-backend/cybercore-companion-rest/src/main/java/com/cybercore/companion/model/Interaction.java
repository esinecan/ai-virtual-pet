package com.cybercore.companion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
public class Interaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID interactionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    @ManyToOne
    @JoinColumn(name = "coreling_id")
    private Coreling coreling;

    @Enumerated(EnumType.STRING)
    private InteractionStatus status = InteractionStatus.PROCESSING;
    private String response;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Interaction(String interactionId, Long userId, InteractionStatus status) {
        this.interactionId = UUID.fromString(interactionId);
        this.userAccount = new UserAccount();
        this.userAccount.setId(userId);
        this.status = status;
    }
}
