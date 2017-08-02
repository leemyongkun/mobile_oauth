package kr.co.businesspeople.auth.configuration.oauth;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.oauth2.config.annotation.builders.JdbcClientDetailsServiceBuilder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
//https://github.com/spring-projects/spring-security-oauth/blob/master/tests/annotation/jdbc/src/main/java/demo/Application.java
//https://github.com/spring-projects/spring-security-oauth/tree/master/samples/oauth2 : Sample
//http://blog.naver.com/wizardkyn/220650609325 : 참고사이트
//http://tugs.tistory.com/122 : proxyTargetClass

//curl -X POST -u "bpClient:bpPassword" -d "grant_type=password&username=leemyongkun@naver.com&password=f8810357b43260c862055b8f93e7232925d746fe69a8e78311aa67c96724733c" http://localhost/oauth/token
//curl -i -D "grant_type=refresh_token&client_id=bpClient&refresh_token=1d6c9a15-facb-4f7c-844e-78d20d682d33" http://localhost/oauth/token
//curl -i -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: Bearer 09f44982-94cc-4be0-b3b8-65578ffe0248" -X GET http://localhost/test/admin
//curl -X POST -u "bpClient:bpPassword" -d "grant_type=password&username=leemyongkun@naver.com&password=dydrms2500" http://localhost/oauth/token
@Configuration
@EnableAutoConfiguration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled=true)
public class OAuth2Configuration {

	@Autowired
	private DataSource log4jdbcProxyDataSource;
	
    private static BCryptPasswordEncoderCustom passwordEncoderCustom = new BCryptPasswordEncoderCustom();
	
	 
    /*
	 * OAUTH 서버
	 */
	@Configuration
	@EnableAuthorizationServer
	protected static class OAuth2Config extends AuthorizationServerConfigurerAdapter {

		@Autowired
		private AuthenticationManager auth;

		@Autowired
		private DataSource log4jdbcProxyDataSource;

		
		
		@Bean
		public JdbcTokenStore tokenStore() {
			return new JdbcTokenStore(log4jdbcProxyDataSource);
		}

		@Bean
		protected AuthorizationCodeServices authorizationCodeServices() {
			return new JdbcAuthorizationCodeServices(log4jdbcProxyDataSource);
		}

		@Override
		public void configure(AuthorizationServerSecurityConfigurer security)
				throws Exception {
				security.passwordEncoder(passwordEncoderCustom)
				// 토큰 체크하는 접속을 허용한다.
				.checkTokenAccess("permitAll()");
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints)
				throws Exception {
			endpoints.authorizationCodeServices(authorizationCodeServices())
					.authenticationManager(auth)
					.tokenStore(tokenStore())
					.approvalStoreDisabled();
		}
		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

			JdbcClientDetailsServiceBuilder jd = new JdbcClientDetailsServiceBuilder();
			
			// @formatter:off
			jd.dataSource(log4jdbcProxyDataSource)
				.passwordEncoder(passwordEncoderCustom)
				.withClient("bpClient")
					.authorizedGrantTypes("password", "authorization_code","refresh_token", "implicit")
					//.authorities("ROLE_CLIENT", "ROLE_TRUSTED_CLIENT")
					.authorities("USER", "ADMIN")
					.scopes("read", "write", "trust")
					.secret("bpPassword")
					.resourceIds("oauth2-resource")
					.accessTokenValiditySeconds(6000);
			 
			clients.setBuilder(jd);
		}

	}

	@Autowired
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		// @formatter:off
		//auth.jdbcAuthentication().dataSource(log4jdbcProxyDataSource).getUserDetailsService();//.withUser("kkuni").password("12345").roles("USER", "ACTUATOR");
				auth.jdbcAuthentication()
				.passwordEncoder(passwordEncoderCustom)
				.dataSource(log4jdbcProxyDataSource)
				.usersByUsernameQuery(getUserQuery())
				.authoritiesByUsernameQuery(getAuthoritiesQuery());
			// @formatter:on
	}

	 private String getUserQuery() {
        //return "SELECT username as principal, password as credentials, isEnabled FROM test_user WHERE username = ?";
		 return "SELECT "+ 
					"B.MAIL AS principal, "+
					"A.PWD AS credentials, "+
					"1 "+
				"FROM TBL_PEOPLE A JOIN TBL_PPMAIL B "+
				"ON A.CD_PEOPLE = B.CD_PEOPLE "+
				"WHERE B.MAIL = ? ";
    }

    private String getAuthoritiesQuery() {
        return " SELECT username as principal, role FROM test_authority WHERE username = ? ";
    }

    
	 /**
	  * 
	  * @Auth ykleem
	  * @Date 2017. 7. 25.
	  * @Description 
	  *  > [TODO] SNS 인증 Query
	  */
	private String getSNSQuery() {
		 return null;
   }
    
}