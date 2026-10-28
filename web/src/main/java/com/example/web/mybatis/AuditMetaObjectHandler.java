package com.example.web.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Populates the audit fields declared by {@code BaseEntity}.
 *
 * <p>{@code gmtCreate} and {@code gmtModified} are set on insert, {@code gmtModified} is
 * refreshed on update, and {@code version} starts at zero. Values already present are left
 * untouched.</p>
 *
 * @author nanak
 */
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    private static final String VERSION = "version";
    private static final String GMT_CREATE = "gmtCreate";
    private static final String GMT_MODIFIED = "gmtModified";

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, VERSION, Integer.class, 0);
        strictInsertFill(metaObject, GMT_CREATE, LocalDateTime.class, now);
        strictInsertFill(metaObject, GMT_MODIFIED, LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, GMT_MODIFIED, LocalDateTime.class, LocalDateTime.now());
    }
}
