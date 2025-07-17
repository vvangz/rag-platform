package com.huawei.ict.rag.ai.message;


import com.huawei.ict.rag.ai.session.AiSession;
import com.huawei.ict.rag.infrastructure.jimmer.BaseEntity;
import jakarta.validation.constraints.Null;
import lombok.Data;
import lombok.experimental.Accessors;
import org.babyfish.jimmer.sql.*;
import org.springframework.ai.chat.messages.MessageType;

import java.util.List;

/**
 * 历史消息
 */

@Entity
public interface AiMessage extends BaseEntity {

    /**
     * 消息类型(用户/助手/系统)
     */
    MessageType type();

    /**
     * 消息内容
     */
    String textContent();

    @Serialized
    @Null
    List<Media> medias();

    @IdView
    String sessionId();

    /**
     * 会话
     */
    @ManyToOne
    @JoinColumn(name = "ai_session_id")
    @OnDissociate(DissociateAction.DELETE)
    AiSession session();

    @Data
    @Accessors(chain = true)
    class Media {
        public String type;
        public String data;
    }
}

