package com.boot.ict05_final_admin.domain.menu.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "allergy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allergy_id")
    private Integer allergyId;

    @Column(name = "allergy_name", length = 50, nullable = false, unique = true)
    private String allergyName;
}

