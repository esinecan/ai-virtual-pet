package com.cybercore.companion.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Coreling {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int dataIntegrity = 100;
    private int processingLoad = 50;
    private int emotionalCharge = 50;

    @Column(columnDefinition = "vector(1536)") // For pgvector
    private float[] memoryVector;

    @OneToMany(mappedBy = "coreling")
    private List<ActionHistory> actionHistory;

    private Long userAccountId;
}
