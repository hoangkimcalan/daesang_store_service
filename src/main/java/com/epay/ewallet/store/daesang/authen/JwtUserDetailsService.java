package com.epay.ewallet.store.daesang.authen;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.epay.ewallet.store.daesang.mapperOne.IUser;
import com.epay.ewallet.store.daesang.model.User;

@Service
public class JwtUserDetailsService implements UserDetailsService {
	
	@Autowired
	private IUser userDao;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userDao.getUserByPhoneNumber(username);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
		String userName = user.getPhoneNumber();
		String pass = user.getPassword();

		return new org.springframework.security.core.userdetails.User(userName, pass, new ArrayList<>());
	}
}
