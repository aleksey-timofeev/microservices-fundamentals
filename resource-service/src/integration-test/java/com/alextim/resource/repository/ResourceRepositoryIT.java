package com.alextim.resource.repository;

import com.alextim.configuration.TestcontainersConfiguration;
import com.alextim.resource.persistence.entity.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(TestcontainersConfiguration.class)
class ResourceRepositoryIT {
    private static final UUID FIRST_LOCATION_UUID = UUID.randomUUID();
    private static final UUID SECOND_LOCATION_UUID = UUID.randomUUID();

    @Autowired
    private ResourceRepository repository;

    @Test
    void findExistingIds_shouldReturnOnlyExistingIds_whenInputContainsExistingAndMissingIds() {
        Resource first = repository.save(createResource(FIRST_LOCATION_UUID.toString()));
        Resource second = repository.save(createResource(SECOND_LOCATION_UUID.toString()));

        List<Integer> actualIds = repository.findExistingIds(
                List.of(first.getId(), second.getId(), Integer.MAX_VALUE));

        assertThat(actualIds).containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    private static Resource createResource(String uuid) {
        return Resource.builder()
                .location(uuid)
                .build();
    }
}
