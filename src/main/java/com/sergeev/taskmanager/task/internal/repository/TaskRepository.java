package com.sergeev.taskmanager.task.internal.repository;

import com.sergeev.taskmanager.task.internal.entity.Task;
import com.sergeev.taskmanager.task.internal.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
// TODO Удалить Query запросы связанные с CompanyId, изменить логику в сервисах
public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByColumnId(Long columnId);

    @Query("SELECT t FROM Task t JOIN Board b ON t.boardId = b.id WHERE b.companyId = :companyId")
    List<Task> findAllByCompanyId(@Param("companyId") Long companyId);

    @Query("SELECT t FROM Task t JOIN Board b ON t.boardId = b.id WHERE b.companyId = :companyId AND t.assignedTo = :assignedTo")
    List<Task> findAllByCompanyIdAndAssignedTo(@Param("companyId") Long companyId,
                                               @Param("assignedTo") Long assignedTo);

    @Query("SELECT t FROM Task t JOIN Board b ON t.boardId = b.id WHERE b.companyId = :companyId AND t.createdBy = :createdBy")
    List<Task> findAllByCompanyIdAndCreatedBy(@Param("companyId") Long companyId,
                                              @Param("createdBy") Long createdBy);

    @Query("""
        select t from Task t
        where t.dueDate between :now and :threshold
          and t.deadlineWarningSent = false
          and t.status not in :excludedStatuses
        """)
    List<Task> findApproachingDeadline(
            @Param("now") LocalDateTime now,
            @Param("threshold") LocalDateTime threshold,
            @Param("excludedStatuses") Collection<TaskStatus> excludedStatuses);

    @Query("""
        select t from Task t
        where t.dueDate < :now
          and t.deadlineOverdueSent = false
          and t.status not in :excludedStatuses
        """)
    List<Task> findOverdue(
            @Param("now") LocalDateTime now,
            @Param("excludedStatuses") Collection<TaskStatus> excludedStatuses);

    List<Task> findAllByBoardId(Long boardId);

    @Query(value = """
            SELECT b.company_id
            FROM tasks t
            JOIN board_columns bc ON t.column_id = bc.id
            JOIN boards b ON bc.board_id = b.id
            WHERE t.id = :taskId
            """, nativeQuery = true)
    Long findCompanyIdByTaskId(Long taskId);
}
