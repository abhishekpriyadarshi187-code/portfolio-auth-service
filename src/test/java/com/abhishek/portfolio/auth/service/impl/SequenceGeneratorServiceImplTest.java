package com.abhishek.portfolio.auth.service.impl;

import com.abhishek.portfolio.auth.model.DatabaseSequence;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SequenceGeneratorServiceImplTest {

    @Mock
    private MongoOperations mongoOperations;

    @Test
    void generateSequence_shouldReturnIncrementedDatabaseSequence() {
        // Arrange
        DatabaseSequence sequence = new DatabaseSequence();
        sequence.setId("user_sequence");
        sequence.setSeq(25L);
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(DatabaseSequence.class)
        )).thenReturn(sequence);
        SequenceGeneratorServiceImpl service = new SequenceGeneratorServiceImpl(mongoOperations);

        // Act
        long result = service.generateSequence("user_sequence");

        // Assert
        assertThat(result).isEqualTo(25L);
    }

    @Test
    void generateSequence_shouldReturnOne_whenMongoReturnsNull() {
        // Arrange
        when(mongoOperations.findAndModify(
                any(Query.class),
                any(Update.class),
                any(FindAndModifyOptions.class),
                eq(DatabaseSequence.class)
        )).thenReturn(null);
        SequenceGeneratorServiceImpl service = new SequenceGeneratorServiceImpl(mongoOperations);

        // Act
        long result = service.generateSequence("user_sequence");

        // Assert
        assertThat(result).isEqualTo(1L);
    }
}
