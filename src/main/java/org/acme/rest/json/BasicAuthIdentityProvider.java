/*
 * Copyright 2022 by MediData AG
 * ALL RIGHTS RESERVED
 *
 * MediData AG - CommercialSoftwareLicense
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.acme.rest.json;

import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.credential.PasswordCredential;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.UsernamePasswordAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.subscription.UniEmitter;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class BasicAuthIdentityProvider implements IdentityProvider<UsernamePasswordAuthenticationRequest> {

   @Override
   public Class<UsernamePasswordAuthenticationRequest> getRequestType() {
      return UsernamePasswordAuthenticationRequest.class;
   }

   @Override
   public Uni<SecurityIdentity> authenticate(UsernamePasswordAuthenticationRequest request, AuthenticationRequestContext context) {
      return Uni.createFrom().emitter(new Consumer<UniEmitter<? super SecurityIdentity>>() {
         @Override
         public void accept(UniEmitter<? super SecurityIdentity> uniEmitter) {
             try {
                 uniEmitter.complete(createSecurityIdentity(request));
             } catch (AuthenticationFailedException e) {
                 uniEmitter.fail(e);
             }
         }
     });
   }

   private SecurityIdentity createSecurityIdentity(UsernamePasswordAuthenticationRequest request) {
      if (request.getUsername().equals("admin") && "pass".equals(new String(request.getPassword().getPassword()))) {
         QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
               .setPrincipal(new QuarkusPrincipal("admin"))
               .addCredential(new PasswordCredential(request.getPassword().getPassword()))
               .addRole("role");
         RoutingContext routingContext = HttpSecurityUtils.getRoutingContextAttribute(request);
         if (routingContext != null) {
            builder.addAttribute(RoutingContext.class.getName(), routingContext);
         }
         return builder.build();
      } else {
         throw new AuthenticationFailedException("Invalid credentials");
      }
   }
}
