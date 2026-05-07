package org.adultofuncional.main.finances.application.dto.category;

import java.time.LocalDateTime;
import java.util.UUID;
import org.adultofuncional.main.finances.domain.enums.CategoryType;
import org.adultofuncional.main.shared.security.OwnedResource;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse implements OwnedResource {
    private UUID id;
    private String name;
    private CategoryType type;
    private LocalDateTime createdAt;
    private boolean deleted;
    @Override
    public String getEmail() { return null; }
}
