package com.english.api.user.repository;

import com.english.api.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.isActive FROM User u WHERE u.id = :userId")
    Boolean isUserActive(@Param("userId") UUID userId);

    boolean existsByEmail(String email);

    @Query("""
                SELECT u.id, u.email, u.fullName, u.avatarUrl, u.provider, r.code
                FROM User u
                LEFT JOIN u.roles r
                WHERE u.id = :id
            """)
    List<Object[]> findUserWithRoles(@Param("id") UUID id);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    @Query(
            value = """
            SELECT * FROM users u
            WHERE unaccent(lower(u.full_name)) LIKE unaccent(lower(concat('%', :searchTerm, '%')))
               OR unaccent(lower(u.email)) LIKE unaccent(lower(concat('%', :searchTerm, '%')))
            """,
            countQuery = """
            SELECT count(*) FROM users u
            WHERE unaccent(lower(u.full_name)) LIKE unaccent(lower(concat('%', :searchTerm, '%')))
               OR unaccent(lower(u.email)) LIKE unaccent(lower(concat('%', :searchTerm, '%')))
            """,
            nativeQuery = true
    )
    Page<User> findByFullNameOrEmail(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE (:isActive IS NULL OR u.isActive = :isActive)
              AND (:searchTerm IS NULL OR :searchTerm = '' 
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
            """)
    Page<User> findByStatusAndSearch(@Param("isActive") Boolean isActive, @Param("searchTerm") String searchTerm, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id IN :ids")
    List<User> findByIdIn(@Param("ids") List<UUID> ids);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.provider = :provider AND u.providerUid = :providerUid")
    Optional<User> findByProviderAndProviderUidWithRoles(@Param("provider") String provider, @Param("providerUid") String providerUid);

    @Query(value = """
      SELECT 
         DATE_TRUNC('month', u.created_at AT TIME ZONE 'UTC')::DATE AS month,
         COUNT(u.id)::BIGINT AS new_users,
         SUM(CASE WHEN u.is_active = true THEN 1 ELSE 0 END)::BIGINT AS active_users,
         SUM(CASE WHEN u.email_verified = true THEN 1 ELSE 0 END)::BIGINT AS verified_users
      FROM users u 
      WHERE u.created_at >= :startDate
      GROUP BY month 
      ORDER BY month DESC
      """, nativeQuery = true)
   List<Object[]> getUserGrowthByMonth(@Param("startDate") Instant startDate);
}
