package org.adultofuncional.main.finances.application.usecase.movement;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.finances.application.dto.movement.MovementFilterRequest;
import org.adultofuncional.main.finances.application.dto.movement.MovementResponse;
import org.adultofuncional.main.finances.domain.model.Movement;
import org.adultofuncional.main.finances.domain.repository.MovementRepository;
import org.adultofuncional.main.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ListMovementsUseCase {
    private final MovementRepository movementRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<MovementResponse> execute(UUID accountId, MovementFilterRequest filter) {
        accountRepository.findById(accountId)
            .orElseThrow(() -> new NotFoundException("Cuenta no encontrada con id: " + accountId));

        List<Movement> movements = movementRepository.findAllByAccountId(accountId);
        if (filter != null) {
            if (filter.getMovementType() != null) {
                movements = movements.stream()
                    .filter(m -> m.getType() == filter.getMovementType())
                    .collect(Collectors.toList());
            }
            if (filter.getCategoryId() != null) {
                movements = movements.stream()
                    .filter(m -> m.getCategoryId() != null && m.getCategoryId().equals(filter.getCategoryId()))
                    .collect(Collectors.toList());
            }
            if (filter.getStartDate() != null) {
                movements = movements.stream()
                    .filter(m -> !m.getDate().isBefore(filter.getStartDate()))
                    .collect(Collectors.toList());
            }
            if (filter.getEndDate() != null) {
                movements = movements.stream()
                    .filter(m -> !m.getDate().isAfter(filter.getEndDate()))
                    .collect(Collectors.toList());
            }
            if (StringUtils.hasText(filter.getSearchTerm())) {
                String term = filter.getSearchTerm().toLowerCase();
                movements = movements.stream()
                    .filter(m -> m.getDescription() != null && m.getDescription().toLowerCase().contains(term))
                    .collect(Collectors.toList());
            }
        }
        return movements.stream()
            .map(m -> MovementResponse.builder()
                .id(m.getId())
                .movementType(m.getType())
                .amount(m.getAmount())
                .registerDate(m.getCreatedAt())
                .description(m.getDescription())
                .movementDate(m.getDate())
                .category(null)
                .build())
            .collect(Collectors.toList());
    }
}
