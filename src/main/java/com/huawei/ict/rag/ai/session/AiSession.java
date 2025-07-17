package com.huawei.ict.rag.ai.session;


import com.huawei.ict.rag.ai.message.AiMessage;
import com.huawei.ict.rag.infrastructure.jimmer.BaseEntity;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.OneToMany;
import org.babyfish.jimmer.sql.OrderedProp;

import java.util.List;

/**
 * 会话
 */
@Entity
public interface AiSession extends BaseEntity {

    /**
     * 会话名称
     */
    String name();

    /**
     * 一对多关联消息，按创建时间升序
     */

    @OneToMany(mappedBy = "session", orderedProps = @OrderedProp(value = "createdTime"))
    List<AiMessage> messages();
}

