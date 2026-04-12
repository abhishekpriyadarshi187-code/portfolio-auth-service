package com.abhishek.portfolio.auth.service.impl;

import com.abhishek.portfolio.auth.model.DatabaseSequence;
import com.abhishek.portfolio.auth.service.SequenceGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

@Service
@RequiredArgsConstructor
public class SequenceGeneratorServiceImpl implements SequenceGeneratorService {

    private final MongoOperations mongoOperations;

    @Override
    public long generateSequence(String sequenceName) {

        Query query = new Query(Criteria.where("_id").is(sequenceName));

        Update update = new Update().inc("seq", 1);

        FindAndModifyOptions options = options()
                .returnNew(true)
                .upsert(true);

        DatabaseSequence counter = mongoOperations.findAndModify(
                query,
                update,
                options,
                DatabaseSequence.class
        );

        return counter != null ? counter.getSeq() : 1;
    }
}
