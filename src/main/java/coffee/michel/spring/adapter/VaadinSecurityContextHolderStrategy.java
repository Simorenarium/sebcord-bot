package coffee.michel.spring.adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.WrappedSession;

public class VaadinSecurityContextHolderStrategy
		implements org.springframework.security.core.context.SecurityContextHolderStrategy {

	private final static Map<String, SecurityContext> contextBySession = new HashMap<>();

	@Override
	public void clearContext() {
		// TODO Auto-generated method stub

	}

	@Override
	public SecurityContext getContext() {
		var id = getSessionId();

		synchronized (contextBySession) {
			return contextBySession.computeIfAbsent(id, k -> createEmptyContext());
		}
	}

	@Override
	public void setContext(SecurityContext context) {
		var id = getSessionId();

		synchronized (contextBySession) {
			contextBySession.put(id, context);
		}
	}

	private String getSessionId() {
		VaadinSession currentSession = VaadinSession.getCurrent();
		var id = Optional.ofNullable(currentSession).map(VaadinSession::getSession).map(WrappedSession::getId)
				.orElse(null);
		return id;
	}

	@Override
	public SecurityContext createEmptyContext() {
		return new SecurityContextImpl();
	}

}
