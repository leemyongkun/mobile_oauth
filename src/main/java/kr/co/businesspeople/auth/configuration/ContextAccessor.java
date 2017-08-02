package kr.co.businesspeople.auth.configuration;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ContextAccessor implements ApplicationContextAware {

	private static ContextAccessor contextAccessor;
	private ApplicationContext applicationContext;

	@PostConstruct
	public void registerInstance() {
		contextAccessor = this;
	}

	/**
	 * class타입으로 bean을 가져온다.
	 * 
	 * @auth ykleem
	 * @description
	 */
	public static <T> T getBean(Class<T> clazz) {
		return contextAccessor.applicationContext.getBean(clazz);
	}

	/**
	 * 이름으로 bean을 가져온다.
	 * 
	 * @auth ykleem
	 * @description
	 */
	public static Object getBean(String beanName) {
		return contextAccessor.applicationContext.getBean(beanName);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

}
