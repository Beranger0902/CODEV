package com.novacore.identity.application;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import com.novacore.identity.AccountSummary;
import com.novacore.identity.Accounts;
import com.novacore.identity.domain.AccountStatus;
import com.novacore.identity.domain.UserAccount;
import com.novacore.identity.domain.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class AccountsFacade implements Accounts {

	private final UserAccountRepository users;

	AccountsFacade(UserAccountRepository users) {
		this.users = users;
	}

	@Override
	public Optional<AccountSummary> findById(UUID id) {
		return users.findById(id).map(AccountsFacade::toSummary);
	}

	@Override
	public Optional<AccountSummary> findByUsername(String username) {
		return users.findByUsername(username.trim().toLowerCase(Locale.ROOT)).map(AccountsFacade::toSummary);
	}

	private static AccountSummary toSummary(UserAccount user) {
		return new AccountSummary(user.getId(), user.getUsername(), user.getDisplayName(),
				user.getStatus() == AccountStatus.ACTIVE);
	}

}
