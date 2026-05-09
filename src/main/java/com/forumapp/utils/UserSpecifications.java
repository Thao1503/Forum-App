package com.forumapp.utils;

import com.forumapp.entity.CommentEntity;
import com.forumapp.entity.PostEntity;
import com.forumapp.entity.UserEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSpecifications {
    public static Specification<UserEntity> hasSearch(String search){
        return (root, query, cb) -> search == null ? null :
                cb.or(cb.like(root.get("username"), "%" + search + "%"),
                        cb.like(root.get("email"),  "%" + search + "%"));
    }

    public static Specification<PostEntity> hasSearchingByTitleOrContent(String search) {
        String pattern = "%" + search.toLowerCase() + "%";

        return (root, query, cb) -> search == null ? null :
                cb.or(cb.like(root.get("title"), pattern),
                        cb.like(root.get("content"),  pattern));

    }


    public static Specification<UserEntity> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null :
                cb.equal(root.get("status"), status);
    }

}
