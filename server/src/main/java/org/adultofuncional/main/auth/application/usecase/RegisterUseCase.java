package org.adultofuncional.main.auth.application.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

import org.adultofuncional.main.account.domain.model.Account;
import org.adultofuncional.main.account.domain.repository.AccountRepository;
import org.adultofuncional.main.auth.application.dto.AuthResponse;
import org.adultofuncional.main.auth.application.dto.RegisterRequest;
import org.adultofuncional.main.shared.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
//TODO: descomentar cuando JwtService esté implementado
//import org.adultofuncional.main.config.security.JwtService;


@Service
public class RegisterUseCase {
    
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    //private final JwtService jwtService;
    //agregarlo al contructor 


    public RegisterUseCase(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {

        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        //this.jwtService = jwtService;
    }

    public AuthResponse execute(RegisterRequest request) {

        //Verifica que el correo no esté registrado
        accountRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new BusinessException("El correo ya está registrado");
        });

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        boolean hasMasterKey = request.getMasterKey() != null && !request.getMasterKey().isBlank();

        //TODO: igual que hashedPassword, masterKey no se persiste aun
        String hashedMasterKey = hasMasterKey ? passwordEncoder.encode(request.getMasterKey()) : null;

        Account account = Account.reconstitute(
            UUID.randomUUID(),
            request.getNames(),
            request.getLastnames(),
            request.getEmail(),
            request.getPhone(),
            LocalDateTime.now()
        );

        // TODO: AccountRepository.save(Account) no persiste password ni masterKey
        // porque Account (dominio) no tiene esos campos — están en AccountEntity.
        // Debemos definir cómo pasar hashedPassword y hashedMasterKey
        // al repositorio. Opciones:
        //   a) Agregar register(Account, String password, String masterKey) al repositorio
        //   b) Delegar esta responsabilidad a AuthDomainService
        
        Account savedAccount = accountRepository.save(account);

        //TODO: generar token JWT cuando JwtService esté implementado
        //String token = jwtService.generateToken(savedAccount.getEmail());

        return AuthResponse.builder()
            //TODO: asignar token real cuando JwtService esté listo
            .token(null)
            .tokenType("Bearer")
            //TODO: asignar expiresIn desde jwtService.getExpirationTime()
            .expiresIn(null)
            .accountId(savedAccount.getId())
            .names(savedAccount.getNames())
            .lastnames(savedAccount.getLastnames())
            .email(savedAccount.getEmail())
            .phone(savedAccount.getPhone())
            .createdAt(savedAccount.getCreatedAt())
            .hasMasterKey(hasMasterKey)
            .build();

    }

}
