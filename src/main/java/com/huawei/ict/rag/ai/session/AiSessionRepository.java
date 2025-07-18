package com.huawei.ict.rag.ai.session;

import cn.dev33.satoken.stp.StpUtil;
import com.huawei.ict.rag.ai.message.AiMessageFetcher;
import org.babyfish.jimmer.spring.repository.JRepository;

import java.util.List;

public interface AiSessionRepository extends JRepository<AiSession, String> {
    AiSessionTable t = AiSessionTable.$;
    AiSessionFetcher FETCHER = AiSessionFetcher.$.allScalarFields()
            .messages(AiMessageFetcher.$.allScalarFields().sessionId());

    default List<AiSession> findByUser() {
        return sql().createQuery(t)
                .where(t.creatorId().eq(StpUtil.getLoginIdAsString()))
                .select(t.fetch(FETCHER))
                .execute();
    }
}
