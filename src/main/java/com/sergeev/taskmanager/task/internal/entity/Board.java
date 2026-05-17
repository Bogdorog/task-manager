package com.sergeev.taskmanager.task.internal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "boards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 4000)
    private String description;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "created_by")
    private Long createdBy;

    @Builder.Default
    @OneToMany(
            mappedBy = "board",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<BoardColumn> columns = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
