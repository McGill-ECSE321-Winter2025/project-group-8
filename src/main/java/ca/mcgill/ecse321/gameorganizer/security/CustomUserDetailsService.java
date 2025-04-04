package ca.mcgill.ecse321.gameorganizer.security;

import java.util.ArrayList;
import java.util.List;
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
import ca.mcgill.ecse321.gameorganizer.models.GameOwner; // Import GameOwner
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
        
        // Assign roles based on the actual account type
        List<SimpleGrantedAuthority> authorities;
        if (account instanceof GameOwner) {
            authorities = List.of(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ROLE_GAME_OWNER")
            );
        } else {
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(account.getEmail(), account.getPassword(), authorities);
    }
}
