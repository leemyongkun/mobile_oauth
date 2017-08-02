package kr.co.businesspeople.auth.configuration;

import javax.annotation.PostConstruct;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import net.sf.log4jdbc.Log4jdbcProxyDataSource;
import net.sf.log4jdbc.tools.Log4JdbcCustomFormatter;
import net.sf.log4jdbc.tools.LoggingType;

/**
 * @author ykleem
 * @Description 
 * 		DataSource / TX / sqlSessionTemplate 설정 (Option SQL Proxy )
 * @date 20170428
 */
@Configuration
public class DBConnectionConfiguration {
	
	@Autowired
	private Environment env; //Main(@SpringBootApplication)에 설정된 Resouce에서 DI 함.
	
	@Autowired
	private ResourcePatternResolver patternResolver;
	
	@Autowired
	private ContextAccessor contextAccessor; //ApplicationContext에서 Bean을 직접 가져오기 위해 생성함.
	
	
	private String mode = "local";
	
	@PostConstruct
	public void SetConfiguration(){
		//this.mode = this.mode; //System.getProperty("mode"); 
	}
	
	@Bean(destroyMethod = "close")
	@Primary
	public DataSource dataSource(){
		DataSource ds = new DataSource();
		ds.setDriverClassName(env.getProperty("driverClassName."+mode));
		ds.setUrl(env.getProperty("url."+mode));
		ds.setUsername(env.getProperty("username."+mode));
		ds.setPassword(env.getProperty("password."+mode));
		return ds;
	}
	
 	@Bean(name="log4jdbcProxyDataSource")
	@Autowired
	public Log4jdbcProxyDataSource log4jdbcProxyDataSource(DataSource dataSource){ //Log4jdbcProxyDataSource가 DataSource를 Implement함
		Log4jdbcProxyDataSource log = new Log4jdbcProxyDataSource(dataSource);
		Log4JdbcCustomFormatter ldcf = new Log4JdbcCustomFormatter();
		ldcf.setLoggingType(LoggingType.MULTI_LINE);
		ldcf.setSqlPrefix("SQL ["+this.mode+"] >>> ");
		log.setLogFormatter(ldcf);
		return log;
	} 
	
	
	//@Qualifier("log4jdbcProxyDataSource") //@Autowired
	@Bean
	public SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception{
	 
		SqlSessionFactoryBean sfb = new SqlSessionFactoryBean();
		
		if(mode.equals("real")){
			sfb.setDataSource(contextAccessor.getBean(DataSource.class));	
		}else{
			sfb.setDataSource(contextAccessor.getBean(Log4jdbcProxyDataSource.class));	
		}
		
		sfb.setConfigLocation(new DefaultResourceLoader().getResource("classpath:config/mybatis/mybatis-config.xml"));
		//sfb.setMapperLocations(patternResolver.getResources("classpath*:config/mybatis/mapper/**/*.xml"));
		return sfb;
	}
	
	@Bean(destroyMethod = "clearCache")
	@Autowired
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactoryBean sqlSessionFactoryBean ) throws Exception{
		SqlSessionTemplate template = new SqlSessionTemplate(sqlSessionFactoryBean.getObject());
		return template;
	}
	
	
	@Bean
	public DataSourceTransactionManager transactionManater(){
		if(mode.equals("real")){
			return new DataSourceTransactionManager(contextAccessor.getBean(DataSource.class));	
		}else{
			return new DataSourceTransactionManager(contextAccessor.getBean(Log4jdbcProxyDataSource.class));	
		}
	}
	
}
