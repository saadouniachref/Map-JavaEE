package tn.esprit.utlities;


import java.io.IOException;
import java.security.Key;
import java.security.Principal;

import javax.annotation.Priority;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import io.jsonwebtoken.Jwts;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
	private static final String AUTHENTICATION_SCHEME = "Bearer";

	
	ContainerRequestContext requestContext;
	SecurityContext securityContext;
	
	 @Inject
	 @AuthenticatedUser
	 Event<String> userAuthenticatedEvent;

	 
	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {

		System.out.println("request filter invoked...");

		final SecurityContext currentSecurityContext = requestContext.getSecurityContext();

		this.requestContext = requestContext;
		this.securityContext = currentSecurityContext;

		// Get the Authorization header from the request
		String authorizationHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

		// Validate the Authorization header
		if (!isTokenBasedAuthentication(authorizationHeader)) {
			abortWithUnauthorized(requestContext);
			return;
		}

		// Extract the token from the Authorization header
		String token = authorizationHeader.substring(AUTHENTICATION_SCHEME.length()).trim();

		try {

			// Validate the token
			validateToken(token);
			String keyString = "simplekey";
			Key key = new SecretKeySpec(keyString.getBytes(), 0, keyString.getBytes().length, "DES");
	        userAuthenticatedEvent.fire(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject());
	        System.out.println(Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject());

			/*
			 * Identifying the current user
			 */

			// String username=currentSecurityContext.getUserPrincipal().getName();
			// System.out.println("current user: "+
			// currentSecurityContext.getAuthenticationScheme());

			requestContext.setSecurityContext(new SecurityContext() {

				@Override
				public Principal getUserPrincipal() {

					return new Principal() {

						@Override
						public String getName() {
							// return username;
							return "";
						}
					};
				}

				@Override
				public boolean isUserInRole(String role) {
					return true;
				}

				@Override
				public boolean isSecure() {
					return false;
				}

				@Override
				public String getAuthenticationScheme() {
					return null;
				}
			});

		} catch (Exception e) {
			requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
		}

	}

	private boolean isTokenBasedAuthentication(String authorizationHeader) {

		// Check if the Authorization header is valid
		// It must not be null and must be prefixed with "Bearer" plus a whitespace
		// Authentication scheme comparison must be case-insensitive
		return authorizationHeader != null
				&& authorizationHeader.toLowerCase().startsWith(AUTHENTICATION_SCHEME.toLowerCase() + " ");
	}

	private void abortWithUnauthorized(ContainerRequestContext requestContext) {

		// Abort the filter chain with a 401 status code
		// The "WWW-Authenticate" header is sent along with the response
		requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
				.header(HttpHeaders.WWW_AUTHENTICATE, AUTHENTICATION_SCHEME).build());
	}

	private void validateToken(String token) {
		// Check if it was issued by the server and if it's not expired
		// Throw an Exception if the token is invalid

		try {

			// Validate the token
			String keyString = "simplekey";
			Key key = new SecretKeySpec(keyString.getBytes(), 0, keyString.getBytes().length, "DES");
			System.out.println("the key is : " + key);

			System.out.println("test:" + Jwts.parser().setSigningKey(key).parseClaimsJws(token));
			System.out.println("#### valid token : " + token);
			

		} catch (Exception e) {
			System.out.println("#### invalid token : " + token);
			(this.requestContext).abortWith(Response.status(Response.Status.UNAUTHORIZED).build());

		}
	}

}
