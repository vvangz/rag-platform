package com.huawei.ict.rag.infrastructure.jimmer;

import org.babyfish.jimmer.sql.MappedSuperclass;

import java.time.LocalDateTime;

@MappedSuperclass
public interface BaseDateTime {

  LocalDateTime createdTime();

  LocalDateTime editedTime();
}
