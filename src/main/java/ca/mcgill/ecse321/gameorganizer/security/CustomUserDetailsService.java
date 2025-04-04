package ca.mcgill.ecse321.gameorganizer.security;

import java.util.ArrayList;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

@Service
@Primary
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // TODO: Implement proper role mapping based on Account subclass or role field
        // For now, grant a default USER role to all authenticated users.
        return new User(account.getEmail(), account.getPassword(), 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
