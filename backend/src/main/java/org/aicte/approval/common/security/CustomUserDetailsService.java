package org.aicte.approval.common.security;

import lombok.RequiredArgsConstructor;
import org.aicte.approval.core.model.Institution;
import org.aicte.approval.core.model.User;
import org.aicte.approval.core.repository.InstitutionRepository;
import org.aicte.approval.core.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + username));

        Long institutionId = institutionRepository.findByContactUserId(user.getId())
                .map(Institution::getId)
                .orElse(null);

        return UserPrincipal.create(user, institutionId);
    }
}
