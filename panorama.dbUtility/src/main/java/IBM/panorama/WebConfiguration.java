package IBM.panorama;

import org.directwebremoting.spring.DwrSpringServlet;
import org.h2.server.web.WebServlet;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfiguration
{
	@Bean
	ServletRegistrationBean h2servletRegistration()
	{
		ServletRegistrationBean registrationBean = new ServletRegistrationBean(new WebServlet());
		registrationBean.addUrlMappings("/h2/*");
		return registrationBean;
	}

	@Bean
	ServletRegistrationBean dwrservletRegistration()
	{
		DwrSpringServlet servlet = new DwrSpringServlet();
		ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet, "/dwr/*");
		registrationBean.addInitParameter("debug", "true");
		registrationBean.addInitParameter("pollAndCometEnabled", "true");

		registrationBean.addInitParameter("activeReverseAjaxEnabled", "true");
		registrationBean.addInitParameter("maxWaitAfterWrite", "60");
		return registrationBean;
	}
}