package org.adultofuncional.main.agenda.infrastructure.persistence.mapper;

import org.adultofuncional.main.account.infrastructure.persistence.entity.AccountEntity;
import org.adultofuncional.main.agenda.domain.model.Event;
import org.adultofuncional.main.agenda.infrastructure.persistence.entity.EventEntity;
import org.adultofuncional.main.finances.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    public Event toDomain(EventEntity entity) {
    return Event.reconstitute(
            entity.getEventId(),
            entity.getEventTitle(),
            entity.getEventDescription(),
            entity.getEventPriority(),
            entity.getEventDate(),
            entity.getEventFrequency(),
            entity.getEventReminder(),
            entity.getEventStartHour(),
            entity.getEventEndHour(),
            entity.getEventStatus(),
            entity.getCategory() != null ? entity.getCategory().getCategoryId() : null,
            entity.getAccount().getAccountId()
    );
}
    public EventEntity toEntity(Event domain) {
        EventEntity entity = new EventEntity();

        entity.setEventId(domain.getId());
        entity.setEventTitle(domain.getTitle());
        entity.setEventDescription(domain.getDescription());
        entity.setEventPriority(domain.getPriority());
        entity.setEventDate(domain.getDate());
        entity.setEventFrequency(domain.getFrequency());
        entity.setEventReminder(domain.getReminder());
        entity.setEventStartHour(domain.getStartHour());
        entity.setEventEndHour(domain.getEndHour());
        entity.setEventStatus(domain.getStatus());

        AccountEntity account = new AccountEntity();
        account.setAccountId(domain.getAccountId());
        entity.setAccount(account);

        if (domain.getCategoryId() != null) {
            CategoryEntity category = new CategoryEntity();
            category.setCategoryId(domain.getCategoryId());
            entity.setCategory(category);
        }

        return entity;
    }
}