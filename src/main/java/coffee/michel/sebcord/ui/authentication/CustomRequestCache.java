package coffee.michel.sebcord.ui.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class CustomRequestCache implements RequestCache {

	@Override
	public void saveRequest(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
	}

	@Override
	public SavedRequest getRequest(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServletRequest getMatchingRequest(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeRequest(HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

}
