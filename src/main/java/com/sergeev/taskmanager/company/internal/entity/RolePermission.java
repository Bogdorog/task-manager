package com.sergeev.taskmanager.company.internal.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(RolePermissionId.class)
public class RolePermission {

    @Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private CompanyRole role;

    @Id
    @ManyToOne
    @JoinColumn(name = "permission_id")
    private Permission permission;
}
